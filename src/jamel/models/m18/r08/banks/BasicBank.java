package jamel.models.m18.r08.banks;

/*
 * 2018-03-29: jamel/models/m18/r04/banks/BasicBank.java
 * Fusion de BasicBank et BasicBank2.
 * Le comportement de fixation des taux d'intérêt est plus prudent.
 * 
 * jamel/models/m18/r01/banks/BasicBank.java
 * 
 * jamel/models/m18/q04/banks/BasicBank2.java
 * Introduction de titres de propriété.
 * 
 * Radical refactoring (2017-08) with the integration of the definitions of the
 * main banking objects (accounts, loans, deposits...)
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r08.data.AgentDataset;
import jamel.models.m18.r08.data.BasicAgentDataset;
import jamel.models.m18.r08.data.BasicPeriodDataset;
import jamel.models.m18.r08.data.PeriodDataset;
import jamel.models.m18.r08.roles.Bank;
import jamel.models.m18.r08.roles.Shareholder;
import jamel.models.m18.r08.util.Account;
import jamel.models.m18.r08.util.AccountHolder;
import jamel.models.m18.r08.util.Amount;
import jamel.models.m18.r08.util.BasicAmount;
import jamel.models.m18.r08.util.BasicOwnership;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.Equity;
import jamel.util.Agent;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
 * A basic bank with 'derivative control' when updating interest rate.
 */
public class BasicBank extends JamelObject implements AccountHolder, Bank {

	/**
	 * An abstract cheque.
	 */
	private abstract class AbstractCheque extends Amount implements Cheque {

		/**
		 * The issue date of this cheque.
		 */
		protected Integer issue = null;

		/**
		 * The payee.
		 */
		protected AccountHolder payee = null;

		/**
		 * Clears the cheque.
		 */
		protected abstract void clear();

		/**
		 * Initializes and returns this cheque for a new payment.
		 * 
		 * @param newPayee
		 *            the payee.
		 * @param newAmount
		 *            the amount.
		 * @return this cheque.
		 */
		protected Cheque init(final AccountHolder newPayee, final long newAmount) {
			ArgChecks.negativeOr0NotPermitted(newAmount, "newAmount");
			if (this.payee != null || this.getAmount() > 0 || this.issue != null) {
				throw new RuntimeException("Not empty");
			}
			this.payee = newPayee;
			this.plus(newAmount);
			this.issue = BasicBank.this.getPeriod();
			return this;
		}

	}

	/**
	 * A bank cheque.
	 */
	private class BankCheque extends AbstractCheque {

		/**
		 * Should not be called.
		 */
		@Override
		protected void cancel() {
			Jamel.notUsed();
		}

		/**
		 * Clears the cheque
		 */
		@Override
		protected void clear() {
			super.cancel();
			this.issue = null;
			this.payee = null;
		}

	}

	/**
	 * The bank ownership.
	 */
	private class BankOwnership extends BasicOwnership {

		/**
		 * Creates a new ownership for this bank.
		 */
		private BankOwnership() {
			super(BasicBank.this);
		}

		/**
		 * Pays the dividends to the owners.
		 * 
		 * @param amount
		 *            the total amount to be distributed.
		 * @return the effective amount distributed.
		 */
		private long payDividends(long amount) {
			long dividends = 0;
			if (amount > this.size()) {
				for (final Equity title : this.equities) {
					final long newDividend = (amount * title.getValue()) / this.getTotalValue();
					final Shareholder shareholder = title.getOwner();
					shareholder.acceptDividendCheque(BasicBank.this.bankCheque.init(shareholder, newDividend));
					dividends += newDividend;
					if (dividends > amount) {
						throw new RuntimeException("Inconsistency");
					}
				}
			}
			return dividends;
		}

	}

	/**
	 * A basic implementation of {@code Account}.
	 */
	private class BasicAccount implements Account {

		/**
		 * The debt.
		 */
		private class Debt {

			/**
			 * A basic implementation of {@code Loan}.
			 */
			private class BasicLoan extends Amount implements Loan {

				/**
				 * If the loan is amortizing.
				 */
				final private boolean amortizing;

				/**
				 * The interest rate.
				 */
				final private double loanRate;

				/**
				 * The maturity date of this loan.
				 */
				final private int maturityDate;

				/**
				 * Creates a new {@code BasicLoan}.
				 * 
				 * @param amount
				 *            the amount of the loan.
				 * @param term
				 *            the term of the loan.
				 * @param amortizing
				 *            if the loan is amortizing.
				 */
				private BasicLoan(final long amount, final int term, final boolean amortizing) {
					if (term < 0) {
						throw new IllegalArgumentException("Bad term: " + term);
					}
					if (amount <= 0) {
						throw new RuntimeException("Bad amount: " + amount);
					}
					BasicAccount.this.deposit.plus(amount);
					this.plus(amount);
					this.loanRate = BasicBank.this.rateNormal;
					this.maturityDate = BasicBank.this.getPeriod() + term;
					this.amortizing = amortizing;
					Debt.this.loanList.add(this);
					Debt.this.normalDebtAmount.plus(amount);
					BasicBank.this.loans.add(this);
					BasicBank.this.outstandingDebtAmount.plus(amount);
					// ***
					// Debt.this.checkConsistency();
					// BasicBank.this.checkConsistency();
					// ***
				}

				/**
				 * Removes the specified value from this debt.
				 * 
				 * @param subtrahend
				 *            the amount to be removed.
				 */
				@Override
				protected void minus(final long subtrahend) {
					BasicBank.this.outstandingDebtAmount.minus(subtrahend);
					Debt.this.normalDebtAmount.minus(subtrahend);
					super.minus(subtrahend);
				}

				@Override
				public int getMaturity() {
					return this.maturityDate;
				}

				@Override
				public void repay() {
					// TODO Check une fois et une seule par période.
					final long interest = (long) (this.getAmount() * this.loanRate);
					final long principal;
					final long term = this.maturityDate - BasicBank.this.getPeriod();
					if (term == 0) {
						principal = this.getAmount();
					} else if (this.amortizing) {
						principal = this.getAmount() / (term + 1);
					} else {
						principal = 0;
					}
					final long installment = principal + interest;
					if (installment > deposit.getAmount()) {
						if (Debt.this.overdueDebt == null) {
							Debt.this.overdueDebt = new OverdueDebt();
							BasicBank.this.overdueDebts.add(overdueDebt);
						}
						Debt.this.overdueDebt.add(installment - deposit.getAmount());
					}
					BasicAccount.this.deposit.minus(installment);
					this.minus(principal);
					if (this.getAmount() == 0) {
						loanList.remove(this);
					}
					BasicBank.this.installments += installment;
					BasicBank.this.interests += interest;
					BasicBank.this.interestsNormal += interest;
					Debt.this.installments += installment;
					Debt.this.interests += interest;
				}

			}

			/**
			 * Represents the overdue debt.
			 */
			private class OverdueDebt extends Amount implements Loan {

				/**
				 * Adds a new amount of overdue debt.
				 * The same amount is simultaneously credited to the related
				 * deposit.
				 * 
				 * @param newDebt
				 *            the amount of new debt.
				 */
				private void add(long newDebt) {
					BasicAccount.this.deposit.plus(newDebt);
					this.plus(newDebt);
				}

				@Override
				protected void cancel() {
					this.minus(this.getAmount());
				}

				/**
				 * Removes the specified value from this debt.
				 * 
				 * @param subtrahend
				 *            the amount to be removed.
				 */
				@Override
				protected void minus(final long subtrahend) {
					BasicBank.this.overdueDebtAmount.minus(subtrahend);
					super.minus(subtrahend);
				}

				/**
				 * Adds the specified value to this debt.
				 * 
				 * @param addend
				 *            the value to be added.
				 */
				@Override
				protected void plus(long addend) {
					BasicBank.this.overdueDebtAmount.plus(addend);
					super.plus(addend);
				}

				@Override
				public int getMaturity() {
					Jamel.notUsed();
					return 0;
				}

				@Override
				public void repay() {
					// TODO Check une fois et une seule par période.
					final long interest = (long) (this.getAmount() * BasicBank.this.ratePenalty);
					this.plus(interest);
					final long installment = Math.min(this.getAmount(), BasicAccount.this.deposit.getAmount());
					BasicAccount.this.deposit.minus(installment);
					this.minus(installment);
					BasicBank.this.installments += installment;
					BasicBank.this.interests += interest;
					BasicBank.this.interestsOverdue += interest;
					Debt.this.installments += installment;
					Debt.this.interests += interest;
				}

			}

			/**
			 * Total theoretical debt service of the period (principal +
			 * interests).
			 */
			@SuppressWarnings("hiding")
			private long installments = 0;

			/**
			 * Total theoretical interests of the period.
			 */
			@SuppressWarnings("hiding")
			private long interests = 0;

			/**
			 * The loans.
			 */
			private final List<BasicLoan> loanList = new LinkedList<>();

			/**
			 * Total amount of the debt of this account.
			 */
			private final BasicAmount normalDebtAmount = new BasicAmount();

			/**
			 * The overdue debt.
			 */
			private OverdueDebt overdueDebt = null;

			/**
			 * Cancels the specified amount of debt.
			 * 
			 * @param writeOff
			 *            the amount of debt to be cancelled.
			 */
			private void cancel(long writeOff) {

				ArgChecks.negativeOr0NotPermitted(writeOff, "writeOff");
				if (Debt.this.overdueDebt == null) {
					Debt.this.overdueDebt = new OverdueDebt();
					BasicBank.this.overdueDebts.add(overdueDebt);
				}
				if (this.overdueDebt.getAmount() >= writeOff) {
					this.overdueDebt.minus(writeOff);
				} else {
					long remainder = writeOff;
					remainder -= this.overdueDebt.getAmount();
					this.overdueDebt.cancel();
					final List<Loan> cancelled = new LinkedList<>();
					for (final BasicLoan loan : loanList) {
						if (loan.getAmount() >= remainder) {
							loan.minus(remainder);
							if (loan.getAmount() == 0) {
								cancelled.add(loan);
							}
							remainder = 0;
							break;
						}
						remainder -= loan.getAmount();
						loan.minus(loan.getAmount());
						cancelled.add(loan);
					}
					this.loanList.removeAll(cancelled);

					if (remainder > 0) {
						throw new RuntimeException("Remainder should be 0");
					}
				}
				// ***
				// checkConsistency();
				// BasicBank.this.checkConsistency();
				// ***
			}

			/**
			 * For debugging purpose.
			 */
			@SuppressWarnings("unused")
			private void checkConsistency() {
				long sum = 0;
				if (this.overdueDebt != null) {
					sum += this.overdueDebt.getAmount();
				}
				for (Loan loan : this.loanList) {
					sum += loan.getAmount();
				}
				if (this.getAmount() != sum) {
					Jamel.println();
					Jamel.println("this.getAmount()", this.getAmount());
					Jamel.println("sum", sum);
					Jamel.println();
					throw new RuntimeException("Inconsistency");
				}
				Jamel.println("Debt consistency: Ok");
			}

			/**
			 * Returns the amount of the debt (normal debt+overdue debt).
			 * 
			 * @return the amount of the debt.
			 */
			private long getAmount() {
				return this.normalDebtAmount.getAmount() + this.getOverdueDebt();
			}

			/**
			 * Computes and returns the amount of long run debt.
			 * 
			 * @return the amount of long run debt.
			 */
			private long getLongRunAmount() {
				long sum = 0;
				for (Loan loan : this.loanList) {
					if (loan.getMaturity() > getPeriod() + 12) {
						sum += loan.getAmount();
					}
				}
				return sum;
			}

			/**
			 * Returns the amount of the overdue debt.
			 * 
			 * @return the amount of the overdue debt.
			 */
			private long getOverdueDebt() {
				long result = 0l;
				if (this.overdueDebt != null) {
					result += this.overdueDebt.getAmount();
				}
				return result;
			}

			/**
			 * Computes and returns the amount of short run debt.
			 * 
			 * @return the amount of short run debt.
			 */
			private long getShortRun() {
				long sum = 0;
				for (Loan loan : this.loanList) {
					if (loan.getMaturity() <= getPeriod() + 12) {
						sum += loan.getAmount();
					}
				}
				return sum + this.getOverdueDebt();
			}

			/**
			 * Creates a new loan with the specified attributes. The amount of
			 * the loan is simultaneously credited on the deposit account.
			 * 
			 * @param newDebt
			 *            the amount of the loan to be created.
			 * @param term
			 *            the term of the loan.
			 * @param amortizing
			 *            if the loan is amortizing.
			 */
			@SuppressWarnings("unused")
			private void newLoan(long newDebt, int term, boolean amortizing) {
				new BasicLoan(newDebt, term, amortizing);
				// the new loan seems to be unused, but is automatically
				// recorded in the loan list when created.
			}

		}

		/**
		 * A money deposit.
		 */
		private class Deposit extends Amount {

			/**
			 * A basic cheque.
			 */
			private class BasicCheque extends AbstractCheque {

				/**
				 * Clears the cheque
				 */
				@Override
				protected void clear() {
					Deposit.this.minus(getAmount());
					super.cancel();
					this.issue = null;
					this.payee = null;
				}

			}

			/**
			 * The pending payment.
			 */
			private final BasicCheque pendingCheque = new BasicCheque();

			/**
			 * Removes the specified value from this deposit.
			 * 
			 * @param subtrahend
			 *            the amount to be removed.
			 */
			@Override
			protected void minus(final long subtrahend) {
				BasicBank.this.depositsAmount.minus(subtrahend);
				super.minus(subtrahend);
			}

			/**
			 * Adds the specified value to this amount.
			 * 
			 * @param addend
			 *            the value to be added.
			 */
			@Override
			protected void plus(long addend) {
				BasicBank.this.depositsAmount.plus(addend);
				super.plus(addend);
			}

			/**
			 * Deposits a cheque in this account.
			 * 
			 * @param cheque
			 *            the cheque to be deposited.
			 */
			public void deposit(Cheque cheque) {
				if (!(cheque instanceof AbstractCheque)) {
					throw new RuntimeException("Unknown type of cheque: " + cheque.getClass().getName());
				}
				this.plus(cheque.getAmount());
				((AbstractCheque) cheque).clear();
				// ***
				// checkConsistency();
				// ***
			}

			/**
			 * Issues a new cheque.
			 * 
			 * @param payee
			 *            the payee.
			 * @param amount
			 *            the amount of the cheque.
			 * @return a new cheque.
			 */
			public Cheque issueCheque(AccountHolder payee, long amount) {
				return this.pendingCheque.init(payee, amount);
			}

		}

		/**
		 * The debt account.
		 */
		private final Debt debt = new Debt();

		/**
		 * The deposit account.
		 */
		final private Deposit deposit = new Deposit();

		/**
		 * The account holder.
		 */
		final private AccountHolder holder;

		/**
		 * If the account is open.
		 */
		private boolean open = false;

		/**
		 * The current period (used to test if the account has been correctly
		 * closed and open).
		 */
		private Integer period = null;

		/**
		 * Creates a new {@code BasicAccount}.
		 * 
		 * @param holder
		 *            the holder of the new account.
		 */
		private BasicAccount(AccountHolder holder) {
			this.holder = holder;
		}

		@Override
		public void borrow(long amount, int term, boolean amortizing) {
			// ***
			// checkConsistency();
			// ***
			if (amount <= 0) {
				throw new IllegalArgumentException("Bad amount: " + amount);
			}
			if (term < 0) {
				throw new IllegalArgumentException("Bad term: " + term);
			}
			this.debt.newLoan(amount, term, amortizing);
			// ***
			// checkConsistency();
			// ***
		}

		@Override
		public void cancelDebt(long amount) {
			BasicBank.this.debtCancellationCount++;
			BasicBank.this.debtCancellationValue += amount;
			this.debt.cancel(amount);
		}

		@Override
		public void close() {
			if (!this.open) {
				throw new RuntimeException("Already closed.");
			}
			this.open = false;
		}

		@Override
		public void deposit(Cheque cheque) {
			this.deposit.deposit(cheque);
		}

		@Override
		public AccountHolder getAccountHolder() {
			return this.holder;
		}

		@Override
		public long getAmount() {
			return this.deposit.getAmount();
		}

		@Override
		public long getDebt() {
			return this.debt.getAmount();
		}

		@Override
		public long getDebtService() {
			return this.debt.installments;
		}

		@Override
		public long getInterests() {
			return this.debt.interests;
		}

		@Override
		public long getLongTermDebt() {
			return this.debt.getLongRunAmount();
		}

		@Override
		public long getNewDebt() {
			Jamel.notUsed();
			return 0;
		}

		/**
		 * Returns the amount of the overdue debt.
		 * 
		 * @return the amount of the overdue debt.
		 */
		@Override
		public long getOverdueDebt() {
			return this.debt.getOverdueDebt();
		}

		@Override
		public float getRealRate() {
			final float result;
			if (BasicBank.this.realRate != null) {
				result = BasicBank.this.realRate;
			} else {
				result = BasicBank.this.rateNormal;
			}
			return result;
		}

		@Override
		public long getRepaidDebt() {
			Jamel.notUsed();
			return 0;
		}

		@Override
		public long getShortTermDebt() {
			return this.debt.getShortRun();
		}

		@Override
		public Cheque issueCheque(AccountHolder payee, long amount) {
			ArgChecks.negativeOr0NotPermitted(amount, "amount");
			if (amount > this.getAmount()) {
				final String message = "Not enough money";
				Jamel.println("***");
				Jamel.println(message);
				Jamel.println("Cheque amount: " + amount);
				Jamel.println("Account amount: " + this.getAmount());
				Jamel.println();
				throw new RuntimeException(message);
			}
			return this.deposit.issueCheque(payee, amount);
		}

		@Override
		public void open() {
			if (this.period != null) {
				this.period++;
				if (this.period != getPeriod()) {
					throw new RuntimeException("Time inconsistency.");
				}
			}
			if (this.open) {
				throw new RuntimeException("Already open.");
			}
			this.open = true;
			this.debt.installments = 0;
			this.debt.interests = 0;
		}

	}

	/**
	 * The set of data keys.
	 */
	protected static final BankKeys keys = BankKeys.getInstance();

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
		case "payDividends":
			action = (agent) -> {
				((BasicBank) agent).payDividends();
			};
			break;
		case "debtRecovery":
			action = (agent) -> {
				((BasicBank) agent).debtRecovery();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * Returns the data keys.
	 * 
	 * @return the data keys.
	 */
	static public DataKeys getDataKeys() {
		return keys;
	}

	/**
	 * The collection of accounts.
	 */
	private final List<BasicAccount> accounts = new ArrayList<>();

	/**
	 * The bank cheque.
	 */
	final private BankCheque bankCheque = new BankCheque();

	/**
	 * To count the number of debt cancellation since the start of the period.
	 */
	private long debtCancellationCount;

	/**
	 * To compute the total value of debt cancellation since the start of the
	 * period.
	 */
	private long debtCancellationValue;

	/**
	 * Le pas utilisé lors des changements de taux.
	 */
	final private float delta = 0.0025f;

	/**
	 * The amount of outstanding deposits.
	 */
	private final BasicAmount depositsAmount = new BasicAmount();

	/**
	 * The id of this bank.
	 */
	final private int id;

	/**
	 * Total theoretical debt service of the period (principal + interests).
	 */
	private long installments = 0;

	/**
	 * Le taux d'intérêt (nominal) courant.
	 */
	private double interestRate = 0;

	/**
	 * Total theoretical interests of the period.
	 */
	private long interests = 0;

	/**
	 * 
	 */
	private long interestsNormal = 0;

	/**
	 * 
	 */
	private long interestsOverdue = 0;

	/**
	 * The loans.
	 */
	final private List<Loan> loans = new LinkedList<>();

	/**
	 * The amount of outstanding loans.
	 */
	private final BasicAmount outstandingDebtAmount = new BasicAmount();

	/**
	 * The amount of overdue debts.
	 */
	private final BasicAmount overdueDebtAmount = new BasicAmount();

	/**
	 * The overdue debts.
	 */
	final private List<Loan> overdueDebts = new LinkedList<>();

	/**
	 * The ownership of the firm.
	 */
	final private BankOwnership ownership = new BankOwnership();

	/**
	 * Le changement de taux opéré à la période précédente.
	 */
	private double previousChange = 0;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * The agent dataset.
	 */
	protected final AgentDataset agentDataset;

	/**
	 * The constants of the bank.
	 */
	protected final BankConstants cons;

	/**
	 * The period dataset.
	 */
	protected PeriodDataset periodDataset;

	/**
	 * The interest rate.
	 */
	protected float rateNormal = 0;

	/**
	 * The penalty rate, ie the interest rate for overdue debts.
	 */
	protected float ratePenalty = 0;

	/**
	 * The real interest rate.
	 */
	protected Float realRate = 0f;

	/**
	 * Creates a new basic agent.
	 * 
	 * @param sector
	 *            the parent sector.
	 * @param id
	 *            the id of the agent.
	 */
	public BasicBank(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		final Parameters params = this.sector.getParameters();
		ArgChecks.nullNotPermitted(params, "params");
		this.cons = new BankConstants(params);
		this.agentDataset = new BasicAgentDataset(this);
	}

	/**
	 * For debugging purposes.
	 */
	@SuppressWarnings("unused")
	private void checkConsistency() {
		long sumDebts = 0;
		long sumDeposits = 0;
		long sumOverdues = 0;
		long sumInterests = 0;
		long sumInstallments = 0;
		for (BasicAccount account : this.accounts) {
			sumDebts += account.getDebt();
			sumDeposits += account.getAmount();
			sumOverdues += account.getOverdueDebt();
			sumInterests += account.getInterests();
			sumInstallments += account.getDebtService();
		}
		if (sumDebts != this.outstandingDebtAmount.getAmount() + this.overdueDebtAmount.getAmount()
				|| sumDeposits != this.depositsAmount.getAmount() || sumOverdues != this.overdueDebtAmount.getAmount()
				|| sumInterests != this.interests || sumInstallments != this.installments) {
			Jamel.println();
			Jamel.println("Debts", sumDebts,
					this.outstandingDebtAmount.getAmount() + this.overdueDebtAmount.getAmount());
			Jamel.println("Overdue Debts", sumOverdues, this.overdueDebtAmount.getAmount());
			Jamel.println("Deposits", sumDeposits, this.depositsAmount.getAmount());
			Jamel.println("installments", sumInstallments, this.installments);
			Jamel.println("sumInterests", sumInterests, this.interests);
			Jamel.println();
			throw new RuntimeException("Inconsistency");
		}
		Jamel.println("Bank consistency: Ok");
	}

	/**
	 * Recovers due debts.
	 */
	private void debtRecovery() {
		// ***
		// checkConsistency();
		// ***

		final ListIterator<Loan> it1 = this.overdueDebts.listIterator();
		while (it1.hasNext()) {
			final Loan overdueDebt = it1.next();
			overdueDebt.repay();
			// ***
			// checkConsistency();
			// ***
		}

		final ListIterator<Loan> it2 = this.loans.listIterator();
		final int limit = getPeriod() + 12;
		long longTermDebt = 0;
		long shortTermDebt = 0;
		while (it2.hasNext()) {
			final Loan loan = it2.next();
			loan.repay();
			// ***
			// checkConsistency();
			// ***
			if (loan.isEmpty()) {
				it2.remove();
			}

			if (loan.getMaturity() > limit) {
				longTermDebt += loan.getAmount();
			} else {
				shortTermDebt += loan.getAmount();
			}
		}
		shortTermDebt += this.overdueDebtAmount.getAmount();

		this.periodDataset.put(keys.longTermDebt, longTermDebt);
		this.periodDataset.put(keys.shortTermDebt, shortTermDebt);
		// checkConsistency();
	}

	/**
	 * Initializes the owners of this bank.
	 */
	private void initOwners() {
		final Agent[] selection = this.getSimulation().getSector(cons.shareholders).selectArray(10);
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] != null) {
				final Shareholder agent = (Shareholder) selection[i];
				final Equity title = ownership.issue(agent, 1);
				agent.acceptTitle(title);
			}
		}
	}

	/**
	 * The dividend payment phase.
	 */
	private void payDividends() {
		if (this.ownership.isEmpty()) {
			throw new RuntimeException("No owners.");
		}
		final long assets = this.outstandingDebtAmount.getAmount() + this.overdueDebtAmount.getAmount();
		final long liabilities = this.depositsAmount.getAmount();
		final long capital = assets - liabilities;
		final long capitalTarget = (long) (assets * this.cons.capitalTargetRatio);
		final long capitalExcess = Math.max(capital - capitalTarget, 0);
		final long dividends = this.ownership.payDividends(capitalExcess);
		this.periodDataset.put(keys.dividends, dividends);
	}

	/**
	 * Updates the interest rates according to the Taylor rule.
	 */
	protected void updateRates() {

		final Double inflation2 = this.getPublicData("inflation2");
		final Double inflation = this.getPublicData("inflation");

		final double previousRate = this.interestRate;

		if (getPeriod() < this.cons.supervision) {
			this.rateNormal = 0.05f / 12f;
			this.ratePenalty = this.rateNormal + this.cons.penaltyPremium;
			if (inflation2 != null) {
				this.realRate = (float) (this.rateNormal - inflation2);
			}
		} else {

			/*if (inflation2 != null && inflation2.isNaN()) {
				throw new RuntimeException("not a number");
			}*/
			if (inflation != null) {

				final double targetRate = this.cons.taylorCoef * (inflation - this.cons.taylorTarget);

				// inflation en t-1
				final Double previousInflation = this.agentDataset.getData(keys.inflation2, getPeriod() - 1);

				// Si le taux est supérieur à la cible
				if (interestRate + delta <= targetRate && previousChange >= 0) {
					// hausse du taux
					interestRate += delta;
					if (interestRate + delta <= targetRate && inflation2 > previousInflation) {
						interestRate += delta;
					}
				} else // Si le taux est inférieur à la cible
				if (interestRate - delta >= targetRate && previousChange <= 0) {
					// baisse du taux
					interestRate -= delta;
					if (interestRate - delta >= targetRate && inflation2 < previousInflation) {
						interestRate -= delta;
					}
				}

				// On interdit les taux négatifs
				if (interestRate < 0) {
					interestRate = 0;
				}

				// On calcule les taux mensuels.
				if (interestRate > 0) {
					this.rateNormal = (float) (interestRate / 12);
				} else {
					this.rateNormal = 0f;
				}
				this.ratePenalty = this.rateNormal + this.cons.penaltyPremium;
				this.realRate = (float) (this.rateNormal - inflation2);

			} else {
				this.rateNormal = 0f;
				this.ratePenalty = 0f;
				this.realRate = null;
			}
		}

		this.previousChange = this.interestRate - previousRate;

		this.periodDataset.put(keys.inflation2, inflation2);
		this.periodDataset.put(keys.nominalRate, this.rateNormal);
		this.periodDataset.put(keys.inflation, inflation);
		this.periodDataset.put(keys.realRate, this.realRate);
	}

	/**
	 * Closes this agent.
	 */
	@Override
	public void close() {

		final long assets = this.outstandingDebtAmount.getAmount() + this.overdueDebtAmount.getAmount();
		final long liabilities = this.depositsAmount.getAmount();

		this.ownership.updateValue(assets - liabilities);

		this.periodDataset.put(keys.count, 1);
		this.periodDataset.put(keys.assets, assets);
		this.periodDataset.put(keys.liabilities, liabilities);
		this.periodDataset.put(keys.equities, assets - liabilities);
		this.periodDataset.put(keys.installments, this.installments);
		this.periodDataset.put(keys.interests, this.interests);
		this.periodDataset.put(keys.interestsNormal, this.interestsNormal);
		this.periodDataset.put(keys.interestsOverdue, this.interestsOverdue);
		this.periodDataset.put(keys.overdueDebt, this.overdueDebtAmount.getAmount());
		this.periodDataset.put(keys.debtCancellationCount, this.debtCancellationCount);
		this.periodDataset.put(keys.debtCancellationValue, this.debtCancellationValue);
		this.agentDataset.put(periodDataset);
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Double getData(int dataIndex, int t) {
		return this.agentDataset.getData(dataIndex, t);
	}

	@Override
	public String getName() {
		return "Bank " + this.id;
	}

	@Override
	public boolean isSolvent() {
		Jamel.notUsed();
		return false;
	}

	/**
	 * Opens this agent.
	 */
	@Override
	public void open() {
		if (this.ownership.isEmpty()) {
			initOwners();
		}
		this.interests = 0;
		this.interestsNormal = 0;
		this.interestsOverdue = 0;
		this.installments = 0;
		this.debtCancellationCount = 0;
		this.debtCancellationValue = 0;
		this.periodDataset = new BasicPeriodDataset(this);
		this.updateRates();
	}

	@Override
	public Account openAccount(final AccountHolder accountHolder) {
		final BasicAccount account = new BasicAccount(accountHolder);
		this.accounts.add(account);
		return account;
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
