package jamel.basic.agents.firms;

import jamel.basic.agents.roles.CapitalOwner;
import jamel.basic.util.BankAccount;

/**
 * Represents the industrial sector.
 */
public interface IndustrialSector {

	/**
	 * Returns a new bank account for the specified firm.
	 * @param firm the holder of the new account.
	 * @return a new bank account.
	 */
	BankAccount getNewAccount(Firm firm);

	/**
	 * Returns the parameter to which the specified key is mapped, or null if the parameters contain no mapping for the key.
	 * @param key the key whose associated parameter is to be returned.
	 * @return a string.
	 */
	String getParameter(String key);

	/**
	 * Returns an agent selected at random in the collection of agents that can be a capital owner.
	 * @return a capital owner.
	 */
	CapitalOwner selectCapitalOwner();

}
