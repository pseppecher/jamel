package jamel.basic.util;

import jamel.basic.agents.roles.AccountHolder;

/**
 * Represents a bank account.
 */
public interface BankAccount {

	/**
	 * Deposits the cheque.
	 * @param cheque the cheque to be deposited.
	 */
	void deposit(Cheque cheque);

	/**
	 * Returns the account holder.
	 * @return the account holder.
	 */
	AccountHolder getAccountHolder();

	/**
	 * Returns the available amount of money on this account.
	 * @return the available amount of money.
	 */
	long getAmount();

	/**
	 * Returns the total debt for this account.
	 * @return the total debt.
	 */
	long getDebt();

	/**
	 * Returns <code>true</code> if the account is open, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	boolean isOpen();

	/**
	 * Lends the given amount.
	 * @param principal the amount of the loan.
	 */
	void lend(long principal);

	/**
	 * Returns a new cheque from this account.
	 * @param amount the amount of money to pay.
	 * @return a new cheque.
	 */
	Cheque newCheque(long amount);

}

// ***
