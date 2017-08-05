package jamel.v170804.models.basicModel1.banks;

/**
 * Represents a cheque.
 */
public interface Cheque {

	/**
	 * Returns the amount of this cheque.
	 * 
	 * @return the amount of this cheque.
	 */
	long getAmount();

	/**
	 * Returns the drawer of this cheque.
	 * 
	 * @return the drawer of this cheque.
	 */
	AccountHolder getDrawer();

	/**
	 * Returns the issue date.
	 * 
	 * @return the issue date.
	 */
	int getIssue();

	/**
	 * Returns the payee.
	 * 
	 * @return the payee.
	 */
	AccountHolder getPayee();

	/**
	 * Returns <code>true</code> if the cheque is valid.
	 * 
	 * @return <code>true</code> if the cheque is valid.
	 */
	boolean isValid();

}
