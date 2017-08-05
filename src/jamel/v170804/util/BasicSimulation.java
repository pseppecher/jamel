package jamel.v170804.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jfree.data.xy.XYSeries;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jamel.Jamel;
import jamel.util.Expression;
import jamel.util.Parameters;
import jamel.util.Phase;
import jamel.util.Sector;
import jamel.util.Simulation;
import jamel.v170804.data.Export;
import jamel.v170804.data.ExpressionFactory;
import jamel.v170804.gui.DynamicXYSeries;
import jamel.v170804.gui.Gui;

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
	private static Gui getNewGui(final Parameters params, final Simulation simulation) {

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
					.getConstructor(Parameters.class, File.class, Simulation.class)
					.newInstance(guiDescription, guiFile, simulation);
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

	/**
	 * The list of the series to update.
	 */
	private List<DynamicXYSeries> dynamicSeries = new LinkedList<>();

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
	 * The name of the simulation.
	 */
	private final String name;

	/**
	 * The start of the simulation (in milliseconds).
	 * For performance measure.
	 */
	private Long now = null;

	/**
	 * A flag that indicates whether the simulation is paused or not.
	 */
	private boolean pause = true;

	/**
	 * The list of the phases of the period.
	 */
	final private List<Phase> phases = new LinkedList<>();

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
	final private Map<String, Sector> sectors = new HashMap<>();

	/**
	 * A simple date format.
	 */
	final private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);

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
	 * An expression that returns the value of the current period.
	 */
	final private Expression time = new Expression() {

		@Override
		public Double getValue() {
			return (double) timer.getValue();
		}

		@Override
		public String toString() {
			return "t";
		}

	};

	/**
	 * The timer.
	 */
	final private Timer timer;

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
		this.name = file.getName();

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

		// Looks for the phases.

		{
			final Parameters phasesTag = this.scenario.get("phases");
			for (final Parameters params : phasesTag.getAll("phase")) {
				final String phaseName = params.getAttribute("name");
				final String[] options = params.getAttribute("options").split(",");
				// TODO IMPLEMENT : shuffle =
				// params.getElementsByTagName("shuffle").item(0) != null;
				final String[] sectorNames = params.splitTextContent(",");
				for (String sectorName : sectorNames) {
					final Sector sector = this.sectors.get(sectorName);
					if (sector == null) {
						throw new RuntimeException("Sector not found: \'" + sectorName + "\'");
					}
					final Phase phase = sector.getPhase(phaseName, options);
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

		// Looks for the gui.

		/*try {
			SwingUtilities.invokeAndWait(new Runnable() {
		
				@Override
				public void run() {
					{
						if (guiP == null) {
							gui = null;
						} else {
							gui = getNewGui(guiP, BasicSimulation.this);
						}
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

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

	}

	/**
	 * Executes the specified event.
	 * 
	 * @param event
	 *            the event to be executed.
	 */
	private void doEvent(Parameters event) {
		switch (event.getName()) {
		case "pause":
			this.pause = true;
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
				if (event.getAttribute("sector").isEmpty()) {
					if (event.getName().startsWith("gui.")) {
						this.gui.doEvent(event);
					} else {
						this.doEvent(event);
					}
				} else {
					final String sectorName = event.getAttribute("sector");
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
			this.gui.refresh();// this.gui.notifyPause(true);
			while (isPaused()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// this.gui.notifyPause(false);
		}
		/*else {
			// TODO tester l'utilité de cette dernière notification.
			this.gui.notifyPause(false);
		}*/
	}

	/**
	 * Executes a period of the simulation.
	 */
	private void period() {

		this.gui.open();

		for (Sector sector : this.sectors.values()) {
			sector.open();
		}

		for (final Phase phase : this.phases) {
			try {
				phase.run();
			} catch (Exception e) {
				if (this.gui != null) {
					this.gui.displayErrorMessage("Error", "Something went wrong.<br>" + "Sector: '"
							+ phase.getSector().getName() + "', phase: '" + phase.getName() + "'");
				}
				throw new RuntimeException("Something went wrong while running the phase: '" + phase.getName()
						+ "', for the sector: '" + phase.getSector().getName() + "'.", e);
			}
		}

		for (Sector sector : this.sectors.values()) {
			sector.close();
		}

		final boolean refereshGui = this.timer.getValue() % this.refresh == 0;
		for (final DynamicXYSeries series : dynamicSeries) {
			series.update(refereshGui);
		}
		if (refereshGui) {
			this.gui.refresh();
		}

		this.gui.close();

		for (final Export export : this.exports) {
			export.run();
		}
		this.doEvents();
		this.doPause();
		this.timer.next();
	}

	@Override
	public Expression getDataAccess(final String key) {
		final Expression result;

		if (key.equals("t")) {
			result = this.time;
		}

		else if (key.equals("speed")) {
			result = new Expression() {

				@Override
				public Double getValue() {
					return speed;
				}

				@Override
				public String toString() {
					return "speed";
				}

			};
		}

		else if (key.equals("totalMemory")) {
			result = new Expression() {

				@Override
				public Double getValue() {
					return (double) Runtime.getRuntime().totalMemory();
				}

				@Override
				public String toString() {
					return "totalMemory";
				}

			};
		}

		else if (key.equals("freeMemory")) {
			result = new Expression() {

				@Override
				public Double getValue() {
					return (double) Runtime.getRuntime().freeMemory();
				}

				@Override
				public String toString() {
					return "freeMemory";
				}

			};
		}

		else if (key.equals("alea")) {
			result = new Expression() {

				@Override
				public Double getValue() {
					return random.nextDouble();
				}

				@Override
				public String toString() {
					return "alea";
				}

			};
		}

		else if (key.equals("duration")) {
			result = new Expression() {

				@Override
				public Double getValue() {
					final Double value;
					if (now == null || start == null) {
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
		}

		else if (Pattern.matches("val[\\(].*[\\)]", key)) {
			final String argString = key.substring(4, key.length() - 1);
			final String[] split = argString.split(",");
			final Sector sector = this.sectors.get(split[0]);
			if (sector == null) {
				throw new RuntimeException("Sector not found: " + split[0]);
			}
			final String[] args = Arrays.copyOfRange(split, 1, split.length);
			// TODO le premier argument devrait contenir non seulement le nom du
			// secteur, mais aussi (éventuellement) des instructions permettant
			// de limiter la sélection à un sous-ensemble des agents de ce
			// secteur.
			result = sector.getDataAccess(args);
		} else {
			throw new RuntimeException("Not yet implemented: \'" + key + "\'");
		}
		return result;
	}

	@Override
	public Expression getExpression(String key) {
		return this.expressionFactory.getExpression(key);
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public String getInfo(final String query) {
		final String result;
		if (query.equals("name")) {
			result = this.name;
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
		return this.name;
	}

	@Override
	public int getPeriod() {
		return this.timer.getValue();
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
		return this.sectors.get(sectorName);
	}

	@Override
	public XYSeries getSeries(final String x, final String y, final String conditions) {
		DynamicXYSeries newSeries = null;
		try {
			final Expression xExp = getExpression(x);
			final Expression yExp = getExpression(y);
			if (conditions == null) {
				newSeries = new DynamicXYSeries(xExp, yExp);
			} else {
				final String[] strings = ExpressionFactory.split(conditions);
				final Expression[] conditionsExp = new Expression[strings.length];
				for (int i = 0; i < strings.length; i++) {
					conditionsExp[i] = getExpression(strings[i]);
				}
				newSeries = new DynamicXYSeries(xExp, yExp, conditionsExp);
			}
			this.dynamicSeries.add(newSeries);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return newSeries;
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
		this.now = start;
		while (this.run) {
			final long before = System.currentTimeMillis();
			this.period();
			final long after = System.currentTimeMillis();
			this.speed = 1. / (after - before);
			this.now = System.currentTimeMillis();
		}

	}

}
