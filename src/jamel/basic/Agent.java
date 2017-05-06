package jamel;

/**
 * Represents an agent.
 */
public interface Agent {

	/**
	 * Returns the specified data.
	 * 
	 * @param dataKey
	 *            the key of the data to be returned.
	 * @param period
	 *            the period of the data to be returned.
	 * @return the specified data.
	 */
	Double getData(String dataKey, String period);

	/**
	 * Returns the name of the agent.
	 * 
	 * @return the name of the agent.
	 */
	String getName();

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	Simulation getSimulation();

}
