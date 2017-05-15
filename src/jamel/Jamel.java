package jamel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import jamel.util.NotYetImplementedException;
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
			final String defaultFolder = "scenarios";
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

	/** The user preferences. */

	/** This version of Jamel. */
	final private static String version = "jamel-20170501";

	/**
	 * Creates and returns a new simulation.
	 * 
	 * @param element
	 *            an XML element that contains the description of the new
	 *            simulation.
	 * @param file
	 *            the file of the scenario.
	 * @return a new simulation.
	 */
	private static Simulation newSimulation(final Element element, final File file) {
		if (file == null) {
			throw new IllegalArgumentException("Path is null");
		}
		final Simulation simulation;
		if (!element.getNodeName().equals("simulation")) {
			throw new RuntimeException("Bad element: " + element.getNodeName());
		}
		final Node simulationClassNameNode = element.getElementsByTagName("simulationClassName").item(0);
		/*final String simulationClassName = element.getAttribute("className");
		if (simulationClassName.isEmpty()) {
			throw new RuntimeException("Attribute \"className\" is missing or empty.");
		}*/if (simulationClassNameNode==null) {
			throw new RuntimeException("Missing node: \"simulationClassName\".");
		}
		try {
			simulation = (Simulation) Class.forName(simulationClassNameNode.getTextContent().trim(), false, ClassLoader.getSystemClassLoader())
					.getConstructor(Element.class, File.class).newInstance(element, file);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while creating the simulation.", e);
		}
		return simulation;
	}

	/**
	 * Repeats several simulations.
	 * 
	 * @param element
	 *            an element that contains the description of the simulations to
	 *            be repeated.
	 * @param parentFile
	 *            the parent file.
	 */
	private static void repeat(final Element element, final File parentFile) {

		// Sets the name of the scenario file.

		if (element.getAttribute("src").isEmpty()) {
			throw new RuntimeException("Simulation attribute src is empty or missing.");
		}
		final String fileName = parentFile.getPath() + "/" + element.getAttribute("src");

		// Reading the scenario file.

		String scenario;
		try (final BufferedReader reader = new BufferedReader(new FileReader(fileName));) {
			final StringBuilder stringBuilder = new StringBuilder();
			final String ls = System.getProperty("line.separator");

			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			scenario = stringBuilder.toString();
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Something went wrong while reading this file: " + fileName, e);
		}

		// Replaces some strings in the scenario

		final NodeList replaceNodeList = element.getElementsByTagName("replace");
		for (int i = 0; i < replaceNodeList.getLength(); i++) {
			final Element item = (Element) replaceNodeList.item(i);
			final String regex = item.getAttribute("regex");
			final String replacement = item.getAttribute("replacement");
			scenario = scenario.replaceAll(regex, replacement);
		}

		// Sets the number of replications

		final int replications;
		if (element.getAttribute("repeat").isEmpty()) {
			throw new RuntimeException("Repeat attribute is missing or empty.");
		}
		try {
			replications = Integer.parseInt(element.getAttribute("repeat"));
		} catch (@SuppressWarnings("unused") java.lang.NumberFormatException e) {
			throw new RuntimeException("Repeat attribute is not a number: " + element.getAttribute("repeat"));
		}

		// Gets the variables.

		final Map<String, String[]> variables = new LinkedHashMap<>();
		final NodeList nodeList = element.getElementsByTagName("variable");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Element item = (Element) nodeList.item(i);
			if (item.getAttribute("key").isEmpty()) {
				throw new RuntimeException("key attribute is missing or empty.");
			}
			final String key = item.getAttribute("key");
			if (item.getAttribute("values").isEmpty()) {
				throw new RuntimeException("values attribute is missing or empty.");
			}
			final String[] values = item.getAttribute("values").split(",");
			if (values.length != replications) {
				Jamel.println();
				Jamel.println("key: " + key);
				Jamel.println("values: " + item.getAttribute("values"));
				Jamel.println("values.lenght() is: " + values.length);
				Jamel.println("but expected was: " + replications);
				Jamel.println();
				throw new RuntimeException("Error while parsing the variables. See log file for more details.");
			}
			variables.put(key, values);
		}

		// Repeats the simulation

		for (int i = 0; i < replications; i++) {

			String newScenario = scenario;

			// Changes the variables

			for (String key : variables.keySet()) {
				newScenario = newScenario.replaceAll(key, variables.get(key)[i]);
			}

			final Element elem2;
			try {
				elem2 = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new InputSource(new StringReader(newScenario))).getDocumentElement();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				Jamel.println();
				Jamel.println("fileName", fileName);
				Jamel.println("replication", i);
				Jamel.println();
				throw new RuntimeException("Something went wrong while creating the XML document.", e);
			}

			simulate(elem2, new File(fileName));

		}

	}

	/**
	 * Runs Jamel.
	 */
	private static void run() {

		final Chooser scenarioChooser = new Chooser();

		try {
			SwingUtilities.invokeAndWait(scenarioChooser);
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException("Something went wrong while choosing the scenario file.", e);
		}

		final File file = scenarioChooser.getFile();

		if (file != null) {
			Jamel.println("run " + file.getPath());

			final Document document;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			} catch (final Exception e) {
				throw new RuntimeException("Something went wrong while reading the file \"" + file.getName() + "\"", e);
			}
			final Element root = document.getDocumentElement();
			if (root.getTagName().equals("simulation")) {
				// C'est une simulation, on l'exécute directement.
				simulate(root, file);
			} else if (root.getTagName().equals("multi-simulation")) {
				// C'est une collection de simulations,
				// on les exécute les unes après les autres
				final NodeList nodeList = root.getElementsByTagName("simulation");
				for (int index = 0; index < nodeList.getLength(); index++) {
					final Element element = (Element) nodeList.item(index);
					repeat(element, file.getParentFile());
				}
			} else {
				// C'est n'importe quoi, fin.
				throw new RuntimeException("Bad root element: " + root.getTagName());
			}

		}
	}

	/**
	 * Creates and runs a new simulation.
	 * 
	 * @param scenario
	 *            an element that contains the description of the simulation.
	 * @param file
	 *            the file.
	 */
	private static void simulate(final Element scenario, final File file) {
		final Simulation simulation = newSimulation(scenario, file);
		try {
			simulation.run();
		} catch (RuntimeException e) {
			e.printStackTrace();
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
	 * The main method for Jamel.
	 * 
	 * @param args
	 *            unused.
	 */
	public static void main(String[] args) {

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);

		Jamel.println(version);
		Jamel.println("Start", simpleDateFormat.format(new Date()));
		Jamel.println();

		try {
			run();
		} catch (RuntimeException e) {
			e.printStackTrace();
			final String message;
			if (e.getCause() != null && e.getCause().getMessage() != null) {
				message = e.getMessage() + "<br>" + e.getCause().getMessage();
			} else {
				message = e.getMessage();
			}
			errorMessage("Runtime Error", message);
		}

		Jamel.println();
		Jamel.println("End", simpleDateFormat.format(new Date()));
		Jamel.println();
	}

	/**
	 * Throws a new <code>NotYetImplementedException</code>.
	 */
	public static void notYetImplemented() {
		throw new NotYetImplementedException();
	}

	/**
	 * A short cut for <code>System.out.println()</code>.
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
	 *            The <code>Objects</code> to be printed.
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

// ***
