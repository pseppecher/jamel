package jamel.v170801.basicModel0.banks;

import jamel.Jamel;

/**
 * A basic account for the basic bank.
 */
class BasicAccount implements Account {

	/*
	 * TODO
	 * Il manque un traitement statistique des opérations du compte, un recueil des données.
	 * Il faut une ouverture du compte, avec remise à 0 des compteurs,
	 * Il faut une fermeture du compte, avec compilation des compteurs dans un dataset à disposition du holder.
	 */

	/**
	 * The bank.
	 */
	private final BasicBank bank;

	/**
	 * The deposit account.
	 */
	private final DepositAccount deposit;

	/**
	 * The account holder.
	 */
	final private AccountHolder holder;

	/**
	 * The loan account.
	 */
	private final LoanAccount loans;

	/**
	 * The pending payment.
	 */
	private Cheque pending = null;

	/**
	 * Creates a basic account.
	 * 
	 * @param bank
	 *            the bank.
	 * @param accountHolder
	 *            the account holder.
	 * 
	 */
	BasicAccount(final BasicBank bank, final AccountHolder accountHolder) {
		this.bank = bank;
		this.holder = accountHolder;
		this.deposit = this.bank.getNewDepositAccount();
		this.loans = this.bank.getNewLoanAccount(this.deposit);
	}

	/**
	 * Recovers due debts.
	 * 
	 * @param penaltyRate
	 *            the penalty rate.
	 */
	void debtRecovery(final double penaltyRate) {
		this.loans.debtRecovery(penaltyRate);
	}

	/**
	 * Returns the holder of this account.
	 * 
	 * @return the holder of this account.
	 */
	AccountHolder getHolder() {
		return this.holder;
	}

	@Override
	public void borrow(long amount, int term, boolean amortizing) {
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		if (term <= 0) {
			throw new RuntimeException("Bad term: " + amount);
		}
		final Loan loan = new BasicLoan(this, amount, this.bank.getRate(), this.bank.getPeriod() + term, amortizing);
		this.loans.add(loan);
	}

	@Override
	public void deposit(final Cheque cheque) {
		if (cheque.getPayee() != this.holder || cheque.getIssue() != this.bank.getPeriod() || !cheque.isValid()) {
			throw new RuntimeException("Bad cheque.");
		}

		if (cheque.getDrawer() == this.bank) {
			// Todo check if this cheque is pending
			((BankCheque) cheque).cancel();
		} else {

			final BasicAccount drawerAccount = ((BasicCheque) cheque).getDrawerAccount();
			if (drawerAccount.pending != cheque) {
				Jamel.println();
				Jamel.println("drawerAccount.pending", drawerAccount.pending);
				Jamel.println("cheque", cheque);
				Jamel.println("drawerAccount.holder", drawerAccount.holder);
				Jamel.println("cheque.getDrawer()", cheque.getDrawer());
				Jamel.println();
				throw new RuntimeException("Bad cheque.");
			}

			drawerAccount.pending = null;
			drawerAccount.deposit.newWithdrawal(cheque.getAmount());
			((BasicCheque) cheque).cancel();
		}
		this.deposit.newDeposit(cheque.getAmount());
	}

	@Override
	public long getAmount() {
		return this.deposit.getAmount();
	}

	@Override
	public long getDebt() {
		return this.loans.getAmount();
	}

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	public int getPeriod() {
		return this.bank.getPeriod();
	}

	@Override
	public Cheque issueCheque(final AccountHolder payee, final long amount) {
		if (this.pending != null) {
			throw new RuntimeException("this.pending should be null");
		}
		if (this.deposit.getAmount() < amount) {
			throw new RuntimeException("Not enough money.");
		}
		this.pending = new BasicCheque(this, payee, amount, this.bank.getPeriod());
		return this.pending;
	}

}
