package jamel;

/**
 * A basic timer.
 */
public class BasicTimer implements Timer {

	/**
	 * The current period.
	 */
	private int currentPeriod;

	/**
	 * Creates the timer.
	 * 
	 * @param start
	 *            the starting period.
	 */
	public BasicTimer(int start) {
		this.currentPeriod = start;
	}

	@Override
	public int getPeriod() {
		return this.currentPeriod;
	}

	@Override
	public void next() {
		currentPeriod++;
	}

}
