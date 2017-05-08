package jamel.jamel.banks;

/**
 * Represents an account.
 */
public interface Account {

	/**
	 * Transfers the specified amount from this account to the payee account.
	 * 
	 * @param amount
	 *            the amount to be transfered.
	 * @param payee
	 *            the payee.
	 * @param reason the reason of the payment.
	 */
	void transfer(long amount, AccountHolder payee, String reason);
	
	void borrow(long amount, int term, boolean amortizing);

}
