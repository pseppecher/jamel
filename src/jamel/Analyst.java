/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
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
 */

package jamel;

import jamel.util.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;

import org.jfree.data.time.Month;

/**
 * A class for the sensitivity analyses.  
 */
public class Analyst implements AbstractSimulator {

	/**
	 * Creates and returns a new output file.
	 * @param fileName the name of the scenario file.
	 * @return the output file.
	 */
	private static File getNewOutputFile(String fileName) {
		final String rc = System.getProperty("line.separator");
		final File outputFile = new File(new File("exports/"),fileName+"-"+(new Date()).getTime()+".csv");
		try {
			final FileWriter writer = new FileWriter(outputFile);
			writer.write("Sensitivity analysis: "+fileName+rc);
			writer.write((new Date()).toString()+rc);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return outputFile;
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
	 * Returns an array of double converted from an array of strings. 
	 * @param input an array of strings.
	 * @return an array of doubles.
	 */
	private static double[] toDouble(String[] input) {
		final double[] output = new double[input.length];
		for(int i=0;i<input.length;i++) {
			output[i]=Double.parseDouble(input[i]);
		}
		return output;
	}

	/**
	 * The main method.
	 * @param args  the arguments.
	 */
	public static void main(String[] args) {
		final File file = selectScenario();
		if (file!=null) {
			LinkedList<String> parameters = new LinkedList<String>();
			try {
				Scanner scanner=new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					parameters.add(line);
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("File not found.");
			}
			new Analyst(file.getName(),parameters);
		}
	}

	/** The basic scenario: a list of strings representing an instruction of the scenario. */
	final private LinkedList<String> basicScenario;

	/** The max value of the random seed. */
	private int randomSeedMax;

	/** The min value of the random seed. */
	private int randomSeedMin;

	/** A flag that indicates whether or not the simulation is running. */
	private boolean run=false;

	/** The total number of simulations to run. */
	private Integer sim=null;

	/** The variables of the analysis. */
	final private HashMap<String,double[]> variables;
	
	/**
	 * Creates a new simulator.
	 * @param name  the name of the simulation.
	 * @param aParameters  the parameters of the simulation.
	 */
	public Analyst(String name, LinkedList<String> aParameters) {
		this.basicScenario = new LinkedList<String>(); // Creates a new empty scenario.
		this.variables = new HashMap<String,double[]>(); // Creates a new empty map that contains variables.
		initAnalysis(aParameters); // Extract analysis parameters and fills the scenario with the instructions. 
		final long start = (new Date()).getTime();
		final File outputFile = getNewOutputFile(name);
		
		final int max = sim*(1+randomSeedMax-randomSeedMin);
		int count = 0;
		final ProgressMonitor progressMonitor = new ProgressMonitor(null,
				"Running "+name,
				"", count,max);
		progressMonitor.setMillisToDecideToPopup(0);

		for(int i=0; i<sim; i++) {
			for(int randomSeed=this.randomSeedMin; randomSeed<=randomSeedMax; randomSeed++) {
				progressMonitor.setProgress(count);
				progressMonitor.setNote("Simulation "+count+" on "+max);
				newSimulation(name,i,randomSeed,outputFile);
				count++;
			}			
		}
		try {
			final FileWriter writer = new FileWriter(outputFile,true);
			writer.write("Duration: "+((new Date()).getTime()-start)/1000.+" s.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		progressMonitor.close();
	}

	/**
	 * Returns a scenario.
	 * @param i  the number of the scenario.
	 * @param randomSeed  the random seed.
	 * @return  a list of strings.
	 */
	private LinkedList<String> getParameters(int i, int randomSeed) {
		final LinkedList<String> scenario = new LinkedList<String> () ;
		scenario.addFirst("Circuit.2000-01.set(randomSeed="+randomSeed+")");
		for (String string: this.basicScenario) {
			String newString = string;
			for(Map.Entry<String, double[]> entry : this.variables.entrySet()) {
				String key = entry.getKey();
				double value = entry.getValue()[i];
				if ((int)value==value)
					newString = newString.replace(key, ""+(int)value);
				else
					newString = newString.replace(key, ""+value);
			}		
			//System.out.println(newString);
			scenario.add(newString);
		}
		return scenario;
	}

	/**
	 * Etablit le scénario de base des simulations.
	 * Les données variables sont extraites et enregistrées dans les champs correspondants.
	 * @param aParameters  une liste de chaînes contenant des instructions.
	 */
	private void initAnalysis(LinkedList<String> aParameters) {
		for (String line: aParameters) {
			final String[] temp1 = line.split("\\(", 2);
			if (temp1.length==2) {
				if (temp1[0].trim().equals("Analyst.set")) {
					final String[] temp2 = temp1[1].trim().split("\\)", 2);
					final String[] temp3 = temp2[0].trim().split(":", 2);
					final String key = temp3[0].trim(); 
					if (key.equals("randomSeed")) {
						final String[] temp4 = temp3[1].trim().split("-", 2);
						this.randomSeedMin= Integer.parseInt(temp4[0]);
						this.randomSeedMax= Integer.parseInt(temp4[1]);						
					}
					else {
						final String[] temp4 = temp3[1].trim().split(",");
						if (sim==null) sim=temp4.length;
						else if (sim!=temp4.length)
							throw new RuntimeException("Not valid number of values for the variable "+key+". Found: "+temp4.length+" expected: "+sim);
						final double[] data = toDouble(temp4);
						this.variables.put(key, data);
					}
				}
				else {
					this.basicScenario.add(line);
				}
			}		
		}
		if (sim==null) sim=1;
	}

	/**
	 * Runs a new simulation.
	 * @param name  the name of the scenario.
	 * @param i  the number of the simulation.
	 * @param randomSeed  the randomSeed.
	 * @param outputFile  the output file.
	 */
	private void newSimulation(String name,int i,int randomSeed, File outputFile) {
		JamelObject.setTimer(new Timer());
		//JamelObject.setRandom(new Random());
		JamelObject.setScenarioFileName(name+" "+i);
		final LinkedList<String> scenario = getParameters(i,randomSeed);
		final Circuit circuit = new Circuit(this, scenario);
		circuit.setOutputFile(outputFile);
		this.run=true;
		while (this.run) {
			circuit.doPeriod();
		}
		/*String[] td = {""+i,"randomSeed" ,
				"period" ,
				"date" ,
				"Inflation - Annual" ,
				"Unemployment" ,
				"Profit share" ,
				"Relative price" ,
				"Capacity Utilization - Sector 1" ,
				"Capacity Utilization - Sector 2" ,
				"Number of firms - Sector 1" ,
				"Number of firms - Sector 2" ,
				"Profit by firm - sector 1" ,
				"Profit by firm - sector 2" ,
				"Markup - Sector 1" ,
				"Markup - Sector 2"};
		circuit.write(td);*/
	}

	/**
	 * Stops the simulation before the normal ending.
	 */
	@Override
	public void failure() {
		run = false;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void marker(String label, Month month) {
	}

	/**
	 * Stops the simulation.
	 */
	@Override
	public void pause(boolean b) {
		run = false;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void println(String message) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void setChart(int tabIndex, int panelIndex, String chartPanelName) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void setVisiblePanel(int index) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void zoom(int aZoom) {
	}	

}
