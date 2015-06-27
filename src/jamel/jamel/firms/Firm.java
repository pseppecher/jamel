package jamel.jamel.firms;

import jamel.basic.agent.Agent;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Supplier;
import jamel.jamel.widgets.JobOffer;

/**
 * Represents an individual firm.
 */
public interface Firm extends Agent, AccountHolder, Corporation, Supplier {

	/**
	 * Closes the firm at the end of the period.
	 */
	void close();

	/**
	 * Returns the job offer (if any) of the firm.
	 * @return the job offer.
	 */
	JobOffer getJobOffer();

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

//***
