package jamel.util;

import jamel.data.Expression;

/**
 * Represents a sector.
 */
public interface Sector {

	/**
	 * Returns an expression that provides an access to the specified data.
	 * 
	 * @param args
	 *            the arguments specifying the data to be accessed through the
	 *            expression.
	 * @return an expression.
	 */
	Expression getDataAccess(String[] args);

	/**
	 * Returns the name of this sector.
	 * 
	 * @return the name of this sector.
	 */
	String getName();

	/**
	 * Returns the specified phase.
	 * 
	 * @param name
	 *            the name of the phase to be returned.
	 * 
	 * @return the specified phase.
	 */
	Phase getPhase(String name);

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	Simulation getSimulation();

}
