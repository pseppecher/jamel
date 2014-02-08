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
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.SwingUtilities;

/**
 * A simulator with a GUI for dynamic simulations.  
 */
public class Simulator extends Jamel {
	
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
		new Simulator();
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
	 * Runs the simulation.
	 */
	protected void run() {		
		final File file = selectScenario();
		if (file!=null) {
			this.name=file.getName();
			JamelObject.setTimer(new Timer());
			this.window = new JamelWindow(name);
			final Circuit circuit;
			try {
				final CircuitFactory circuitFactory = new CircuitFactory(this);
				circuit = circuitFactory.getNewCircuit(read(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new RuntimeException("File not found: "+file.getAbsolutePath());
			}
			this.window.doEvent(JamelWindow.COMMAND_SELECT_PANEL,0);
			this.pause(false);
			this.run=true;
			final long start = (new Date()).getTime();
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
			this.export(circuit.getExportData());
			System.out.println("Duration: "+((new Date()).getTime()-start)/1000.+" s." ) ;
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
