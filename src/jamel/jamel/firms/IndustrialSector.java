package jamel.jamel.firms;

import jamel.basic.util.Timer;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;

import java.util.List;
import java.util.Random;

/**
 * Represents the industrial sector.
 */
public interface IndustrialSector {
	
	/**
	 * Returns the float value of the specified parameter.  
	 * @param key the key of the parameter.
	 * @return a Float.
	 */
	float getFloatParameter(String key);

	/**
	 * Returns a new bank account for the specified firm.
	 * @param firm the holder of the new account.
	 * @return a new bank account.
	 */
	BankAccount getNewAccount(Firm firm);

	/**
	 * Returns the random.
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns a simple random sample of firms. 
	 * @param size the of the sample. 
	 * @return a sample of firms.
	 */
	List<Firm> getSimpleRandomSample(int size);
	
	/**
	 * Returns the ID of the simulation.
	 * @return the ID of the simulation.
	 */
	long getSimulationID();

	/**
	 * Returns the timer.
	 * @return the timer.
	 */
	Timer getTimer();

	/**
	 * Returns an agent selected at random in the collection of agents that can be a capital owner.
	 * @return a capital owner.
	 */
	Shareholder selectCapitalOwner();

	/**
	 * Returns a sample of agents selected at random in the collection of agents that can be a capital owner.
	 * @param n the number of capital owners to select.
	 * @return an list of capital owners.
	 */
	List<Shareholder> selectCapitalOwner(int n);

}

//***
