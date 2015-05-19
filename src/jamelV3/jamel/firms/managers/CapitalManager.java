package jamelV3.jamel.firms.managers;

/**
 * The capital manager.
 */
public interface CapitalManager {

	/**
	 * Should be called when the firm is bankrupted.
	 */
	void bankrupt();

	/**
	 * Closes the capital manager.
	 */
	void close();

	/**
	 * Returns the amount of the capital of the firm (at book value).
	 * @return the amount of the capital of the firm (at book value).
	 */
	long getCapital();

	/**
	 * Returns the amount of debt exceeding the firm target. 
	 * @return the amount of debt exceeding the firm target.
	 */
	double getLiabilitiesExcess();

	/**
	 * Returns the target value of the liabilities.
	 * @return the target value of the liabilities.
	 */
	double getLiabilitiesTarget();

	/**
	 * Returns <code>true</code> if the current level of capital is <i>satisfacing</i>. 
	 * @return <code>true</code> if the current level of capital is <i>satisfacing</i>.
	 */
	boolean isSatisfacing();

	/**
	 * Opens the capital manager at the beginning of the period.
	 */
	void open();

	/**
	 * Determines and pays the dividend to the owner of the firm.
	 */
	void payDividend();

	/**
	 * Determines and returns the amount that will be paid as dividend for the current period.
	 * @return the amount of the dividend for the current period.
	 */
	long newDividend();

	/**
	 * Updates the ownership of the firm.
	 */
	void updateOwnership();

}

// ***
