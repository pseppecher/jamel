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

package jamel.agents.households;

import jamel.JamelObject;
import jamel.spheres.realSphere.LaborPower;
import jamel.spheres.realSphere.Machine;
import jamel.util.Timer.JamelPeriod;

/**
 * Represents a household.
 */
abstract public class AbstractHousehold extends JamelObject implements Household {

	/**
	 * The labor power of the household.
	 */
	private class HouseholdLaborPower implements LaborPower {

		/** A flag that indicates whether this labor power is exhausted or not. */
		private boolean exhausted=false;

		/** The period of validity of this labor power. */
		private JamelPeriod period = getCurrentPeriod().getFuturePeriod(-1);

		/**
		 * Next labor power.
		 */
		private void updates() {
			if (!this.period.isJustBefore(getCurrentPeriod()))
				throw new RuntimeException("Time inconsistency.");
			this.period = getCurrentPeriod();
			this.exhausted = false;
		}

		/**
		 * Expends the labor power.
		 */
		@Override
		public void expend() {
			if (!this.period.isCurrentPeriod()) 
				throw new RuntimeException("This labor power is out-of-date.");
			if (this.exhausted) 
				throw new RuntimeException("This labor power is exhausted.");
			this.exhausted=true;
		}

	}

	/** The labor power of the household. */
	final private HouseholdLaborPower laborPower = new HouseholdLaborPower();

	/**
	 * Returns a flag that indicates wether the labor power of the household is available or not.
	 * @return <code>true</code> if the labor power is available, <code>false</code> otherwise.
	 */
	protected boolean isLaborPowerAvailable() {
		return !this.laborPower.exhausted;
	}

	/**
	 * Updates the labor power.
	 */
	protected void updateLaborPower() {
		this.laborPower.updates();
	}
	
	/**
	 * Generates a <code>RuntimeException</code>, cause a household cannot be bankrupt (in the current version of <code>Jamel</code>).
	 */
	@Override
	public void goBankrupt() {
		throw new RuntimeException("A household cannot be bankrupt.");
	}
	
	/**
	 * 
	 */
	@Override
	public void work(Machine machine) {
		machine.work(this.laborPower );
	}

}
