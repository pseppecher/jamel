package jamel.v170804.models.basicModel4.households;

import jamel.v170804.models.basicModel4.interfaces.AccountHolder;
import jamel.v170804.models.basicModel4.interfaces.Cheque;

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
