package jamel.basicModel.banks;

/**
 * A basic cheque.
 */
class BasicCheque implements Cheque {

	/**
	 * The drawer account.
	 */
	final private BasicAccount account;

	/**
	 * The amount of this cheque.
	 */
	final private long amount;

	/**
	 * The issue date of this cheque.
	 */
	final private int issue;

	/**
	 * The payee.
	 */
	final private AccountHolder payee;

	/**
	 * <code>true</code> if this cheque is valid.
	 */
	private boolean valid = true;

	/**
	 * Creates a basic cheque.
	 * 
	 * @param account
	 *            the drawer account.
	 * @param payee
	 *            the payee.
	 * @param amount
	 *            the amount.
	 * @param issue
	 *            the issue date.
	 */
	BasicCheque(BasicAccount account, AccountHolder payee, long amount, int issue) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		this.account = account;
		this.payee = payee;
		this.amount = amount;
		this.issue = issue;
	}

	/**
	 * Cancels this cheque.
	 */
	void cancel() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		this.valid = false;
	}

	/**
	 * Returns the drawer account.
	 * 
	 * @return the drawer account.
	 */
	BasicAccount getDrawerAccount() {
		return this.account;
	}

	@Override
	public long getAmount() {
		return this.amount;
	}

	@Override
	public AccountHolder getDrawer() {
		return this.account.getHolder();
	}

	@Override
	public int getIssue() {
		return this.issue;
	}

	@Override
	public AccountHolder getPayee() {
		return this.payee;
	}

	@Override
	public boolean isValid() {
		return (this.account.getPeriod() == issue) && (this.valid);
	}

}
