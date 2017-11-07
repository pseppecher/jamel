package jamel.models.modelJEE.households;

import jamel.models.modelJEE.roles.Consumer;
import jamel.models.modelJEE.roles.Shareholder;
import jamel.models.util.AccountHolder;
import jamel.models.util.Worker;

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
