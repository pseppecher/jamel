package jamel.v170804.models.basicModel1.banks;

/**
 * A deposit account.
 */
class DepositAccount {

	/**
	 * The total amount of deposits at the bank level.
	 */
	private final Amount bankDeposits;

	/**
	 * The amount of this deposit.
	 */
	private final Amount deposit = new Amount();

	/**
	 * Creates a new deposit account.
	 * 
	 * @param bankDeposits
	 *            the amount of deposits at the bank level.
	 */
	DepositAccount(final Amount bankDeposits) {
		this.bankDeposits = bankDeposits;
	}

	/**
	 * Returns the amount of this deposit.
	 * 
	 * @return the amount of this deposit.
	 */
	long getAmount() {
		return this.deposit.getAmount();
	}

	/**
	 * Records a new deposit on this account.
	 * 
	 * @param amount
	 *            the amount to be deposited.
	 */
	void newDeposit(long amount) {
		this.bankDeposits.plus(amount);
		this.deposit.plus(amount);
	}

	/**
	 * Records a new withdrawal from this account.
	 * 
	 * @param amount
	 *            the amount to be withdrawn.
	 */
	void newWithdrawal(long amount) {
		this.bankDeposits.minus(amount);
		this.deposit.minus(amount);
	}

}
