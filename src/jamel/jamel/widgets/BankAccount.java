package jamel.jamel.widgets;

import jamel.jamel.roles.AccountHolder;

/**
 * Represents a bank account.
 */
public interface BankAccount extends Chequable{

	/**
	 * Deposits the cheque on this account.
	 * 
	 * @param cheque
	 *            the cheque to be deposited.
	 */
	void deposit(Cheque cheque);

	/**
	 * Returns the account holder.
	 * 
	 * @return the account holder.
	 */
	AccountHolder getAccountHolder();

	/**
	 * Returns the available amount of money on this account.
	 * 
	 * @return the available amount of money.
	 */
	long getAmount();

	/**
	 * Returns the amount of debt canceled by the bank for this account for the
	 * current period.
	 * 
	 * @return the amount of debt canceled by the bank for this account for the
	 *         current period.
	 */
	double getCanceledDebt();

	/**
	 * Returns the amount of money canceled by the bank for this account for the
	 * current period.
	 * 
	 * @return the amount of money canceled by the bank for this account for the
	 *         current period.
	 */
	double getCanceledMoney();

	/**
	 * Returns the total debt for this account.
	 * 
	 * @return the total debt.
	 */
	long getDebt();

	/**
	 * Returns the amount paid as interest for the current period.
	 * 
	 * @return the amount paid as interest for the current period.
	 */
	long getInterest();

	/**
	 * Returns the amount of the new loans for this account for the current
	 * period.
	 * 
	 * @return the amount of the new loans for this account for the current
	 *         period.
	 */
	double getNewDebt();

	/**
	 * Returns the amount of the loans repaid for this account for the current
	 * period.
	 * 
	 * @return the amount of the loans repaid for this account for the current
	 *         period.
	 */
	double getRepaidDebt();

	/**
	 * Returns <code>true</code> if the account is cancelled, <code>false</code>
	 * otherwise.
	 * 
	 * @return a boolean.
	 */
	boolean isCancelled();

	/**
	 * Creates a new loan. The principal of the new loan is credited to this
	 * account.
	 * 
	 * @param principal
	 *            the amount of the new loan.
	 */
	void lend(long principal);

}

// ***
