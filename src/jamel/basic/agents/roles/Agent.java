package jamel.basic.agents.roles;

import jamel.basic.data.AgentDataset;

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
	 * Returns the data of the agent.
	 * @return the data of the agent.
	 */
	AgentDataset getData();

	/**
	 * Forces the agent to update its parameters.
	 */
	void updateParameters();

}

//***
