package jamel.v170801.basicModel1.households;

import jamel.v170801.basicModel1.banks.AccountHolder;
import jamel.v170801.basicModel1.banks.Cheque;

/**
 * Represents a shareholder.
 */
public interface Shareholder extends AccountHolder {

	/**
	 * Receives a dividend cheque.
	 * 
	 * @param cheque
	 *            the dividend cheque.
	 */
	void acceptDividendCheque(Cheque cheque);

}
