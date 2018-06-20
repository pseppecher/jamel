package jamel.models.m18.r08.roles;

import jamel.models.m18.r08.util.Account;
import jamel.models.m18.r08.util.AccountHolder;
import jamel.util.Agent;

/**
 * Represents a bank.
 */
public interface Bank extends Agent {

	/**
	 * Creates and returns a new account for the specified {@link AccountHolder}
	 * 
	 * @param accountHolder
	 *            the {@link AccountHolder}.
	 * @return a new account.
	 */
	Account openAccount(AccountHolder accountHolder);

}
