package jamel;

import jamel.basic.util.JamelParameters;
import jamel.util.Circuit;
import jamel.util.FileParser;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The main class for Jamel.
 */
public class Simulator {

	/**
	 * A convenient class to store String constants.
	 */
	private static class KEY {

		/** The key for the parameter that contains the fully qualified name of the desired class of Circuit. */
		public static final String CIRCUIT_TYPE = "Circuit.type"; 

		/** The "Download" string. */
		public static final String DOWNLOAD = "Download";

		/** The URI to download the latest version of Jamel. */
		public static final String DOWNLOAD_URI = "http://p.seppecher.free.fr/jamel/download.php";

		/** The key for the parameter that contains the name of the scenario file. */
		public static final String FILENAME = "Circuit.fileName";

		/** The "Remind me later" string. */
		public static final String REMIND_ME_LATER = "Remind me later";

		/** The default path to the scenario folder. */
		public static final String SCENARIO_DEFAULT_PATHNAME = "scenarios/";

		/** The key for the scenario path preference.*/
		public static final String SCENARIO_PATHNAME_PREF = "The key for the scenario path preference";

		/** The URL to check the latest version. */
		public static final String VERSION_URL = "http://p.seppecher.free.fr/jamel/version.php";

	}

	/** The user preferences. */
	private static final Preferences prefs = Preferences.userRoot();

	/** The remind-me-later period (in ms). */
	private static final long remindMeLaterPeriod = 15*24*60*60*1000;

	/** The scenario file. */
	private static File scenarioFile;

	/** This version of Jamel. */
	final public static int version = 20150207;

	/**
	 * Downloads the latest version of Jamel.
	 * @return <code>true</code> if the download is successful, <code>false</code> otherwise.
	 */
	private static boolean download() { // TODO WORK IN PROGRESS
		boolean result;
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(KEY.DOWNLOAD_URI));
				result = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = false;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
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
	 * Returns a new circuit.
	 * @param jamelParameters a map of parameters for the new circuit.
	 * @return a new circuit.
	 */
	private static Circuit getNewCircuit(JamelParameters jamelParameters) {
		Circuit circuit = null;
		final String circuitName = jamelParameters.get(KEY.CIRCUIT_TYPE);
		if (circuitName!=null) {
			try {
				circuit = (Circuit) Class.forName(circuitName,false,ClassLoader.getSystemClassLoader()).getConstructor(JamelParameters.class).newInstance(jamelParameters);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (SecurityException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (InstantiationException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				showErrorDialog("Error while creating the circuit.<br>See log file for more details.");
			}
		}
		else {
			showErrorDialog("Circuit type not found.");			
		}
		return circuit;
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
	 * Returns the file scenario selected by the user.
	 * @return the file selected.
	 */
	private static File selectScenario() {
		final String path = prefs.get(KEY.SCENARIO_PATHNAME_PREF,KEY.SCENARIO_DEFAULT_PATHNAME);
		@SuppressWarnings("serial") final JFileChooser fc = new JFileChooser() {{
			this.setFileFilter(new FileNameExtensionFilter("Scenario files", "ini"));}
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
	private static boolean updateJamel() {
		final boolean result;
		if (isOutOfDate()) {
			final long now = System.currentTimeMillis();
			long previous = prefs.getLong(KEY.REMIND_ME_LATER,0);
			if (now>previous+remindMeLaterPeriod) {
				final Object[] options = {KEY.DOWNLOAD, KEY.REMIND_ME_LATER};
				final int n = JOptionPane.showOptionDialog(null, "A new version of Jamel is available.", "New version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n==0) {
					result = download();
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
	 * Returns the scenario file.
	 * @return the scenario file.
	 */
	public static File getScenarioFile() {
		return scenarioFile;
	}

	/**
	 * The main method for Jamel.
	 * @param args unused.
	 */
	public static void main(String[] args) {
		if (updateJamel()) {} else 
		{
			// Selects a file containing a scenario
			scenarioFile = selectScenario();
			if (scenarioFile!=null) {
				try {
					// Reads the file and parses parameters and events.
					final JamelParameters jamelParameters = new JamelParameters(FileParser.parseMap(scenarioFile));					
					jamelParameters.put(KEY.FILENAME, scenarioFile.getName());
					// Creates the circuit.
					Circuit circuit = getNewCircuit(jamelParameters);
					// Launches the simulation.
					if (circuit!=null) {
						circuit.run();					
					}
					else {
						showErrorDialog("The circuit is null.");
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Brings up a dialog that displays an error message.
	 * @param message the message to display.
	 */
	public static void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(null,"<html>"+message+"</html>","Error",JOptionPane.ERROR_MESSAGE);
	}

}

// ***
