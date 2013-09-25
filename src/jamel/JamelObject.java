/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
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
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jfree.data.time.Month;

import jamel.util.Timer;
import jamel.util.Timer.JamelPeriod;

/**
 * An abstract class with methods to get time and random.
 */
public abstract class JamelObject {

	/** The output directory. */
	private static File outputDirectory;

	/** The random. */
	private static Random random;

	/** The name of the scenario file. */
	private static String scenarioFileName;

	/** The timer. */
	private static  Timer timer;

	/** The line separator */
	final static public String rc = System.getProperty("line.separator");

	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public static JamelPeriod getCurrentPeriod() {
		return timer.getCurrentPeriod();
	}

	/**
	 * Returns the random.
	 * @return the random.
	 */
	public static Random getRandom() {
		return random;
	}

	/**
	 * Sets the random.
	 * @param random2  the random to set.
	 */
	public static void setRandom(Random random2) {
		random = random2;
	}

	/**
	 * Sets the name of the scenario file.
	 * @param name the name to set.
	 */
	public static void setScenarioFileName(String name) {
		scenarioFileName = name;
		outputDirectory = new File("../exports/"+scenarioFileName+"-"+(new Date()).getTime());
	}

	/**
	 * Sets the timer.
	 * @param aTimer - the timer to set.
	 */
	public static void setTimer(Timer aTimer) {
		timer = aTimer;
	}

	/** Messages to print in the journal file. */
	private final List<String> buffer = new LinkedList<String>();

	/** A flag that indicates whether the object is verbose or not. */
	private boolean verbose = false;

	/**
	 * Sets the verbosity of this object.
	 * @param b  if true, then the object will be verbose. 
	 */
	protected void setVerbose(boolean b) {
		this.verbose = b;
	}

	/**
	 * Writes a string in the buffer.
	 * @param str The <code>String</code> to be written. 
	 */
	protected void write(String str) {
		if (verbose) {
			buffer.add(str);
		}
	}

	/**
	 * Writes the content of the string buffer in the file associated to this object.
	 */
	protected void writeJournal() {
		if ((verbose)&&(!buffer.isEmpty())) {
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs();
			}
			try {
				String myName = ""+this+".html";
				final FileWriter writer = new FileWriter(new File(outputDirectory,myName),true);
				for (String str:buffer) {
					writer.write(str);
				}
				buffer.clear();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error while writing in the file.");
			}
		}
	}

	/**
	 * Writes a String in the buffer and then terminate the line.
	 * @param str The <code>String</code> to be written. 
	 */
	protected void writeln(String str) {
		write(str+rc);
	}

	/**
	 * Goes to the next period.
	 */
	void nextPeriod() {
		timer.nextPeriod();
	}

	/**
	 * Returns the origin of the timer.
	 * @return the origin.
	 */
	public JamelPeriod getOrigin() {
		return timer.getOrigin();
	}

	/**
	 * Returns a new period for the given month.
	 * @param month - the month of the new period.
	 * @return a new period.
	 */
	public JamelPeriod newJamelPeriod(Month month) {
		return timer.newJamelPeriod(month);
	}

}
