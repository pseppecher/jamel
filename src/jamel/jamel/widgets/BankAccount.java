package jamel.jamel.widgets;

import jamel.jamel.roles.AccountHolder;

/**
 * Represents a bank account.
 */
public interface BankAccount extends Chequable {

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
	long getCanceledDebt();

	/**
	 * Returns the amount of money canceled by the bank for this account for the
	 * current period.
	 * 
	 * @return the amount of money canceled by the bank for this account for the
	 *         current period.
	 */
	long getCanceledMoney();

	/**
	 * Returns the total debt for this account.
	 * 
	 * @return the total debt.
	 */
	long getDebt();

	/**
	 * Returns some informations about this account.
	 * 
	 * @return a string that contains some informations about this account.
	 */
	String getInfo();

	/**
	 * Returns the amount paid as interest for the current period.
	 * 
	 * @return the amount paid as interest for the current period.
	 */
	long getInterest();

	/**
	 * Returns the long term debt.
	 * 
	 * @return the long term debt.
	 */
	long getLongTermDebt();

	/**
	 * Returns the amount of the new loans for this account for the current
	 * period.
	 * 
	 * @return the amount of the new loans for this account for the current
	 *         period.
	 */
	long getNewDebt();

	/**
	 * Returns the amount of the loans repaid for this account for the current
	 * period.
	 * 
	 * @return the amount of the loans repaid for this account for the current
	 *         period.
	 */
	long getRepaidDebt();

	/**
	 * Returns the short term debt.
	 * 
	 * @return the short term debt.
	 */
	long getShortTermDebt();

	/**
	 * Returns <code>true</code> if the account is cancelled, <code>false</code>
	 * otherwise.
	 * 
	 * @return a boolean.
	 */
	boolean isCancelled();

	/**
	 * Creates a new long-term loan. The principal of the new loan is credited
	 * to this account.
	 * 
	 * @param principal
	 *            the amount of the new loan.
	 */
	void newLongTermLoan(long principal);

	/**
	 * Creates a new short-term loan. The principal of the new loan is credited
	 * to this account.
	 * 
	 * @param principal
	 *            the amount of the new loan.
	 */
	void newShortTermLoan(long principal);

}

// ***
