package jamel;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import jamel.basic.Circuit;
import jamel.basic.util.InitializationException;

/**
 * The main class for Jamel.
 */
@SuppressWarnings("unused")
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
			final String PREF_SCENARIO_PATH = "Jamel.scenario.path";
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

	/**
	 * A convenient class to store String constants.
	 */
	private static class KEY {

		/** The "Download" string. */
		private static final String DOWNLOAD = "Download";

		/** The URI to download the latest version of Jamel. */
		private static final String DOWNLOAD_URI = "http://p.seppecher.free.fr/jamel/download.php";

		/** The "Remind me later" string. */
		private static final String REMIND_ME_LATER = "Remind me later";

		/**
		 * Message see log file for more details.
		 */
		private static final String seeLogFile = "See the jamel.log file for more details.";

		/**
		 * A string of four em spaces. To simulate a tab character.
		 */
		private static final String tab = "&emsp;&emsp;&emsp;&emsp;";

		/** The URL to check the latest version. */
		private static final String VERSION_URL = "http://p.seppecher.free.fr/jamel/version.php";

	}

	/** The user preferences. */
	private static final Preferences prefs = Preferences.userRoot();

	/** The remind-me-later period (in ms). */
	private static final long remindMeLaterPeriod = 15 * 24 * 60 * 60 * 1000;

	/** The simulationID. */
	private static final long simulationID = (new Date()).getTime();

	/** This version of Jamel. */
	final public static int version = 20160509;

	/**
	 * Performs a sensitivity analysis.
	 * 
	 * @param analysis
	 *            an XML doc that describes the analysis to be performed.
	 * @param parent
	 *            the current parent folder.
	 */
	private static void analyse(Document analysis, String parent) {
		final Node scenarioNode = analysis.getElementsByTagName("scenario").item(0);
		if (scenarioNode == null) {
			throw new RuntimeException("Missing node: \"scenario\"");
		}

		final Element scenarioElement = (Element) scenarioNode;

		final String scenarioFileName = scenarioElement.getAttribute("file");
		if ("".equals(scenarioFileName)) {
			throw new RuntimeException("scenario: file attribute is missing or empty.");
		}

		final Map<String, String[]> map = new LinkedHashMap<String, String[]>();
		Integer size = null;
		final NodeList nodeList = analysis.getElementsByTagName("variable");
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Element item = (Element) nodeList.item(i);
			final String key = item.getAttribute("key");
			final String[] values = item.getAttribute("values").split(",");
			if (size == null) {
				size = values.length;
			} else if (size != values.length) {
				Jamel.println("**********");
				Jamel.println("key: " + key);
				Jamel.println("values: " + item.getAttribute("values"));
				Jamel.println("values.lenght(): " + values.length);
				Jamel.println("but expected was: " + size);
				Jamel.println("**********");
				throw new RuntimeException("Error while parsing the variables. See log file for more details.");
			}
			map.put(key, values);
		}

		final String scenarioFilePath = parent + "/" + scenarioFileName;
		File scenarioFile = new File(scenarioFilePath);
		if (!scenarioFile.exists()) {
			throw new RuntimeException("Scenario file not found: " + scenarioFile.getPath());
		}

		// Reading the scenario file.

		final BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(scenarioFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Scenario file not found: " + scenarioFile.getPath(), e);
		}

		final String scenario;
		final StringBuilder stringBuilder = new StringBuilder();
		final String ls = System.getProperty("line.separator");

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			scenario = stringBuilder.toString();
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading the file: " + scenarioFile.getPath(), e);
		}

		//

		final long start = (new Date()).getTime();
		for (int i = 0; i < size; i++) {
			String newScenario = scenario;
			for (String key : map.keySet()) {
				newScenario = newScenario.replaceAll(key, map.get(key)[i]);
			}
			final Document document;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new InputSource(new StringReader(newScenario)));
			} catch (SAXException e) {
				throw new RuntimeException("Something went wrong while creating the document.", e);
			} catch (IOException e) {
				throw new RuntimeException("Something went wrong while creating the document.", e);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("Something went wrong while creating the document.", e);
			}
			try {
				final Circuit circuit = Jamel.newCircuit(document, scenarioFile.getParent(), null);
				circuit.run();
			} catch (InitializationException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			final long now = (new Date()).getTime();
			final int laps = (int) ((now - start) / 1000);
			Jamel.println("since start: " + laps + " s");
		}
	}

	/**
	 * Downloads the latest version of Jamel.
	 * 
	 * @return <code>true</code> if the download is successful,
	 *         <code>false</code> otherwise.
	 */
	private static boolean downloadLatestVersion() {
		boolean result;
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(KEY.DOWNLOAD_URI));
				result = true;
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				result = false;
			}
		} else {
			final JEditorPane jEditorPane = new JEditorPane();
			jEditorPane.setContentType("text/html");
			jEditorPane.setText("<html>Visit <a href=\"" + KEY.DOWNLOAD_URI + "\">" + KEY.DOWNLOAD_URI
					+ "</a> to download Jamel.</html>");
			jEditorPane.setEditable(false);
			jEditorPane.setBackground((new JLabel()).getBackground());
			jEditorPane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
						try {
							java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
				}
			});
			result = true;
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if this version of Jamel is out of date,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this version of Jamel is out of date,
	 *         <code>false</code> otherwise.
	 */
	private static boolean isOutOfDate() {
		class FileReader extends Thread {
			private boolean isOutOfDate = false;

			@Override
			public void run() {
				try {
					final URL u = new URL(KEY.VERSION_URL + "?v=" + version);
					BufferedReader d = new BufferedReader(new InputStreamReader(u.openStream()));
					String line;
					while ((line = d.readLine()) != null) {
						final String words[] = line.split(":");
						if (words.length == 2) {
							if ("version".equals(words[0].trim())) {
								final int newVersion = Integer.parseInt(words[1]);
								isOutOfDate = (newVersion > version);
							}
						}
					}
					d.close();
				} catch (MalformedURLException e) {
				} catch (IOException e) {
				}
			}
		}
		final FileReader fileReader = new FileReader();
		fileReader.start();
		try {
			fileReader.join(500);
		} catch (InterruptedException e) {
		}
		return fileReader.isOutOfDate;
	}

	/**
	 * Creates and returns a new circuit.
	 * 
	 * @param document
	 *            a XML doc that contains the parameters of the new circuit.
	 * @param path
	 *            the path to the scenario file.
	 * @param name
	 *            the name of the scenario file.
	 * @return a new circuit.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static Circuit newCircuit(Document document, String path, String name) throws InitializationException {
		if (path == null) {
			throw new IllegalArgumentException("Path is null");
		}
		Circuit circuit = null;
		final Element root = document.getDocumentElement();
		if (!root.getNodeName().equals("circuit")) {
			throw new InitializationException("The root node of the scenario file must be \"circuit\".");
		}
		final String circuitType = root.getAttribute("type");
		if (circuitType == "") {
			throw new InitializationException("Attribute \"type\" not found for the tag \"circuit\".");
		}
		try {
			circuit = (Circuit) Class.forName(circuitType, false, ClassLoader.getSystemClassLoader())
					.getConstructor(Element.class, String.class, String.class).newInstance(root, path, name);
		} catch (Exception e) {
			throw new InitializationException("Something went wrong while creating the circuit.", e);
		}
		return circuit;
	}

	/**
	 * Parses the content of the given file as an XML document and return a new
	 * DOM Document object.
	 * 
	 * @param file
	 *            the file containing the XML to parse.
	 * @return A new DOM Document object.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static Document readXMLFile(File file) throws InitializationException {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (final Exception e) {
			throw new InitializationException("Something went wrong while reading the file \"" + file.getName() + "\"",
					e);
		}
		return document;
	}

	/**
	 * Performs the specified simulation.
	 * 
	 * @param scenario
	 *            an XML document that describes the simulation to be performed.
	 * @param path
	 *            the path to scenario file.
	 * @param name
	 *            the name of the scenario file.
	 */
	private static void simulate(Document scenario, String path, String name) {

		Circuit circuit = null;
		try {
			circuit = newCircuit(scenario, path, name);
		} catch (InitializationException e) {
			JOptionPane.showMessageDialog(null, "<html>" + e.getMessage() + "<br>" + KEY.seeLogFile + "</html>",
					"Initialization Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
			/*
			 * if (out != null) { out.close(); } System.exit(1);
			 */
		}
		try {
			circuit.run();
		} catch (RuntimeException e) {
			final String message = e.getMessage() + "<br>";
			final String cause;
			final String where;
			if (e.getCause() != null) {
				if (e.getCause().getMessage() == null) {
					if (e.getCause().getCause() != null && e.getCause().getCause().getMessage() != null) {
						cause = KEY.tab + "Cause: " + e.getCause().getCause().getMessage() + "<br>";
						where = KEY.tab + "Where: " + e.getCause().getCause().getStackTrace()[0].toString() + "<br>";
					} else {
						cause = KEY.tab + "Cause: " + e.getCause().toString() + "<br>";
						where = KEY.tab + "Where: " + e.getCause().getStackTrace()[0].toString() + "<br>";
					}
				} else {
					cause = KEY.tab + "Cause: " + e.getCause().getMessage() + "<br>";
					where = KEY.tab + "Where: " + e.getCause().getStackTrace()[0].toString() + "<br>";
				}
			} else {
				cause = KEY.tab + "Cause: unknown.<br>";
				where = "";
			}
			JOptionPane.showMessageDialog(null, "<html>" + message + cause + where + KEY.seeLogFile + "</html>",
					"Runtime Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			/*
			 * if (out != null) { out.close(); }
			 */
		}

	}

	/**
	 * Looks for the latest version of Jamel.
	 * 
	 * @return <code>true</code> if a new version is available and the user
	 *         chooses to download it, <code>false</code> otherwise.
	 */
	private static boolean updateVersion() {
		final boolean result;
		if (isOutOfDate()) {
			final long now = System.currentTimeMillis();
			long previous = prefs.getLong(KEY.REMIND_ME_LATER, 0);
			if (now > previous + remindMeLaterPeriod) {
				final Object[] options = { KEY.DOWNLOAD, KEY.REMIND_ME_LATER };

				final int n = JOptionPane.showOptionDialog(null, "A new version of Jamel is available.", "New version",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					result = downloadLatestVersion();
				} else {
					prefs.putLong(KEY.REMIND_ME_LATER, now);
					result = false;
				}
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Returns the simulation ID.
	 * 
	 * @return the simulation ID.
	 */
	public static long getSimulationID() {
		return simulationID;
	}

	/**
	 * Returns a string description of the current version of Jamel.
	 * 
	 * @return a string description of the current version of Jamel.
	 */
	public static String getVersion() {
		return ""+version;
	}

	/**
	 * The main method for Jamel.
	 * 
	 * @param args
	 *            unused.
	 */
	public static void main(String[] args) {
		PrintStream out = null;
		/*
		 *TODO: commenter ce bloc pour envoyer les messages vers le fichier
		 * log plutot que vers la console d'Eclipse.
		 */
		try {
			out = new PrintStream(new FileOutputStream("jamel.log"));
			System.setOut(out);
			System.setErr(out);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);
		final String dateStr = simpleDateFormat.format(new Date());
		Jamel.println(dateStr);
		Jamel.println(getVersion());

		if (!updateVersion()) {

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
				final String filePath = file.getParent();
				final String fileName = file.getName();
				Document scenario = null;
				try {
					scenario = readXMLFile(file);
				} catch (InitializationException e) {
					JOptionPane.showMessageDialog(null, "<html>" + e.getMessage() + "<br>" + KEY.seeLogFile + "</html>",
							"Initialization Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					if (out != null) {
						out.close();
					}
					System.exit(1);
				}
				if (scenario == null) {
					if (out != null) {
						out.close();
					}
					System.exit(0);
				}

				final String root = scenario.getDocumentElement().getTagName();

				if (root.equals("analysis")) {
					analyse(scenario, filePath);
				} else if (root.equals("circuit")) {
					simulate(scenario, filePath, fileName);
				} else {
					println("This file doesn't seem to be a Jamel scenario: bad root node: " + root);
				}

			}
		}
		if (out != null) {
			out.close();
		}
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
