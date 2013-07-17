/**
 * 
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
		private JamelPeriod period = getCurrentPeriod().getNewPeriod(-1);

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
