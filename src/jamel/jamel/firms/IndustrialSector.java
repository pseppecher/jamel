package jamel.jamel.firms;

import jamel.basic.sector.Sector;
import jamel.basic.util.Timer;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Supply;

import java.util.List;
import java.util.Random;

/**
 * Represents the industrial sector. The sector must be populated with agents
 * implementing the {@link Firm} interface.
 */
interface IndustrialSector extends Sector {

	/**
	 * Returns a new {@link BankAccount} for the specified {@link Firm}.
	 * 
	 * @param firm
	 *            the holder of the new account.
	 * @return a new {@link BankAccount}.
	 */
	BankAccount getNewAccount(Firm firm);

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns a wage selected at random between the wages recorded at the
	 * previous period.
	 * 
	 * @return a wage selected at random between the wages recorded at the
	 *         previous period.
	 */
	Double getRandomWage();

	/**
	 * Returns a simple random sample of firms.
	 * 
	 * @param size
	 *            the size of the sample.
	 * @return a list of firms.
	 */
	List<Firm> getSimpleRandomSample(int size);

	/**
	 * Returns the ID of the simulation.
	 * 
	 * @return the ID of the simulation.
	 */
	long getSimulationID();

	/**
	 * Returns an array of supplies.
	 * 
	 * @param type
	 *            the type of goods to be supplied.
	 * 
	 * @param i
	 *            the number of supplies to be returned.
	 * @return an array of supplies.
	 */
	Supply[] getSupplies(String type, int i);

	/**
	 * Returns the current technology.
	 * 
	 * @return the current technology.
	 */
	Technology getTechnology();

	/**
	 * Returns the {@link Timer} of the sector.
	 * 
	 * @return the {@link Timer} of the sector.
	 */
	Timer getTimer();

	/**
	 * Returns an agent selected at random in the collection of agents that can
	 * be a capital owner.
	 * 
	 * @return a {@link Shareholder}.
	 */
	Shareholder selectCapitalOwner();

	/**
	 * Returns a sample of agents selected at random in the collection of agents
	 * that can be a capital owner.
	 * 
	 * @param n
	 *            the number of capital owners to select.
	 * @return an list of {@link Shareholder}.
	 */
	List<Shareholder> selectCapitalOwner(int n);

}

// ***
