package jamel.models.util;

/**
 * A shareholder is an agent that can receives dividends.
 */
public interface Shareholder extends AccountHolder {

	/**
	 * Receives its dividend cheque.
	 * 
	 * @param cheque
	 *            the dividend cheque.
	 */
	void acceptDividendCheque(Cheque cheque);

}
