package jamel.v170804.models.basicModel0.households;

import jamel.v170804.models.basicModel0.banks.AccountHolder;
import jamel.v170804.models.basicModel0.banks.Cheque;

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
