package jamel.models.m18.r01.util;

/**
 * An {@code Amount} with public mutators.
 */
public class BasicAmount extends Amount {

	/**
	 * Creates a new empty {@code Amount}.
	 */
	public BasicAmount() {
		super();
	}

	/**
	 * Creates a new {@code Amount} of the specified value.
	 * 
	 * @param value
	 *            the value of the new amount.
	 */
	public BasicAmount(final long value) {
		super();
		this.plus(value);
	}

	/**
	 * Cancels this amount.
	 * 
	 * Sets the amount to zero.
	 */
	@Override
	public void cancel() {
		super.cancel();
	}

	/**
	 * Removes the specified value from this amount.
	 * 
	 * @param subtrahend
	 *            the amount to be removed.
	 */
	@Override
	public void minus(long subtrahend) {
		super.minus(subtrahend);
	}

	/**
	 * Adds the specified value to this amount.
	 * 
	 * @param addend
	 *            the value to be added.
	 */
	@Override
	public void plus(long addend) {
		super.plus(addend);
	}

}
