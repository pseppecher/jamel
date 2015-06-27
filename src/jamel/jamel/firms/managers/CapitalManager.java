package jamel.jamel.firms.managers;

import jamel.basic.agent.AgentDataset;

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
	 * Returns the dataset of the manager.
	 * @return the dataset of the manager.
	 */
	AgentDataset getData();

	/**
	 * Returns <code>true</code> if the firm accounting is consistent, <code>false</code> otherwise.
	 * @return <code>true</code> if the firm accounting is consistent, <code>false</code> otherwise.
	 */
	boolean isConsistent();

	/**
	 * Determines and returns the amount that will be paid as dividend for the current period.
	 * @return the amount of the dividend for the current period.
	 */
	long newDividend();

	/**
	 * Opens the capital manager at the beginning of the period.
	 */
	void open();

	/**
	 * Determines and pays the dividend to the owner of the firm.
	 */
	void payDividend();

	/**
	 * Updates the ownership of the firm.
	 */
	void updateOwnership();

}

// ***
