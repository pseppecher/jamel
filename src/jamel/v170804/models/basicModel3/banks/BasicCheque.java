package jamel.v170804.models.basicModel3.banks;

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
	private long amount = 0;

	/**
	 * The issue date of this cheque.
	 */
	private int issue = 0;

	/**
	 * The payee.
	 */
	private AccountHolder payee = null;

	/**
	 * <code>true</code> if this cheque is valid.
	 */
	private boolean valid = false;

	/**
	 * Creates a basic cheque.
	 * 
	 * @param account
	 *            the drawer account.
	 */
	BasicCheque(BasicAccount account) {
		this.account = account;
	}

	/**
	 * Cancels this cheque.
	 */
	void cancel() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		this.payee = null;
		this.amount = 0;
		this.issue = 0;
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

	/**
	 * Initializes the cheque for a new payment.
	 * 
	 * @param newPayee
	 *            the payee.
	 * @param newAmount
	 *            the amount.
	 * @param newIssue
	 *            the issue date.
	 */
	void init(final AccountHolder newPayee, final long newAmount, final int newIssue) {
		if (this.payee != null) {
			throw new RuntimeException("Not null.");
		}
		if (newAmount <= 0) {
			throw new RuntimeException("Bad amount: " + newAmount);
		}
		this.payee = newPayee;
		this.amount = newAmount;
		this.issue = newIssue;
		this.valid = true;
	}

	@Override
	public long getAmount() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		return this.amount;
	}

	@Override
	public AccountHolder getDrawer() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		return this.account.getHolder();
	}

	@Override
	public int getIssue() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		return this.issue;
	}

	@Override
	public AccountHolder getPayee() {
		if (!this.valid) {
			throw new RuntimeException("Not valid");
		}
		return this.payee;
	}

	@Override
	public boolean isValid() {
		return (this.account.getPeriod() == issue) && (this.valid);
	}

}
