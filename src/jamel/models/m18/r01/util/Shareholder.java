package jamel.models.m18.r01.util;

import jamel.models.util.AccountHolder;
import jamel.models.util.Cheque;

/**
 * A shareholder is an agent that can receives dividends.
 * 
 * 2018-02-16: m18.q04.util.Shareholder.
 * Un nouveau shareholder qui reçoit des titres de propriété.
 */
public interface Shareholder extends AccountHolder {

	/**
	 * Receives a dividend cheque.
	 * 
	 * @param cheque
	 *            the dividend cheque.
	 */
	void acceptDividendCheque(Cheque cheque);

	/**
	 * Receives an ownership title.
	 * 
	 * @param title
	 *            the title.
	 */
	void acceptTitle(Equity title);

}
