/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher and contributors.
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

import jamel.gui.JamelWindow;
import jamel.util.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.jfree.data.time.Month;

/**
 * The main class of Jamel.  
 */
public class Simulator extends AbstractSimulator {

	/**
	 * Read the file and returns its content as a list of strings. 
	 * @param file  the file to read.
	 * @return a list of strings.
	 */
	private static LinkedList<String> getParametersFrom(File file) {
		final LinkedList<String> parameters = new LinkedList<String>();
		try {
			Scanner scanner=new Scanner(file);
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				final String[] truc1 = line.split("\\(");
				if (truc1[0].equals("include")) {
					final String[] truc2 = truc1[1].split("\\)");
					final String fileName=file.getParent()+"/"+truc2[0];
					final File file2 = new File(fileName);
					parameters.addAll(getParametersFrom(file2));
				}
				else {
					parameters.add(line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("File not found.");
		}
		return parameters;
	}

	/**
	 * The main method.
	 * @param args - the arguments.
	 */
	public static void main(String[] args) {
		final long start = (new Date()).getTime();
		final File file = selectScenario();
		if (file!=null) {
			final LinkedList<String> parameters = getParametersFrom(file);
			new Simulator(file.getName(),parameters);
		}
		System.out.println("Duration: "+((new Date()).getTime()-start)/1000.) ;
	}

	/** A flag that indicates whether or not the simulation is paused. */
	private boolean pause=true;

	/** The application window. */
	private JamelWindow window=null;

	/**
	 * Creates a new simulator.
	 * @param name  the name of the simulation.
	 * @param parameters  the parameters of the simulation.Ž
	 */
	public Simulator(String name, LinkedList<String> parameters) {
		JamelObject.setTimer(new Timer());
		JamelObject.setScenarioFileName(name);
		this.name=name;
		final Circuit circuit = new Circuit(this, parameters);
		this.window = new JamelWindow();
		this.window.setSelectedTab(5);
		this.window.setTitle(name);
		this.window.setVisible(true);
		this.println(parameters);
		this.window.setSelectedTab(0);
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

	/**
	 * Prints the list of strings in the console panel.
	 * @param strings a list of strings.
	 */
	private void println(LinkedList<String> strings) {
		for(String string: strings) {
			println(string);
		}
	}

	@Override
	int getSimulationId() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#failure()
	 */
	@Override
	public void failure() {
		pause(true);
		window.failure();
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#marker(java.lang.String, org.jfree.data.time.Month)
	 */
	@Override
	public void marker(String label, Month month) {
		window.addMarker(label, month);		
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#pause(boolean)
	 */
	@Override
	public void pause(boolean b) {
		pause = b;
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#println(java.lang.String)
	 */
	@Override
	public void println(String message) {
		window.println(message);
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#setChart(int, int, java.lang.String)
	 */
	@Override
	public void setChart(int tabIndex, int panelIndex, String chartPanelName) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		window.setChart(tabIndex,panelIndex,chartPanelName);
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#setVisiblePanel(int)
	 */
	@Override
	public void setVisiblePanel(int index) {
		window.setSelectedTab(index);
	}

	/* (non-Javadoc)
	 * @see jamel.AbstractSimulator#zoom(int)
	 */
	@Override
	public void zoom(int aZoom) {
		window.zoom(aZoom);
	}	

}
