package jamelV3.jamel.households;

import jamelV3.jamel.roles.AccountHolder;
import jamelV3.jamel.roles.Consumer;
import jamelV3.jamel.roles.Shareholder;
import jamelV3.jamel.roles.Worker;

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
