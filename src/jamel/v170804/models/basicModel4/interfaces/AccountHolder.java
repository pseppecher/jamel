package jamel.v170804.models.basicModel4.interfaces;

/**
 * Represents an account holder.
 */
public interface AccountHolder {

	@SuppressWarnings("javadoc")
	long getAssetTotalValue();

	@SuppressWarnings("javadoc")
	int getBorrowerStatus();

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
	 * Returns {@code true} if the agent is solvent, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the agent is solvent, {@code false}
	 *         otherwise.
	 */
	boolean isSolvent();

}