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

import jamel.JamelObject;
import jamel.util.Timer.JamelPeriod;

/**
 * An abstract process of production.
 * 	 * <p>
 * A production process is incremented by the expenditure of a {@link LaborPower}.
 * When the production process is completed, a new nonempty {@link FinalGoods} object is created.
 */
abstract class AbstractProductionProcess extends JamelObject implements ProductionProcess {

	/** A flag that controls whether or not the process is canceled. */
	private boolean canceled=false;

	/** The production cycle time (= the number of successive labor-power expenditures required to complete the process). */
	final private int productionTime;

	/** The productivity of the process. */
	final private double productivity;

	/** The progress of the production process (= the number of labor-powers already expended in this process). */
	private int progress = 0;

	/** The value of the process (= the sum of the wages payed for). */
	protected long value = 0;

	/** The last period in which the process was incremented. */
	private JamelPeriod lastUsed = getOrigin();

	/**
	 * Creates a new production process.
	 * @param aProductivity - the productivity.
	 * @param aProductionTime - the production time.
	 */
	public AbstractProductionProcess(double aProductivity, int aProductionTime) {
		this.productivity = aProductivity;
		this.productionTime = aProductionTime;
	}

	/**
	 * Adds a new value to the process value.
	 * @param value - the value to add.
	 */
	@Override
	public void addValue(long value) {
		if (value<=0) new RuntimeException("Walue must be stricly positive.");
		this.value+=value;
	}

	/**
	 * Cancels the process.<br>
	 * Generates an exception if the process is already canceled.
	 */
	@Override
	public void cancel() {
		if (this.canceled) new RuntimeException("This process is already canceled.");
		canceled=true;
		progress=0;
		value=0;
	}

	/**
	 * Returns the last period of increment of the process.
	 * @return the period.
	 */
	@Override
	public JamelPeriod getLastUsed() {
		return lastUsed;
	}

	/**
	 * Returns a number that represents the productivity.
	 * @return a double.
	 */
	@Override
	public double getProductivity() {
		return this.productivity;
	}

	/**
	 * Returns an number that represents the progress of the production process.
	 * @return an integer.
	 */
	@Override
	public int getProgress() {
		return progress;
	}

	/** 
	 * Returns a number that represents the value of the production process.
	 * @return a long integer.
	 */
	@Override
	public long getValue() {
		return value;
	}

	/**
	 * Returns a flag that indicates whether or not the process is completed.
	 * @return a boolean.
	 */
	@Override
	public boolean isCompleted() {
		return (progress==productionTime);
	}

	/**
	 * Increments the production process.<br>
	 * The labor power is expended().
	 * If the process is completed, a new volume of commodities is created.<br>
	 * Generates an exception if the process has been already called in the current period
	 * or if the process is already completed or canceled.
	 * @param laborPower - the labor power to expend.
	 */
	@Override
	public void progress(LaborPower laborPower) {
		if (this.canceled) new RuntimeException("This process is cancelled.");
		if (isCompleted()) new RuntimeException("This process is already completed.");
		if (progress>productionTime) new RuntimeException("The progress exceeds the production cycle time.");
		if (lastUsed.isAfter(getCurrentPeriod())) new RuntimeException("This process has been already incremented in this period.");
		lastUsed=getCurrentPeriod();
		laborPower.expend();
		progress++;
	}

}
