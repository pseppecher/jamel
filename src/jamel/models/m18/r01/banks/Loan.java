package jamel.models.m18.r01.banks;

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
	 * Returns the maturity of this loan, ie, the final payment date of this
	 * loan.
	 * 
	 * @return the maturity of this loan.
	 */
	int getMaturity();

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
