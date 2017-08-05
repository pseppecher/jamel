package jamel.v170804.util;

/**
 * An interface for the timers.
 */
public interface Timer {

	/**
	 * Increments the current period.
	 */
	void next();

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	int getValue();

}
