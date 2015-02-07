package jamel.basic.agents.firms;

import java.util.List;

import jamel.basic.agents.roles.Shareholder;
import jamel.basic.util.BankAccount;

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
	 * Returns a simple random sample of firms. 
	 * @param size the of the sample. 
	 * @return a sample of firms.
	 */
	List<Firm> getSimpleRandomSample(int size);

	/**
	 * Returns the parameter to which the specified key is mapped, or null if the parameters contain no mapping for the key.
	 * @param key the key whose associated parameter is to be returned.
	 * @return a string.
	 */
	String getStringParameter(String key);
	
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
