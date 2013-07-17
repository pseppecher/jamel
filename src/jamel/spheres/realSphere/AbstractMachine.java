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

	/** The factory to which the new product will be delivered. */
	//protected Factory factory;

	/** The last period the machine worked.*/
	private JamelPeriod lastUsed;

	/** The production process. */
	protected ProductionProcess process;

	/** The production cycle time. */
	protected int productionTime ;

	/** The productivity (= the volume of commodities that the machine can product on average in one period). */
	protected int productivity ;

	/**
	 * Creates a new machine with the given productivity.
	 * @param productivity the productivity.
	 * @param productionTime the production cycle time.
	 */
	public AbstractMachine(int productivity, int productionTime) {
		this.productivity = productivity;
		this.productionTime = productionTime;
		this.lastUsed = null;
	}

	/**
	 * Changes the productivity of the machine.
	 * @param ratio the change ratio.
	 */
	public void changeProductivity(float ratio) {
		this.productivity = (int) (this.productivity*ratio) ;
	}

	/**
	 * Returns the value of the current production process (= the sum of wages payed).
	 * @return the value of the current production process.
	 */
	public long getProductionProcessValue() {
		if (process==null) return 0;
		return process.getValue();
	}

	/**
	 * Returns an integer that represents the productivity of the machine.
	 * @return an integer.
	 */
	public int getProductivity() { 
		return productivity ; 
	}

	/**
	 * Returns an integer that represents the progress of the production process.
	 * @return an integer.
	 */
	public int getProgress() { 
		if (process==null) return 0;
		return process.getProgress() ; 
	}

	/**
	 * 
	 */
	public void kill() {
		this.process = null;
	}

	/**
	 * Sets the production cycle time.
	 * @param time the production cycle time.
	 */
	public void setProdTime(int time) {
		productionTime = time ;
	}

	/**
	 * Sets the productivity.
	 * @param newProductivity - the productivity to set.
	 */
	public void setProductivity(int newProductivity) {
		this.productivity = newProductivity ;		
	}

	/**
	 * Expends the given labor power in the production-process progress.
	 * @param labourPower the labor power to expend.
	 */
	public void work(LaborPower labourPower) {
		this.process.progress(labourPower);
	}

	/**
	 * Increments the production-process by the expenditure of a labor power.<br>
	 * Can be called only once in a period (else an exception is generated).
	 * @param worker the worker.
	 * @param wage the wage payed.
	 */
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
	
	/**
	 * Returns a new production process.
	 * @return the new process.
	 */
	protected abstract ProductionProcess newProductionProcess();

}
