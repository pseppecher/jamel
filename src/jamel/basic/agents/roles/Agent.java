package jamel.basic.agents.roles;

/**
 * Represents an agent.
 */
public interface Agent {
	
	/**
	 * Returns the name of the agent.
	 * @return the name of the agent.
	 */
	String getName();

	/**
	 * Returns the value to which the specified key is associated, or <code>null</code> if this agent contains no value for the key. 
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is associated, or <code>null</code> if this agent contains no value for the key.
	 */
	Double getData(String key);

	/**
	 * Forces the agent to update its parameters.
	 */
	void updateParameters();

}
