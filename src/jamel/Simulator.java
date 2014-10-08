package jamel;

import jamel.basic.util.JamelParameters;
import jamel.util.Circuit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The main class for Jamel.
 */
public class Simulator {

	/** The key for the parameter that contains the fully qualified name of the desired class of Circuit. */
	private static final String KEY_CIRCUIT_TYPE = "Circuit.type";

	/** The key for the parameter that contains the name of the scenario file. */
	private static final String KEY_FILENAME = "Circuit.fileName";

	/**
	 * Returns a new circuit.
	 * @param jamelParameters a map of parameters for the new circuit.
	 * @return a new circuit.
	 */
	private static Circuit getNewCircuit(JamelParameters jamelParameters) {
		Circuit circuit = null;
		final String circuitName = jamelParameters.get(KEY_CIRCUIT_TYPE);
		try {
			circuit = (Circuit) Class.forName(circuitName,false,ClassLoader.getSystemClassLoader()).getConstructor(JamelParameters.class).newInstance(jamelParameters);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"<html>"
							+ "Error while creating the circuit.<br>"
							+ "See log file for more details."
							+ "</html>",
							"Error",
							JOptionPane.ERROR_MESSAGE);
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
		final JFileChooser fc = new JFileChooser();
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
	 * The main method for Jamel.
	 * @param args unused.
	 */
	public static void main(String[] args) {
		// Selects a file containing a scenario
		final File file = selectScenario();
		if (file!=null) {
			try {
				// Reads the file and parses parameters and events.
				final ArrayList<String> scenario = read(file);
				final JamelParameters jamelParameters = new JamelParameters(scenario);
				jamelParameters.put(KEY_FILENAME, file.getName());
				// Creates the circuit.
				Circuit circuit = getNewCircuit(jamelParameters);
				// Launches the simulation.
				if (circuit!=null) {
					circuit.run();					
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	};

}

// ***
