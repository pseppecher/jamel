package jamel.jamel.banks;

/**
 * Represents a loan.
 */
interface Loan {

	/**
	 * Cancels the loan.
	 */
	void cancel();

	/**
	 * Cancels partially this loan.
	 * 
	 * @param amount
	 *            the amount to be cancelled.
	 */
	void cancel(long amount);

	/**
	 * Returns a String that contains some info about this loan.
	 * @return a String that contains some info about this loan.
	 */
	String getInfo();

	/**
	 * Returns the maturity date of this loan.
	 * 
	 * @return the maturity date of this loan.
	 */
	int getMaturity();

	/**
	 * Returns the period when the loan was taken out.
	 * 
	 * @return the period when the loan was taken out.
	 */
	int getOrigin();

	/**
	 * Returns the principal.
	 * 
	 * @return the principal.
	 */
	long getPrincipal();

	/**
	 * Pays back the loan.
	 */
	void payBack();

}

// ***
