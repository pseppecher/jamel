package jamel.v170804.models.basicModel1.banks;

/**
 * Represents an account holder.
 */
public interface AccountHolder {

	@SuppressWarnings("javadoc")
	Long getAssetTotalValue();

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
	 * Returns <code>true</code> if the agent is solvent, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if the agent is solvent, <code>false</code>
	 *         otherwise.
	 */
	boolean isSolvent();

}