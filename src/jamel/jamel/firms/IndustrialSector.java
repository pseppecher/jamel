package jamel.jamel.firms;

import jamel.basic.util.Timer;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;

import java.util.List;
import java.util.Random;

/**
 * Represents the industrial sector.
 * The sector must be populated with agents implementing the {@link Firm} interface.
 */
public interface IndustrialSector {
	
	/**
	 * Returns a new {@link BankAccount} for the specified {@link Firm}.
	 * @param firm the holder of the new account.
	 * @return a new {@link BankAccount}.
	 */
	BankAccount getNewAccount(Firm firm);

	/**
	 * Returns the float value of the specified parameter.  
	 * @param key the key of the parameter.
	 * @return  the float value of the specified parameter.
	 */
	float getParam(String key);

	/**
	 * Returns the random.
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns a wage selected at random between the wages recorded at the previous period.
	 * @return a wage selected at random between the wages recorded at the previous period.
	 */
	Double getRandomWage();
	
	/**
	 * Returns a simple random sample of firms. 
	 * @param size the size of the sample. 
	 * @return a list of firms.
	 */
	List<Firm> getSimpleRandomSample(int size);

	/**
	 * Returns the ID of the simulation.
	 * @return the ID of the simulation.
	 */
	long getSimulationID();

	/**
	 * Returns the {@link Timer} of the sector.
	 * @return the {@link Timer} of the sector.
	 */
	Timer getTimer();

	/**
	 * Returns an agent selected at random in the collection of agents that can be a capital owner.
	 * @return a {@link Shareholder}.
	 */
	Shareholder selectCapitalOwner();

	/**
	 * Returns a sample of agents selected at random in the collection of agents that can be a capital owner.
	 * @param n the number of capital owners to select.
	 * @return an list of {@link Shareholder}.
	 */
	List<Shareholder> selectCapitalOwner(int n);

}

//***
