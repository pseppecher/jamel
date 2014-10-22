package jamel.basic.agents.firms;

import java.util.Map;

import jamel.basic.agents.roles.AccountHolder;
import jamel.basic.agents.roles.Agent;
import jamel.basic.agents.roles.Asset;
import jamel.basic.agents.roles.Supplier;
import jamel.basic.util.JobOffer;
import jamel.basic.util.Supply;

/**
 * Represents an individual firm.
 */
public interface Firm extends Agent, AccountHolder, Asset, Supplier {

	/**
	 * Closes the firm at the end of the period.
	 */
	void close();

	/**
	 * Returns the data of the firm.
	 * @return the data.
	 */
	Map<String, Double> getData();

	/**
	 * Returns the job offer (if any) of the firm.
	 * @return the job offer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns the supply (if any) of the firm.
	 * @return the supply.
	 */
	Supply getSupply();

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
