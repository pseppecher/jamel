package jamel.v170801.basicModel1.banks;

/**
 * Represents a loan.
 */
interface Loan {

	/**
	 * Cancels partially this loan.
	 * 
	 * @param amount
	 *            the amount to be cancelled.
	 */
	void cancel(long amount);

	/**
	 * Returns the amount of the next installment.
	 * 
	 * @return the amount of the next installment.
	 */
	long getInstallment();

	/**
	 * Returns the amount of the next interest payment.
	 * 
	 * @return the amount of the next interest payment.
	 */
	long getInterest();

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

}

// ***
