package jamelV3.jamel.sectors;

import jamelV3.jamel.roles.AccountHolder;
import jamelV3.jamel.widgets.BankAccount;

/**
 * A sector that contains (potential) shareholders.
 * Used to initialize the ownership of corporations.
 */
public interface BankingSector {

	/**
	 * Creates and returns a new account for the specified <code>AccountHolder</code>. 
	 * @param accountHolder the <code>AccountHolder</code>.
	 * @return a new account.
	 */
	BankAccount getNewAccount(AccountHolder accountHolder);

}

// ***
