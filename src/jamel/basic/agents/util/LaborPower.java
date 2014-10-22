package jamel.basic.agents.util;

/**
 * Represents the labor power of a worker. 
 */
public interface LaborPower {

	/**
	 * Expends all the energy.
	 */
	void expend();

	/**
	 * Expends the specified amount of energy.
	 * @param energy the amount of energy to be expended.
	 */
	void expend(float energy);

	/**
	 * Returns the energy of the labor power.
	 * When the energy equals 0, the labor power is exhausted. 
	 * @return a float in [0,1].
	 */
	float getEnergy();

	/**
	 * Returns the remaining value of this labor power.
	 * @return the remaining value.
	 */
	long getValue();

	/**
	 * Returns the wage paid for this labor power.
	 * @return the wage.
	 */
	long getWage();

	/**
	 * Returns <code>true</code> if this labor power is exhausted, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	boolean isExhausted();

}

// ***
