package jamel.jamel.sectors;

import jamel.jamel.roles.AccountHolder;
import jamel.jamel.widgets.BankAccount;

/**
 * A sector that contains (potential) shareholders.
 * Used to initialize the ownership of corporations.
 */
public interface BankingSector {

	/**
	 * Creates and returns a new account for the specified {@link AccountHolder}. 
	 * @param accountHolder the {@link AccountHolder}.
	 * @return a new account.
	 */
	BankAccount getNewAccount(AccountHolder accountHolder);

}

// ***
