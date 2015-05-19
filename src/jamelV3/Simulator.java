package jamelV3;

import jamelV3.basic.Circuit;
import jamelV3.basic.util.InitializationException;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The main class for Jamel.
 */
public class Simulator {

	/**
	 * A convenient class to store String constants.
	 */
	private static class KEY {

		/** The "Download" string. */
		public static final String DOWNLOAD = "Download";

		/** The URI to download the latest version of Jamel. */
		public static final String DOWNLOAD_URI = "http://p.seppecher.free.fr/jamel/download.php";

		/** The "Remind me later" string. */
		public static final String REMIND_ME_LATER = "Remind me later";

		/** The default path to the scenario folder. */
		public static final String SCENARIO_DEFAULT_PATHNAME = "scenarios";

		/** The key for the scenario path preference.*/
		public static final String SCENARIO_PATHNAME_PREF = "The key for the scenario path preference";

		/** The URL to check the latest version. */
		public static final String VERSION_URL = "http://p.seppecher.free.fr/jamel/version.php";

	}

	/** The user preferences. */
	private static final Preferences prefs = Preferences.userRoot();

	/** The remind-me-later period (in ms). */
	private static final long remindMeLaterPeriod = 15*24*60*60*1000;

	/** The simulationID. */
	private static final long simulationID = (new Date()).getTime();

	/** This version of Jamel. */
	final public static int version = 20150515;

	/**
	 * Downloads the latest version of Jamel.
	 * @return <code>true</code> if the download is successful, <code>false</code> otherwise.
	 */
	private static boolean downloadLatestVersion() { // TODO WORK IN PROGRESS
		boolean result;
		if(Desktop.isDesktopSupported()) {
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
		}
		else {
			// TODO proposer un lien dans un dialog
			result = true;
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if this version of Jamel is out of date, <code>false</code> otherwise.
	 * @return <code>true</code> if this version of Jamel is out of date, <code>false</code> otherwise.
	 */
	private static boolean isOutOfDate() {
		class FileReader extends Thread {
			private boolean isOutOfDate = false;
			@Override
			public void run() {
				try {
					final URL u = new URL(KEY.VERSION_URL);
					BufferedReader d = new BufferedReader(new InputStreamReader(u.openStream()));
					String line;
					while ((line = d.readLine()) != null) {
						final String words[] = line.split(":");
						if (words.length==2) {
							if ("version".equals(words[0].trim())) {
								final int newVersion = Integer.parseInt(words[1]);
								isOutOfDate=(newVersion>version);
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
	 * @param document a XML doc that contains the parameters of the new circuit. 
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @return a new circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	private static Circuit newCircuit(Document document, String path, String name) throws InitializationException {
		Circuit circuit = null;
		final Element root = document.getDocumentElement();
		final NodeList circuitNodeList = root.getElementsByTagName("circuit");
		if (circuitNodeList.getLength()>1) {
			throw new InitializationException("Multiple tags \"circuit\"");
		}
		final Element circuitElement = (Element) circuitNodeList.item(0);
		if (circuitElement==null) {
			throw new InitializationException("Tag \"circuit\" not found.");
		}
		final String circuitType = circuitElement.getAttribute("type");
		if (circuitType=="") {
			throw new InitializationException("Attribute \"type\" not found for the tag \"circuit\".");				
		}
		try {
			circuit = (Circuit) Class.forName(circuitType,false,ClassLoader.getSystemClassLoader()).getConstructor(Document.class,String.class,String.class).newInstance(document,path,name);
		} catch (IllegalArgumentException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (SecurityException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (InstantiationException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (IllegalAccessException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (InvocationTargetException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (NoSuchMethodException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		} catch (ClassNotFoundException e) {
			throw new InitializationException("Circuit creation failure.",e.getCause());				
		}
		if (circuit==null) {
			throw new InitializationException("Circuit creation failed for an unknown reason.");				
		}		
		return circuit;
	}

	/**
	 * Parses the content of the given file as an XML document and return a new DOM Document object.
	 * @param file the file containing the XML to parse.
	 * @return A new DOM Document object.
	 * @throws InitializationException If something goes wrong.
	 */
	private static Document readXMLFile(File file) throws InitializationException {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		}
		catch (final Exception e) {
			throw new InitializationException("Something went wrong while reading the file \""+file.getName()+"\"",e);			
		}
		return document;
	}

	/**
	 * Returns the file scenario selected by the user.
	 * @return the file selected.
	 */
	private static File selectScenario() {
		final String path = prefs.get(KEY.SCENARIO_PATHNAME_PREF,KEY.SCENARIO_DEFAULT_PATHNAME);
		final JFileChooser fc = new JFileChooser() {{
			this.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));}
		};
		final File dir = new File(path);
		final File file;
		fc.setDialogTitle("Open Scenario");
		fc.setCurrentDirectory(dir);
		final int returnVal = fc.showOpenDialog(null);
		if (returnVal==JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			prefs.put(KEY.SCENARIO_PATHNAME_PREF, file.getPath());
		}
		else {
			file=null;
		}
		return file;
	}

	/**
	 * Looks for the latest version of Jamel.
	 * @return <code>true</code> if a new version is available and the user chooses to download it, <code>false</code> otherwise.
	 */
	@SuppressWarnings("unused")
	private static boolean updateVersion() {
		final boolean result;
		if (isOutOfDate()) {
			final long now = System.currentTimeMillis();
			long previous = prefs.getLong(KEY.REMIND_ME_LATER,0);
			if (now>previous+remindMeLaterPeriod) {
				final Object[] options = {KEY.DOWNLOAD, KEY.REMIND_ME_LATER};
				final int n = JOptionPane.showOptionDialog(null, "A new version of Jamel is available.", "New version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n==0) {
					result = downloadLatestVersion();
				}
				else {
					prefs.putLong(KEY.REMIND_ME_LATER,now);
					result = false;
				}
			}
			else {
				result = false;
			}
		}
		else {
			result = false;
		}
		return result;
	}

	/**
	 * Returns the simulation ID.
	 * @return the simulation ID.
	 */
	public static long getSimulationID() {
		return simulationID;
	}

	/**
	 * The main method for Jamel.
	 * @param args unused.
	 */
	public static void main(String[] args) {
		// if (updateJamel()) {} else 
		{
			final File file = selectScenario();
			if (file!=null) {
				final String path = file.getParent();
				final String name = file.getName();
				Document scenario = null;
				try {
					scenario = readXMLFile(file);
				} catch (InitializationException e1) {
					throw new RuntimeException("Circuit initialization failure.",e1);
				}
				Circuit circuit = null;
				try {
					circuit = newCircuit(scenario, path, name);
				} catch (InitializationException e2) {
					throw new RuntimeException("Circuit initialization failure.",e2);
				}
				circuit.run();					
			}
		}
	}

}

// ***
