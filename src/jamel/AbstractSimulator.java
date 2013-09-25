package jamel;

import jamel.util.data.AbstractDataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jfree.data.time.Month;

/**
 *
 */
public abstract class AbstractSimulator {
	
	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** The output file. */
	protected static File outputFile;

	/**
	 * Returns the file scenario selected by the user.
	 * @return the file selected.
	 */
	static File selectScenario() {
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
	 * Exports a string in a csv file.
	 * @param string  the string to export.
	 */
	public void export(String string) {
		if (outputFile==null)
			setNewOutputFile();
		try {
			final FileWriter writer = new FileWriter(outputFile,true);
			writer.write(string);
			writer.write(rc);
			writer.close();
		} catch (IOException e) {
			final String message = "Error while writing data in the output file.";
			JOptionPane.showMessageDialog(null,
					message,
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new RuntimeException(message);
		}		
	}

	/** The name of the scenario file. */
	protected String name;

	/** A flag that indicates whether or not the simulation is running. */
	protected boolean run=false;

	/**
	 * Exports the data in a csv file.
	 * @param data  the record of data.
	 * @param keys  a string that contains the keys of the values to export, separated by commas.
	 */
	void export(AbstractDataset data, String keys) {
		final String[] keyArray=keys.split(",");
		try {
			if (outputFile==null)
				setNewOutputFile();
			final FileWriter writer = new FileWriter(outputFile,true);
			for (String key:keyArray) {
				String value = key;
				if (key.startsWith("%")) {
					if (key.equals("%simulation")) {
						value=""+this.getSimulationId();
					}
					else try {
						value=data.getFieldValue(key.substring(1)).toString();
					} catch (NoSuchFieldException e) {
						value="No Such Field: "+key;
					}
				}
				writer.write(value+",");
			}
			writer.write(rc);
			writer.close();
		} catch (IOException e) {
			final String message = "Error while writing data in the output file.";
			JOptionPane.showMessageDialog(null,
					message,
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new RuntimeException(message);
		}		
	}

	/**
	 * Returns the ID of the current simulation.
	 * @return the ID of the current simulation.
	 */
	abstract int getSimulationId();

	/**
	 * Creates a new output file.
	 */
	void setNewOutputFile() {
		final String rc = System.getProperty("line.separator");
		outputFile = new File(new File("exports/"),name+"-"+(new Date()).getTime()+".csv");
		try {
			final FileWriter writer = new FileWriter(outputFile);
			writer.write("Sensitivity analysis: "+name+rc);
			writer.write((new Date()).toString()+rc);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Systemic crisis.
	 */
	public abstract void failure();

	/**
	 * Adds a marker to the time charts.
	 * @param label the label.
	 * @param month the month.
	 */
	public abstract void marker(String label, Month month);

	/**
	 * Sets the sate of the simulation (paused or running).
	 * @param b a flag that indicates whether or not the simulation must be paused.
	 */
	public abstract void pause(boolean b);

	/**
	 * Prints a String in the console panel.
	 * @param message the String to print.
	 */
	public abstract void println(String message);

	/**
	 * Sets the chart in the specified panel.
	 * @param tabIndex the index of the tab to customize.
	 * @param panelIndex the id of the ChartPanel to customize.
	 * @param chartPanelName the name of the ChartPanel to set.
	 * @throws ClassNotFoundException... 
	 * @throws NoSuchMethodException...
	 * @throws InvocationTargetException... 
	 * @throws IllegalAccessException...
	 * @throws InstantiationException...
	 * @throws SecurityException...
	 * @throws IllegalArgumentException... 
	 */
	public abstract void setChart(int tabIndex, int panelIndex,
			String chartPanelName) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException;

	/**
	 * Sets the selected index for the tabbedpane of the application window.
	 * @param index the index to be selected.
	 */
	public abstract void setVisiblePanel(int index);

	/**
	 * Zooms in the time charts.
	 * @param aZoom the zoom factor.
	 */
	public abstract void zoom(int aZoom);

}