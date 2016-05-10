package jamel.jamel.firms;

import jamel.jamel.firms.managers.Askable;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Supplier;
import jamel.jamel.widgets.JobOffer;

/**
 * Represents an individual firm. TODO: modifier should be package
 */
public interface Firm extends AccountHolder, Corporation, Supplier, Askable {

	/**
	 * Closes the firm at the end of the period.
	 */
	void close();

	/**
	 * Returns the job offer (if any) of the firm.
	 * 
	 * @return the job offer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns the size of this firm (the number of machines).
	 * 
	 * @return the size of this firm (the number of machines).
	 */
	int getSize();

	/**
	 * Returns the total value of the assets of this firm.
	 * 
	 * @return the total value of the assets of this firm.
	 */
	long getValueOfAssets();

	/**
	 * Returns the total value of the liabilities of this firm.
	 * 
	 * @return the total value of the liabilities of this firm.
	 */
	long getValueOfLiabilities();

	/**
	 * Buys the inputs for the production process.
	 */
	void inputsPurchase();

	/**
	 * Invests.
	 */
	void invest();

	/**
	 * Opens the firm at the beginning of the period.
	 */
	void open();

	/**
	 * Pays the dividend to the owner of the firm.
	 */
	void payDividend();

	/**
	 * Prepares the production.
	 */
	void prepareProduction();

	/**
	 * Implements the production.
	 */
	void production();


}

// ***
