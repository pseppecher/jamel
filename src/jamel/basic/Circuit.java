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
	public abstract Period getCurrentPeriod();

	/**
	 * Returns the random.
	 * @return the random.
	 */
	public abstract Random getRandom();

	/**
	 * Returns the specified sector.
	 * Used by a sector to get access to another sector.
	 * @param name the name of the sector to be returned.
	 * @return the specified sector.
	 */
	public abstract Sector getSector(String name);

	/**
	 * Returns the simulation ID.
	 * @return the simulation ID.
	 */
	public abstract long getSimulationID();

	/**
	 * Returns the timer.
	 * @return the timer.
	 */
	public abstract Timer getTimer();

	/**
	 * Returns <code>true</code> if the circuit is paused, <code>false</code> otherwise. 
	 * @return a boolean.
	 */
	public abstract boolean isPaused();

	/**
	 * Runs the simulation.
	 */
	public abstract void run();

	/**
	 * Displays a warning message.
	 * @param message the warning message. 
	 */
	public abstract void warning(String message);

}

// ***
