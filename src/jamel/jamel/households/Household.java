package jamel.jamel.households;

import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Consumer;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.roles.Worker;

/**
 * Represents a household.
 */
public interface Household extends Worker,Consumer,AccountHolder,Shareholder {

	/**
	 * Closes the household at the end of the period.
	 */
	void close();
	
	/**
	 * Opens the household at the beginning of the period.
	 */
	void open();

}

//***
