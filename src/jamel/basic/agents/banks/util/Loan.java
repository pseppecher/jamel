package jamel.basic.agents.banks.util;

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
	 * Pays back the loan.
	 */
	void payBack();

	/**
	 * Pays the interest due.
	 */
	void payInterest();
	
	/**
	 * Return true if the debt is doubtful.
	 * @return a boolean.
	 */
	boolean isDoubtfull();

}

// ***
