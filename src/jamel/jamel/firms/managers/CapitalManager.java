package jamel.jamel.firms.managers;

import jamel.basic.data.AgentDataset;
import jamel.jamel.firms.capital.StockCertificate;

/**
 * The capital manager.
 */
public interface CapitalManager {

	/**
	 * Should be called when the firm is bankrupted.
	 */
	void bankrupt();

	/**
	 * Clears the ownership of the firm.
	 * <p>
	 * Each share is cancelled.
	 */
	void clearOwnership();

	/**
	 * Closes the capital manager.
	 */
	void close();

	/**
	 * Returns the capital of the firm.
	 * 
	 * @return the capital of the firm.
	 */
	long getCapital();

	/**
	 * Returns the dataset of the manager.
	 * 
	 * @return the dataset of the manager.
	 */
	AgentDataset getData();

	/**
	 * Issues the specified number of new shares.
	 * 
	 * @param n
	 *            the number of new shares to be issued.
	 * @return a {@link StockCertificate} that encapsulates the new shares.
	 */
	StockCertificate getNewShares(Integer n);

	/**
	 * Returns the total value of the assets of the firm.
	 * 
	 * @return the total value of the assets of the firm.
	 */
	long getValueOfAssets();

	/**
	 * Returns the total value of the liabilities of the firm.
	 * 
	 * @return the total value of the liabilities of the firm.
	 */
	long getValueOfLiabilities();

	/**
	 * Returns <code>true</code> if the firm accounting is consistent,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the firm accounting is consistent,
	 *         <code>false</code> otherwise.
	 */
	boolean isConsistent();

	/**
	 * Returns <code>true</code> if the firm is solvent, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the firm is solvent, <code>false</code>
	 *         otherwise.
	 */
	boolean isSolvent();

	/**
	 * Determines and returns the amount that will be paid as dividend for the
	 * current period.
	 * 
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
	 * Secures the financing of the specified amount.
	 * 
	 * @param amount
	 *            the amount.
	 */
	void secureFinancing(long amount);

	/**
	 * Updates the ownership of the firm.
	 */
	void updateOwnership();

}

// ***
