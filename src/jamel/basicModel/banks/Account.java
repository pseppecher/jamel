package jamel.basicModel.banks;

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
	 * Returns the outstanding debt of this account.
	 * 
	 * @return the outstanding debt of this account.
	 */
	long getDebt();

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

}
