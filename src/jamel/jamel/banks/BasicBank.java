package jamel.jamel.banks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jamel.Jamel;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.capital.BasicCapitalStock;
import jamel.jamel.capital.CapitalStock;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.Firm;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Chequable;
import jamel.jamel.widgets.Cheque;

/**
 * A basic bank.
 */
class BasicBank implements Bank, Corporation {

	/**
	 * Represents a current account.
	 */
	private class Account implements BankAccount {

		/**
		 * An abstract loan.
		 */
		private abstract class AbstractLoan implements Loan {
			
			/**
			 * Some info about this loan.
			 */
			private final String info;

			/**
			 * The remainder of the interest.
			 */
			private double remainder = 0;

			/** The maturity date. */
			protected final int maturityDate;

			/**
			 * The period when the principal was taken out.
			 */
			protected final int origin;

			/** The remaining principal. */
			protected long principal = 0;

			/** The interest rate. */
			protected final double rate;

			/**
			 * Creates a new loan.
			 * 
			 * @param principal
			 *            the principal.
			 * @param rate
			 *            the normal rate of interest.
			 * @param normalTerm
			 *            the normal term.
			 * @param info
			 *            some info about this loan.
			 */
			public AbstractLoan(long principal, double rate, int normalTerm, String info) {

				if (!open) {
					throw new RuntimeException("This account is closed.");
				}
				if (cancelled) {
					throw new RuntimeException("This account is cancelled.");
				}

				this.origin = timer.getPeriod().intValue();
				this.rate = rate;
				this.maturityDate = timer.getPeriod().intValue() + normalTerm;
				this.info = info;

				Account.this.deposit.credit(principal);
				this.newDebt(principal);
			}

			/**
			 * Adds the specified amount to the principal of this loan.
			 * 
			 * @param amount
			 *            the amount to be added.
			 */
			private void newDebt(long amount) {
				this.principal += amount;
				Account.this.debt += amount;
				Account.this.newDebt += amount;
				BasicBank.this.assets += amount;
				BasicBank.this.capital += amount;
				BasicBank.this.newLoansAmount += amount;
			}

			/**
			 * Increases the principal of this loan by the interest due.
			 * 
			 * @return the amount of the interest due.
			 */
			protected long newInterest() {
				final double newInterest0 = this.principal * this.rate + remainder;
				final long newInterest = (long) newInterest0;
				this.remainder = newInterest - newInterest0;
				this.newDebt(newInterest);
				BasicBank.this.interest += newInterest;
				Account.this.interestPaid += newInterest;
				return newInterest;
			}

			/**
			 * Pays back the specified amount.
			 * 
			 * @param installment
			 *            the amount to be paid.
			 */
			protected void payBack(long installment) {
				if (this.principal < installment) {
					throw new RuntimeException("Inconstency");
				}
				this.principal -= installment;
				Account.this.deposit.debit(installment);
				Account.this.debt -= installment;
				Account.this.repaidDebt += installment;
				BasicBank.this.assets -= installment;
				BasicBank.this.capital -= installment;
				BasicBank.this.loansRepayment += installment;
			}

			@Override
			public void cancel() {
				cancel(this.principal);
			}

			@Override
			public void cancel(long amount) {
				if (amount > this.principal) {
					throw new IllegalArgumentException(
							"The amount to be canceled is larger than the principal of this loan.");
				}
				this.principal -= amount;
				Account.this.canceledDebt += amount;
				Account.this.debt -= amount;
				BasicBank.this.assets -= amount;
				BasicBank.this.capital -= amount;
				BasicBank.this.canceledDebts += amount;
			}

			@Override
			public String getInfo() {
				return this.info;
			}

			@Override
			public int getMaturity() {
				return this.maturityDate;
			}

			@Override
			public int getOrigin() {
				return this.origin;
			}

			@Override
			public long getPrincipal() {
				return this.principal;
			}

		}

		/**
		 * A loan with scheduled periodic payments of both principal and
		 * interest.
		 */
		private class AmortizingLoan extends AbstractLoan {

			/**
			 * Creates a new loan.
			 * 
			 * @param principal
			 *            the principal.
			 * @param rate
			 *            the rate of interest.
			 * @param term
			 *            the term.
			 * @param info
			 *            some info about this loan.
			 */
			public AmortizingLoan(long principal, double rate, int term, String info) {
				super(principal, rate, term, info);
			}

			@Override
			public void payBack() {

				if (currentPeriod > this.maturityDate) {
					throw new RuntimeException("Non-performing loan.");
				}

				final long newInterest = newInterest();

				final int remainingTerm = 1 + this.maturityDate - currentPeriod;
				final long installment = newInterest + (this.principal - newInterest) / remainingTerm;

				if (installment > 0) {
					if (getAmount() < installment) {
						Account.this.isDoubtful = true;
						newNonPerformingLoan(installment - getAmount());
					}
					payBack(installment);
				}

			}

		}

		/**
		 * A type of loan in which payments on the principal are not made, while
		 * interest payments are made regularly.
		 * <p>
		 * As a result, the value of principal does not decrease at all over the
		 * life of the loan. The principal is then paid as a lump sum at the
		 * maturity of the loan.
		 * <p>
		 * Source: http://www.investopedia.com/terms/n/nonamortizing.asp
		 */
		private class NonAmortizingLoan extends AbstractLoan {

			/**
			 * Creates a new loan.
			 * 
			 * @param principal
			 *            the principal.
			 * @param rate
			 *            the rate of interest.
			 * @param term
			 *            the term.
			 * @param info
			 *            some info about this loan.
			 */
			public NonAmortizingLoan(long principal, double rate, int term, String info) {
				super(principal, rate, term, info);
			}

			@Override
			public void payBack() {

				if (currentPeriod > this.maturityDate) {
					throw new RuntimeException("Non-performing loan.");
				}

				final long newInterest = newInterest();

				final long installment;
				if (currentPeriod < this.maturityDate) {
					installment = newInterest;
				} else {
					installment = this.principal;
				}

				if (installment > 0) {
					if (getAmount() < installment) {
						Account.this.isDoubtful = true;
						newNonPerformingLoan(installment - getAmount());
					}
					this.payBack(installment);
				}
			}
		}

		/**
		 * A loan close to being in default.
		 */
		private class NonPerformingLoan extends AbstractLoan {

			/**
			 * Creates a new loan.
			 * 
			 * @param principal
			 *            the principal.
			 * @param rate
			 *            the rate of interest.
			 * @param term
			 *            the term.
			 * @param info
			 *            some info about this loan.
			 */
			public NonPerformingLoan(long principal, double rate, int term, String info) {
				super(principal, rate, term, info);
			}

			@Override
			public void payBack() {

				newInterest();

				final long installment = Math.min(getAmount(), this.principal);
				if (installment > 0) {
					payBack(installment);
				}

				if (this.principal > 0) {
					isDoubtful = true;
				}

				/*
				 * FIXME: what if maturityDate>currentPeriod ? ILLIQUIDITY final
				 * Period current = timer.getPeriod(); if
				 * (!current.isBefore(this.maturityDate) && this.principal != 0)
				 * { bankrupt = true; }
				 */
			}

		}

		/**
		 * The maximum term of short term loans.
		 */
		private static final int shortTermLimit = 12;

		/** The account holder. */
		private final AccountHolder accountHolder;

		/**
		 * A String representation of the state of this account.
		 */
		private StringBuilder accountInfo;

		/** A flag that indicates if the account holder is bankrupt or not. */
		private boolean bankrupt = false;

		/** The canceled debt of the period. */
		private long canceledDebt = 0l;

		/** The canceled money of the period. */
		private long canceledMoney = 0l;

		/** A flag that indicates if the account is canceled or not. */
		private boolean cancelled = false;

		/** Date of creation. */
		private final int creation = timer.getPeriod().intValue();

		/**
		 * The total debt of this account.
		 */
		private long debt = 0;

		/** The deposit. */
		private final Deposit deposit = new Deposit() {

			/** The amount of money. */
			private long amount = 0;

			@Override
			public void cancel() {
				if (this.amount > 0) {
					BasicBank.this.canceledDeposits += this.amount;
					BasicBank.this.liabilities -= this.amount;
					BasicBank.this.capital += this.amount;
					Account.this.canceledMoney += this.amount;
					this.amount = 0;
				}
			}

			@Override
			public void credit(long credit) {
				if (credit <= 0) {
					throw new IllegalArgumentException("Null or negative credit: " + credit);
				}
				this.amount += credit;
				BasicBank.this.liabilities += credit;
				BasicBank.this.capital -= credit;
			}

			@Override
			public void debit(long debit) {
				if (debit <= 0) {
					throw new IllegalArgumentException("Null or negative debit: " + debit + ".");
				}
				if (this.amount < debit) {
					throw new IllegalArgumentException("Not enough money.");
				}
				this.amount -= debit;
				BasicBank.this.liabilities -= debit;
				BasicBank.this.capital += debit;
			}

			@Override
			public long getAmount() {
				if (amount < 0) {
					throw new RuntimeException("Negative deposit.");
				}
				return this.amount;
			}

		};

		/** The interest paid. */
		private long interestPaid = 0;

		/**
		 * A flag that indicates whether this account is doubtful or not.
		 */
		private boolean isDoubtful = false;

		/** The list of loans for this account. */
		private final List<Loan> loans = new LinkedList<Loan>();

		/**
		 * The total debt of the account (equals the sum of the principal of the
		 * loans).
		 */
		// private Long longTermDebt = null;

		/** The new debt of the period. */
		private long newDebt = 0;

		/** The list of the new loans for this account. */
		private final List<Loan> newLoans = new LinkedList<Loan>();

		/** If the account is open. */
		private boolean open = false;

		/** The current period. */
		private Integer period;

		/** The repaid debt. */
		private long repaidDebt = 0;

		/**
		 * Creates a new account.
		 * 
		 * @param accountHolder
		 *            the account holder.
		 */
		private Account(AccountHolder accountHolder) {
			this.accountHolder = accountHolder;
		}

		/**
		 * Definitely closes the account. Cancels all the loans associated with
		 * this account. Called in case of a bankruptcy.
		 */
		private void cancel() {
			if (cancelled) {
				throw new RuntimeException("This account is already cancelled.");
			}
			this.deposit.cancel();
			for (Loan loan : loans) {
				loan.cancel();
			}
			// this.longTermDebt = 0l;
			// this.shortTermDebt = 0l;
			this.loans.clear();
			this.cancelled = true;
		}

		/**
		 * Cancels the specified amount of debt.
		 * 
		 * @param amount
		 *            the amount of debt to be canceled.
		 */
		private void cancelDebt(final long amount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			// this.longTermDebt = 0l;
			// this.shortTermDebt = 0l;
			Collections.sort(loans, compareMaturity);
			long remainder = amount;
			for (Loan loan : loans) {
				if (loan.getPrincipal() <= remainder) {
					remainder -= loan.getPrincipal();
					loan.cancel();
				} else {
					loan.cancel(remainder);
					remainder = 0;
				}
				/*
				 * if (loan.getMaturity() - period > shortTermLimit) {
				 * this.longTermDebt += loan.getPrincipal(); } else {
				 * this.shortTermDebt += loan.getPrincipal(); }
				 */
			}
		}

		/**
		 * Closes the account at the end of the period.
		 */
		private void close() {
			if (!open) {
				throw new RuntimeException("This account is already closed.");
			}
			open = false;
			this.accountInfo.append("Deposit: " + this.getAmount() + "<br/>");
			this.accountInfo.append("Debt: " + this.getDebt() + "<br/>");
			this.accountInfo.append("Account Balance: " + (this.getAmount() - this.getDebt()) + "<br/>");
			this.accountInfo.append("<hr />");
			final int now = timer.getPeriod().intValue();
			for (Loan loan : loans) {
				this.accountInfo.append("Loan: " + loan.getPrincipal() + ", " + (loan.getMaturity() - now) + ", " + loan.getInfo());
				if (loan.getMaturity() > now + shortTermLimit) {
					this.accountInfo.append(" (LT)<br />");
				} else {
					this.accountInfo.append(" (CT)<br />");
				}
			}
		}

		/**
		 * Debt recovery (interest+principal).
		 */
		private void debtRecovery() {

			this.loans.addAll(this.newLoans);
			this.newLoans.clear();

			final Iterator<Loan> itr = this.loans.iterator();
			while (itr.hasNext()) {
				Loan loan = itr.next();
				loan.payBack();
				/*
				 * if (loan.getMaturity() - period > shortTermLimit) {
				 * this.longTermDebt += loan.getPrincipal(); } else {
				 * this.shortTermDebt += loan.getPrincipal(); }
				 */
				if (loan.getPrincipal() == 0) {
					itr.remove();
				}
			}

			for (Loan loan : this.newLoans) {
				/*
				 * if (loan.getMaturity() - period > shortTermLimit) {
				 * this.longTermDebt += loan.getPrincipal(); } else {
				 * this.shortTermDebt += loan.getPrincipal(); }
				 */
				this.loans.add(loan);
			}
			this.newLoans.clear();

			/*
			 * this.shortTermDebt = 0l; this.longTermDebt = 0l; for (Loan loan :
			 * this.newLoans) { if (loan.getMaturity() - period >
			 * shortTermLimit) { this.longTermDebt += loan.getPrincipal(); }
			 * else { this.shortTermDebt += loan.getPrincipal(); } }
			 * 
			 * if (this.longTermDebt + this.shortTermDebt != this.debt) { throw
			 * new RuntimeException("Inconstistency."); }
			 */
		}

		/**
		 * Checks the consistency of the account.
		 * 
		 * @return <code>true</code> if the account is consistent,
		 *         <code>false</code> otherwise.
		 */
		private boolean isConsistent() {
			long sumDebt = 0;
			for (Loan loan : loans) {
				sumDebt += loan.getPrincipal();
			}
			for (Loan loan : newLoans) {
				sumDebt += loan.getPrincipal();
			}
			return sumDebt == this.debt;
		}

		/**
		 * Returns <code>true</code> if the account holder is solvent,
		 * <code>false</code> otherwise.
		 * 
		 * @return a boolean.
		 */
		private boolean isSolvent() {
			return (this.debt == 0 || this.accountHolder.isSolvent());
		}

		/**
		 * Creates a new non-performing loan.
		 * 
		 * @param principal
		 *            the principal.
		 */
		private void newNonPerformingLoan(long principal) {
			this.newLoans.add(new NonPerformingLoan(principal, bankingSector.getParam(PENALTY_RATE),
					bankingSector.getParam(EXTENDED_TERM).intValue(),"Non Performing Loan"));
		}

		/**
		 * Called at the beginning of the period.
		 */
		private void open() {
			if (this.open) {
				throw new RuntimeException("This account is already open.");
			}
			if (this.cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			if (this.period == null) {
				this.period = timer.getPeriod().intValue();
			} else {
				this.period++;
				if (this.period != timer.getPeriod().intValue()) {
					throw new AnachronismException();
				}
			}
			this.isDoubtful = false;
			this.open = true;
			this.repaidDebt = 0;
			this.interestPaid = 0;
			this.newDebt = 0;
			this.canceledDebt = 0;
			this.canceledMoney = 0;
			// this.longTermDebt = null;
			// this.shortTermDebt = null;
			this.accountInfo = new StringBuilder();
			this.accountInfo.append("Account Holder: " + this.accountHolder.getName() + "<br/>");
			this.accountInfo.append("Period: " + timer.getPeriod().intValue() + "<br/>");
		}

		/**
		 * Sets the account bankrupt or not.
		 * 
		 * @param b
		 *            a boolean.
		 */
		private void setBankrupt(boolean b) {
			this.bankrupt = b;
		}

		@Override
		public void deposit(Cheque cheque) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cheque.payment()) {
				this.deposit.credit(cheque.getAmount());
			} else {
				throw new RuntimeException("The payment was refused.");
			}
		}

		/**
		 * Returns the account holder.
		 * 
		 * @return the account holder.
		 */
		@Override
		public AccountHolder getAccountHolder() {
			return accountHolder;
		}

		@Override
		public long getAmount() {
			if (cancelled && this.deposit.getAmount() != 0) {
				throw new RuntimeException("This account is closed but the amount is not 0.");
			}
			return this.deposit.getAmount();
		}

		@Override
		public long getCanceledDebt() {
			return this.canceledDebt;
		}

		@Override
		public long getCanceledMoney() {
			return this.canceledMoney;
		}

		@Override
		public long getDebt() {
			return this.debt;
		}

		@Override
		public String getInfo() {
			return this.accountInfo.toString();
		}

		@Override
		public long getInterest() {
			return this.interestPaid;
		}

		@Override
		public long getLongTermDebt() {
			long longTermDebt = 0;
			final int now = timer.getPeriod().intValue();
			for (Loan loan : loans) {
				if (loan.getMaturity() > now + shortTermLimit) {
					longTermDebt += loan.getPrincipal();
				}
			}
			return longTermDebt;
		}

		@Override
		public long getNewDebt() {
			return this.newDebt;
		}

		@Override
		public long getRepaidDebt() {
			return this.repaidDebt;
		}

		@Override
		public long getShortTermDebt() {
			long shortTermDebt = 0;
			final int now = timer.getPeriod().intValue();
			for (Loan loan : loans) {
				if (loan.getMaturity() <= now + shortTermLimit) {
					shortTermDebt += loan.getPrincipal();
				}
			}
			return shortTermDebt;
		}

		@Override
		public boolean isCancelled() {
			return this.cancelled;
		}

		@Override
		public Cheque newCheque(final long amount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			if (amount<=0) {
				throw new RuntimeException("Negative amount.");
			}
			return new Cheque() {

				/** A flag that indicates if the check is paid. */
				private boolean paid = false;

				@Override
				public long getAmount() {
					return amount;
				}

				@Override
				public boolean payment() {
					if (paid) {
						throw new RuntimeException("Bad cheque: Already paid.");
					}
					final boolean result;
					if (Account.this.getAmount() >= amount) {
						Account.this.deposit.debit(amount);
						this.paid = true;
						result = true;
					} else {
						result = false;
						throw new RuntimeException(
								"Bad cheque: " + amount + "; Non-sufficient funds: " + Account.this.getAmount());
					}
					return result;
				}

				@Override
				public String toString() {
					return "Drawer: " + accountHolder.getName() + ", amount: " + amount;
				}
			};
		}

		@Override
		public void newLoan(long principal, int term, boolean amortized) {
			if (principal<=0) {
				throw new IllegalArgumentException("Principal must be positive.");
			}
			final Loan newLoan;
			if (amortized) {
				newLoan = new AmortizingLoan(principal, bankingSector.getParam(RATE), term, "Amortized");
			} else {
				newLoan = new NonAmortizingLoan(principal, bankingSector.getParam(RATE), term, "Non Amortized");
			}
			this.newLoans.add(newLoan);
		}

		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			final String NEW_LINE = System.getProperty("line.separator");
			result.append(this.getClass().getName() + " {" + NEW_LINE);
			result.append(" Holder: " + this.accountHolder.getName() + NEW_LINE);
			result.append(" Creation: " + creation + NEW_LINE);
			result.append(" Deposit: " + deposit.getAmount() + NEW_LINE);
			result.append(" Debt: " + debt + NEW_LINE);
			result.append("}");
			return result.toString();
		}

	}

	@SuppressWarnings("javadoc")
	private static final String CAPITAL_PROP_TO_DISTRIBUTE = "capital.propensityToDistributeExcess";

	@SuppressWarnings("javadoc")
	private static final String CAPITAL_RATIO = "capital.targetedRatio";

	/**
	 * To compare the loans according to their maturity.
	 */
	private static final Comparator<? super Loan> compareMaturity = new Comparator<Loan>() {

		@Override
		public int compare(Loan l0, Loan l1) {
			final int result;
			if (l0.getMaturity() == l1.getMaturity()) {
				result = 0;
			} else if (l0.getMaturity() > l1.getMaturity()) {
				result = 1;
			} else {
				result = -1;
			}
			return result;
		}

	};

	@SuppressWarnings("javadoc")
	private final static String EXTENDED_TERM = "term.extended";

	/**
	 * The max value of liabilities (to avoid overflow).
	 */
	private static final long MAX_VALUE = (long) (0.95f * Long.MAX_VALUE);

	@SuppressWarnings("javadoc")
	private final static String PATIENCE = "patience";

	@SuppressWarnings("javadoc")
	private final static String PENALTY_RATE = "rate.penalty";

	@SuppressWarnings("javadoc")
	private final static String RATE = "rate.normal";

	/** The list of customers accounts. */
	private final List<Account> accounts = new ArrayList<Account>(1000);

	/**
	 * The total assets of this bank.
	 */
	private long assets;

	/**
	 * The banking sector.
	 */
	private final BankingSector bankingSector;

	/**
	 * The number of bankruptcies for the current period.
	 */
	private long bankruptcies;

	/**
	 * The amount of debt cancelled for the current period.
	 */
	private long canceledDebts;

	/**
	 * The amount of deposits cancelled for the current period.
	 */
	private long canceledDeposits;

	/**
	 * The amount of capital of this bank.
	 */
	private long capital;

	/** The capital stock. */
	private CapitalStock capitalStock;

	/**
	 * The current period.
	 */
	private Integer currentPeriod = null;

	/** The data. */
	private AgentDataset dataset;

	/**
	 * The amount of dividends paid by this bank for the current period.
	 */
	private long dividends;

	/**
	 * The amount of interests paid to this bank for the current period.
	 */
	private long interest;

	/**
	 * The total amount of liabilities of this bank.
	 */
	private long liabilities;

	/**
	 * The amount of loans repaid to this bank for the current period.
	 */
	private long loansRepayment;

	/**
	 * The total amount of long term loans.
	 */
	// private long longTermLoans = 0;

	private Long longTermLoans = null;

	/** The sector name. */
	private final String name;

	/**
	 * The amount of new loans issued by this bank for the current period.
	 */
	private long newLoansAmount;

	/**
	 * A flag that indicates whether the ownership of this bank is distributed
	 * or not.
	 */
	private boolean ownership = false;

	/** The random. */
	private final Random random;

	/**
	 * The total amount of short term loans.
	 */
	private Long shortTermLoans = null;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new banking sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param bankingSector
	 *            the banking sector.
	 * @param random
	 *            the random.
	 * @param timer
	 *            the timer.
	 */
	public BasicBank(String name, BankingSector bankingSector, Random random, Timer timer) {
		this.name = name;
		this.bankingSector = bankingSector;
		this.random = random;
		this.timer = timer;
		this.dataset = new BasicAgentDataset(name);
		this.updateDataset();
	}

	/**
	 * Checks the consistency of the banking sector.
	 * 
	 * @return <code>true</code> if the banking sector is consistent,
	 *         <code>false</code> otherwise.
	 */
	private boolean checkConsistency() {
		if (this.liabilities > MAX_VALUE) {
			throw new RuntimeException("The total amount of liabilities exceeds the max value.");
		}
		boolean result = true;
		long sumDeposit = 0;
		long sumDebt = 0;
		for (Account account : this.accounts) {
			if (account.isConsistent()) {
				sumDeposit += account.getAmount();
				sumDebt += account.getDebt();
			} else {
				result = false;
				break;
			}
		}
		if (result == true) {
			if (sumDeposit != this.liabilities || sumDebt != this.assets
					|| this.assets - this.liabilities != this.capital) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * "Foreclosure is a legal process in which a lender attempts to recover the
	 * balance of a loan from a borrower who has stopped making payments to the
	 * lender by forcing the sale of the asset used as the collateral for the
	 * loan."
	 * 
	 * (ref:
	 * <a href="https://en.wikipedia.org/wiki/Foreclosure">wikipedia.org</a>)
	 * 
	 * @param account
	 *            the bankrupted account.
	 */
	private void foreclosure(Account account) {
		final AccountHolder accountHolder = account.getAccountHolder();
		if (!(accountHolder instanceof Firm)) {
			throw new RuntimeException("The account holder should be a firm.");
		}
		final Firm firm = (Firm) accountHolder;
		if (firm.getSize() > 0) {
			final long firmAssets = firm.getValueOfAssets();
			final long targetedLiabilites=(long) (0.8 * firmAssets);
			//final long targetedLiabilites=Math.max((long) (0.8 * firmAssets),firmAssets-200);
			// TODO: 0.8 should be a parameter;
			final long debtToBeCancelled = firm.getValueOfLiabilities() - targetedLiabilites;
			account.cancelDebt(debtToBeCancelled);
			final Cheque[] cheques;
			try {
				cheques = this.bankingSector.sellCorporation(firm);
			} catch (Exception e) {
				Jamel.println();
				Jamel.println("***********************");
				Jamel.println();
				Jamel.println("firmAssets = " + firmAssets);
				Jamel.println("targetedLiabilites = " + targetedLiabilites);
				Jamel.println("debtToBeCancelled = " + debtToBeCancelled);
				Jamel.println();
				Jamel.println("***********************");
				Jamel.println();
				throw new RuntimeException("Something went wrong while selling the firm.", e);
			}
			double foreclosures = this.dataset.get("foreclosures");
			// TODO: foreclosures should be a field.
			for (Cheque cheque : cheques) {
				foreclosures += cheque.getAmount();
				cheque.payment();
			}
			this.dataset.put("foreclosures", foreclosures);
			account.setBankrupt(false);
			/*
			 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
			 * account.getDebt()) { throw new RuntimeException("Inconsistency");
			 * }
			 */
		} else {
			account.cancel();
			firm.goBankrupt();
			/*
			 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
			 * account.getDebt()) { throw new RuntimeException("Inconsistency");
			 * }
			 */
		}
		/*
		 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
		 * account.getDebt()) { throw new RuntimeException("Inconsistency"); }
		 */
	}

	/**
	 * Returns the doubtful debt amount.
	 * 
	 * @return the doubtful debt amount.
	 */
	private long getDoubtfulDebt() {
		long result = 0;
		for (Account account : accounts) {
			if (account.isDoubtful) {
				result += account.getDebt();
			}
		}
		return result;
	}

	/**
	 * Creates and returns a new capital stock for this bank.
	 * 
	 * @param size
	 *            the number of shareholders.
	 * 
	 * @return a new capital stock for this bank.
	 */
	private CapitalStock getNewCapitalStock(final int size) {
		final Chequable account = new Chequable() {

			@Override
			public Cheque newCheque(final long amount) {
				return new Cheque() {
					private boolean paid = false;

					@Override
					public long getAmount() {
						return amount;
					}

					@Override
					public boolean payment() {
						final boolean result;
						if (!paid && BasicBank.this.capital >= amount) {
							this.paid = true;
							result = true;
							// In this case, there is no deposit to debit:
							// the money corresponding to the dividend will be
							// created by the credit of the owner account.
						} else {
							result = false;
						}
						return result;
					}
				};
			}

		};

		return new BasicCapitalStock(this, size, account, timer);
	}

	/**
	 * Updates the dataset.
	 */
	private void updateDataset() {
		this.dataset.put("doubtfulDebt", getDoubtfulDebt());
		this.dataset.put("dividends", this.dividends);
		this.dataset.put("capital", this.capital);
		this.dataset.put("liabilities", this.liabilities);
		this.dataset.put("assets", this.assets);
		this.dataset.put("loans.longTerm", this.longTermLoans);
		this.dataset.put("loans.shortTerm", this.shortTermLoans);
		this.dataset.put("bankruptcies", this.bankruptcies);
		this.dataset.put("interest", this.interest);
		this.dataset.put("canceledDebts", this.canceledDebts);
		this.dataset.put("canceledDeposits", this.canceledDeposits);
		this.dataset.put("loans.new", this.newLoansAmount);
		this.dataset.put("loans.repayment", this.loansRepayment);
	}

	/**
	 * Updates the ownership of the bank.
	 */
	private void updateOwnership() {
		if (!ownership) {
			final List<Shareholder> shareHolders = this.bankingSector.selectCapitalOwner(10);
			if (shareHolders.size() > 0) {
				this.capitalStock = getNewCapitalStock(shareHolders.size());
				List<StockCertificate> truc = this.capitalStock.getCertificates();
				for (int id = 0; id < shareHolders.size(); id++) {
					final StockCertificate certif = truc.get(id);
					final Shareholder shareHolder = shareHolders.get(id);
					shareHolder.addAsset(certif);
				}
				ownership = true;
			}

		}
	}

	/**
	 * Close the bank.
	 * <p>
	 * The data of the period are computed.
	 */
	@Override
	public void close() {
		this.capitalStock.close();
		for (Account account : this.accounts) {
			account.close();
		}
		this.updateDataset();
		if (!checkConsistency()) { // TODO faut-il v�rifier la coh�rence
			// automatiquement ? r�fl�chir � �a.
			throw new RuntimeException("Inconsistency");
		}
		if (this.capital<0) {
			throw new RuntimeException("Bank Failure");
		}
	}

	/**
	 * Recovers due debts and interests.
	 */
	@Override
	public void debtRecovery() {
		Collections.shuffle(accounts, this.random);
		final Iterator<Account> iterAccount = accounts.iterator();
		final int now = this.timer.getPeriod().intValue();

		this.shortTermLoans = 0l;
		this.longTermLoans = 0l;
		long totalDebt = 0;

		while (iterAccount.hasNext()) {
			Account account = iterAccount.next();
			account.debtRecovery();
			/*
			 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
			 * account.getDebt()) { throw new RuntimeException("Inconsistency");
			 * }
			 */
			if (!account.isSolvent()) {
				if (now - account.creation > this.bankingSector.getParam(PATIENCE)) {
					account.bankrupt = true;
				}
			}
			if (account.bankrupt) {
				if (account.isSolvent()) {
					throw new RuntimeException("This account is solvent.");
					// TODO: en fait il pourrait y avoir faillite d'un agent
					// solvable, s'il n'est pas liquide.
					// TO IMPLEMENT LATER
				}
				this.bankruptcies++;
				foreclosure(account);
				/*
				 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
				 * account.getDebt()) { throw new
				 * RuntimeException("Inconsistency"); }
				 */
				if (account.bankrupt) {
					iterAccount.remove();
					// throw new RuntimeException("Not yet implemented.");
				}
				/*
				 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
				 * account.getDebt()) { throw new
				 * RuntimeException("Inconsistency"); }
				 */
			}
			this.shortTermLoans += account.getShortTermDebt();
			this.longTermLoans += account.getLongTermDebt();
			totalDebt += account.getDebt();
			/*
			 * if (account.getShortTermDebt() + account.getLongTermDebt() !=
			 * account.getDebt()) { throw new RuntimeException("Inconsistency");
			 * }
			 */
		}

		if (totalDebt != this.assets) {
			Jamel.println("shortTermLoans: ", this.shortTermLoans);
			Jamel.println("longTermLoans: ", this.longTermLoans);
			Jamel.println("shortTermLoans+longTermLoans: ", this.shortTermLoans + this.longTermLoans);
			Jamel.println("assets: ", this.assets);
			Jamel.println("totalDebt: ", totalDebt);
			throw new RuntimeException("Inconsistency");
		}
		if (this.shortTermLoans + this.longTermLoans != this.assets) {
			Jamel.println("shortTermLoans: ", this.shortTermLoans);
			Jamel.println("longTermLoans: ", this.longTermLoans);
			Jamel.println("shortTermLoans+longTermLoans: ", this.shortTermLoans + this.longTermLoans);
			Jamel.println("assets: ", this.assets);
			Jamel.println("totalDebt: ", totalDebt);
			throw new RuntimeException("Inconsistency");
		}

	}

	@Override
	public Long getBookValue() {
		return this.capital;
	}

	@Override
	public AgentDataset getData() {
		return this.dataset;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public BankAccount getNewAccount(AccountHolder accountHolder) {
		final Account account = new Account(accountHolder);
		accounts.add(account);
		return account;
	}

	@Override
	public StockCertificate[] getNewShares(List<Integer> shares) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	/**
	 * Opens the sector.
	 */
	@Override
	public void open() {
		if (this.currentPeriod == null) {
			this.currentPeriod = this.timer.getPeriod().intValue();
		} else {
			this.currentPeriod++;
		}
		updateOwnership();
		this.capitalStock.open();
		this.bankruptcies = 0;
		this.canceledDebts = 0;
		this.canceledDeposits = 0;
		this.dividends = 0;
		this.interest = 0;
		this.loansRepayment = 0;
		this.newLoansAmount = 0;
		this.shortTermLoans = null;
		this.longTermLoans = null;
		this.dataset = new BasicAgentDataset(this.name);
		this.dataset.put("foreclosures", 0d);
		for (Account account : this.accounts) {
			account.open();
		}
	}

	/**
	 * Pays the dividend to its owner.
	 */
	@Override
	public void payDividend() {
		if (this.assets < 0) {
			throw new RuntimeException("Bad assets value: " + this.assets);
		}
		final long requiredCapital = (long) (this.assets * bankingSector.getParam(CAPITAL_RATIO));
		final long excedentCapital = Math.max(0, this.capital - requiredCapital);
		final long dividend = (long) (excedentCapital * bankingSector.getParam(CAPITAL_PROP_TO_DISTRIBUTE));

		capitalStock.setDividend(dividend);
		this.dividends = dividend;
	}

}

// ***