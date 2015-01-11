package jamel.basic.agents.roles;

import jamel.basic.data.dataSets.AgentDataset;

/**
 * Represents an agent.
 */
public interface Agent {
	
	/**
	 * Executes an instruction.
	 * @param instruction the instruction to be executed.
	 * @param args some additional arguments.
	 * @return the result of the action.
	 * @since 22-11-2014
	 */
	Object execute(String instruction, Object... args);

	/**
	 * Returns the data of the agent.
	 * @return the data of the agent.
	 */
	AgentDataset getData();

	/**
	 * Returns the name of the agent.
	 * @return the name of the agent.
	 */
	String getName();

}

//***
