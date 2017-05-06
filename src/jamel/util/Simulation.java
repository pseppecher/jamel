package jamel.util;

import java.io.File;
import java.util.Random;

import jamel.data.Expression;

/**
 * Represents a simulation.
 */
public interface Simulation {

	/**
	 * Returns an expression that provides access to the specified simulation
	 * data.
	 * 
	 * @param key
	 *            the description of the data to be returned.
	 * @return an expression that provides access to the specified simulation
	 *         data.
	 */
	Expression getDataAccess(String key);

	/**
	 * Returns the specified numerical expression.
	 * 
	 * @param key
	 *            the description of the expression to be returned.
	 * @return the specified numerical expression.
	 */
	Expression getExpression(String key);

	/**
	 * Returns the scenario file of this simulation.
	 * 
	 * @return the scenario file of this simulation.
	 */
	File getFile();

	/**
	 * Returns the name of the simulation.
	 * 
	 * @return the name of the simulation.
	 */
	String getName();

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	int getPeriod();

	Random getRandom();

	/**
	 * Returns <code>true</code> if this simulation is paused,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this simulation is paused,
	 *         <code>false</code> otherwise.
	 */
	boolean isPaused();

	void pause();

	/**
	 * Runs the simulation.
	 */
	void run();

}
