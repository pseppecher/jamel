package jamel.models.modelJEE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jamel.Jamel;
import jamel.data.Expression;
import jamel.data.ExpressionFactory;
import jamel.gui.Gui;
import jamel.util.BasicTimer;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * A basic class of the {@link Simulation}.
 */
public class BasicSimulation implements Simulation {

	/**
	 * Returns a string that contains the specified meta-data from the specified
	 * set of parameters.
	 * 
	 * @param parameters
	 *            the specified set of parameters.
	 * 
	 * @param key
	 *            the key of the meta-data to be returned.
	 * @return a string that contains the required meta-data.
	 */
	static private String getMeta(final Parameters parameters, final String key) {
		final String result;
		final Parameters meta = parameters.get("meta");
		if (meta == null) {
			result = null;
		} else {
			if (meta.hasAttribute(key)) {
				result = meta.getAttribute(key);
			} else {
				final Parameters sub = meta.get(key);
				if (sub == null) {
					result = null;
				} else {
					result = sub.toString();
				}
			}
		}
		return result;
	}

	/**
	 * Returns the events.
	 * 
	 * @param events
	 *            a XML element with the description of the events.
	 * @return a map that contains the events.
	 */
	private static Map<Integer, List<Parameters>> getNewEvents(Parameters events) {
		final HashMap<Integer, List<Parameters>> map = new HashMap<Integer, List<Parameters>>();
		if (events != null) {
			final List<Parameters> eventsList = events.getAll();
			for (int i = 0; i < eventsList.size(); i++) {
				final Parameters item = eventsList.get(i);
				final String periodKey = item.getAttribute("period");
				if ("".equals(periodKey)) {
					throw new RuntimeException("Malformed event: Missing attribute: period");
				}
				final Integer period = Integer.parseInt(periodKey);
				if (map.containsKey(period)) {
					final List<Parameters> list = map.get(period);
					list.add(item);
				} else {
					final List<Parameters> list = new LinkedList<Parameters>();
					list.add(item);
					map.put(period, list);
				}
			}
		}
		return map;
	}

	/**
	 * Creates and returns a new Gui.
	 * 
	 * @param params
	 *            the parameters of the Gui.
	 * @param simulation
	 *            the parent simulation.
	 * @return a new Gui.
	 */
	private static Gui getNewGui(final Parameters params, final BasicSimulation simulation) {

		if (!params.getName().equals("gui")) {
			// TODO : ça aussi devrait être une JamelInitialisationException.
			throw new RuntimeException("Bad element: " + params.getName());
		}

		final File guiFile;
		final Parameters guiDescription;

		if (params.hasAttribute("src")) {

			/*
			 * Opens and reads the XML file that contains the specification of the gui.
			 */

			final String src = params.getAttribute("src");
			final String fileName = simulation.getFile().getParent() + "/" + src;
			guiFile = new File(fileName);
			if (!guiFile.exists()) {
				// TODO : ça, par exemple, devrait être une
				// JamelInitialisationException.
				// Elle serait interceptée plus haut et donnerait à
				// l'utilisateur les instructions pour corriger son scénario.
				throw new RuntimeException(fileName + " (No such file)");
			}
			final Element root;
			try {
				final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(guiFile);
				root = document.getDocumentElement();
			} catch (final Exception e) {
				throw new RuntimeException("Something went wrong while reading \"" + fileName + "\"", e);
			}
			if (!root.getTagName().equals("gui")) {
				throw new RuntimeException(fileName + ": Bad element: " + root.getTagName());
			}
			guiDescription = new Parameters(root);

		} else {
			guiFile = simulation.getFile();
			guiDescription = params;
		}

		final String guiClassName = guiDescription.getAttribute("className");
		if (guiClassName.isEmpty()) {
			throw new RuntimeException("Attribute \"className\" is missing or empty.");
		}

		/*
		 *  Creates the gui.
		 */

		final Gui gui;
		try {
			gui = (Gui) Class.forName(guiClassName, false, ClassLoader.getSystemClassLoader())
					.getConstructor(Parameters.class, File.class, Simulation.class, ExpressionFactory.class)
					.newInstance(guiDescription, guiFile, simulation, simulation.expressionFactory);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while creating the gui.", e);
		}

		return gui;
	}

	/**
	 * Initializes and returns the list of phases of the circuit period.
	 * 
	 * @param param
	 *            a XML element with the phases description.
	 * @param sectors
	 *            a collection (a Map:name,sector) of sectors.
	 * @return a list of phases.
	 * @throws RuntimeException
	 *             If an <code>RuntimeException</code> occurs.
	 */
	private static LinkedList<Phase> getNewPhases(Map<String, Sector> sectors, Parameters phasesNode)
			throws RuntimeException {
		if (phasesNode == null) {
			throw new RuntimeException("phasesNode is null");
		}
		final LinkedList<Phase> result = new LinkedList<Phase>();
		final List<Parameters> phases = phasesNode.getAll();
		for (final Parameters element : phases) {
			final String sectorName = element.getName();
			final String phaseName = element.getAttribute("action");
			final boolean shuffle = Boolean.parseBoolean(element.getAttribute("shuffle"));
			final Sector sector = sectors.get(sectorName);
			if (sector == null) {
				throw new RuntimeException(
						"Error while parsing phases: sector: " + sectorName + " (sector not found).");
			}
			final Phase newPhase = sector.getPhase(phaseName, shuffle);
			if (newPhase == null) {
				throw new RuntimeException("Error while parsing phases: null phase \"" + phaseName + "\" for sector \""
						+ sectorName + "\".");
			}
			result.add(newPhase);
		}
		return result;
	}

	private static Sector getNewSector(final Simulation simulation, final Parameters parameters,
			final String defaultClassName) {
		if (!parameters.getName().equals("sector")) {
			throw new RuntimeException("Bad element: " + parameters.getName());
		}

		final String sectorClassName;
		if (parameters.getAttribute("className").isEmpty()) {
			sectorClassName = defaultClassName;
		} else {
			sectorClassName = parameters.getAttribute("className");
		}
		if (sectorClassName.isEmpty()) {
			throw new RuntimeException("Sector: Attribute \"className\" is missing or empty.");
		}

		final Sector sector;
		try {
			sector = (Sector) Class.forName(sectorClassName, false, ClassLoader.getSystemClassLoader())
					.getConstructor(Parameters.class, Simulation.class).newInstance(parameters, simulation);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while creating the sector.", e);
		}

		return sector;
	}

	/**
	 * Initializes and returns the sectors.
	 * 
	 * @param circuit
	 *            the circuit.
	 * @param params
	 *            the parameters.
	 * @return a map (name of the sector, sector).
	 * @throws RuntimeException
	 *             If something goes wrong.
	 */
	private static HashMap<String, Sector> getNewSectors(Simulation circuit, Element params) throws RuntimeException {
		final LinkedHashMap<String, Sector> result = new LinkedHashMap<String, Sector>();
		final Element sectorsNode = (Element) params.getElementsByTagName("sectors").item(0);
		final NodeList sectorsList = sectorsNode.getChildNodes();
		for (int i = 0; i < sectorsList.getLength(); i++) {
			final Node item = sectorsList.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				final Parameters sectorParameters = new Parameters(item);
				final Element element = (Element) item;
				final String sectorName = element.getNodeName();
				final String sectorQualifiedName = element.getAttribute("type");
				if (sectorQualifiedName == null) {
					throw new RuntimeException(sectorName + ".type not found");
				}
				Sector sector = null;
				try {
					sector = (Sector) Class.forName(sectorQualifiedName, false, ClassLoader.getSystemClassLoader())
							.getConstructor(Parameters.class, Simulation.class).newInstance(sectorParameters, circuit);
					result.put(sectorName, sector);
				} catch (Exception e) {
					throw new RuntimeException(
							"Error while creating \"" + sectorName + "\" as \"" + sectorQualifiedName + "\"", e);
				}
			}
		}
		return result;
	}

	/**
	 * The date of creation of this simulation.
	 */
	private Date date = new Date();

	/** The events. */
	private final Map<Integer, List<Parameters>> events;

	/**
	 * The expression factory;
	 */
	final private ExpressionFactory expressionFactory = new ExpressionFactory(this);

	/** The scenario file. */
	final private File file;

	/** The graphical user interface. */
	private final Gui gui;

	/**
	 * The model.
	 */
	final private String model;

	/** A flag that indicates if the simulation is paused or not. */
	private boolean pause;

	/** The phases of the circuit. */
	private final List<Phase> phases;

	/**
	 * A collection of data the agents can access (e.g. the inflation rate)
	 */
	private final Map<String, Expression> publicData = new HashMap<>();

	/** The random. */
	private final Random random;

	/** A flag that indicates if the simulation is running or not. */
	private boolean run = true;

	/**
	 * 
	 */
	private final Parameters scenario;

	/** The sectors of the circuit. */
	private final Map<String, Sector> sectors = new HashMap<>();

	/**
	 * Provides access to the simulation duration.
	 */
	final private Expression simDuration = new Expression() {

		@Override
		public Double getValue() {
			final long now = System.currentTimeMillis();
			final Double value;
			if (start == null) {
				value = null;
			} else {
				value = new Double(now - start);
			}
			return value;
		}

		@Override
		public String toString() {
			return "simulationDuration";
		}

	};

	/**
	 * Provides access to the free memory.
	 */
	private Expression simFreeMemory = new Expression() {

		@Override
		public Double getValue() {
			return (double) Runtime.getRuntime().freeMemory();
		}

		@Override
		public String toString() {
			return "freeMemory";
		}

	};

	/**
	 * A simple date format.
	 */
	final private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);

	/**
	 * Provides access to the simulation instantaneous speed.
	 */
	private final Expression simSpeed = new Expression() {

		@Override
		public Double getValue() {
			Jamel.notYetImplemented();
			return null;
			// TODO return speed;
		}

		@Override
		public String toString() {
			return "speed";
		}

	};

	/**
	 * Provides access to the value of the current period.
	 */
	final private Expression simTime = new Expression() {

		@Override
		public Double getValue() {
			return (double) timer.getPeriod();
		}

		@Override
		public String toString() {
			return "t";
		}

	};

	/**
	 * Provides access to the simulation total memory.
	 */
	private final Expression simTotalMemory = new Expression() {

		@Override
		public Double getValue() {
			return (double) Runtime.getRuntime().totalMemory();
		}

		@Override
		public String toString() {
			return "totalMemory";
		}

	};

	/**
	 * The start date of the simulation, in millisecond.
	 */
	private Long start = null;

	/** The timer. */
	private final BasicTimer timer;

	/**
	 * Creates a new basic circuit.
	 * 
	 * @param elem
	 *            an XML element with the parameters for the new circuit.
	 * @param file
	 *            the scenario file.
	 * @throws RuntimeException
	 *             If something goes wrong.
	 */
	public BasicSimulation(final Parameters scenario, final File file) throws RuntimeException {

		this.file = file;
		this.scenario = scenario;
		this.timer = new BasicTimer(0);

		this.random = new Random(this.scenario.getIntAttribute("randomSeed"));

		this.model = scenario.getAttribute("model");

		// Looks for the sectors.

		{
			final Parameters sectorsTag = this.scenario.get("sectors");
			final String defaultClassName;
			final String defaultClassAttribute = sectorsTag.getAttribute("defaultClassName");
			if (this.model.isEmpty()) {
				defaultClassName = defaultClassAttribute;
			} else {
				defaultClassName = this.model + "." + defaultClassAttribute;
			}
			for (final Parameters params : sectorsTag.getAll("sector")) {
				final Sector sector = getNewSector(this, params, defaultClassName);
				this.sectors.put(sector.getName(), sector);
				Jamel.println("new sector", sector.getName());
			}
		}

		this.phases = getNewPhases(this.sectors, scenario.get("phases"));
		this.events = getNewEvents(scenario.get("events"));
		this.gui = getNewGui(scenario.get("gui"), this);

		// TODO should be a parameter.
		final String inflation = "val( Firms , salesValue , t, sum ) * val( Firms , salesVolume , t-12, sum ) /  ( val( Firms , salesVolume , t, sum ) * val( Firms , salesValue , t-12, sum )) -1";
		this.publicData.put("inflation", this.expressionFactory.getExpression(inflation));

	}

	/**
	 * Executes the specified event.
	 * 
	 * @param event
	 *            a XML element that describes the event to be executed.
	 */
	private void doEvent(Parameters event) {
		if (event.getName().equals("pause")) {
			pause();
			// 2016-05-01
			// } else if (event.getNodeName().equals("export")) {
			// this.dataManager.export(event);
		} else if (event.getName().equals("end")) {
			this.run = false;
		} else if (event.getName().equals("marker")) {
			// 2016-03-27
			// Does nothing (a marker is added to the charts).
		} else {
			throw new RuntimeException("Unknown event: " + event.getName());
		}
	}

	/**
	 * Executes the events of the simulation.
	 */
	private void doEvents() {
		final List<Parameters> eventList = this.events.get(this.timer.getPeriod());
		if (eventList != null) {
			for (Parameters event : eventList) {
				final String eventName = event.getName();
				/*final String markerMessage = event.getAttribute("marker");
				if (!"".equals(markerMessage)) {
					this.gui.addMarker(markerMessage, this.timer.getPeriod().intValue());
					TODO IMPLEMENT ME
				}*/
				final Sector sector = this.sectors.get(eventName);
				if (sector != null) {
					sector.doEvent(event);
				} else {
					this.doEvent(event);
				}
			}
		}
	}

	/**
	 * Pause.
	 */
	private void doPause() {
		if (isPaused()) {
			while (isPaused()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Executes a period of the circuit.
	 */
	private void doPeriod() {

		this.doEvents();
		this.doPause();
		this.timer.next();

		for (Phase phase : phases) {
			try {
				phase.run();
			} catch (Exception e) {
				throw new RuntimeException("Something went wrong while running the phase: '" + phase.getName()
						+ "', sector: '" + phase.getSector().getName() + "'.", e);
			}
		}

		this.gui.refresh();

		/*
		final boolean refereshGui = this.timer.getPeriod() % this.refresh == 0;
		if (refereshGui) {
			this.gui.refresh();
		}
		 */

	}

	@Override
	public Expression getDuration() {
		return this.simDuration;
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public Expression getFreeMemory() {
		return this.simFreeMemory;
	}

	@Override
	public String getInfo(String query) {
		final String result;
		if (query.equals("name")) {
			result = this.file.getName();
		} else if (query.equals("date")) {
			result = simpleDateFormat.format(this.date);
		} else if (query.equals("path")) {
			result = this.file.getPath();
		} else if (query.startsWith("meta-")) {
			result = getMeta(this.scenario, query.split("-", 2)[1]);
		} else {
			throw new IllegalArgumentException("Bad query: \"" + query + "\"");
		}
		return result;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	@Override
	public String getName() {
		return this.file.getName();
	}

	@Override
	public int getPeriod() {
		return this.timer.getPeriod();
	}

	@Override
	public Double getPublicData(String key) {
		/*
		 * 2016-03-17 / Pour permettre à la banque d'accéder au taux
		 * d'inflation.
		 */
		return this.publicData.get(key).getValue();
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public Sector getSector(String sectorName) {
		return this.sectors.get(sectorName);
	}

	@Override
	public Expression getSpeed() {
		return this.simSpeed;
	}

	@Override
	public Expression getTime() {
		return this.simTime;
	}

	@Override
	public Expression getTotalMemory() {
		return this.simTotalMemory;
	}

	@Override
	public boolean isPaused() {
		return pause;
	}

	@Override
	public void pause() {
		this.pause = !this.pause;
	}

	/**
	 * Runs the simulation.
	 */
	@Override
	public void run() {

		Jamel.println("BasicCircuit.run()");
		this.start = System.currentTimeMillis();

		while (this.run) {
			this.doPeriod();
		}

		// Ciao bye bye.

	}

}
