package jamel.util;

import java.awt.Component;

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

	/**
	 * Returns a time counter (a graphical component that display simulation time).
	 * @return a time counter.
	 */
	public Component getCounter();
	
}

// ***
