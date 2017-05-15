package jamel.basicModel.households;

import jamel.basicModel.banks.AccountHolder;
import jamel.basicModel.banks.Cheque;

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
