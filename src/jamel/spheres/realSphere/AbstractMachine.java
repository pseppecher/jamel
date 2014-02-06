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

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.roles.Worker;
import jamel.util.Timer.JamelPeriod;

/**
 * Represents a machine.
 * <p> 
 * A Machine is an object which products commodities by an iterative process of labor-power expenditure.
 * Each time a {@link LaborPower} is provided to the machine, the associated {@link ProductionProcess} is incremented.
 * At the end of the production cycle, the product is available as a new nonempty {@link FinalGoods} object and it can be provided to the {@link Factory}.
 */
abstract class AbstractMachine extends JamelObject implements Machine {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRODUCTION_TIME = "Firms.productionTime";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRODUCTIVITY = "Firms.productivity";

	/** The last period the machine worked.*/
	private JamelPeriod lastUsed;

	/** The production process. */
	protected ProductionProcess process;

	/**
	 * Creates a new machine.
	 */
	public AbstractMachine() {
		this.lastUsed = null;
	}

	/**
	 * Returns a new production process.
	 * @return the new process.
	 */
	protected abstract ProductionProcess newProductionProcess();

	/**
	 * Returns the value of the current production process (= the sum of wages payed).
	 * @return the value of the current production process.
	 */
	@Override
	public long getProductionProcessValue() {
		if (process==null) return 0;
		return process.getValue();
	}

	/**
	 * Returns the production time. 
	 * @return the production time.
	 */
	@Override
	public int getProductionTime() {
		return Integer.parseInt(Circuit.getParameter(PARAM_PRODUCTION_TIME));
	}

	/**
	 * Returns the productivity of the machine. 
	 * @return the productivity of the machine.
	 */
	@Override
	public int getProductivity() {
		return Integer.parseInt(Circuit.getParameter(PARAM_PRODUCTIVITY));
	}

	/**
	 * Returns an integer that represents the progress of the production process.
	 * @return an integer.
	 */
	@Override
	public int getProgress() { 
		if (process==null) return 0;
		return process.getProgress() ; 
	}

	
	/**
	 * 
	 */
	@Override
	public void kill() {
		this.process = null;
	}

	/**
	 * Expends the given labor power in the production-process progress.
	 * @param labourPower the labor power to expend.
	 */
	@Override
	public void work(LaborPower labourPower) {
		this.process.progress(labourPower);
	}
	
	/**
	 * Increments the production-process by the expenditure of a labor power.<br>
	 * Can be called only once in a period (else an exception is generated).
	 * @param worker the worker.
	 * @param wage the wage payed.
	 */
	@Override
	public void work(Worker worker, long wage) {
		if ((this.lastUsed!=null) && (!this.lastUsed.isBefore(getCurrentPeriod()))) 
			throw new RuntimeException("This machine has already worked in the current period.");
		if (this.process==null) 
			this.process = newProductionProcess();
		this.process.addValue(wage) ;
		worker.work(this);
		if ((this.process!=null) && (!this.process.getLastUsed().isCurrentPeriod())) 
			throw new RuntimeException("The process has not been incremented.");
		this.lastUsed = getCurrentPeriod();
	}

}
