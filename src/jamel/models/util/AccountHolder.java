package jamel.models.util;

import jamel.util.Agent;

/**
 * Represents an account holder.
 */
public interface AccountHolder extends Agent {

	/**
	 * Returns <code>true</code> if the agent is solvent, <code>false</code> otherwise.
	 * @return <code>true</code> if the agent is solvent, <code>false</code> otherwise.
	 */
	boolean isSolvent();

}
