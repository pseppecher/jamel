package jamel.models.modelJEE.banks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.AgentDataset;
import jamel.data.BasicAgentDataset;
import jamel.data.DataKeys;
import jamel.models.modelJEE.capital.BasicCapitalStock;
import jamel.models.modelJEE.capital.CapitalStock;
import jamel.models.modelJEE.capital.StockCertificate;
import jamel.models.modelJEE.roles.Corporation;
import jamel.models.modelJEE.roles.Shareholder;
import jamel.models.modelJEE.util.Memory;
import jamel.models.util.Account;
import jamel.models.util.AccountHolder;
import jamel.models.util.Bank;
import jamel.models.util.Cheque;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
 * A bank with a Taylor Rule.
 */
public class BasicBank extends JamelObject implements Bank, Corporation {

	/**
	 * An abstract cheque.
	 */
	private abstract interface AbstractCheque extends Cheque {

		/**
		 * Debits the drawer account of the cheque amount and cancels the
		 * cheque.
		 * 
		 * @return <code>true</code> if the payment is accepted,
		 *         <code>false</code>
		 *         otherwise.
		 */
		boolean payment();

	}

	/**
	 * Represents a current account.
	 */
	private class BasicBankAccount implements Account {

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
			protected int maturityDate;

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

				this.origin = getPeriod();
				this.rate = rate;
				this.maturityDate = getPeriod() + normalTerm;
				this.info = info;

				BasicBankAccount.this.deposit.credit(principal);
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
				BasicBankAccount.this.debt += amount;
				BasicBankAccount.this.newDebt += amount;
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
				BasicBankAccount.this.interestPaid += newInterest;
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
				BasicBankAccount.this.deposit.debit(installment);
				BasicBankAccount.this.debt -= installment;
				BasicBankAccount.this.repaidDebt += installment;
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
					Jamel.println("***");
					Jamel.println("this.principal: " + this.principal);
					Jamel.println("amount: " + amount);
					Jamel.println("***");
					throw new IllegalArgumentException(
							"The amount to be canceled " + "is larger than the principal of this loan.");
				}
				this.principal -= amount;
				BasicBankAccount.this.canceledDebt += amount;
				BasicBankAccount.this.debt -= amount;
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
						BasicBankAccount.this.isDoubtful = true;
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
						BasicBankAccount.this.isDoubtful = true;
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
			 * @param info
			 *            some info about this loan.
			 */
			public NonPerformingLoan(long principal, double rate, String info) {
				super(principal, rate, 1, info);
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
					this.maturityDate = currentPeriod + 1;
				}
			}

		}

		/**
		 * The maximum term of short term loans.
		 */
		private static final int shortTermLimit = 12;

		/** The account holder. */
		private final AccountHolder accountHolder;

		/** A flag that indicates if the account holder is bankrupt or not. */
		private boolean bankrupt = false;

		/** The canceled debt of the period. */
		private long canceledDebt = 0l;

		/** A flag that indicates if the account is canceled or not. */
		private boolean cancelled = false;

		/** Date of creation. */
		private final int creation = getPeriod();

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
				// 2016-04-03: not used.
				throw new RuntimeException("Not used");
				/*if (this.amount > 0) {
					BasicBank2.this.canceledDeposits += this.amount;
					BasicBank2.this.liabilities -= this.amount;
					BasicBank2.this.capital += this.amount;
					Account.this.canceledMoney += this.amount;
					this.amount = 0;
				}*/
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
		private BasicBankAccount(AccountHolder accountHolder) {
			this.accountHolder = accountHolder;
		}

		/**
		 * Cancels the specified amount of debt.
		 * 
		 * @param amount
		 *            the amount of debt to be canceled.
		 */
		private void debtCancellation(final long amount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (amount > debt) {
				Jamel.println("Amount: " + amount);
				Jamel.println("Debt: " + debt);
				throw new RuntimeException("Amount>debt");
			}
			Collections.sort(loans, compareMaturity);
			long remainder = amount;

			/* 2016-04-04
			 * Modification de la procédure.
			 * Les NPL doivent être annulés en priorité.
			 * On travaille maintenant en 2 passes:
			 * 1st pass: non performing loans,
			 * 2nd pass: other loans.
			 */

			// 1st pass: non performing loans

			for (Loan loan : loans) {
				if (loan instanceof NonPerformingLoan) {
					if (loan.getPrincipal() < remainder) {
						remainder -= loan.getPrincipal();
						loan.cancel();
					} else {
						loan.cancel(remainder);
						remainder = 0;
						break;
					}
				}
			}

			// 2nd pass: other loans

			if (remainder > 0) {
				for (Loan loan : loans) {
					if (loan.getPrincipal() > 0) {
						if (loan.getPrincipal() < remainder) {
							remainder -= loan.getPrincipal();
							loan.cancel();
						} else {
							loan.cancel(remainder);
							remainder = 0;
							break;
						}
					}
				}
			}

			if (remainder > 0) {
				Jamel.println("Debt: " + debt);
				Jamel.println("remainder: " + remainder);
				throw new RuntimeException("Remainder>0");
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
				if (loan.getPrincipal() == 0) {
					itr.remove();
				}
			}
			for (Loan loan : this.newLoans) {
				this.loans.add(loan);
			}
			this.newLoans.clear();
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
			this.newLoans.add(new NonPerformingLoan(principal, ratePenalty, "Non Performing Loan"));
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
		public void borrow(long principal, int term, boolean amortized) {
			if (principal <= 0) {
				throw new IllegalArgumentException("Principal must be positive.");
			}
			final Loan newLoan;
			if (amortized) {
				newLoan = new AmortizingLoan(principal, rateNormal, term, "Amortized");
			} else {
				newLoan = new NonAmortizingLoan(principal, rateNormal, term, "Non Amortized");
			}
			this.newLoans.add(newLoan);
		}

		@Override
		public void cancelDebt(long amount) {
			Jamel.notUsed();
		}

		/**
		 * Closes the account at the end of the period.
		 */
		public void close() {
			if (!open) {
				throw new RuntimeException("This account is already closed.");
			}
			open = false;
		}

		@Override
		public void deposit(Cheque cheque) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (((AbstractCheque) cheque).payment()) {
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
		public long getDebt() {
			return this.debt;
		}

		@Override
		public long getDebtService() {
			Jamel.notUsed();
			return 0;
		}

		@Override
		public long getInterests() {
			return this.interestPaid;
		}

		@Override
		public long getLongTermDebt() {
			long longTermDebt = 0;
			final int now = getPeriod();
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
		public long getOverdueDebt() {
			Jamel.notYetImplemented();
			return 0;
		}

		@Override
		public float getRealRate() {
			final float result;
			if (realRate != null) {
				result = realRate;
			} else {
				result = rateNormal;
			}
			return result;
		}

		@Override
		public long getRepaidDebt() {
			return this.repaidDebt;
		}

		@Override
		public long getShortTermDebt() {
			long shortTermDebt = 0;
			final int now = getPeriod();
			for (Loan loan : loans) {
				if (loan.getMaturity() <= now + shortTermLimit) {
					shortTermDebt += loan.getPrincipal();
				}
			}
			return shortTermDebt;
		}

		@Override
		public Cheque issueCheque(final AccountHolder payee, final long amount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			if (amount <= 0) {
				throw new RuntimeException("Negative amount.");
			}
			return new AbstractCheque() {

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
					if (BasicBankAccount.this.getAmount() >= amount) {
						BasicBankAccount.this.deposit.debit(amount);
						this.paid = true;
						result = true;
					} else {
						result = false;
						throw new RuntimeException("Bad cheque: " + amount + "; Non-sufficient funds: "
								+ BasicBankAccount.this.getAmount());
					}
					return result;
				}

			};
		}

		/**
		 * Called at the beginning of the period.
		 */
		public void open() {
			if (this.open) {
				throw new RuntimeException("This account is already open.");
			}
			if (this.cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			if (this.period == null) {
				this.period = getPeriod();
			} else {
				this.period++;
				if (this.period != getPeriod()) {
					throw new RuntimeException();
				}
			}
			this.isDoubtful = false;
			this.open = true;
			this.repaidDebt = 0;
			this.interestPaid = 0;
			this.newDebt = 0;
			this.canceledDebt = 0;
			// this.canceledMoney = 0;
			// this.longTermDebt = null;
			// this.shortTermDebt = null;
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

	/**
	 * The data keys.
	 */
	private static final BasicBankKeys keys = BasicBankKeys.getInstance();

	/**
	 * The max value of liabilities (to avoid overflow).
	 */
	private static final long MAX_VALUE = (long) (0.95 * Long.MAX_VALUE);

	@SuppressWarnings("javadoc")
	private final static String PATIENCE = "patience";

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static public Consumer<? super Agent> getAction(final String phaseName) {

		final Consumer<? super Agent> action;

		switch (phaseName) {
		case "opening":
			action = (agent) -> {
				((BasicBank) agent).openning();
			};
			break;
		case "pay_dividend":
			action = (agent) -> {
				((BasicBank) agent).payDividend();
			};
			break;
		case "debt_recovery":
			action = (agent) -> {
				((BasicBank) agent).debtRecovery();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicBank) agent).closure();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	static public DataKeys getDataKeys() {
		return keys;
	}

	/** The list of customers accounts. */
	private final List<BasicBankAccount> accounts = new ArrayList<BasicBankAccount>(1000);

	/**
	 * The total assets of this bank.
	 */
	private long assets;

	/**
	 * The banking sector.
	 */
	private final Sector bankingSector;

	/**
	 * The number of bankruptcies for the current period.
	 */
	private long bankruptcies;

	/**
	 * The amount of debt cancelled for the current period.
	 */
	private long canceledDebts;

	/**
	 * The amount of capital of this bank.
	 */
	private long capital;

	private final float capitalRatioTarget;

	/** The capital stock. */
	private CapitalStock capitalStock;

	/**
	 * The current period.
	 */
	private Integer currentPeriod = null;

	/** The data of the agent. */
	private AgentDataset dataset = new BasicAgentDataset(this, keys);

	/**
	 * The amount of dividends paid by this bank for the current period.
	 */
	private long dividends;

	/**
	 * The value of the firms sold in the case of foreclosure since the
	 * beginning of the period.
	 */
	private long foreclosureVal = 0;

	/**
	 * The inflation rate.
	 */
	private Double inflation = null;

	/**
	 * The capital at the beginning of the period.
	 */
	private long initialCapital;

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
	private Long longTermLoans = null;

	/** The sector name. */
	private final String name;

	/**
	 * The memory of past net profits.
	 */
	private Memory<Long> netProfitMemory = new Memory<Long>(12);

	/**
	 * The amount of new loans issued by this bank for the current period.
	 */
	private long newLoansAmount;

	/**
	 * A flag that indicates whether the ownership of this bank is distributed
	 * or not.
	 */
	private boolean ownership = false;

	final private Parameters parameters;

	final private float patience;

	/**
	 * The risk premium on doubtful debt.
	 */
	private final float penaltyPremium;

	private final float propensityToDistributeExcessInCapital;

	/**
	 * The rate of interest on bank loans (nominal)
	 */
	private Float rateNormal = null;

	/**
	 * The rate of interest on doubtful loans (=normal+premium)
	 */
	private Float ratePenalty = null;

	/**
	 * The real rate of interest
	 */
	private Float realRate = null;

	/**
	 * Recapitalization rate (for insolvent firms)
	 */
	private final float recapitalizationTargetDebtRatio;

	/**
	 * The total amount of short term loans.
	 */
	private Long shortTermLoans = null;

	/**
	 * 
	 */
	private final double taylorCoef;

	/**
	 * 
	 */
	private final double taylorTarget;

	/**
	 * Creates a new banking sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param bankingSector
	 *            the banking sector.
	 */
	public BasicBank(String name, Sector bankingSector) {
		super(bankingSector.getSimulation());
		this.name = name;
		this.bankingSector = bankingSector;
		this.parameters = this.bankingSector.getParameters();

		this.recapitalizationTargetDebtRatio = this.parameters.getFloat("recapitalization.debtRatio.target");
		this.taylorCoef = this.parameters.getFloat("taylorRule.coef");
		this.taylorTarget = this.parameters.getFloat("taylorRule.target");
		this.penaltyPremium = this.parameters.getFloat("penalty.premium");
		this.patience = this.parameters.getFloat(PATIENCE);
		this.capitalRatioTarget = this.parameters.getFloat(CAPITAL_RATIO);
		this.propensityToDistributeExcessInCapital = this.parameters.getFloat(CAPITAL_PROP_TO_DISTRIBUTE);
	}

	/**
	 * Checks the consistency of the banking sector.
	 * 
	 * @return {@code true} if the banking sector is consistent,
	 *         <code>false</code> otherwise.
	 */
	private boolean checkConsistency() {
		if (this.liabilities > MAX_VALUE) {
			throw new RuntimeException("The total amount of liabilities exceeds the max value.");
		}
		boolean result = true;
		long sumDeposit = 0;
		long sumDebt = 0;
		for (BasicBankAccount account : this.accounts) {
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
	 * Close the bank.
	 * <p>
	 * The data of the period are computed.
	 */
	private void closure() {
		this.capitalStock.close();
		for (BasicBankAccount account : this.accounts) {
			account.close();
		}
		this.netProfitMemory.add(this.capital - this.initialCapital + this.dividends);
		this.updateDataset();
		if (!checkConsistency()) {
			// TODO faut-il vérifier la cohérence
			// automatiquement ? réfléchir à ça.
			throw new RuntimeException("Inconsistency");
		}
		if (this.capital < 0) {
			throw new RuntimeException("Bank Failure");
		}
		this.dataset.close();
	}

	/**
	 * Recovers due debts and interests.
	 */
	private void debtRecovery() {
		Collections.shuffle(accounts, this.getRandom());
		final Iterator<BasicBankAccount> iterAccount = accounts.iterator();
		final int now = this.getPeriod();

		this.shortTermLoans = 0l;
		this.longTermLoans = 0l;
		long totalDebt = 0;

		while (iterAccount.hasNext()) {
			BasicBankAccount account = iterAccount.next();
			account.debtRecovery();
			if (!account.isSolvent()) {
				if (now - account.creation > this.patience) {
					account.bankrupt = true;
				}
			}
			if (account.bankrupt) {
				if (account.isSolvent()) {
					throw new RuntimeException("This account is solvent.");
					// En fait il pourrait y avoir faillite d'un agent
					// solvable, s'il n'est pas liquide.
					// A voir.
				}
				this.bankruptcies++;
				foreclosure(account);
				if (account.bankrupt) {
					iterAccount.remove();
				}
			}
			this.shortTermLoans += account.getShortTermDebt();
			this.longTermLoans += account.getLongTermDebt();
			totalDebt += account.getDebt();
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
	private void foreclosure(BasicBankAccount account) {
		final AccountHolder accountHolder = account.getAccountHolder();
		if (!(accountHolder instanceof Corporation)) {
			throw new RuntimeException("The account holder should be a firm.");
		}
		final Corporation firm = (Corporation) accountHolder;
		if (firm.getSize() > 0) {
			final long firmAssets = firm.getValueOfAssets();
			final long targetedCapital = (long) ((1f - this.recapitalizationTargetDebtRatio) * firmAssets) + 1;
			final long bookValue = firm.getBookValue();
			final long debtToBeCancelled = Math.min(targetedCapital - bookValue, firm.getValueOfLiabilities());
			account.debtCancellation(debtToBeCancelled);
			final Cheque[] cheques;
			try {
				cheques = this.sellCorporation(firm);
			} catch (Exception e) {
				Jamel.println();
				Jamel.println("***");
				Jamel.println();
				Jamel.println("firmAssets = " + firmAssets);
				Jamel.println("debtToBeCancelled = " + debtToBeCancelled);
				Jamel.println("firm.getValueOfLiabilities() = " + firm.getValueOfLiabilities());
				Jamel.println("bookValue = " + bookValue);
				Jamel.println("firm.getBookValue() = " + firm.getBookValue());
				Jamel.println();
				Jamel.println("***");
				Jamel.println();
				throw new RuntimeException("Something went wrong while selling the firm.", e);
			}

			for (Cheque cheque : cheques) {
				this.foreclosureVal += cheque.getAmount();
				((AbstractCheque) cheque).payment();
			}
			account.setBankrupt(false);
		} else {

			// 2016-03-27: ça ne devrait pas arriver, les entreprises n'ont
			// jamais une taille nulle.

			throw new RuntimeException("Not implemented");

			// account.cancel();
			// firm.goBankrupt();

		}
	}

	/**
	 * Returns the doubtful debt amount.
	 * 
	 * @return the doubtful debt amount.
	 */
	private long getDoubtfulDebt() {
		long result = 0;
		for (BasicBankAccount account : accounts) {
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
		final Account account = new Account() {

			@Override
			public void borrow(long principal, int term, boolean amortized) {
				Jamel.notUsed();
			}

			@Override
			public void cancelDebt(long amount) {
				Jamel.notUsed();
			}

			@Override
			public void close() {
				Jamel.notUsed();
			}

			@Override
			public void deposit(Cheque cheque) {
				Jamel.notUsed();
			}

			@Override
			public AccountHolder getAccountHolder() {
				Jamel.notUsed();
				return null;
			}

			@Override
			public long getAmount() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getDebtService() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getInterests() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getLongTermDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getNewDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getOverdueDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public float getRealRate() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getRepaidDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public long getShortTermDebt() {
				Jamel.notUsed();
				return 0;
			}

			@Override
			public Cheque issueCheque(final AccountHolder payee, final long amount) {
				return new AbstractCheque() {
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

			@Override
			public void open() {
				Jamel.notUsed();
			}

		};

		return new BasicCapitalStock(this, size, account, this.getSimulation());
	}

	/**
	 * Opens the bank.
	 */
	private void openning() {
		if (this.currentPeriod == null) {
			this.currentPeriod = this.getPeriod();
		} else {
			this.currentPeriod++;
		}
		this.dataset.open();
		updateOwnership();
		this.initialCapital = this.capital;
		this.capitalStock.open();
		this.bankruptcies = 0;
		this.canceledDebts = 0;
		this.dividends = 0;
		this.interest = 0;
		this.loansRepayment = 0;
		this.newLoansAmount = 0;
		this.shortTermLoans = null;
		this.longTermLoans = null;
		this.foreclosureVal = 0;
		this.updateRates();
		for (BasicBankAccount account : this.accounts) {
			account.open();
		}
	}

	/**
	 * Pays the dividend to its owner.
	 */
	private void payDividend() {
		if (this.assets < 0) {
			throw new RuntimeException("Bad assets value: " + this.assets);
		}
		final long requiredCapital = (long) (this.assets * this.capitalRatioTarget);
		final long excedentCapital = Math.max(0, this.capital - requiredCapital);
		final long dividend = (long) (excedentCapital * this.propensityToDistributeExcessInCapital);

		capitalStock.setDividend(dividend);
		this.dividends = dividend;
	}

	private Cheque[] sellCorporation(Corporation firm) {
		final long firmValue = firm.getBookValue();
		if (firmValue < 0) {
			throw new RuntimeException("firmValue: " + firmValue);
		}
		final List<Agent> all = new LinkedList<Agent>(this.getSector("Households").selectAll());
		final List<Shareholder> buyers = new ArrayList<Shareholder>(10);
		final List<Long> prices = new ArrayList<Long>(10);
		final List<Integer> shares = new ArrayList<Integer>(10);
		long priceOfOneShare = firmValue / 100;
		if (priceOfOneShare < 2) {
			priceOfOneShare = 1;
		}

		class Auctioneer {
			// TODO: It is not really an auctioneer. To be renamed.
			int auction(long sharePrice, long minimalFinancialCapacity) {
				int nonIssuedShares = 100;
				for (Agent shareholder : all) {
					final long shareholderFinancialCapacity = ((Shareholder) shareholder).getFinancialCapacity();
					if (shareholderFinancialCapacity > minimalFinancialCapacity) {

						final int nShares0 = (int) (shareholderFinancialCapacity / sharePrice);
						final int nShares = Math.min(nShares0, nonIssuedShares);
						final long priceOfTheShares = sharePrice * nShares;
						buyers.add((Shareholder) shareholder);
						prices.add(priceOfTheShares);
						shares.add(nShares);
						nonIssuedShares -= nShares;
					}
					if (nonIssuedShares == 0) {
						break;
					}
				}
				return nonIssuedShares;
			}
		}

		final Auctioneer auctioneer = new Auctioneer();

		int nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 10);
		if (nonIssuedShares > 0) {
			buyers.clear();
			prices.clear();
			shares.clear();
			nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 5);
			if (nonIssuedShares > 0) {
				buyers.clear();
				prices.clear();
				shares.clear();
				nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 2);
				if (nonIssuedShares > 0) {
					int count = 0;
					while (true) {
						buyers.clear();
						prices.clear();
						shares.clear();
						nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare);
						if (nonIssuedShares == 0) {
							break;
						}
						priceOfOneShare -= 0.1 * priceOfOneShare;
						if (count > 10 || priceOfOneShare == 0) {
							// On n'a pas réussi à vendre tout le capital de la
							// firme !!!
							// Pourtant on a baissé le prix 10 fois.
							Jamel.println("priceOfOneShare: " + priceOfOneShare);
							throw new RuntimeException("Non issued shares: " + nonIssuedShares);
							// FIXME: il faut implémenter une solution à ce cas.
						}
					}
				}
			}
		}

		if (buyers.size() == 0) {
			throw new RuntimeException("Buyers list is empty: " + firm.getName() + ", period " + getPeriod());
		}

		final StockCertificate[] newShares = firm.getNewShares(shares);
		// Tout le capital est maintenant partagé proportionnellement aux
		// contributions de chacun.

		final Cheque[] cheques = new Cheque[buyers.size()];
		for (int i = 0; i < buyers.size(); i++) {
			cheques[i] = buyers.get(i).buy(newShares[i], prices.get(i));
		}

		return cheques;
	}

	/**
	 * Updates the dataset.
	 */
	private void updateDataset() {

		this.dataset.put(keys.overdueDebt, getDoubtfulDebt());
		// this.newDataset.put(keys.dividends, this.dividends);
		// this.newDataset.put(keys.capital, this.capital);
		this.dataset.put(keys.liabilities, this.liabilities);
		this.dataset.put(keys.assets, this.assets);
		this.dataset.put(keys.equities, this.assets - this.liabilities);
		this.dataset.put(keys.longTermDebt, this.longTermLoans);
		this.dataset.put(keys.shortTermDebt, this.shortTermLoans);
		// this.newDataset.put(keys.bankruptcies, this.bankruptcies);
		this.dataset.put(keys.interests, this.interest);
		// this.newDataset.put(keys.canceledDebts, this.canceledDebts);
		// this.newDataset.put("loans.new", this.newLoansAmount);
		// this.newDataset.put("loans.repayment", this.loansRepayment);
		// this.newDataset.put("foreclosures.val", this.foreclosureVal);
		// this.newDataset.put("netProfit", this.netProfitMemory.getMean());

	}

	/**
	 * Updates the ownership of the bank.
	 */
	private void updateOwnership() {
		if (!ownership) {
			final List<? extends Agent> shareHolders = this.getSector("Households").selectList(10);
			if (shareHolders.size() > 0) {
				this.capitalStock = getNewCapitalStock(shareHolders.size());
				List<StockCertificate> truc = this.capitalStock.getCertificates();
				for (int id = 0; id < shareHolders.size(); id++) {
					final StockCertificate certif = truc.get(id);
					final Shareholder shareHolder = (Shareholder) shareHolders.get(id);
					shareHolder.addAsset(certif);
				}
				ownership = true;
			}

		}
	}

	/**
	 * Updates the interest rates according to the Taylor rule.
	 */
	private void updateRates() {

		/*
		 * 2016-03-17 / Calcul du taux d'intéret selon la règle de Taylor.
		 */

		// TODO / A revoir, c'est un peu bricolé tout ça

		this.inflation = this.getPublicData("inflation");
		if (inflation != null && inflation.isNaN()) {
			inflation = null;
		}
		final Double interestRate;
		if (inflation != null) {
			interestRate = taylorCoef * (inflation - taylorTarget);
			if (interestRate > 0) {
				this.rateNormal = (float) (interestRate / 12);
			} else {
				this.rateNormal = 0f;
			}
			this.ratePenalty = this.rateNormal + this.penaltyPremium;
			this.realRate = (float) (this.rateNormal - inflation / 12);
		} else {
			this.rateNormal = 0f;
			this.ratePenalty = 0f;
			this.realRate = null;
		}
		this.dataset.put(keys.nominalRate, this.rateNormal);
		this.dataset.put(keys.inflation, inflation);
		this.dataset.put(keys.realRate, this.realRate);

	}

	@Override
	public void close() {
		Jamel.notUsed();
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Long getBookValue() {
		return this.capital;
	}

	@Override
	public Double getData(int dataIndex, int t) {
		return this.dataset.getData(dataIndex, t);
	}

	@Override
	public Double getData(String dataKey, int t) {
		return this.dataset.getData(dataKey, t);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public StockCertificate[] getNewShares(List<Integer> shares) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public int getSize() {
		Jamel.notUsed();
		return 0;
	}

	@Override
	public long getValueOfAssets() {
		Jamel.notUsed();
		return 0;
	}

	@Override
	public long getValueOfLiabilities() {
		Jamel.notUsed();
		return 0;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void open() {
		Jamel.notUsed();
	}

	@Override
	public Account openAccount(AccountHolder accountHolder) {
		final BasicBankAccount account = new BasicBankAccount(accountHolder);
		accounts.add(account);
		return account;
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
