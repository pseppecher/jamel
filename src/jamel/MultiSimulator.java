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

// TODO tester
// TODO revoir la question de l'affichage du scénario, et du ralentissement de l'affichage

package jamel;

import jamel.util.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * A simulator for sensitivity analysis.
 */
public class MultiSimulator extends Simulator {

	@SuppressWarnings("javadoc")
	private static final String CMD_ANALYSIS_END = "*ANALYSIS END*";

	@SuppressWarnings("javadoc")
	private static final String CMD_ANALYSIS_START = "*ANALYSIS START*";

	@SuppressWarnings("javadoc")
	private static final String CMD_GET_VARIABLES = "getVariables";

	@SuppressWarnings("javadoc")
	private static final String CMD_REPLACE = "replace";

	@SuppressWarnings("javadoc")
	private static final String CMD_SCENARIO_FILE = "scenario";

	/**
	 * Returns an String without space and comments.
	 * @param string the String to clean up.
	 * @return a String.
	 */
	private static String getInstruction(String string) {
		return string.split("//")[0].trim();
	}

	/**
	 * The main method.
	 * @param args  the arguments (not used).
	 */
	public static void main(String[] args) {
		new MultiSimulator();
	}

	/**
	 * Prints a line.
	 * @param string  the line to be printed.
	 */
	@Override
	protected void printLine(String string) {
		println(string);
	}

	/**
	 * Creates a new MultiSimulator.
	 */
	@Override
	protected void run() {
		final File file = selectScenario();
		if (file!=null) {

			this.name=file.getName();

			ArrayList<String> text;
			try {
				text = new ArrayList<String>(read(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new RuntimeException("File not found: "+file.getAbsolutePath());
			}
			int count=0;

			// On cherche le début de la définition de l'analyse de sensibilité. 

			while (true) {
				if (count==text.size()) {
					throw new RuntimeException("Error in analysis file: missing command \""+CMD_ANALYSIS_START+"\"." );
				}
				final String instruction = getInstruction(text.get(count));
				if (instruction.equals(CMD_ANALYSIS_START)) {
					count++;
					break;
				}
				count++;
			}

			// Début de la définition de l'analyse de sensibilité.

			List<String> rawScenario = null;
			Integer numSim = null; // The number of simulations.
			final HashMap<String,String[]> variables = new HashMap<String,String[]>();
			while (true) {
				if (count==text.size()) {
					throw new RuntimeException("Error in analysis file: missing command \""+CMD_ANALYSIS_END+"\"." );
				}
				final String instruction = getInstruction(text.get(count));
				if (instruction.equals(CMD_ANALYSIS_END)) {
					break;
				}
				if (!instruction.isEmpty()) {
					String[] command = parseInstruction(instruction);
					if (command[0].equals(CMD_SCENARIO_FILE)) {
						final File file2 = new File(file.getParent()+"/"+command[1]);
						try {
							rawScenario = read(file2);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							throw new RuntimeException("Error in analysis file, line "+count+": File not found: "+file2.getAbsolutePath());
						}
					}
					else if (command[0].equals(CMD_REPLACE)) {
						if (rawScenario!=null) {
							final List<String> copy = new LinkedList<String>(); 
							for(String line:rawScenario) {
								final String[] sequence=command[1].split(",");
								copy.add(line.replace(sequence[0], sequence[1]));
							}
							rawScenario=copy;
						}
						else {
							throw new RuntimeException("Error in analysis file, line "+count+": the scenario must be defined before the command \""+CMD_REPLACE+"\"." );					
						}
					}
					else if (command[0].equals(CMD_GET_VARIABLES)) {
						final File vFile = new File(file.getParent()+"/"+command[1]);
						try {
							final LinkedList<String> rawVariables = new LinkedList<String>(read(vFile));
							int count2=1;
							for (String line:rawVariables) {
								if (!line.isEmpty()) {
									final String[] variable = line.split(",",2);
									final String key = variable[0];
									final String[] values = variable[1].split(",");
									if (!variables.containsKey(key)) {
										variables.put(key,values);
									}
									else {
										throw new RuntimeException("Error in variables file, line "+count2+": duplicated key \""+key+"\"." );
									}
									if (numSim==null) {
										numSim=values.length;
									}
									else {
										if (values.length!=numSim) {
											throw new RuntimeException("Error in variables file, line "+count2+": bad number of values for the key \""+key+"\" (expected:"+numSim+", found:"+values.length+")." );							
										}
									}
									count2++;
								} else {
									break;
								}
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							throw new RuntimeException("Error in analysis file, line "+count+": File not found: "+vFile.getAbsolutePath());
						}
					}
					else {
						throw new RuntimeException("Error in analysis file, line "+count+": unknown command \""+instruction+"\"." );					
					}
				}
				count++;
			}

			if (rawScenario==null) {
				throw new RuntimeException("Error in analysis file: missing command \""+CMD_SCENARIO_FILE+"\"." );			
			}

			// Fin de la définition de l'analyse de sesibilité.
			// Début de l'analyse de sensibilité.

			for (int i =0; i<numSim; i++) {
				// On commence par construire le scénario en remplaçant chaque key par la valeur correspondante.
				final ArrayList<String> scenario =new ArrayList<String>();
				for (String line: rawScenario) {
					for (Entry<String,String[]> entry: variables.entrySet() ) {
						final String key = entry.getKey();
						final String value = entry.getValue()[i];
						line=line.replace(key, value);
					}
					println(line);
					scenario.add(line);
				}
				JamelObject.setTimer(new Timer());
				final Circuit circuit = getNewCircuit(scenario);
				this.pause(false);
				this.run=true;
				while (this.run) {
					circuit.doPeriod();
				}
			}

		}
	}

	/* (non-Javadoc)
	 * @see jamel.Simulator#doEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void doEvent(String key, Object val) {
		if (key.startsWith("window")) {
			System.out.println("Event ignored: "+key);
		} else {
			super.doEvent(key, val);
		}
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#pause(boolean)
	 */
	@Override
	public void pause(boolean b) {
		if(b){
			// Does nothing.
		} else {
			pause = false;			
		}
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#println(java.lang.String)
	 */
	@Override
	public void println(String message) {
		System.out.println(message);
	}

}























