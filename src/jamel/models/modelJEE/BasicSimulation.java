package jamel.models.modelJEE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	 * @param sectors
	 *            a collection (a Map:name,sector) of sectors.
	 * @param description
	 *            the description of the phases.
	 * @return a list of phases.
	 * @throws RuntimeException
	 *             If an <code>RuntimeException</code> occurs.
	 */
	private static LinkedList<Phase> getNewPhases(Map<String, Sector> sectors, Parameters description)
			throws RuntimeException {
		if (description == null) {
			throw new RuntimeException("phasesNode is null");
		}
		final LinkedList<Phase> result = new LinkedList<>();
		final List<Parameters> phases = description.getAll();
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

	/**
	 * Creates and returns a new sector.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 * @param parameters
	 *            the parameters.
	 * @param defaultClassName
	 *            the default class name of the sector.
	 * @return a new sector.
	 */
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
	 * The date of creation of this simulation.
	 */
	private Date date = new Date();

	/** The events. */
	private final Map<Integer, Parameters> events = new HashMap<>();

	/**
	 * The expression factory;
	 */
	final private ExpressionFactory expressionFactory = new ExpressionFactory(this);

	/** The scenario file. */
	final private File file;

	/** The graphical user interface. */
	private final Gui gui;

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
	 * Creates a new basic simulation.
	 * 
	 * @param scenario
	 *            the parameters for the new simulation.
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

		// Looks for the sectors.

		{
			final Parameters sectorsTag = this.scenario.get("sectors");
			final String defaultClassAttribute = sectorsTag.getAttribute("defaultClassName");
			final String defaultClassName = defaultClassAttribute;
			for (final Parameters params : sectorsTag.getAll("sector")) {
				final Sector sector = getNewSector(this, params, defaultClassName);
				this.sectors.put(sector.getName(), sector);
				Jamel.println("new sector", sector.getName());
			}
		}

		this.phases = getNewPhases(this.sectors, scenario.get("phases"));

		// Looks for the events.

		{
			final Parameters eventsTag = this.scenario.get("events");
			if (eventsTag != null) {
				for (Parameters event : eventsTag.getAll("when")) {
					final int period = event.getIntAttribute("t");
					if (this.events.containsKey(period)) {
						throw new RuntimeException("Events already defined for the period: " + period);
					}
					this.events.put(period, event);
				}
			}
		}

		this.gui = getNewGui(scenario.get("gui"), this);

		// TODO should be a parameter.
		final String inflation = "val( Firms , salesValue , t, sum ) * val( Firms , salesVolume , t-12, sum ) /  ( val( Firms , salesVolume , t, sum ) * val( Firms , salesValue , t-12, sum )) -1";
		this.publicData.put("inflation", this.expressionFactory.getExpression(inflation));

	}

	/**
	 * Executes the specified event.
	 * 
	 * @param event
	 *            the event to be executed.
	 */
	private void doEvent(Parameters event) {
		switch (event.getName()) {
		case "do":
			final String action = event.getAttribute("action");
			switch (action) {
			case "pause":
				this.pause = true;
				break;
			case "exportCharts":
				this.gui.doEvent(event);
				break;
			default:
				throw new RuntimeException("Not yet implemented: \'" + action + "\'");
			}
			break;
		default:
			throw new RuntimeException("Not yet implemented: \'" + event.getName() + "\'");
		}
	}

	/**
	 * Executes the events of the simulation.
	 */
	private void doEvents() {
		final Parameters currentEvents = this.events.get(getPeriod());
		if (currentEvents != null) {
			for (Parameters event : currentEvents.getAll()) {
				if (!event.hasAttribute("sector")) {
					this.doEvent(event);
				} else {
					final String sectorName = event.getAttribute("sector");
					// TODO test if sector exists else throw new exception
					this.sectors.get(sectorName).doEvent(event);
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
	public void displayErrorMessage(String title, String message) {
		if (this.gui != null) {
			this.gui.displayErrorMessage(title, message);
		} else {
			Jamel.errorMessage(title, message);
		}
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
