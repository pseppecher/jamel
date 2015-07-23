package jamel.basic;

import jamel.basic.sector.Sector;
import jamel.basic.util.Period;
import jamel.basic.util.Timer;

import java.util.Random;

/**
 * Defines the interface for the macro-economic circuit.
 */
public interface Circuit {

	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	Period getCurrentPeriod();

	/**
	 * Returns the random.
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns the specified sector.
	 * Used by a sector to get access to another sector.
	 * @param name the name of the sector to be returned.
	 * @return the specified sector.
	 */
	Sector getSector(String name);

	/**
	 * Returns the simulation ID.
	 * @return the simulation ID.
	 */
	long getSimulationID();

	/**
	 * Returns the timer.
	 * @return the timer.
	 */
	Timer getTimer();

	/**
	 * Returns <code>true</code> if the circuit is paused, <code>false</code> otherwise. 
	 * @return a boolean.
	 */
	boolean isPaused();

	/**
	 * Changes the state of the simulation.
	 * @param b a boolean. 
	 * If <code>true</code>, the simulation will be paused. 
	 * If <code>false</code>, the simulation will run.
	 */
	void pause(boolean b);

	/**
	 * Runs the simulation.
	 */
	void run();

	/**
	 * Displays a warning message.
	 * @param message the warning message to display. 
	 * @param toolTipText the tool tip text.
	 */
	void warning(String message,String toolTipText);

	/**
	 * Returns the name of the simulation.
	 * @return the name of the simulation.
	 */
	String getName();

}

// ***
