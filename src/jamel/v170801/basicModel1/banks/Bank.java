package jamel.v170801.basicModel1.banks;

/**
 * Represents a bank.
 */
public interface Bank extends AccountHolder {

	/**
	 * Opens a new account for the specified {@link AccountHolder}
	 * 
	 * @param accountHolder
	 *            the {@link AccountHolder}.
	 * @return a new account.
	 */
	Account openAccount(AccountHolder accountHolder);

}