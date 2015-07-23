package jamel.basic.util;


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

}

// ***
