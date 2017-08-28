package jamel.v170804.models.basicModel3.banks;

import jamel.v170804.models.basicModel3.households.Shareholder;

/**
 * A basic cheque.
 */
class BankCheque implements Cheque {

	// TODO utile ? pnser à essayer plutot de doter la banque d'un compte en
	// bonne et due forme .... ou alors un compte spécial...

	/**
	 * The amount of this cheque.
	 */
	final private long amount;

	/**
	 * The bank.
	 */
	private BasicBank bank;

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
	 * @param bank
	 *            the bank.
	 * @param payee
	 *            the payee.
	 * @param amount
	 *            the amount.
	 * @param issue
	 *            the issue date.
	 */
	BankCheque(BasicBank bank, Shareholder payee, long amount, int issue) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		this.bank = bank;
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

	@Override
	public long getAmount() {
		return this.amount;
	}

	@Override
	public AccountHolder getDrawer() {
		return this.bank;
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
		return (this.bank.getPeriod() == issue) && (this.valid);
	}

}
