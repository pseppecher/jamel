package jamel.basic.agents.households;

import jamel.basic.agents.roles.AccountHolder;
import jamel.basic.agents.roles.CapitalOwner;
import jamel.basic.agents.roles.Consumer;
import jamel.basic.agents.roles.Worker;

/**
 * Represents a household.
 */
public interface Household extends Worker,Consumer,AccountHolder,CapitalOwner {

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
