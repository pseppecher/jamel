package jamel.jamel.banks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import jamel.basic.Circuit;
import jamel.basic.data.BasicSectorDataset;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;
import jamel.basic.util.Period;
import jamel.basic.util.Timer;
import jamel.jamel.capital.BasicCapitalStock;
import jamel.jamel.capital.CapitalStock;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.Firm;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.sectors.BankingSector;
import jamel.jamel.sectors.CapitalistSector;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Chequable;
import jamel.jamel.widgets.Cheque;

/**
 * A basic banking sector.
 */
public class BasicBankingSector implements Sector, Corporation, BankingSector {

	/**
	 * Represents a current account.
	 */
	public class Account implements BankAccount {

		/** The account holder. */
		private final AccountHolder accountHolder;

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
		 * The total debt of the account (equals the sum of the principal of the
		 * loans).
		 */
		private long debt = 0;

		/** The deposit. */
		private final Deposit deposit = new Deposit() {

			/** The amount of money. */
			private long amount = 0;

			@Override
			public void cancel() {
				if (this.amount > 0) {
					BasicBankingSector.this.canceledDeposits += this.amount;
					BasicBankingSector.this.liabilities -= this.amount;
					BasicBankingSector.this.capital += this.amount;
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
				BasicBankingSector.this.liabilities += credit;
				BasicBankingSector.this.capital -= credit;
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
				BasicBankingSector.this.liabilities -= debit;
				BasicBankingSector.this.capital += debit;
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
		private Account(AccountHolder accountHolder) {
			this.accountHolder = accountHolder;
		}

		/**
		 * Debt recovery (interest+principal).
		 */
		private void debtRecovery() {
			this.loans.addAll(this.newLoans);
			this.newLoans.clear();
			this.payInterest();
			this.payBack();
			this.loans.addAll(this.newLoans);
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
			return this.accountHolder.isSolvent();
		}

		/**
		 * TODO: RENAME
		 * 
		 * @param principal
		 *            the principal.
		 */
		private void newSpecialLoan(long principal) {
			Account.this.debt += principal;
			Account.this.newDebt += principal;
			BasicBankingSector.this.assets += principal;
			BasicBankingSector.this.capital += principal;
			BasicBankingSector.this.newLoansAmount += principal;
			Account.this.deposit.credit(principal);
			this.newLoans.add(new AbstractLoan(principal, params.get(RATE), params.get(TERM).intValue(),
					BasicBankingSector.this.timer) {

				@Override
				public void cancel(long amount) {
					if (amount > this.principal) {
						throw new IllegalArgumentException(
								"The amount to be canceled is larger than the principal of this loan.");
					}
					BasicBankingSector.this.assets -= amount;
					BasicBankingSector.this.capital -= amount;
					BasicBankingSector.this.canceledDebts += amount;
					Account.this.canceledDebt += amount;
					Account.this.debt -= amount;
					this.principal -= amount;
				}

				@Override
				public void payBack() { // FIXME : est-ce réfléchi ? à tester
					final Period current = timer.getPeriod();
					if (!current.isBefore(this.maturityDate)) {
						final Long repayment = Math.min(getAmount(), this.principal);
						if (repayment > 0) {
							deposit.debit(repayment);
							this.principal -= repayment;
							debt -= repayment;
							repaidDebt += repayment;
							BasicBankingSector.this.assets -= repayment;
							BasicBankingSector.this.capital -= repayment;
							BasicBankingSector.this.loansRepayment += repayment;
						}
						/* FIXME
						 * if (!current.isBefore(this.maturityDate) && this.principal != 0) {
							bankrupt = true;
						}*/
					}
				}

				@Override
				public void payInterest() {
					/*
					 * if (this.lastInterestPayment != null) { if
					 * (currentPeriod<this.lastInterestPayment) { throw new
					 * AnachronismException("Bad date."); } if
					 * (currentPeriod==this.lastInterestPayment) { throw new
					 * RuntimeException("It's already paid for."); } }
					 */
					Account.this.isDoubtful = true;
					final long newInterest = (long) (this.principal * this.rate);

					if (newInterest > 0) {
						this.principal += newInterest;
						Account.this.debt += newInterest;
						Account.this.newDebt += newInterest;
						BasicBankingSector.this.assets += newInterest;
						BasicBankingSector.this.capital += newInterest;
						BasicBankingSector.this.newLoansAmount += newInterest;
						final long payment = Math.min(newInterest, deposit.getAmount());
						if (payment < newInterest) {
							Account.this.isDoubtful = true;
						}
						if (payment > 0) {
							Account.this.deposit.debit(payment);
							this.principal -= payment;
							Account.this.debt -= payment;
							Account.this.newDebt -= payment;
							BasicBankingSector.this.assets -= payment;
							BasicBankingSector.this.capital -= payment;
							BasicBankingSector.this.newLoansAmount -= payment;
						}
						Account.this.interestPaid += newInterest;
						BasicBankingSector.this.interest += newInterest;
					}
					// this.lastInterestPayment = currentPeriod;
				}

			});
		}

		/**
		 * Recovers loans. Empty loans are removed.
		 */
		private void payBack() {
			final Iterator<Loan> itr = this.loans.iterator();
			while (itr.hasNext()) {
				Loan loan = itr.next();
				loan.payBack();
				if (loan.getPrincipal() == 0) {
					itr.remove();
				}
			}
		}

		/**
		 * Pays interest due for each loan.
		 */
		private void payInterest() {
			for (Loan loan : this.loans) {
				loan.payInterest();
			}
		}

		/**
		 * Definitely closes the account. Cancels all the loans associated with
		 * this account. Called in case of a bankruptcy.
		 */
		public void cancel() {
			if (cancelled) {
				throw new RuntimeException("This account is already cancelled.");
			}
			this.deposit.cancel();
			for (Loan loan : loans) {
				loan.cancel();
			}
			this.loans.clear();
			this.cancelled = true;
		}

		/**
		 * Cancels the specified amount of debt.
		 * 
		 * @param amount
		 *            the amount of debt to be canceled.
		 */
		public void cancelDebt(final long amount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			Collections.sort(loans, compareMaturity);
			long remainder = amount;
			for (Loan loan : loans) {
				if (loan.getPrincipal() <= remainder) {
					remainder -= loan.getPrincipal();
					loan.cancel();
				} else {
					loan.cancel(remainder);
					remainder = 0;
					break;
				}
			}
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
		public long getInterest() {
			return this.interestPaid;
		}

		@Override
		public double getNewDebt() {
			return this.newDebt;
		}

		@Override
		public double getRepaidDebt() {
			return this.repaidDebt;
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
		public void newLongTermLoan(long principal) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			Account.this.debt += principal;
			Account.this.newDebt += principal;
			BasicBankingSector.this.assets += principal;
			BasicBankingSector.this.capital += principal;
			BasicBankingSector.this.newLoansAmount += principal;
			Account.this.deposit.credit(principal);

			final float longTermRate = params.get(RATE);
			// TODO: prévoir un paramètre spécifique.

			final int longTerm = 120;
			// TODO: should be a parameter.

			final long initialAmount = principal;

			final Loan newLongTermLoan = new AbstractLoan(principal, longTermRate, longTerm,
					BasicBankingSector.this.timer) {

				@Override
				public void cancel(long amount) {
					if (amount > this.principal) {
						throw new IllegalArgumentException(
								"The amount to be canceled is larger than the principal of this loan.");
					}
					BasicBankingSector.this.assets -= amount;
					BasicBankingSector.this.capital -= amount;
					BasicBankingSector.this.canceledDebts += amount;
					Account.this.canceledDebt += amount;
					Account.this.debt -= amount;
					this.principal -= amount;
				}

				@Override
				public void payBack() {

					// TODO WORK IN PROGRESS 11-09-2015

					if (currentPeriod > this.maturityDate) {
						System.out.println(initialAmount + "," + this.principal);
						throw new RuntimeException("Not yet implemented");
					}
					final int remainingTerm = 1 + this.maturityDate - currentPeriod;
					final long installment = this.principal / remainingTerm;

					if (installment > 0) {
						if (getAmount() < installment) {
							Account.this.isDoubtful = true;
							newSpecialLoan(installment - getAmount());
						}
						deposit.debit(installment);
						this.principal -= installment;
						debt -= installment;
						repaidDebt += installment;
						BasicBankingSector.this.assets -= installment;
						BasicBankingSector.this.capital -= installment;
						BasicBankingSector.this.loansRepayment += installment;
					}

				}

				@Override
				public void payInterest() {
					/*
					 * FIXME: revoir ces controles. if (this.lastInterestPayment
					 * != null) { if (currentPeriod<this.lastInterestPayment) {
					 * throw new AnachronismException("Bad date."); } if
					 * (currentPeriod==this.lastInterestPayment) { throw new
					 * RuntimeException("It's already paid for."); } }
					 */
					final long newInterest = (long) (this.principal * this.rate);
					if (newInterest > 0) {
						this.principal += newInterest;
						debt += newInterest;
						newDebt += newInterest;
						BasicBankingSector.this.assets += newInterest;
						BasicBankingSector.this.capital += newInterest;
						BasicBankingSector.this.newLoansAmount += newInterest;
						final long payment = Math.min(newInterest, deposit.getAmount());
						if (payment > 0) {
							Account.this.deposit.debit(payment);
							this.principal -= payment;
							debt -= payment;
							newDebt -= payment;
							BasicBankingSector.this.assets -= payment;
							BasicBankingSector.this.capital -= payment;
							BasicBankingSector.this.newLoansAmount -= payment;
						}
						Account.this.interestPaid += newInterest;
						BasicBankingSector.this.interest += newInterest;
					}
					// this.lastInterestPayment = currentPeriod;
				}

			};
			this.newLoans.add(newLongTermLoan);
		}

		@Override
		public void newShortTermLoan(final long principal) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			Account.this.debt += principal;
			Account.this.newDebt += principal;
			BasicBankingSector.this.assets += principal;
			BasicBankingSector.this.capital += principal;
			BasicBankingSector.this.newLoansAmount += principal;
			Account.this.deposit.credit(principal);

			// TODO: work in progress:
			final float penaltyRate = params.get(PENALTY_RATE);
			final int extendedDate = timer.getPeriod().intValue() + params.get(EXTENDED_TERM).intValue();
			// FIXME: Why not used ???

			this.newLoans.add(new AbstractLoan(principal, params.get(RATE), params.get(TERM).intValue(),
					BasicBankingSector.this.timer) {

				@Override
				public void cancel(long amount) {
					if (amount > this.principal) {
						throw new IllegalArgumentException(
								"The amount to be canceled is larger than the principal of this loan.");
					}
					BasicBankingSector.this.assets -= amount;
					BasicBankingSector.this.capital -= amount;
					BasicBankingSector.this.canceledDebts += amount;
					Account.this.canceledDebt += amount;
					Account.this.debt -= amount;
					this.principal -= amount;
				}

				@Override
				public void payBack() {
					final Period current = timer.getPeriod();
					if (!current.isBefore(this.maturityDate)) {
						final Long repayment = Math.min(getAmount(), this.principal);
						if (repayment > 0) {
							deposit.debit(repayment);
							this.principal -= repayment;
							debt -= repayment;
							repaidDebt += repayment;
							BasicBankingSector.this.assets -= repayment;
							BasicBankingSector.this.capital -= repayment;
							BasicBankingSector.this.loansRepayment += repayment;
						}
						if (this.principal != 0) {
							Account.this.isDoubtful = true;
						}
						/* TODO: FIXME
						 * if (!current.isBefore(extendedDate) && this.principal != 0) {
							bankrupt = true;
						}*/
					}
				}

				@Override
				public void payInterest() {
					/*
					 * if (this.lastInterestPayment != null) { if
					 * (currentPeriod<this.lastInterestPayment) { throw new
					 * AnachronismException("Bad date."); } if
					 * (currentPeriod==this.lastInterestPayment) { throw new
					 * RuntimeException("It's already paid for."); } }
					 */
					final long newInterest;
					if (currentPeriod <= this.maturityDate) {
						newInterest = (long) (this.principal * this.rate);
					} else {
						newInterest = (long) (this.principal * penaltyRate);
						Account.this.isDoubtful = true;
					}
					if (newInterest > 0) {
						this.principal += newInterest;
						debt += newInterest;
						newDebt += newInterest;
						BasicBankingSector.this.assets += newInterest;
						BasicBankingSector.this.capital += newInterest;
						BasicBankingSector.this.newLoansAmount += newInterest;
						final long payment = Math.min(newInterest, deposit.getAmount());
						if (payment < newInterest) {
							Account.this.isDoubtful = true;
						}
						if (payment > 0) {
							Account.this.deposit.debit(payment);
							this.principal -= payment;
							debt -= payment;
							newDebt -= payment;
							BasicBankingSector.this.assets -= payment;
							BasicBankingSector.this.capital -= payment;
							BasicBankingSector.this.newLoansAmount -= payment;
						}
						Account.this.interestPaid += newInterest;
						BasicBankingSector.this.interest += newInterest;
					}
					// this.lastInterestPayment = currentPeriod;
				}

			});
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
		}

		/**
		 * Sets the account bankrupt or not.
		 * 
		 * @param b
		 *            a boolean.
		 */
		public void setBankrupt(boolean b) {
			this.bankrupt = b;
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
	private final static String EXTENDED_TERM = "term.extended"; // FIXME:  why not used ?

	@SuppressWarnings("javadoc")
	private final static String PATIENCE = "patience";

	@SuppressWarnings("javadoc")
	private final static String PENALTY_RATE = "rate.penalty";

	/** Key word for the "check consistency" phase. */
	private static final String PHASE_CHECK_CONSISTENCY = "check_consistency";

	/** Key word for the "closure" phase. */
	private static final String PHASE_CLOSURE = "closure";

	/** Key word for the "debt recovery" phase. */
	private static final String PHASE_DEBT_RECOVERY = "debt_recovery";

	/** Key word for the "opening" phase. */
	private static final String PHASE_OPENING = "opening";

	/** Key word for the "pay dividend" phase. */
	private static final String PHASE_PAY_DIVIDEND = "pay_dividend";

	@SuppressWarnings("javadoc")
	private final static String RATE = "rate.normal";

	@SuppressWarnings("javadoc")
	private final static String TERM = "term.normal";

	/** Key word for the <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The list of customers accounts. */
	private final List<Account> accounts = new ArrayList<Account>(1000);

	/**
	 * The capitalist sector. Used to select the initial owner of the bank.
	 */
	private CapitalistSector capitalistSector = null;

	/** The capital stock. */
	private CapitalStock capitalStock;

	/** The circuit. */
	private final Circuit circuit;

	/**
	 * The current period.
	 */
	private Integer currentPeriod = null;

	/** The data. */
	private SectorDataset dataset;

	/** The sector name. */
	private final String name;

	/**
	 * A flag that indicates whether the ownership of this bank is distributed
	 * or not.
	 */
	private boolean ownership = false;

	/** The random. */
	private final Random random;

	/** The timer. */
	private final Timer timer;

	/**
	 * The total assets of this bank.
	 */
	protected long assets;

	/**
	 * The number of bankruptcies for the current period.
	 */
	protected long bankruptcies;

	/**
	 * The amount of debt cancelled for the current period.
	 */
	protected long canceledDebts;

	/**
	 * The amount of deposits cancelled for the current period.
	 */
	protected long canceledDeposits;

	/**
	 * The amount of capital of this bank.
	 */
	protected long capital;

	/**
	 * The amount of dividends paid by this bank for the current period.
	 */
	protected long dividends;

	/**
	 * The amount of interests paid to this bank for the current period.
	 */
	protected long interest;

	/**
	 * The total amount of liabilities of this bank.
	 */
	protected long liabilities;

	/**
	 * The amount of loans repaid to this bank for the current period.
	 */
	protected long loansRepayment;

	/**
	 * The amount of new loans issued by this bank for the current period.
	 */
	protected long newLoansAmount;

	/** The parameters of this sector. */
	protected final JamelParameters params = new BasicParameters();

	/**
	 * Creates a new banking sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public BasicBankingSector(String name, Circuit circuit) {
		this.name = name;
		this.circuit = circuit;
		this.random = circuit.getRandom();
		this.timer = circuit.getTimer();
		this.dataset = new BasicSectorDataset();
		this.updateDataset();
	}

	/**
	 * Checks the consistency of the banking sector.
	 * 
	 * @return <code>true</code> if the banking sector is consistent,
	 *         <code>false</code> otherwise.
	 */
	private boolean checkConsistency() {
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
	 * Close the bank.
	 * <p>
	 * The data of the period are computed.
	 */
	private void close() {
		this.capitalStock.close();
		for (Account account : this.accounts) {
			account.close();
		}
		this.updateDataset();
		if (!checkConsistency()) { // TODO faut-il v�rifier la coh�rence
			// automatiquement ? r�fl�chir � �a.
			throw new RuntimeException("Inconsistency");
		}
	}

	/**
	 * Recovers due debts and interests.
	 */
	private void debtRecovery() {
		Collections.shuffle(accounts, this.random);
		final Iterator<Account> iterAccount = accounts.iterator();
		final int now = this.timer.getPeriod().intValue();
		while (iterAccount.hasNext()) {
			Account account = iterAccount.next();
			if (account.getDebt() > 0) {
				account.debtRecovery();
				if (!account.isSolvent()) {
					if (now - account.creation > this.params.get(PATIENCE)) {
						account.bankrupt = true;
					}
				}
				if (account.bankrupt) {
					if (account.isSolvent()) {
						throw new RuntimeException("This account is solvent.");
						// TODO: en fait il pourrait y avoir faillite d'un agent
						// solvable, s'il n'est pas liquide.
						// On implémentera ça plus tard.
					}
					foreclosure(account);
					this.bankruptcies += 1l;
					if (account.bankrupt) {
						iterAccount.remove();
						throw new RuntimeException("Not yet implemented.");
					}
				}
			}
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
	private void foreclosure(Account account) {
		final AccountHolder accountHolder = account.getAccountHolder();
		if (!(accountHolder instanceof Firm)) {
			throw new RuntimeException("The account holder should be a firm.");
		}
		final Firm firm = (Firm) accountHolder;
		final long firmAssets = firm.getValueOfAssets();
		final long targetedLiabilites = (long) (0.8f * firmAssets);
		// TODO: 0.8 should be a parameter;
		final long debtToBeCancelled = firm.getValueOfLiabilities() - targetedLiabilites;
		account.cancelDebt(debtToBeCancelled);
		final Cheque[] cheques = this.capitalistSector.sellFim(firm);
		double foreclosures = this.dataset.getSectorialValue("foreclosures");
		// TODO: foreclosures should be a field.
		for (Cheque cheque : cheques) {
			foreclosures += cheque.getAmount();
			cheque.payment();
		}
		this.dataset.putSectorialValue("foreclosures", foreclosures);
		account.setBankrupt(false);
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
				result += account.debt;
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
						if (!paid && BasicBankingSector.this.capital >= amount) {
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
	 * Opens the sector.
	 */
	private void open() {
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
		this.dataset = new BasicSectorDataset();
		this.dataset.putSectorialValue("foreclosures", 0d);
		for (Account account : this.accounts) {
			account.open();
		}
	}

	/**
	 * Pays the dividend to its owner.
	 */
	private void payDividend() {
		final long requiredCapital = (long) (this.assets * params.get(CAPITAL_RATIO));
		final long excedentCapital = Math.max(0, this.capital - requiredCapital);
		final long dividend = (long) (excedentCapital * params.get(CAPITAL_PROP_TO_DISTRIBUTE));

		capitalStock.setDividend(dividend);
		this.dividends = dividend;
	}

	/**
	 * Updates the dataset.
	 */
	private void updateDataset() {
		this.dataset.putSectorialValue("doubtfulDebt", (double) getDoubtfulDebt());
		this.dataset.putSectorialValue("dividends", (double) this.dividends);
		this.dataset.putSectorialValue("capital", (double) this.capital);
		this.dataset.putSectorialValue("liabilities", (double) this.liabilities);
		this.dataset.putSectorialValue("assets", (double) this.assets);
		this.dataset.putSectorialValue("bankruptcies", (double) this.bankruptcies);
		this.dataset.putSectorialValue("interest", (double) this.interest);
		this.dataset.putSectorialValue("canceledDebts", (double) this.canceledDebts);
		this.dataset.putSectorialValue("canceledDeposits", (double) this.canceledDeposits);
		this.dataset.putSectorialValue("loans.new", (double) this.newLoansAmount);
		this.dataset.putSectorialValue("loans.repayment", (double) this.loansRepayment);
	}

	/**
	 * Updates the ownership of the bank.
	 */
	private void updateOwnership() {
		if (!ownership) {
			final List<Shareholder> shareHolders = this.capitalistSector.selectRandomCapitalOwners(10);
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

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Long getBookValue() {
		return this.capital;
	}

	@Override
	public SectorDataset getDataset() {
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
	public Phase getPhase(final String phaseName) {
		Phase result = null;
		if (phaseName.equals(PHASE_OPENING)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicBankingSector.this.open();
				}
			};
		} else if (phaseName.equals(PHASE_PAY_DIVIDEND)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicBankingSector.this.payDividend();
				}
			};
		} else if (phaseName.equals(PHASE_DEBT_RECOVERY)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicBankingSector.this.debtRecovery();
				}
			};
		} else if (phaseName.equals(PHASE_CLOSURE)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicBankingSector.this.close();
				}
			};
		} else if (phaseName.equals(PHASE_CHECK_CONSISTENCY)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					// For debugging purpose. TODO valider cette methode.
					BasicBankingSector.this.checkConsistency();
				}
			};
		}
		return result;
	}

	@Override
	public void init(Element element) throws InitializationException {
		// Initialization of the dependencies:
		if (element == null) {
			throw new IllegalArgumentException("Element is null");
		}
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: " + DEPENDENCIES);
		}
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement == null) {
			throw new InitializationException("Element not found: " + key1);
		}
		final String capitalists = capitalistSectorElement.getAttribute("value");
		if (capitalists == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalistSector = (CapitalistSector) circuit.getSector(capitalists);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				try {
					this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
				} catch (NumberFormatException e) {
					throw new InitializationException(
							"For settings attribute: " + attr.getName() + "=\"" + attr.getValue() + "\"", e);
				}
			}
		}
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

}

// ***
