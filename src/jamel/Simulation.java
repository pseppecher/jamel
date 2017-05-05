package jamel;

import java.io.File;

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

	/**
	 * Returns <code>true</code> if this simulation is paused,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this simulation is paused,
	 *         <code>false</code> otherwise.
	 */
	boolean isPaused();

	/**
	 * Runs the simulation.
	 */
	void run();

	/**
	 * Pauses or resumes this simulation.
	 * 
	 * @param b
	 *            <code>true</code> pauses the simulation, if <code>false</code>
	 *            resumes the simulation.
	 */
	void setPause(boolean b);

}
