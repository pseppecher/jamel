package jamel.basic.util;

import jamel.jamel.util.AnachronismException;

/**
 * The timer.
 */
public interface Timer {
	
	/**
	 * Adds a {@link TimeListener} to the timer.
	 * @param listener  the {@link TimeListener} to be added.
	 */
	public void addListener(TimeListener listener);

	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public Period getPeriod();

	/**
	 * Detects a possible chronological inconsistency between the current period and the specified value.
	 * <p>
	 * If the value of the current period is not equal to the specified value, a new {@link AnachronismException} is thrown.
	 * @param period the value of the period to be tested.
	 */
	public void checkConsistency(int period);

}

// ***
