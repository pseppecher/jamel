package jamel.basic.util;

/**
 * Represents a check.
 */
public interface Cheque {

	/**
	 * Returns the amount of the check.
	 * @return the amount of the check.
	 */
	long getAmount();

	/**
	 * Debits the drawer account of the check amount and cancels the check.  
	 * @return <code>true</code> if the payment is accepted, <code>false</code> otherwise.  
	 */
	boolean payment();

}
