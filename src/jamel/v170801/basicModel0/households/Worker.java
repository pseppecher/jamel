package jamel.v170801.basicModel0.households;

import jamel.v170801.basicModel0.banks.AccountHolder;
import jamel.v170801.basicModel0.banks.Cheque;

/**
 * Represents a worker.
 */
public interface Worker extends AccountHolder {

	/**
	 * Receives its pay cheque.
	 * 
	 * @param cheque
	 *            the pay cheque.
	 */
	void acceptPayCheque(Cheque cheque);

	/**
	 * Works.
	 * 
	 * @return <code>true</code> if the worker works.
	 */
	boolean work();

}
