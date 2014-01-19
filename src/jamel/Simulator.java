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
import jamel.util.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

/**
 * The main class of Jamel.  
 */
public class Simulator extends Jamel {

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

	/**
	 * Reads the file and returns its content as a list of strings. 
	 * @param file  the file to read.
	 * @return a list of strings.
	 * @throws FileNotFoundException if the file is not found.
	 */
	protected static List<String> read(File file) throws FileNotFoundException {
		final LinkedList<String> parameters = new LinkedList<String>();
			Scanner scanner=new Scanner(file);
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final String[] truc1 = line.split("\\(");
				if (truc1[0].equals("include")) {
					final String[] truc2 = truc1[1].split("\\)");
					final String fileName=file.getParent()+"/"+truc2[0];
					final File file2 = new File(fileName);
					parameters.addAll(read(file2));
				}
				else {
					parameters.add(line);
				}
			}
			scanner.close();
		return parameters;
	}

	/**
	 * The main method.
	 * @param args  the arguments (not used).
	 */
	public static void main(String[] args) {
		final long start = (new Date()).getTime();
		new Simulator();
		System.out.println("Duration: "+((new Date()).getTime()-start)/1000.+" s." ) ;
	}

	/** The application window. */
	protected JamelWindow window=null;

	/**
	 * Creates a new simulator.
	 */
	public Simulator() {
		run();
	}

	/**
	 * Prints a line of the scenario in the console. 
	 * @param splitLine  an array of 2 Strings (The first contains an instruction, the second a comment)
	 * @param color  a String that defines the html color for the instruction.
	 */
	protected void printScenario(String[] splitLine,String color) {
		String string = "<FONT COLOR=\""+color+"\">"+splitLine[0]+"</FONT>";
		if (splitLine.length==2){
			string += "<FONT COLOR=\"#21610B\">//"+splitLine[1]+"</FONT>";
		}
		println(string);
		try {
			Thread.sleep(15); // Gives the time to see the scenario in the console panel.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a new <code>Circuit</code>.<p>
	 * TODO: This method could be refactored.
	 * @param instructions  a list of (raw) strings constituting the scenario of the simulation.
	 * @return a new <code>Circuit</code>.
	 */
	protected Circuit getNewCircuit(List<String> instructions) {
		int count=0;
		boolean inPreamble=false;
		boolean inSimulation=false;
		boolean terminated=false;
		Circuit circuit=null;
		for (String line: instructions) {
			count++;
			String color="#000000";
			String[] splitLine = line.split("//", 2);
			final String instruction = splitLine[0].trim(); // removes whitespace.
			if (instruction.isEmpty()) {
				// does nothing.
			}
			else if (instruction.equals(CMD_PREAMBLE_BEGIN)){ // detecting the start of the preamble. 
				if (inPreamble||inSimulation) {
					throw new RuntimeException("Error in scenario file, line "+count+": unexpected \""+CMD_PREAMBLE_BEGIN+"\" command." );
				}
				inPreamble=true;
				color="#013ADF";
			} 
			else if (instruction.equals(CMD_SIMULATION_BEGIN)) { // detecting the end of the preamble and the start of the simulation.
				if (!inPreamble) {
					throw new RuntimeException("Error in scenario file, line "+count+": unexpected \""+CMD_SIMULATION_BEGIN+"\" command." );
				}
				if (circuit==null) {
					throw new RuntimeException("Error in scenario file, line "+count+": missing \""+CMD_SET_CIRCUIT+"\" command." );
				}
				inSimulation=true;
				inPreamble=false;
				color="#013ADF";
			}
			else if (instruction.equals(CMD_SIMULATION_END)) { // detecting the end of the simulation.
				if (!inSimulation) {
					throw new RuntimeException("Error in scenario file, line "+count+": unexpected \""+CMD_SIMULATION_END+"\" command." );
				}
				inSimulation=false;
				terminated=true;
				color="#013ADF";
			}
			else if (inPreamble) {
				final String[] words = instruction.split("=", 2);
				final String key = words[0].trim();  
				if (words.length!=2) {
					throw new RuntimeException("Error in scenario file, line "+count+": syntax error in \""+instruction+"\"." );
				}
				final String val = words[1].trim();
				if (key.equals(CMD_SET_CIRCUIT)) {	// creates the new circuit;
					if (!inPreamble) {
						throw new RuntimeException("Error in scenario file, line "+count+": unexpected \""+CMD_SET_CIRCUIT+"\" command." );
					}
					try {
						// val must contain the fully qualified name of the desired class of Circuit.
						circuit = (Circuit) Class.forName(val,false,ClassLoader.getSystemClassLoader()).getConstructor(Jamel.class).newInstance(this);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (SecurityException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (InstantiationException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						throw new RuntimeException("Error in scenario file, line "+count+": while creating the new Circuit \""+val+"\"." );
					}
				} else {
					if (circuit==null) {
						throw new RuntimeException("Error in scenario file, line "+count+": missing \""+CMD_SET_CIRCUIT+"\" command." );
					}
					if (key.equals(CMD_SET_WINDOW_RANGE)) {
						this.doEvent(JamelWindow.COMMAND_ZOOM,Integer.parseInt(val));
					} else {
						try {
							circuit.init(key,val);
						} catch (InvalidKeyException e) {
							e.printStackTrace();
							throw new RuntimeException("Error in scenario file, line "+count+": unexpected \""+key+"\" command." );
						}
					}
				}
			}
			else if (inSimulation) {
				try {
					circuit.addEvent(instruction);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("Error in scenario file, line "+count+": illegal syntax in \""+instruction+"\" command." );
				}
			}
			else {
				if (inPreamble|inSimulation)	{
					throw new RuntimeException("Error in scenario file, line "+count+": unknown error." );
				}
				// Does nothing (we are before the preamble).
				color="#21610B";
			}
			printScenario(splitLine,color);
		}
		if(!terminated) {
			throw new RuntimeException("Error in scenario file, line "+count+": missing command \""+CMD_SIMULATION_END+"\".");
		}
		return circuit;
	}

	/**
	 * Prints the list of strings in the console panel.
	 * @param strings a list of strings.
	 */
	protected void println(LinkedList<String> strings) {
		for(String string: strings) {
			println(string);
		}
	}

	/**
	 * Runs the simulation.
	 */
	protected void run() {		
		final File file = selectScenario();
		if (file!=null) {
			this.name=file.getName();
			JamelObject.setTimer(new Timer());
			this.window = new JamelWindow(name);
			Circuit circuit;
			try {
				circuit = getNewCircuit(read(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new RuntimeException("File not found: "+file.getAbsolutePath());
			}
			this.window.doEvent(JamelWindow.COMMAND_SELECT_PANEL,0);
			this.pause(false);
			this.run=true;
			while (this.run) {
				if (!this.pause){
					circuit.doPeriod();
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run(){
								Simulator.this.window.update();
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
				}
			}		
		}
	}

	@Override
	public void doEvent(String key, Object val) {
		if (key.startsWith("window")) {
			this.window.doEvent(key,val);
		} else {
			throw new IllegalArgumentException("Unknown command: "+key);
		}
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#pause(boolean)
	 */
	@Override
	public void pause(boolean b) {
		pause = b;
		this.window.updatePauseButton();
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#println(java.lang.String)
	 */
	@Override
	public void println(String message) {
		window.println(message);
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#failure()
	 */
	@Override
	public void systemicFailure() {
		pause(true);
		window.failure();
	}

}
