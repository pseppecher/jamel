package jamel.models.modelJEE.capital;

import java.util.List;

import jamel.models.modelJEE.roles.Corporation;

/**
 * Represents the capital stock of a {@link Corporation}.
 */
public interface CapitalStock {

	/**
	 * Called when the corporation goes bankrupt or when the ownership of the
	 * corporation is completely changed.
	 * <p>
	 * Each share of this capital stock is cancelled.
	 */
	void cancel();

	/**
	 * Closes this capital stock.
	 * <p>
	 * Must be called at the end of the period.
	 */
	void close();

	/**
	 * Returns the list of the stock certificates.
	 * 
	 * @return the list of the stock certificates.
	 */
	List<StockCertificate> getCertificates();

	/**
	 * Returns the corporation.
	 * 
	 * @return the corporation.
	 */
	Corporation getCorporation();

	/**
	 * Returns the period when the stock was created.
	 * 
	 * @return the period when the stock was created.
	 */
	int getDate();

	/**
	 * Returns the amount of dividends actually distributed to the shareholders.
	 * <p>
	 * This amount can differ from the dividend to be distributed because the
	 * shareholders may not have taken their dividend yet.
	 * 
	 * @return the amount of dividends actually distributed to the shareholders.
	 */
	double getDistributedDividends();

	/**
	 * Returns <code>true</code> if this capital stock is open,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this capital stock is open,
	 *         <code>false</code> otherwise.
	 */
	boolean isOpen();

	/**
	 * Issues and returns new shares of this capital stock.
	 * 
	 * @param nShares
	 *            the number of shares to be returned.
	 * @return a {@link StockCertificate} that contains the issued shares.
	 */
	StockCertificate issueNewShares(Integer nShares);

	/**
	 * Opens this capital stock.
	 * <p>
	 * Must be called at the beginning of the period.
	 */
	void open();

	/**
	 * Sets the dividend to be distributed.
	 * 
	 * @param dividend
	 *            the dividend to be distributed.
	 */
	void setDividend(long dividend);

}

// ***
