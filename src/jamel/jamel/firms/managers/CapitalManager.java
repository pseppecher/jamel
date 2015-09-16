package jamel.jamel.firms.managers;

import jamel.basic.util.Timer;
import jamel.jamel.capital.StockCertificate;

/**
 * The capital manager.
 */
public abstract class CapitalManager extends AbstractManager implements Askable {

	/**
	 * Creates a new capital manager.
	 * @param name the name of the manager.
	 * @param timer the timer.
	 */
	public CapitalManager(String name, Timer timer) {
		super(name, timer);
	}

	/**
	 * Should be called when the firm is bankrupted.
	 */
	public abstract void bankrupt();

	/**
	 * Clears the ownership of the firm.
	 * <p>
	 * Each share is cancelled.
	 */
	public abstract void clearOwnership();

	/**
	 * Issues the specified number of new shares.
	 * 
	 * @param n
	 *            the number of new shares to be issued.
	 * @return a {@link StockCertificate} that encapsulates the new shares.
	 */
	public abstract StockCertificate getNewShares(Integer n);

	/**
	 * Returns <code>true</code> if the firm accounting is consistent,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the firm accounting is consistent,
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean isConsistent();

	/**
	 * Returns <code>true</code> if the firm is solvent, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the firm is solvent, <code>false</code>
	 *         otherwise.
	 */
	public abstract boolean isSolvent();

	/**
	 * Determines and pays the dividend to the owner of the firm.
	 */
	public abstract void payDividend();

	/**
	 * Secures the financing of the specified amount.
	 * 
	 * @param amount
	 *            the amount.
	 */
	public abstract void secureFinancing(long amount);

	/**
	 * Updates the ownership of the firm.
	 */
	public abstract void updateOwnership();

}

// ***
