package jamel.jamel.sectors;

import java.util.Random;

import jamel.basic.util.Timer;
import jamel.jamel.households.Household;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

/**
 * Represents the sector of the households.
 */
public interface HouseholdSector {

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
	 * Returns the float value of the specified parameter.  
	 * @param key the key of the parameter.
	 * @return a Float.
	 */
	Float getParam(String key);

	/**
	 * Returns the random.
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns an array of supplies.
	 * @param i the number of supplies to be returned.
	 * @return an array of supplies.
	 */
	Supply[] getSupplies(int i);

	/**
	 * Returns the timer.
	 * @return the timer.
	 */
	Timer getTimer();
	
}

// ***
