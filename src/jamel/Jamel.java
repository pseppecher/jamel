/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel;

import jamel.util.data.GlobalDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * An abstract simulator.
 */
public abstract class Jamel {

	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** The output file. */
	protected static File outputFile;

	/**
	 * Returns an array of two strings, the first with the instruction, the second the parameters.
	 * @param string  a String that contains an instruction like "instruction(parameter)".
	 * @return an array of two strings.
	 */
	public static String[] parseInstruction(String string) {
		return string.split("\\)",2)[0].split("\\(",2);
	}

	/**
	 * Reads the specified file in resource and returns its contain as a String.
	 * @param filename  the name of the resource file.
	 * @return a String.
	 */
	public static String readFile(String filename) {
		BufferedReader br=new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/"+filename)));
		String line;
		String string="";
		try {
			while ((line=br.readLine())!=null){
				string+=line+"\n";
			}
			br.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * Returns the file scenario selected by the user.
	 * @return the file selected.
	 */
	public static File selectScenario() {
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

	/** The name of the scenario file. */
	protected String name;

	/** A flag that indicates whether or not the simulation is paused. */
	protected boolean pause=true;

	/**
	 * Returns the ID of the current simulation.
	 * @return the ID of the current simulation.
	 */
	//protected abstract int getSimulationId();

	/** A flag that indicates whether or not the simulation is running. */
	protected boolean run=false;

	/**
	 * Creates a new output file.
	 */
	protected void setNewOutputFile() {
		final String rc = System.getProperty("line.separator");
		final File exports = new File("exports");
		if (exports.exists()&&exports.isDirectory()) {
			outputFile = new File(new File("exports/"),name+"-"+(new Date()).getTime()+".csv");
		}
		else {
			outputFile = new File(name+"-"+(new Date()).getTime()+".csv");			
		}
		try {
			final FileWriter writer = new FileWriter(outputFile);
			writer.write("Export from "+name+rc);
			writer.write((new Date()).toString()+rc);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Receives an instruction (from the Circuit).
	 * @param key  the instruction.
	 * @param val  the value.
	 */
	abstract public void doEvent(String key, Object val);

	/**
	 * Exports the data in a csv file.
	 * @param data  the record of data.
	 * @param keys  a string that contains the keys of the values to export, separated by commas.
	 */
	public void export(GlobalDataset data, String keys) {
		final String[] keyArray=keys.split(",");
		export(data, keyArray);
	}

	/**
	 * Exports the data in a csv file.
	 * @param data  the record of data.
	 * @param keys  the keys of the values to export.
	 */
	public void export(GlobalDataset data, String[] keys) {
		try {
			if (outputFile==null)
				setNewOutputFile();
			final FileWriter writer = new FileWriter(outputFile,true);
			for (String key:keys) {
				String value=null;
				if (key.startsWith("%")) {
					try {
						value=data.getFieldValue(key.substring(1)).toString();
					} catch (NoSuchFieldException e) {
						value="No Such Field: "+key;
					}
				}
				else {
					value=key;
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

	/**
	 * Exports a list of strings in a csv file.
	 * @param exportData  the strings to export.
	 */
	protected void export(List<String> exportData) {
		if (exportData!=null&&exportData.size()!=0) {
			if (outputFile==null)
				setNewOutputFile();
			try {
				final FileWriter writer = new FileWriter(outputFile,true);
				for(String line:exportData) {
					writer.write(line+rc);
				}
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
	}

	/**
	 * Returns <code>true</code> if the simulation is paused, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	public boolean isPaused() {
		return pause;
	}

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
	 * Stops the simulation.
	 */
	public void stop() {
		this.run=false;
	}

	/**
	 * Receives notification of a systemic crisis.
	 */
	public abstract void systemicFailure();

}