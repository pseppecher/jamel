package jamel.jamel.aggregates;

import jamel.jamel.roles.AccountHolder;
import jamel.jamel.widgets.BankAccount;

/**
 * Represents the banking sector.
 */
public interface Banks {

	/**
	 * Creates and returns a new account for the specified {@link AccountHolder}. 
	 * @param accountHolder the {@link AccountHolder}.
	 * @return a new account.
	 */
	BankAccount getNewAccount(AccountHolder accountHolder);

}

// ***
