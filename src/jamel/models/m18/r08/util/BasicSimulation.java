package jamel.models.m18.r08.util;

import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jamel.Jamel;
import jamel.data.Export;
import jamel.data.Expression;
import jamel.data.ExpressionFactory;
import jamel.gui.Gui;
import jamel.util.BasicTimer;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;

/**
 * A basic simulation.
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
				Jamel.errorMessage("Error while creating the GUI", "No such file: '" + fileName + "'");
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
	 * Creates and returns a new sector.
	 * 
	 * @param simulation
	 *            the parent simulation
	 * @param parameters
	 *            an XML element that specifies the sector to be created.
	 * @param defaultClassName
	 *            the default class name of the sector to be created.
	 * 
	 * @return the new sector.
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
	 * The list of the exports.
	 */
	final private List<Export> exports = new LinkedList<>();

	/**
	 * The expression factory;
	 */
	final private ExpressionFactory expressionFactory = new ExpressionFactory(this);

	/**
	 * The file that contains the description of the simulation.
	 */
	final private File file;

	/**
	 * The Gui.
	 */
	private Gui gui;

	/**
	 * A flag that indicates whether the simulation is paused or not.
	 */
	private boolean pause = true;

	/**
	 * The list of the phases of the period.
	 */
	final private List<Phase> phases = new LinkedList<>();

	/**
	 * A collection of expressions to copute the data the agents can access
	 * (e.g. the inflation rate).
	 */
	private final Map<String, Expression> publicData = new HashMap<>();

	/**
	 * A collection of value the agents can access (e.g. the inflation rate).
	 * These values are computed at the end of the period.
	 */
	private final Map<String, Double> publicValues = new HashMap<>();

	/**
	 * The random.
	 */
	final private Random random;

	/**
	 * The number of periods between two refreshing of the gui.
	 */
	final private Integer refresh;

	/**
	 * A flag that indicates if the simulation runs.
	 */
	private boolean run = false;

	/**
	 * The parameters of the simulation.
	 */
	final private Parameters scenario;

	/**
	 * The collection of the sectors, with access by their names.
	 */
	final private Map<String, Sector> sectors = new LinkedHashMap<>();

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
			return speed;
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
	 * The instantaneous speed of the simulation (ie, the number of periods by
	 * ms).
	 * For performance measure.
	 */
	private Double speed = null;

	/**
	 * The start of the simulation (in milliseconds).
	 * For performance measure.
	 */
	private Long start = null;

	/**
	 * The timer.
	 */
	final private BasicTimer timer;

	/**
	 * Creates an new simulation.
	 * 
	 * @param scenario
	 *            the parameters of the simulation.
	 * @param file
	 *            The file that contains the description of the simulation.
	 */
	public BasicSimulation(final Parameters scenario, final File file) {

		this.scenario = scenario;
		this.file = file;
		this.timer = new BasicTimer(0);

		// Inits the random.

		this.random = new Random(this.scenario.getIntAttribute("randomSeed"));

		// Looks for the sectors.

		{
			final Parameters sectorsTag = this.scenario.get("sectors");
			final String defaultClassName = sectorsTag.getAttribute("defaultClassName");
			for (final Parameters params : sectorsTag.getAll("sector")) {
				final Sector sector = getNewSector(this, params, defaultClassName);
				this.sectors.put(sector.getName(), sector);
				Jamel.println("new sector", sector.getName());
			}
		}

		// Populate the sectors.

		{
			for (Sector sector : sectors.values()) {
				if (sector instanceof BasicSector) {
					((BasicSector) sector).populate();
				}
			}
		}

		// Looks for the phases.

		{
			final Parameters phasesTag = this.scenario.get("phases");
			for (final Parameters params : phasesTag.getAll("phase")) {
				final String phaseName = params.getAttribute("name");
				final boolean shuffle = Boolean.parseBoolean(params.getAttribute("shuffle"));
				final String[] sectorNames = params.splitTextContent(",");
				for (String sectorName : sectorNames) {
					final Sector sector = this.sectors.get(sectorName);
					if (sector == null) {
						throw new RuntimeException("Sector not found: \'" + sectorName + "\'");
					}
					final Phase phase = sector.getPhase(phaseName, shuffle);
					if (phase == null) {
						throw new RuntimeException(
								"Sector: " + sectorName + ", unable to create the phase: \'" + phaseName + "\'");
					}
					this.phases.add(phase);
				}
			}
		}

		// Looks for the exports.

		{
			final Parameters exportsParameters = this.scenario.get("exports");
			if (exportsParameters != null) {
				for (final Parameters param : exportsParameters.getAll("export")) {
					this.exports.add(new Export(param, this));
				}
			}
		}

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

		{
			final Parameters guiP = this.scenario.get("gui");
			if (guiP == null) {
				this.gui = null;
				this.refresh = null;
			} else {
				this.gui = getNewGui(guiP, this);
				if (!guiP.hasAttribute("refresh")) {
					throw new RuntimeException("gui: Missing attribute: refresh");
				}
				this.refresh = guiP.getIntAttribute("refresh");
			}
		}

		final Parameters phasesTag = this.scenario.get("public");
		if (phasesTag != null) {
			for (final Parameters params : phasesTag.getAll("data")) {
				final String name = params.getAttribute("name");
				final String value = params.getText();
				this.publicData.put(name, this.expressionFactory.getExpression(value));
			}
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
					this.event(event);
				} else {
					final String sectorName = event.getAttribute("sector");
					// TODO test if sector exists else throw new exception
					this.sectors.get(sectorName).doEvent(event);
				}
			}
		}
	}

	/**
	 * Pauses the simulation.
	 */
	private void doPause() {
		if (isPaused()) {
			this.gui.refresh();
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
	 * Executes a period of the simulation.
	 */
	private void doPeriod() {

		for (Sector sector : this.sectors.values()) {
			sector.open();
		}

		for (final Phase phase : this.phases) {
			try {
				phase.run();
			} catch (Exception e) {
				final String where = "Sector: '" + phase.getSector().getName() + "', Phase: '" + phase.getName() + "'";
				if (this.gui != null) {
					this.gui.displayErrorMessage("Error", "Something went wrong.<br>" + where + ".", e);
				}
				throw new RuntimeException(where, e);
			}
		}

		for (Sector sector : this.sectors.values()) {
			sector.close();
		}

		// 2018-01-30
		// On met à jour les valeurs des données publiques (eg. l'inflation).
		// Cela ne peut être fait qu'une fois tous les secteurs fermés,
		// ie, une fois que toutes les données de la période ont été
		// reccueillies.
		for (final Entry<String, Expression> entry : this.publicData.entrySet()) {
			final String key = entry.getKey();
			final Double value = entry.getValue().getValue();
			this.publicValues.put(key, value);
		}

		final boolean refereshGui = this.timer.getPeriod() % this.refresh == 0;
		if (refereshGui) {
			this.gui.refresh();
		}

		for (final Export export : this.exports) {
			export.run();
		}
		this.doEvents();
		this.doPause();
		this.timer.next();
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
	public void event(Parameters event) {
		switch (event.getName()) {
		case "do":
			final String action = event.getAttribute("action");
			switch (action) {
			case "pause":
				this.pause = true;
				break;
			case "exportCharts":
				/* 
				 * TODO : il me semble qu'il y a un pb ici. 
				 * Plutôt que de recevoir un évènement de la part de l'interface, 
				 * la simulation devrait consulter l'interface et recueillir une liste d'événements à effectuer.
				 */
				this.gui.doEvent(event);
				break;
			case "beep":
				Toolkit.getDefaultToolkit().beep();
				break;
			default:
				throw new RuntimeException("Not yet implemented: \'" + action + "\'");
			}
			break;
		default:
			throw new RuntimeException("Not yet implemented: \'" + event.getName() + "\'");
		}
	}

	@Override
	public boolean eventMethodImplemented() {
		return true;
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
	public String getInfo(final String query) {
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
		 Pour permettre à la banque d'accéder au taux d'inflation.
		 */
		final Expression exp = this.publicData.get(key);
		if (exp == null) {
			throw new RuntimeException("Unknown expression: " + key);
			/*
			En cas d'erreur ici c'est sans doute qu'il manque dans le scénario qqchose comme ça:
			<public comment="Ici les informations accessibles publiquement.">
				<data
					name="inflation"
					comment="Le taux d'inflation ainsi calculé est utilisé par la banque pour calculer le taux d'intérêt."
				>
					val(Sector2, salesValue, t, sum) * val(Sector2, salesVolume, t-12, sum)
					/ (val(Sector2, salesVolume, t, sum) * val(Sector2, salesValue, t-12, sum)) - 1
				</data>
			</public>		
			*/
		}

		/*
		 * On ne calcule pas la valeur de l'expression, parce qu'on est en cours de période.
		 * On renvoie la valeur en cache, qui a été calculée à la fin de la prériode précédente.
		 */
		return this.publicValues.get(key);

	}

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	@Override
	public Random getRandom() {
		return random;
	}

	@Override
	public Sector getSector(final String sectorName) {
		final Sector result = this.sectors.get(sectorName);
		if (result == null) {
			final String message = "Something went wrong while looking for a sector.";
			Jamel.println("***");
			Jamel.println(message);
			Jamel.println("sectorName: " + sectorName);
			Jamel.println();
			throw new RuntimeException(message);
		}
		return result;
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
		return this.pause;
	}

	@Override
	public void pause() {
		this.pause = !this.pause;
	}

	@Override
	public void run() {
		this.run = true;
		this.doPause();
		this.start = System.currentTimeMillis();
		while (this.run) {
			final long before = System.currentTimeMillis();
			this.doPeriod();
			final long after = System.currentTimeMillis();
			this.speed = 1. / (after - before);
		}

	}

}
