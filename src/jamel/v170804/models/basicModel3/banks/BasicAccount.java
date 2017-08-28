package jamel.v170804.models.basicModel3.banks;

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

	private Integer currentPeriod = null;

	/**
	 * The deposit account.
	 */
	final private DepositAccount deposit;

	/**
	 * The account holder.
	 */
	final private AccountHolder holder;

	/**
	 * The loan account.
	 */
	private LoanAccount loans = null;

	/**
	 * The pending payment.
	 */
	private final BasicCheque pendingCheque = new BasicCheque(this);

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
		// this.loans = this.bank.getNewLoanAccount(this.deposit);
	}

	/**
	 * Recovers due debts.
	 * 
	 * @param penaltyRate
	 *            the penalty rate.
	 */
	void debtRecovery(final double penaltyRate) {
		// TODO REMOVE
		Jamel.notUsed();
		this.loans.debtRecovery(penaltyRate);
	}

	DepositAccount getDeposit() {
		return this.deposit;
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
		if (this.loans == null) {
			this.loans = this.bank.getNewLoanAccount(this);
		}
		final Loan loan = new BasicLoan(this, amount, this.bank.getRate(), this.bank.getPeriod() + term, amortizing);
		this.loans.add(loan);
	}

	@Override
	public void deposit(final Cheque cheque) {
		if (cheque.getPayee() != this.holder || cheque.getIssue() != this.bank.getPeriod() || !cheque.isValid()) {
			Jamel.println("cheque.getPayee()", cheque.getPayee());
			Jamel.println("this.holder", this.holder);
			Jamel.println("cheque.getIssue()", cheque.getIssue());
			Jamel.println("this.bank.getPeriod()", this.bank.getPeriod());
			Jamel.println("cheque.isValid()", cheque.isValid());
			throw new RuntimeException("Bad cheque.");
		}

		if (cheque.getDrawer() == this.bank) {
			// TODO check if this cheque is pending
			this.deposit.newDeposit(cheque.getAmount());
			((BankCheque) cheque).cancel();
		} else {
			final BasicAccount drawerAccount = ((BasicCheque) cheque).getDrawerAccount();
			if (drawerAccount.pendingCheque != cheque) {
				Jamel.println();
				Jamel.println("drawerAccount.pending", drawerAccount.pendingCheque);
				Jamel.println("cheque", cheque);
				Jamel.println("drawerAccount.holder", drawerAccount.holder);
				Jamel.println("cheque.getDrawer()", cheque.getDrawer());
				Jamel.println();
				throw new RuntimeException("Bad cheque.");
			}

			drawerAccount.deposit.newWithdrawal(cheque.getAmount());
			this.deposit.newDeposit(cheque.getAmount());
			drawerAccount.pendingCheque.cancel();
		}
	}

	@Override
	public long getAmount() {
		return this.deposit.getAmount();
	}

	@Override
	public long getDebt() {
		final long result;
		if (this.loans == null) {
			result = 0;
		} else {
			result = this.loans.getAmount();
		}
		return result;
	}

	@Override
	public long getDebtService() {
		return this.loans.getDebtService();
	}

	@Override
	public long getInterests() {
		return this.loans.getInterests();
	}

	public int getPeriod() {
		return this.currentPeriod;
	}

	@Override
	public Cheque issueCheque(final AccountHolder payee, final long amount) {
		if (this.deposit.getAmount() < amount) {
			throw new RuntimeException("Not enough money.");
		}
		if (amount <= 0) {
			throw new RuntimeException("Bad amount: " + amount);
		}
		this.pendingCheque.init(payee, amount, this.bank.getPeriod());
		return this.pendingCheque;
	}

	void open() {
		if (this.currentPeriod == null) {
			this.currentPeriod = this.bank.getPeriod();
		} else {
			this.currentPeriod++;
			if (!this.currentPeriod.equals(this.bank.getPeriod())) {
				Jamel.println();
				Jamel.println("this.currentPeriod", this.currentPeriod);
				Jamel.println("this.bank.getPeriod()", this.bank.getPeriod());
				Jamel.println();
				throw new RuntimeException("Inconsistency");
			}
		}
		if (this.loans != null) {
			this.loans.open();
		}
	}

	boolean isSolvent() {
		return holder.isSolvent();
	}

	void bankrupt() {
		final long assets = this.holder.getAssetTotalValue();
		final long debt = this.loans.getAmount();
		if (assets > debt) {
			throw new RuntimeException("Not insolvent");
		}
		final long target = (long) (0.9 * assets);
		final long excess = debt - target;
		this.loans.cancel(excess);
	}

}
