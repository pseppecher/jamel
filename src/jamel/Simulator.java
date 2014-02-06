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
import java.util.ArrayList;
import java.util.Date;
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

	/** The laps after each print of a line in the console panel. */
	private static final int sleep=15;

	/**
	 * Reads the file and returns its content as a list of strings. 
	 * @param file  the file to read.
	 * @return a list of strings.
	 * @throws FileNotFoundException if the file is not found.
	 */
	protected static ArrayList<String> read(File file) throws FileNotFoundException {
		final ArrayList<String> parameters = new ArrayList<String>();
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
	 * Returns a new <code>Circuit</code>.
	 * @param instructions  a list of (raw) strings constituting the scenario of the simulation.
	 * @return a new <code>Circuit</code>.
	 */
	protected Circuit getNewCircuit(ArrayList<String> instructions) {
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
				printLine("<FONT COLOR=\"#013ADF\">"+CMD_PREAMBLE_BEGIN+"</FONT>");
				break;
			}
			printLine("<FONT COLOR=\"#21610B\">"+line+"</FONT>");			
		}
		
		// In the preamble
		
		while (true) {
			count++;
			if (count==instructions.size()) {
				throw new RuntimeException("Error in scenario file: \""+CMD_SIMULATION_BEGIN+"\" command not found." );
			}
			String line=instructions.get(count);
			if (line.equals(CMD_SIMULATION_BEGIN)) {
				printLine("<FONT COLOR=\"#013ADF\">"+CMD_SIMULATION_BEGIN+"</FONT>");
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
						circuit = (Circuit) Class.forName(val,false,ClassLoader.getSystemClassLoader()).getConstructor(Jamel.class).newInstance(this);
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
						this.doEvent(JamelWindow.COMMAND_ZOOM,Integer.parseInt(val));
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
				printLine(splitLine[0]+"<FONT COLOR=\"#21610B\">//"+splitLine[1]+"</FONT>");			
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
				printLine("<FONT COLOR=\"#013ADF\">"+CMD_SIMULATION_END+"</FONT>");
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
				printLine(splitLine[0]+"<FONT COLOR=\"#21610B\">//"+splitLine[1]+"</FONT>");			
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
			printLine("<FONT COLOR=\"#21610B\">"+line+"</FONT>");			
		}
		return circuit;
	}

	/**
	 * Prints a line of the scenario in the console panel.
	 * @param string  the line to be printed.
	 */
	protected void printLine(String string) {
		println(string);
		try {
			Thread.sleep(sleep); // Gives the time to see the scenario in the console panel.
		} catch (InterruptedException e) {
			e.printStackTrace();
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
