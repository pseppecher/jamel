package jamel.basic.agents.households;

import jamel.basic.util.BankAccount;
import jamel.basic.util.JobOffer;
import jamel.basic.util.Supply;

/**
 * Represents the sector of the households.
 */
public interface HouseholdsSector {

	/**
	 * Returns an array of job offers.
	 * @param i the number of job offers to be returned.
	 * @return an array of job offers.
	 */
	JobOffer[] getJobOffers(int i);

	/**
	 * Returns a new bank account for the given household.
	 * @param Household the future owner of the account.
	 * @return a new bank account.
	 */
	BankAccount getNewAccount(Household Household);

	/**
	 * Returns the parameter to which the specified key is mapped, or null if the parameters contain no mapping for the key.
	 * @param key the key whose associated parameter is to be returned.
	 * @return a string.
	 */
	String getParameter(String key);

	/**
	 * Returns an array of supplies.
	 * @param i the number of supplies to be returned.
	 * @return an array of supplies.
	 */
	Supply[] getSupplies(int i);
	
}

// ***
