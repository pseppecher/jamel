package jamel.util;

/**
 * A basic timer.
 */
public class BasicTimer implements Timer {

	/**
	 * The value of the current period.
	 */
	private int value;

	/**
	 * Creates the timer.
	 * 
	 * @param start
	 *            the starting period.
	 */
	public BasicTimer(int start) {
		this.value = start;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public void next() {
		value++;
	}

}
