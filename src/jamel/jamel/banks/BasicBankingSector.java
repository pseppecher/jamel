package jamel.jamel.banks;

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
import jamel.jamel.firms.Firm;
import jamel.jamel.firms.capital.BasicCapitalStock;
import jamel.jamel.firms.capital.CapitalStock;
import jamel.jamel.firms.capital.StockCertificate;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.sectors.BankingSector;
import jamel.jamel.sectors.CapitalistSector;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.util.BasicMemory;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Chequable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A basic banking sector.
 */
public class BasicBankingSector implements Sector, Corporation, BankingSector {

	/**
	 * An implementation of the Variable interface.
	 */
	private class BasicVariables extends HashMap<String, Long> implements
			Variables {

		{
			this.put("liabilities", 0);
			this.put("assets", 0);
			this.put("capital", 0);
			this.put("bankruptcies", 0);
			this.put("interest", 0);
			this.put("canceledDebts", 0);
			this.put("canceledDeposits", 0);
			this.put("dividends", 0);
			this.put("loans.new", 0);
			this.put("loans.repayment", 0);
		}

		@Override
		public void add(String key, Long amount) {
			this.put(key, get(key) + amount);
		}

		@Override
		public Long get(String key) {
			return super.get(key);
		}

		@Override
		public void put(String key, long value) {
			super.put(key, value);
		}

	}

	/**
	 * Storage of the current variables of the bank.
	 */
	private interface Variables {

		/**
		 * Adds the specified value with the existing value.
		 * 
		 * @param key
		 *            key of the exisiting value with which the specified value
		 *            is to be added
		 * @param value
		 *            value to be added with the existing value.
		 */
		void add(String key, Long value);

		/**
		 * Returns the value to which the specified key is mapped, or null if
		 * this map contains no mapping for the key.
		 * 
		 * @param key
		 *            the key whose associated value is to be returned
		 * @return the value to which the specified key is mapped, or null if
		 *         this map contains no mapping for the key
		 */
		Long get(String key);

		/**
		 * Associates the specified value with the specified key in this map. If
		 * the map previously contained a mapping for the key, the old value is
		 * replaced.
		 * 
		 * @param key
		 *            key with which the specified value is to be associated
		 * @param value
		 *            value to be associated with the specified key
		 */
		void put(String key, long value);

	}

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
					BasicBankingSector.this.v.add("canceledDeposits",
							this.amount);
					BasicBankingSector.this.v.add("liabilities", -this.amount);
					BasicBankingSector.this.v.add("capital", this.amount);
					Account.this.canceledMoney += this.amount;
					this.amount = 0;
				}
			}

			@Override
			public void credit(long creditAmount) {
				if (creditAmount <= 0) {
					throw new RuntimeException("Null or negative credit.");
				}
				this.amount += creditAmount;
				BasicBankingSector.this.v.add("liabilities", creditAmount);
				BasicBankingSector.this.v.add("capital", -creditAmount);
			}

			@Override
			public void debit(long debit) {
				if (debit <= 0) {
					throw new RuntimeException("Null or negative debit <"
							+ debit + ">");
				}
				if (this.amount < debit) {
					throw new RuntimeException("Not enough money.");
				}
				this.amount -= debit;
				v.add("liabilities", -debit);
				v.add("capital", debit);
			}

			@Override
			public long getAmount() {
				if (amount < 0) {
					throw new RuntimeException("Negative deposit.");
				}
				return this.amount;
			}

		};

		/** The list of loans for this account. */
		private final List<Loan> loans = new LinkedList<Loan>();

		/** The memory of the account. */
		private final Memory memory = new BasicMemory(timer, 6);

		/** The new debt of the period. */
		private long newDebt = 0;

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
		 * Returns the doubtful debt amount.
		 * 
		 * @return the doubtful debt amount.
		 */
		private long getDoubtfulDebt() {
			long result = 0;
			for (Loan loan : loans) {
				if (loan.isDoubtfull()) {
					result += loan.getPrincipal();
				}
			}
			return result;
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
		 * Pays interest due for each loan.
		 */
		private void payInterest() {
			for (Loan loan : this.loans) {
				loan.payInterest();
			}
		}

		/**
		 * Recovers loans. Empty loans are removed.
		 */
		private void recover() {
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
				throw new RuntimeException(
						"This account is closed but the amount is not 0.");
			}
			return this.deposit.getAmount();
		}

		@Override
		public double getCanceledDebt() {
			return this.canceledDebt;
		}

		@Override
		public double getCanceledMoney() {
			return this.canceledMoney;
		}

		@Override
		public long getDebt() {
			return this.debt;
		}

		@Override
		public long getInterest() {
			final long result;
			Double interest = this.memory.get("interest");
			if (interest == null) {
				result = 0;
			} else {
				result = interest.longValue();
			}
			return result;
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
		public void lend(final long principalAmount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			if (cancelled) {
				throw new RuntimeException("This account is cancelled.");
			}
			this.loans.add(new AbstractLoan(principalAmount, params.get(RATE),
					params.get(PENALTY_RATE), params.get(TERM).intValue(),
					params.get(EXTENDED_TERM).intValue(),
					BasicBankingSector.this.timer) {

				{
					Account.this.debt += this.principal;
					Account.this.newDebt += this.principal;
					BasicBankingSector.this.v.add("assets", this.principal);
					BasicBankingSector.this.v.add("capital", this.principal);
					BasicBankingSector.this.v.add("loans.new", this.principal);
					Account.this.deposit.credit(this.principal);
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
					BasicBankingSector.this.v.add("assets", -amount);
					BasicBankingSector.this.v.add("capital", -amount);
					BasicBankingSector.this.v.add("canceledDebts", amount);
					Account.this.canceledDebt += amount;
					Account.this.debt -= amount;
					this.principal -= amount;
				}

				@Override
				public int getMaturity() {
					return this.maturityDate.intValue();
				}

				@Override
				public void payBack() {
					final Period current = timer.getPeriod();
					if (!current.isBefore(this.maturityDate)) {
						final Long repayment = Math.min(getAmount(),
								this.principal);
						if (repayment > 0) {
							deposit.debit(repayment);
							this.principal -= repayment;
							debt -= repayment;
							repaidDebt += repayment;
							v.add("assets", -repayment);
							v.add("capital", -repayment);
							v.add("loans.repayment", repayment);
						}
						if (!current.isBefore(this.extendedDate)
								&& this.principal != 0) {
							bankrupt = true;
						}
					}
				}

				@Override
				public void payInterest() {
					final Period currentPeriod = timer.getPeriod();
					if (this.lastInterestPayment != null
							&& !currentPeriod.isAfter(this.lastInterestPayment)) {
						throw new RuntimeException("It's already paid for.");
					}
					final long interest;
					if (!currentPeriod.isAfter(this.maturityDate)) {
						interest = (long) (this.principal * this.rate);
					} else {
						interest = (long) (this.principal * this.penaltyRate);
					}
					if (interest > 0) {
						this.principal += interest;
						debt += interest;
						newDebt += interest;
						BasicBankingSector.this.v.add("assets", +interest);
						BasicBankingSector.this.v.add("capital", +interest);
						BasicBankingSector.this.v.add("loans.new", +interest);
						final long payment = Math.min(interest,
								deposit.getAmount());
						if (payment > 0) {
							Account.this.deposit.debit(payment);
							this.principal -= payment;
							debt -= payment;
							newDebt -= payment;
							BasicBankingSector.this.v.add("assets", -payment);
							BasicBankingSector.this.v.add("capital", -payment);
							BasicBankingSector.this.v
									.add("loans.new", -payment);
						}
						Account.this.memory.add("interest", interest);
						v.add("interest", +interest);
					}
					this.lastInterestPayment = currentPeriod;
				}

			});
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
								"Bad cheque: Non-sufficient funds.");
					}
					return result;
				}

				@Override
				public String toString() {
					return "Drawer: " + accountHolder.getName() + ", amount: "
							+ amount;
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
				this.period = timer.getPeriod().intValue();
			} else {
				this.period++;
				if (this.period != timer.getPeriod().intValue()) {
					throw new AnachronismException();
				}
			}
			this.open = true;
			this.repaidDebt = 0;
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
	private final static String EXTENDED_TERM = "term.extended";

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

	/** The capital stock. */
	private CapitalStock capitalStock;

	/** The circuit. */
	private final Circuit circuit;

	/** The data. */
	private SectorDataset dataset;

	/** The sector name. */
	private final String name;

	/**
	 * A flag that indicates whether the ownership of the firm is distributed or
	 * not.
	 */
	private boolean ownership = false;

	/** The random. */
	private final Random random;

	/** The timer. */
	private final Timer timer;

	/** The variables of the sector. */
	private final Variables v = new BasicVariables();

	/**
	 * The capitalist sector. Used to select the initial owner of the bank.
	 */
	protected CapitalistSector capitalistSector = null;

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
			if (sumDeposit != v.get("liabilities")
					|| sumDebt != v.get("assets")
					|| v.get("assets") - v.get("liabilities") != v
							.get("capital")) {
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
		if (!checkConsistency()) { // TODO faut-il vérifier la cohérence
			// automatiquement ? réfléchir à ça.
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
				account.payInterest();
				account.recover();
				if (!account.isSolvent()) {
					if (now - account.creation > this.params.get(PATIENCE)) {
						account.bankrupt = true;
					}
				}
				if (account.bankrupt) {
					foreclosure(account);
					if (account.bankrupt) {
						iterAccount.remove();
						this.v.add("bankruptcies", 1l);
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
	 * (ref: <a href="https://en.wikipedia.org/wiki/Foreclosure">wikipedia.org</a>)
	 * 
	 * @param account
	 *            the bankrupted account.
	 */
	private void foreclosure(Account account) {
		final AccountHolder accountHolder = account.getAccountHolder();
		if (!(accountHolder instanceof Firm)) {
			throw new RuntimeException(
					"The account holder must be a firm, because only firms are indebted.");
		}
		final Firm firm = (Firm) accountHolder;
		final long assets = firm.getValueOfAssets();
		final long targetedLiabilites = (long) (0.8f * assets);
		// TODO: 0.8 should be a parameter;
		final long debtToBeCancelled = firm.getValueOfLiabilities()
				- targetedLiabilites;
		account.cancelDebt(debtToBeCancelled);
		final Cheque[] cheques = this.capitalistSector.sellFim(firm);
		double foreclosures = this.dataset.getSectorialValue("foreclosures");
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
			result += account.getDoubtfulDebt();
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
						if (!paid
								&& BasicBankingSector.this.v.get("capital") >= amount) {
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
		updateOwnership();
		this.capitalStock.open();
		this.dataset = new BasicSectorDataset();
		this.dataset.putSectorialValue("foreclosures", 0d);
		this.v.put("bankruptcies", 0);
		this.v.put("interest", 0);
		this.v.put("canceledDebts", 0);
		this.v.put("canceledDeposits", 0);
		this.v.put("loans.new", 0);
		this.v.put("loans.repayment", 0);
		for (Account account : this.accounts) {
			account.open();
		}
	}

	/**
	 * Pays the dividend to its owner.
	 */
	private void payDividend() {
		final long requiredCapital = (long) (v.get("assets") * params
				.get(CAPITAL_RATIO));
		final long excedentCapital = Math.max(0, v.get("capital")
				- requiredCapital);
		final long dividend = (long) (excedentCapital * params
				.get(CAPITAL_PROP_TO_DISTRIBUTE));

		capitalStock.setDividend(dividend);
		v.put("dividends", dividend);
	}

	/**
	 * Updates the dataset.
	 */
	private void updateDataset() {
		this.dataset.putSectorialValue("doubtfulDebt",
				(double) getDoubtfulDebt());
		this.dataset
				.putSectorialValue("dividends", (double) v.get("dividends"));
		this.dataset.putSectorialValue("capital", (double) v.get("capital"));
		this.dataset.putSectorialValue("liabilities",
				(double) v.get("liabilities"));
		this.dataset.putSectorialValue("assets", (double) v.get("assets"));
		this.dataset.putSectorialValue("bankruptcies",
				(double) v.get("bankruptcies"));
		this.dataset.putSectorialValue("interest", (double) v.get("interest"));
		this.dataset.putSectorialValue("canceledDebts",
				(double) v.get("canceledDebts"));
		this.dataset.putSectorialValue("canceledDeposits",
				(double) v.get("canceledDeposits"));
		this.dataset
				.putSectorialValue("loans.new", (double) v.get("loans.new"));
		this.dataset.putSectorialValue("loans.repayment",
				(double) v.get("loans.repayment"));
	}

	/**
	 * Updates the ownership of the bank.
	 */
	private void updateOwnership() {
		if (!ownership) {
			final List<Shareholder> shareHolders = this.capitalistSector
					.selectRandomCapitalOwners(10);
			if (shareHolders.size() > 0) {
				this.capitalStock = getNewCapitalStock(shareHolders.size());
				List<StockCertificate> truc = this.capitalStock
						.getCertificates();
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
		return v.get("capital");
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
	public Phase getPhase(final String name) {
		Phase result = null;
		if (name.equals(PHASE_OPENING)) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					BasicBankingSector.this.open();
				}
			};
		} else if (name.equals(PHASE_PAY_DIVIDEND)) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					BasicBankingSector.this.payDividend();
				}
			};
		} else if (name.equals(PHASE_DEBT_RECOVERY)) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					BasicBankingSector.this.debtRecovery();
				}
			};
		} else if (name.equals(PHASE_CLOSURE)) {
			result = new AbstractPhase(name, this) {
				@Override
				public void run() {
					BasicBankingSector.this.close();
				}
			};
		} else if (name.equals(PHASE_CHECK_CONSISTENCY)) {
			result = new AbstractPhase(name, this) {
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
		final Element refElement = (Element) element.getElementsByTagName(
				DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: "
					+ DEPENDENCIES);
		}
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement
				.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement == null) {
			throw new InitializationException("Element not found: " + key1);
		}
		final String capitalists = capitalistSectorElement
				.getAttribute("value");
		if (capitalists == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalistSector = (CapitalistSector) circuit
				.getSector(capitalists);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName(
				"settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				try {
					this.params.put(attr.getName(),
							Float.parseFloat(attr.getValue()));
				} catch (NumberFormatException e) {
					throw new InitializationException(
							"For settings attribute: " + attr.getName() + "=\""
									+ attr.getValue() + "\"", e);
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
