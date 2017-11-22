package jamel.util;

import java.io.File;
import java.util.Random;

import jamel.data.Expression;

/**
 * Represents a simulation.
 */
public interface Simulation {

	/**
	 * Brings up a dialog that displays an error message.
	 * 
	 * @param title
	 *            the title string for the dialog.
	 * @param message
	 *            the message to display.
	 */
	void displayErrorMessage(String title, String message);

	/**
	 * Returns an access to the simulation duration.
	 * 
	 * @return an access to the simulation duration.
	 */
	Expression getDuration();

	/**
	 * Returns the scenario file.
	 * 
	 * @return the scenario file.
	 */
	File getFile();

	/**
	 * Returns an access to the simulation free memory.
	 * 
	 * @return an access to the simulation free memory.
	 */
	Expression getFreeMemory();

	/**
	 * Returns some informations about this simulation.
	 * 
	 * @param key
	 *            the key of the information to be returned.
	 * @return some informations about this simulation.
	 */
	String getInfo(String key);

	/**
	 * Returns the model.
	 * 
	 * @return the model.
	 */
	String getModel();

	/**
	 * Returns the name of the simulation.
	 * 
	 * @return the name of the simulation.
	 */
	String getName();

	/**
	 * Returns the current period.
	 * 
	 * @return the current period.
	 */
	int getPeriod();

	/**
	 * Returns a public data.
	 * 
	 * @param key
	 *            the key for the data to be returned.
	 * @return a public data.
	 */
	Double getPublicData(String key);

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	Random getRandom();

	/**
	 * Returns the specified sector.
	 * Used by a sector to get access to another sector.
	 * 
	 * @param name
	 *            the name of the sector to be returned.
	 * @return the specified sector.
	 */
	Sector getSector(String name);

	/**
	 * Returns an access to the simulation speed.
	 * 
	 * @return an access to the simulation speed.
	 */
	Expression getSpeed();

	/**
	 * Returns an access to the current period.
	 * 
	 * @return an access to the current period
	 */
	Expression getTime();

	/**
	 * Returns an access to the simulation total memory.
	 * 
	 * @return an access to the simulation total memory.
	 */
	Expression getTotalMemory();

	/**
	 * Returns <code>true</code> if the circuit is paused, <code>false</code>
	 * otherwise.
	 * 
	 * @return a boolean.
	 */
	boolean isPaused();

	/**
	 * Pauses the simulation.
	 */
	void pause();

	/**
	 * Runs the simulation.
	 */
	void run();

}
