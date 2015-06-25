package jamel.jamel.roles;

import jamel.basic.agent.Agent;

/**
 * Represents an account holder.
 */
public interface AccountHolder extends Agent {

	/**
	 * Returns the total amount of assets owned by this agent.
	 * @return the total amount of assets owned by this agent.
	 */
	long getAssets();
	
	/**
	 * Notifies the account holder of its bankruptcy.
	 */
	void goBankrupt();

	/**
	 * Returns <code>true</code> if the agent is bankrupted, <code>false</code> otherwise.
	 * @return <code>true</code> if the agent is bankrupted, <code>false</code> otherwise.
	 */
	boolean isBankrupted();

}

//***
