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
	 * Returns the principal.
	 * @return the principal.
	 */
	long getPrincipal();

	/**
	 * Return true if the debt is doubtful.
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
