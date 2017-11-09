package jamel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jamel.util.NotUsedException;
import jamel.util.NotYetImplementedException;
import jamel.util.Parameters;
import jamel.util.Simulation;

/**
 * The main class for Jamel.
 */
public class Jamel {

	/**
	 * Provides a simple mechanism for the user to choose a scenario.
	 */
	private static class Chooser implements Runnable {

		/**
		 * The selected file.
		 */
		private File file = null;

		/**
		 * Returns the selected file.
		 * 
		 * @return the selected file.
		 */
		public File getFile() {
			return file;
		}

		/**
		 * Pops up an "Open File" file chooser dialog.
		 */
		@Override
		public void run() {
			final Preferences prefs = Preferences.userRoot();
			final String PREF_SCENARIO_PATH = version + ".scenario.path";
			final String defaultFolder = "src/jamel/models";
			final String path = prefs.get(PREF_SCENARIO_PATH, defaultFolder);
			final JFileChooser fc = new JFileChooser() {
				{
					this.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
				}
			};
			File dir = new File(path);
			if (!dir.exists()) {
				dir = new File(defaultFolder);
			}
			fc.setDialogTitle("Open Scenario");
			fc.setCurrentDirectory(dir);
			final int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				final File parent = file.getParentFile();
				prefs.put(PREF_SCENARIO_PATH, parent.getPath());
			} else {
				file = null;
			}
		}

	}

	/**
	 * Message see log file for more details.
	 */
	private static final String seeLogFile = "See the jamel.log file for more details.";

	/** This version of Jamel. */
	final private static int version = 20171106;

	/**
	 * Creates and returns a new simulation.
	 * 
	 * @param parameters
	 *            the description of the new simulation.
	 * @param file
	 *            the scenario file.
	 * @return a new simulation.
	 */
	private static Simulation newSimulation(final Parameters parameters, final File file) {
		if (file == null) {
			throw new IllegalArgumentException("Path is null");
		}
		final Simulation simulation;
		if (!parameters.getName().equals("simulation")) {
			throw new RuntimeException("Bad element: \'" + parameters.getName() + "\'");
		}
		final String model = parameters.getAttribute("model");
		final String simulationClassName;
		if (model.isEmpty()) {
			simulationClassName = parameters.getAttribute("className");
		} else {
			simulationClassName = model + "." + parameters.getAttribute("className");
		}

		if (simulationClassName.isEmpty()) {
			throw new RuntimeException("className is missing or empty");
		}

		try {
			simulation = (Simulation) Class.forName(simulationClassName, false, ClassLoader.getSystemClassLoader())
					.getConstructor(Parameters.class, File.class).newInstance(parameters, file);
		} catch (Exception e) {
			Jamel.println("simulationClassName", simulationClassName);
			throw new RuntimeException("Something went wrong while creating the simulation.", e);
		}
		return simulation;
	}

	/**
	 * Creates and runs a new simulation.
	 * 
	 * @param scenario
	 *            the parameters of the simulation.
	 * @param file
	 *            the scenario file.
	 */
	private static void simulate(final Parameters scenario, final File file) {
		final Simulation simulation = newSimulation(scenario, file);
		try {
			simulation.run();
		} catch (RuntimeException e) {
			e.printStackTrace();
			errorMessage("Error", "Something went wrong while running the simulation (" + e.getMessage() + ").");
		}
	}

	/**
	 * Displays an error message;
	 * 
	 * @param title
	 *            the title.
	 * @param message
	 *            the message.
	 */
	public static void errorMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(null,
				"<html>Jamel said:<br>\"" + message + "\"<br>See the console for more details.</html>", title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Returns a string description of this version of Jamel.
	 * 
	 * @return a string description of this version of Jamel.
	 */
	public static String getVersion() {
		return "" + version;
	}

	/**
	 * The main method for Jamel.
	 * 
	 * @param args
	 *            unused.
	 */
	public static void main(String[] args) {

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);

		println("Jamel " + version);
		println("Start " + simpleDateFormat.format(new Date()));

		final Chooser scenarioChooser = new Chooser();

		try {
			SwingUtilities.invokeAndWait(scenarioChooser);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		final File file = scenarioChooser.getFile();
		if (file != null) {
			Jamel.println("run " + file.getPath());
			final Document scenario;
			try {
				scenario = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			} catch (SAXException | IOException | ParserConfigurationException e) {
				JOptionPane.showMessageDialog(null, "<html>" + e.getMessage() + "<br>" + seeLogFile + "</html>",
						"Initialization Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}
			if (scenario == null) {
				throw new RuntimeException("Scenario is null");
			}

			final String root = scenario.getDocumentElement().getTagName();

			if (root.equals("simulation")) {
				simulate(new Parameters(scenario.getDocumentElement()), file);
			} else {
				throw new RuntimeException("This file doesn't seem to be a Jamel scenario: bad root node: " + root);
			}

		}

		Jamel.println();
		Jamel.println("End", simpleDateFormat.format(System.currentTimeMillis()));
		Jamel.println();
	}

	/**
	 * Throws a new {@code NotUsedException}.
	 */
	public static void notUsed() {
		throw new NotUsedException();
	}

	/**
	 * Throws a new {@code NotYetImplementedException}.
	 */
	public static void notYetImplemented() {
		throw new NotYetImplementedException("Not yet implemented.");
	}

	/**
	 * Throws a new {@code NotYetImplementedException} with the specified detail
	 * message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public static void notYetImplemented(String message) {
		throw new NotYetImplementedException(message);
	}

	/**
	 * A short cut for {@code System.out.println()}.
	 */
	public static void println() {
		System.out.println();
	}

	/**
	 * Prints the specified numbers into the "standard" output stream. Numbers
	 * are printed on the same line and are separated by commas.
	 * 
	 * @param numbers
	 *            the numbers to be printed.
	 */
	public static void println(Number... numbers) {
		for (int i = 0; i < numbers.length; i++) {
			System.out.print(numbers[i]);
			if (i < numbers.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}

	/**
	 * Prints several objects.
	 *
	 * @param objects
	 *            The objects to be printed.
	 */
	public static void println(Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			final String string;
			if (objects[i] == null) {
				string = "null";
			} else {
				string = objects[i].toString();
			}
			System.out.print(string);
			if (i < objects.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}

	/**
	 * Prints the specified strings into the "standard" output stream. Strings
	 * are printed on the same line and are separated by commas.
	 * 
	 * @param strings
	 *            the strings to be printed.
	 */
	public static void println(String... strings) {
		for (int i = 0; i < strings.length; i++) {
			System.out.print(strings[i]);
			if (i < strings.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}

}
