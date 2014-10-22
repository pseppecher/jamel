package jamel;

import jamel.basic.util.JamelParameters;
import jamel.util.Circuit;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The main class for Jamel.
 */
public class Simulator {

	/** The key for the parameter that contains the fully qualified name of the desired class of Circuit. */
	private static final String KEY_CIRCUIT_TYPE = "Circuit.type";

	/** The key for the parameter that contains the name of the scenario file. */
	private static final String KEY_FILENAME = "Circuit.fileName";

	/** The scenario file. */
	private static File scenarioFile;

	/**
	 * Returns a new circuit.
	 * @param jamelParameters a map of parameters for the new circuit.
	 * @return a new circuit.
	 */
	private static Circuit getNewCircuit(JamelParameters jamelParameters) {
		Circuit circuit = null;
		final String circuitName = jamelParameters.get(KEY_CIRCUIT_TYPE);
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
	 * Reads the file and returns its content as a list of strings. 
	 * @param file  the file to read.
	 * @return a list of strings.
	 * @throws FileNotFoundException if the file is not found.
	 */
	private static ArrayList<String> read(File file) throws FileNotFoundException {
		final ArrayList<String> lines = new ArrayList<String>();
		final Scanner scanner=new Scanner(file);
		while (scanner.hasNextLine()) {
			lines.add(scanner.nextLine());
		}
		scanner.close();
		return lines;
	}

	/**
	 * Returns the file scenario selected by the user.
	 * @return the file selected.
	 */
	private static File selectScenario() {
		@SuppressWarnings("serial") final JFileChooser fc = new JFileChooser() {{
			this.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));}
		};
		final File dir = new File("scenarios/");
		final File file;
		fc.setDialogTitle("Open Scenario");
		fc.setCurrentDirectory(dir);
		final int returnVal = fc.showOpenDialog(null);
		if (returnVal==JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
		else {
			file=null;
		}
		return file;
	}

	/**
	 * Brings up a dialog that displays an error message.
	 * @param message the message to display.
	 */
	public static void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(null,"<html>"+message+"</html>","Error",JOptionPane.ERROR_MESSAGE);
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
		// Selects a file containing a scenario
		scenarioFile = selectScenario();
		if (scenarioFile!=null) {
			try {
				// Reads the file and parses parameters and events.
				final ArrayList<String> scenario = read(scenarioFile);
				final JamelParameters jamelParameters = new JamelParameters(scenario);
				jamelParameters.put(KEY_FILENAME, scenarioFile.getName());
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

// ***
