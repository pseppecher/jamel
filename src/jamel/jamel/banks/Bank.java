package jamel.jamel.banks;

import jamel.util.Agent;

/**
 * Represents a bank.
 */
interface Bank extends Agent {

	/**
	 * Opens a new account for the specified {@link AccountHolder}
	 * 
	 * @param accountHolder
	 *            the {@link AccountHolder}.
	 * @return a new account.
	 */
	Account openAccount(AccountHolder accountHolder);

}

// ***