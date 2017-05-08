package jamel.basicModel.banks;

/**
 * Represent a deposit.
 */
public interface Deposit {

	/**
	 * Adds the specified amount to this deposit.
	 * 
	 * @param credit
	 *            the amount to be removed.
	 */
	void credit(long credit);

	/**
	 * Removes the specified amount from this deposit.
	 * 
	 * @param debit
	 *            the amount to be removed.
	 */
	void debit(long debit);

	/**
	 * Returns the available amount on this deposit.
	 * 
	 * @return the available amount on this deposit.
	 */
	long getAmount();

}