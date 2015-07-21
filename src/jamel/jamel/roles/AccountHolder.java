package jamel.jamel.roles;

import jamel.basic.agent.Agent;

/**
 * Represents an account holder.
 */
public interface AccountHolder extends Agent {

	/**
	 * Notifies the account holder of its bankruptcy.
	 */
	void goBankrupt();

	/**
	 * Returns <code>true</code> if the agent is bankrupted, <code>false</code> otherwise.
	 * @return <code>true</code> if the agent is bankrupted, <code>false</code> otherwise.
	 */
	boolean isBankrupted();

	/**
	 * Returns <code>true</code> if the agent is solvent, <code>false</code> otherwise.
	 * @return <code>true</code> if the agent is solvent, <code>false</code> otherwise.
	 */
	boolean isSolvent();

}

//***
