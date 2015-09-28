package jamel.jamel.banks;

import jamel.basic.agent.Agent;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.widgets.BankAccount;

/**
 * Represents a bank.
 */
interface Bank extends Agent {

	/**
	 * Closes the bank at the end of the period.
	 */
	void close();

	/**
	 * Recovers due debts and interests.
	 */
	void debtRecovery();

	/**
	 * Creates and returns a new account for the specified {@link AccountHolder}
	 * .
	 * 
	 * @param accountHolder
	 *            the {@link AccountHolder}.
	 * @return a new account.
	 */
	BankAccount getNewAccount(AccountHolder accountHolder);

	/**
	 * Opens the bank at the beginning of the period.
	 */
	void open();

	/**
	 * Pays the dividend to its owner.
	 */
	void payDividend();

}
