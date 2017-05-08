package jamel.basicModel.banks;

import jamel.util.Agent;

/**
 * Represents an account holder.
 */
public interface AccountHolder extends Agent {

	// TODO : à revoir, vérifier l'utilité de toutes ces méthodes.

	/**
	 * Notifies the account holder of its bankruptcy.
	 */
	void goBankrupt();

	/**
	 * Returns <code>true</code> if the agent is bankrupted, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the agent is bankrupted, <code>false</code>
	 *         otherwise.
	 */
	boolean isBankrupted();

	/**
	 * Returns <code>true</code> if the agent is solvent, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the agent is solvent, <code>false</code>
	 *         otherwise.
	 */
	boolean isSolvent();

	@SuppressWarnings("javadoc")
	int getBorrowerStatus();

	@SuppressWarnings("javadoc")
	Long getAssetTotalValue();

	@SuppressWarnings("javadoc")
	void creditNotification(long credit, Agent payer, String reason);

}