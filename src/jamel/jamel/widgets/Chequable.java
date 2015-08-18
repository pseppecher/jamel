package jamel.jamel.widgets;

/**
 * Interface for objects (usually accounts) on which cheques can be drawn.
 */
public interface Chequable {

	/**
	 * Creates and returns a new {@link Cheque}.
	 * 
	 * @param amount
	 *            the amount of money to pay.
	 * @return a new cheque.
	 */
	Cheque newCheque(long amount);

}

// ***