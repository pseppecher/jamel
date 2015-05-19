package jamelV3.jamel.banks;

/**
 * Represents a deposit.
 */
public interface Deposit {

	/**
	 * Credits the deposit with the specified amount.
	 * @param creditAmount the amount to be credited.
	 */
	void credit(long creditAmount);

	/**
	 * Debits the deposit of the specified amount.
	 * @param debit the amount to be debited.
	 */
	void debit(long debit);

	/**
	 * Returns the available amount.
	 * @return a long integer.
	 */
	long getAmount();

	/**
	 * Cancels the deposit.
	 */
	void cancel();

}

// ***
