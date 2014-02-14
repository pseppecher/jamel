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

import jamel.gui.JamelWindow;

import java.security.InvalidKeyException;
import java.util.ArrayList;

/**
 * A factory for <Circuit> creation.
 */
public class CircuitFactory {

	@SuppressWarnings("javadoc")
	private static final String CMD_PREAMBLE_BEGIN = "*PREAMBLE*";

	@SuppressWarnings("javadoc")
	private static final String CMD_SET_CIRCUIT = "Circuit";

	@SuppressWarnings("javadoc")
	private static final String CMD_SET_WINDOW_RANGE = "WindowRange";

	@SuppressWarnings("javadoc")
	private static final String CMD_SIMULATION_BEGIN = "*START*";

	@SuppressWarnings("javadoc")
	private static final String CMD_SIMULATION_END = "*END*";

	/** The laps after each print of a line in the console panel. */
	private static final int sleep=15;

	@SuppressWarnings("javadoc")
	protected String FONT_END = "</FONT>";

	@SuppressWarnings("javadoc")
	protected String FONT_BLUE = "<FONT COLOR=\"#013ADF\">";

	@SuppressWarnings("javadoc")
	protected String FONT_GREEN = "<FONT COLOR=\"#21610B\">";

	/** The parent simulator. */
	protected final Simulator simulator;

	/**
	 * Creates a new factory.
	 * @param simulator  the parent simulator.
	 */
	public CircuitFactory(Simulator simulator) {
		this.simulator=simulator;
	}

	/**
	 * Prints a line of the scenario in the console panel.
	 * @param string  the line to be printed.
	 */
	protected void printLine(String string) {
		this.simulator.println(string);
		try {
			Thread.sleep(sleep); // Gives the time to see the scenario in the console panel.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Creates and returns a new <code>Circuit</code>.
	 * @param instructions  a list of strings, each string containing an instruction.
	 * @return the new <code>Circuit</code>.
	 */
	public Circuit getNewCircuit(ArrayList<String> instructions) {
		int count=-1;
		Circuit circuit=null;
		// Before the preamble
		
		while (true) {
			count++;
			if (count==instructions.size()) {
				throw new RuntimeException("Error in scenario file: \""+CMD_PREAMBLE_BEGIN+"\" command not found." );
			}
			String line=instructions.get(count);
			if (line.equals(CMD_PREAMBLE_BEGIN)) {
				printLine(FONT_BLUE+CMD_PREAMBLE_BEGIN+FONT_END);
				break;
			}
			printLine(FONT_GREEN+line+FONT_END);			
		}
		
		// In the preamble
		
		while (true) {
			count++;
			if (count==instructions.size()) {
				throw new RuntimeException("Error in scenario file: \""+CMD_SIMULATION_BEGIN+"\" command not found." );
			}
			String line=instructions.get(count);
			if (line.equals(CMD_SIMULATION_BEGIN)) {
				printLine(FONT_BLUE+CMD_SIMULATION_BEGIN+FONT_END);
				break;
			}
			String[] splitLine = line.split("//", 2);
			final String instruction = splitLine[0].trim(); // removes whitespace.
			if (!instruction.isEmpty()) {
				final String[] words = instruction.split("=", 2);
				if (words.length!=2) {
					throw new RuntimeException("Error in scenario file, line "+(count+1)+": syntax error in \""+instruction+"\"." );
				}
				final String key = words[0].trim();
				final String val = words[1].trim();
				if (key.equals(CMD_SET_CIRCUIT)) {	// creates the new circuit;
					try {
						// val must contain the fully qualified name of the desired class of Circuit.
						circuit = (Circuit) Class.forName(val,false,ClassLoader.getSystemClassLoader()).getConstructor(Jamel.class).newInstance(simulator);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+(count+1)+": while creating the new Circuit \""+val+"\"." );
					}
				} 
				else {
					if (circuit==null) {
						throw new RuntimeException("Error in scenario file, line "+(count+1)+": missing \""+CMD_SET_CIRCUIT+"\" command." );
					}
					if (key.equals(CMD_SET_WINDOW_RANGE)) {
						this.simulator.doEvent(JamelWindow.COMMAND_ZOOM,Integer.parseInt(val));
					} else {
						try {
							circuit.init(key,val);
						} catch (InvalidKeyException e) {
							e.printStackTrace();
							throw new RuntimeException("Error in scenario file, line "+(count+1)+": unexpected \""+key+"\" command." );
						}
					}
				}
			}
			if (splitLine.length==2) {
				printLine(splitLine[0]+FONT_GREEN+" // "+splitLine[1]+FONT_END);			
			}
			else {
				printLine(splitLine[0]);
			}
		}
		
		if (circuit==null) {
			throw new RuntimeException("Error in scenario file, line "+(count+1)+": missing \""+CMD_SET_CIRCUIT+"\" command." );
		}
		
		// In the simulation
		
		while (true) {
			count++;
			if (count==instructions.size()) {
				throw new RuntimeException("Error in scenario file: \""+CMD_SIMULATION_END+"\" command not found." );
			}
			String line=instructions.get(count);
			if (line.equals(CMD_SIMULATION_END)) {
				printLine(FONT_BLUE+CMD_SIMULATION_END+FONT_END);
				break;
			}

			String[] splitLine = line.split("//", 2);
			final String instruction = splitLine[0].trim(); // removes whitespace.
			if (!instruction.isEmpty()) {
				try {
					circuit.addEvent(instruction);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("Error in scenario file, line "+count+": illegal syntax in \""+instruction+"\" command." );
				}				
			}
			if (splitLine.length==2) {
				printLine(splitLine[0]+FONT_GREEN+" // "+splitLine[1]+FONT_END);			
			}
			else {
				printLine(splitLine[0]);
			}			
		}
		
		while (true) {
			count++;
			if (count==instructions.size()) {
				break;
			}
			String line=instructions.get(count);
			printLine(FONT_GREEN+line+FONT_END);			
		}
		return circuit;
	}

}
