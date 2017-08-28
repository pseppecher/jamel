package jamel.v170804.models.basicModel4.interfaces;

/**
 * Represents an account.
 */
public interface Account {

	/**
	 * Borrows the specified amount of money.
	 * 
	 * @param amount
	 *            the amount to be borrowed.
	 * @param term
	 *            the term of the loan.
	 * @param amortizing
	 *            if the loan is amortizing.
	 */
	void borrow(long amount, int term, boolean amortizing);

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
	 * Deposits a cheque in this account.
	 * 
	 * @param cheque
	 *            the cheque to be deposited.
	 */
	void deposit(Cheque cheque);

	/**
	 * Returns the amount of money deposited on this account.
	 * 
	 * @return the amount of money deposited on this account.
	 */
	long getAmount();

	/**
	 * Returns the outstanding debt of this account (normal debt+overdue debt).
	 * 
	 * @return the outstanding debt of this account.
	 */
	long getDebt();

	/**
	 * Returns the amount of the debt service for the current period.
	 * 
	 * @return the amount of the debt service for the current period.
	 */
	long getDebtService();

	/**
	 * Returns the amount of the interests paid on the debt for the current
	 * period.
	 * 
	 * @return the amount of the interests paid on the debt for the current
	 *         period.
	 */
	long getInterests();

	/**
	 * Returns the current amount of the overdue debt.
	 * 
	 * @return the current amount of the overdue debt.
	 */
	long getOverdueDebt();

	/**
	 * Issues a new cheque.
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
