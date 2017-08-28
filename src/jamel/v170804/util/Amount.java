package jamel.v170804.util;

import jamel.Jamel;

/**
 * Represents an amount of money or debt.
 * 
 * Always positive.
 */
abstract public class Amount {

	/**
	 * The amount.
	 */
	private long amount = 0;

	/**
	 * Cancels this amount.
	 * 
	 * Sets the amount to zero.
	 */
	protected void cancel() {
		this.amount = 0;
	}

	/**
	 * Removes the specified value from this amount.
	 * 
	 * @param subtrahend
	 *            the amount to be removed.
	 */
	protected void minus(long subtrahend) {
		if (subtrahend < 0) {
			throw new RuntimeException("Negative subtrahend");
		}
		if (subtrahend > this.amount) {
			Jamel.println();
			Jamel.println("subtrahend", subtrahend);
			Jamel.println("this.amount", this.amount);
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
	protected void plus(long addend) {
		if (addend < 0) {
			throw new RuntimeException("Negative addend");
		}
		this.amount += addend;
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

}
