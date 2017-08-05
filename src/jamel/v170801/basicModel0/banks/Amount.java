package jamel.v170801.basicModel0.banks;

import jamel.Jamel;

/**
 * Encapsulates an amount of money or debt. Always positive.
 */
public class Amount {

	/**
	 * The amount.
	 */
	private long amount = 0;

	/**
	 * Cancels this amount.
	 */
	public void cancel() {
		this.amount = 0;
	}

	/**
	 * Returns the available amount on this deposit.
	 * 
	 * @return the available amount on this deposit.
	 */
	public long getAmount() {
		return this.amount;
	}

	/**
	 * Returns <code>true</code> if this amount is zero.
	 * 
	 * @return <code>true</code> if this amount is zero.
	 */
	public boolean isEmpty() {
		return this.amount == 0;
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
			Jamel.println();
			Jamel.println("subtrahend",subtrahend);
			Jamel.println("this.amount",this.amount);
			Jamel.println();
			throw new RuntimeException("subtrahend > amount");
		}
		this.amount -= subtrahend;
	}

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

}
