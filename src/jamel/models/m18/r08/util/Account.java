package jamel.models.m18.r08.util;

/**
 * Represents an account.
 */
public interface Account {

	/**
	 * Borrows the specified amount of money.
	 * 
	 * @param principal
	 *            the amount to be borrowed.
	 * @param term
	 *            the term of the loan.
	 * @param amortized
	 *            if the loan is amortizing.
	 */
	void borrow(long principal, int term, boolean amortized);

	/**
	 * Cancels the specified amount of debt.
	 * 
	 * @param amount
	 *            the amount of debt to be cancelled.
	 */
	void cancelDebt(long amount);

	/**
	 * Closes the account. Should be called at the end of each period.
	 */
	void close();

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
	 * Returns the total debt for this account.
	 * 
	 * @return the total debt.
	 */
	long getDebt();

	/**
	 * Returns the amount of the debt service for the current period.
	 * 
	 * @return the amount of the debt service for the current period.
	 */
	long getDebtService();

	/**
	 * Returns the amount paid as interest for the current period.
	 * 
	 * @return the amount paid as interest for the current period.
	 */
	long getInterests();

	/**
	 * Returns the long term debt.
	 * 
	 * @return the long term debt.
	 */
	long getLongTermDebt();

	/**
	 * Returns the current amount of the overdue debt.
	 * 
	 * @return the current amount of the overdue debt.
	 */
	long getOverdueDebt();

	/**
	 * Returns the amount of the new loans for this account for the current
	 * period.
	 * 
	 * @return the amount of the new loans for this account for the current
	 *         period.
	 */
	long getNewDebt();

	/**
	 * Returns the real rate.
	 * 
	 * @return the real rate.
	 */
	float getRealRate();

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
	 * Issues a new {@code Cheque}.
	 * 
	 * @param payee
	 *            the payee.
	 * @param amount
	 *            the amount of the cheque.
	 * @return a new cheque.
	 */
	Cheque issueCheque(AccountHolder payee, long amount);

	/**
	 * Opens the account. Should be called at the beginning of each period.
	 */
	void open();

}
