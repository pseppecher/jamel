package jamel.jamel.banks;

/**
 * Represents a deposit.
 */
interface Deposit {

	/**
	 * Credits this deposit with the specified amount.
	 * @param creditAmount the amount to be credited.
	 */
	void credit(long creditAmount);

	/**
	 * Debits the deposit of the specified amount.
	 * @param debit the amount to be debited.
	 */
	void debit(long debit);

	/**
	 * Returns the available amount on this deposit.
	 * @return the available amount.
	 */
	long getAmount();

	/**
	 * Cancels this deposit.
	 */
	void cancel();

}

// ***
