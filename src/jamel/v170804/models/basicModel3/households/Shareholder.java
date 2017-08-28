package jamel.v170804.models.basicModel3.households;

import jamel.v170804.models.basicModel3.banks.AccountHolder;
import jamel.v170804.models.basicModel3.banks.Cheque;

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
