package jamelV3.jamel.firms;

import jamelV3.basic.agent.Agent;
import jamelV3.jamel.roles.AccountHolder;
import jamelV3.jamel.roles.Corporation;
import jamelV3.jamel.roles.Supplier;
import jamelV3.jamel.widgets.JobOffer;

/**
 * Represents an individual firm.
 */
public interface Firm extends Agent, AccountHolder, Corporation, Supplier {

	/**
	 * Closes the firm at the end of the period.
	 */
	void close();

	/**
	 * Returns something.
	 * @param string the key for the thing to return.
	 * @return something.
	 */
	Object get(String string);

	/**
	 * Returns the job offer (if any) of the firm.
	 * @return the job offer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns the wage offered.
	 * @return the wage offered.
	 */
	Double getWage();

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
