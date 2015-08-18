package jamel.jamel.banks;

/**
 * Represents a loan.
 */
public interface Loan {

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
	 * Returns the maturity date of this loan.
	 * 
	 * @return the maturity date of this loan.
	 */
	int getMaturity();

	/**
	 * Returns the principal.
	 * 
	 * @return the principal.
	 */
	long getPrincipal();

	/**
	 * Return true if the debt is doubtful.
	 * 
	 * @return a boolean.
	 */
	boolean isDoubtfull();

	/**
	 * Pays back the loan.
	 */
	void payBack();

	/**
	 * Pays the interest due.
	 */
	void payInterest();

}

// ***
