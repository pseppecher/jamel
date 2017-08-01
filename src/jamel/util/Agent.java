package jamel.util;

/**
 * Represents an agent.
 */
public interface Agent {

	/**
	 * Closes this agent.
	 * Must be called at the end of the period.
	 */
	void close();

	/**
	 * Returns the specified data.
	 * 
	 * @param dataKey
	 *            the key of the data to be returned.
	 * @param t
	 *            the period of the data to be returned.
	 * @return the specified data.
	 */
	Double getData(String dataKey, int t);

	/**
	 * Returns the name of the agent.
	 * 
	 * @return the name of the agent.
	 */
	String getName();

	/**
	 * Returns the sector of this agent.
	 * 
	 * @return the sector of this agent
	 */
	Sector getSector();

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	Simulation getSimulation();

	/**
	 * Opens this agent.
	 * Must be called at the beginning of the period.
	 */
	void open();

}
