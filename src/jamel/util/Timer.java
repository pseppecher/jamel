package jamel.util;


/**
 * The timer.
 */
public interface Timer {
	
	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public Period getPeriod();

	/**
	 * Changes the current period to the next.
	 */
	public void next();
	
}