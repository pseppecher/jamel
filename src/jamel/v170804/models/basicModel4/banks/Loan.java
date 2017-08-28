package jamel.v170804.models.basicModel4.banks;

/**
 * Represents a bank loan.
 */
interface Loan {

	/**
	 * Returns the current amount of the loan (its principal).
	 * 
	 * @return the current amount of the loan.
	 */
	long getAmount();

	/**
	 * Returns {@code true} if the loan is empty (if its principal is 0).
	 * 
	 * @return {@code true} if the loan is empty.
	 */
	boolean isEmpty();

	/**
	 * Repays the loan.
	 */
	void repay();

}
