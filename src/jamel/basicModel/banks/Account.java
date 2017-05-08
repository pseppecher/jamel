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
	 * Transfers the specified amount from this account to the payee account.
	 * 
	 * @param amount
	 *            the amount to be transfered.
	 * @param payee
	 *            the payee.
	 * @param reason
	 *            the reason of the payment.
	 */
	void transfer(long amount, AccountHolder payee, String reason);

}
