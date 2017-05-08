package jamel.basicModel.banks;

/**
 * Encapsulates an amount of money or debt. Always positive.
 */
public class Amount {

	/**
	 * The amount.
	 */
	private long amount = 0;

	/**
	 * Adds the specified value to this amount.
	 * 
	 * @param addend
	 *            the value to be added.
	 */
	public void plus(long addend) {
		if (addend < 0) {
			throw new RuntimeException("Negative addend");
		}
		this.amount += addend;
	}

	/**
	 * Removes the specified value from this amount.
	 * 
	 * @param subtrahend
	 *            the amount to be removed.
	 */
	public void minus(long subtrahend) {
		if (subtrahend < 0) {
			throw new RuntimeException("Negative subtrahend");
		}
		if (subtrahend > this.amount) {
			throw new RuntimeException("subtrahend > amount");
		}
		this.amount -= subtrahend;
	}

	/**
	 * Returns the available amount on this deposit.
	 * 
	 * @return the available amount on this deposit.
	 */
	public long getAmount() {
		return this.amount;
	}

}
