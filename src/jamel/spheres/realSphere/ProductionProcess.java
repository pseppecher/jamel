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

package jamel.spheres.realSphere;

import jamel.util.Timer.JamelPeriod;

/**
 * An abstraction of a process of production.
 */
public interface ProductionProcess {

	/**
	 * Returns <code>true</code> if the process is completed, <code>false</code> else. 
	 * @return a boolean.
	 */
	public abstract boolean isCompleted();

	/**
	 * Returns the advancement of the process, ie the number of times the process was incremented since its beginning.
	 * @return an integer.
	 */
	public abstract int getProgress();

	/**
	 * Returns the productivity (why that?).
	 * @return the productivity.
	 */
	public abstract double getProductivity();

	/**
	 * Returns the value of the process, ie the sum of the wages and other costs spent for its advancement.
	 * @return the value of the process.
	 */
	public abstract long getValue();

	/**
	 * Cancels the process.
	 */
	public abstract void cancel();

	/**
	 * Increments the production process.<br>
	 * The labor power is expended().
	 * If the process is completed, a new volume of commodities is created.<br>
	 * Generates an exception if the process has been already called in the current period
	 * or if the process is already completed or canceled.
	 * @param laborPower - the labor power to expend.
	 */
	public abstract void progress(LaborPower laborPower);

	/**
	 * Adds a new value to the process value.
	 * @param value - the value to add.
	 */
	public abstract void addValue(long value);

	/**
	 * Returns the last period of increment of the process.
	 * @return the period.
	 */
	public abstract JamelPeriod getLastUsed();

}
