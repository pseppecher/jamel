package jamel.v170804.models.basicModel4.interfaces;

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