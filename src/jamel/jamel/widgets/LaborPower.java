package jamel.jamel.widgets;

/**
 * Represents the labor power of a worker. 
 */
public interface LaborPower {

	/**
	 * Expends all the energy.
	 */
	void expend();

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
