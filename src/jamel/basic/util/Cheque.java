package jamel.basic.util;

/**
 * Represents a cheque.
 */
public interface Cheque {

	/**
	 * Returns the amount of the cheque.
	 * @return the amount of the cheque.
	 */
	long getAmount();

	/**
	 * Debits the drawer account of the cheque amount and cancels the cheque.  
	 * @return <code>true</code> if the payment is accepted, <code>false</code> otherwise.  
	 */
	boolean payment();

}

// ***
