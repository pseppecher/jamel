package jamel.basic.agents.banks;

import jamel.basic.agents.banks.util.Deposit;
import jamel.basic.agents.banks.util.Loan;
import jamel.basic.agents.roles.AccountHolder;
import jamel.basic.agents.roles.Asset;
import jamel.basic.agents.roles.CapitalOwner;
import jamel.basic.util.BankAccount;
import jamel.basic.util.Cheque;
import jamel.util.Circuit;
import jamel.util.Period;
import jamel.util.Sector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A basic banking sector.
 */
public class BankingSector implements Sector, Asset {

	/**
	 * Represents a current account.
	 */
	private class Account implements BankAccount {
		
		/** The account holder. */
		private final AccountHolder accountHolder;

		/** A flag that indicates if the account holder is bankrupt or not. */
		private boolean bankrupt=false;

		/** Date of creation. */
		private final int creation = Circuit.getCurrentPeriod().getValue();

		/** The total debt of the account (equals the sum of the principal of the loans). */
		private long debt = 0;

		/** The deposit. */
		private final Deposit deposit = new Deposit() {

			/** The amount of money. */
			private long amount = 0 ;

			@Override
			public void credit(long creditAmount) {
				if (creditAmount<=0) {
					throw new RuntimeException("Null or negative credit.");
				}
				this.amount += creditAmount ;
				BankingSector.this.v.liabilities += creditAmount;
				BankingSector.this.v.capital -= creditAmount;
			}

			@Override
			public void debit(long debit) {
				if (debit<=0) { throw
					new RuntimeException("Null or negative debit <"+debit+">");
				}
				if (this.amount < debit) {
					throw new RuntimeException("Not enough money.");
				}
				this.amount -= debit;
				v.liabilities -= debit;
				v.capital += debit;
			}

			@Override 
			public long getAmount() {
				if (amount<0) {
					throw new RuntimeException("Negative deposit.");
				}
				return this.amount ; 
			}

		};

		/** The list of loans for this account. */
		private final List<Loan> loans = new LinkedList<Loan>();

		/** A flag that indicates if the account is open or not. */
		private boolean open=true;

		/**
		 * Creates a new account.
		 * @param accountHolder the account holder.
		 */
		private Account(AccountHolder accountHolder) {
			this.accountHolder = accountHolder;
		}

		/**
		 * Closes the account. 
		 * Cancels all the loans associated with this account. 
		 * Called in case of a bankruptcy.
		 */
		private void close() {
			if (!open) {
				throw new RuntimeException("This account is already closed.");
			}
			final long cash = getAmount(); 
			if (cash!=0) {
				this.deposit.debit(cash);
			}
			for (Loan loan:loans) {
				loan.cancel();
			}
			this.loans.clear();
			this.open = false;
		}

		/**
		 * Returns the doubtfull debt amount.
		 * @return the doubtfull debt amount.
		 */
		private long getDoubtfulDebt() {
			long result=0;
			for(Loan loan:loans){
				if (loan.isDoubtfull()) {
					result+=loan.getPrincipal();
				}
			}
			return result;
		}

		/**
		 * Checks the consistency of the account.
		 * @return <code>true</code> if the account is consistent, <code>false</code> otherwise.
		 */
		private boolean isConsistent() {
			long sumDebt = 0;
			for (Loan loan: loans) {
				sumDebt+=loan.getPrincipal();
			}
			return sumDebt==this.debt;
		}

		/**
		 * Returns <code>true</code> if the account holder is solvent, <code>false</code> otherwise.
		 * @return a boolean.
		 */
		private boolean isSolvent() {
			return this.accountHolder.getAssets()>this.debt;
		}

		/**
		 * Pays interest due for each loan.
		 */
		private void payInterest() {
			for (Loan loan: this.loans) {
				loan.payInterest();
			}
		}

		/**
		 * Recovers loans.
		 * Empty loans are removed.
		 */
		private void recover() {
			final Iterator<Loan> itr = this.loans.iterator();
			while(itr.hasNext()){
				Loan loan = itr.next();
				loan.payBack();
				if (loan.getPrincipal()==0) {
					itr.remove();
				}
			}
		}

		@Override
		public void deposit(Cheque cheque) {
			if (cheque.payment()) {
				this.deposit.credit(cheque.getAmount());
			}
			else {
				throw new RuntimeException("The payment was refused.");
			}
		}

		/**
		 * Returns the account holder.
		 * @return the account holder.
		 */
		@Override
		public AccountHolder getAccountHolder() {
			return accountHolder;
		}

		@Override
		public long getAmount() {
			if (!open && this.deposit.getAmount()!=0) {
				throw new RuntimeException("This account is closed but the amount is not 0.");
			}
			return this.deposit.getAmount();
		}

		@Override
		public long getDebt() {
			return this.debt;
		}

		@Override
		public boolean isOpen() {
			return this.open;
		}

		@Override
		public void lend(final long principalAmount) {
			if (!open) {
				throw new RuntimeException("This account is closed.");
			}
			this.loans.add(new Loan() {

				/** The extended maturity date.*/
				private final Period extendedDate;

				/** The period of the last payment of interest. */
				private Period lastInterestPayment;

				/** The maturity date.*/
				private final Period maturityDate;

				/** The penalty interest rate. */
				private final double penaltyRate ;

				/** The remaining principal. */
				private long principal ;

				/** The interest rate. */
				private final double rate ;

				{					
					this.principal = principalAmount;
					this.rate = BankingSector.this.p.rate;
					this.penaltyRate = BankingSector.this.p.penaltyRate;
					this.maturityDate = Circuit.getCurrentPeriod().plus(BankingSector.this.p.normalTerm);
					this.extendedDate = Circuit.getCurrentPeriod().plus(BankingSector.this.p.extendedTerm);
					Account.this.debt += this.principal;
					BankingSector.this.v.assets += this.principal;
					BankingSector.this.v.capital += this.principal;
					Account.this.deposit.credit(this.principal);
				}

				@Override
				public void cancel() {
					BankingSector.this.v.assets -= this.principal;
					BankingSector.this.v.capital -= this.principal;
					Account.this.debt -= this.principal;
					this.principal = 0;
				}

				@Override
				public long getPrincipal() {
					return this.principal;
				}

				@Override
				public boolean isDoubtfull() {
					return (Circuit.getCurrentPeriod().isAfter(this.maturityDate));
				}

				@Override
				public void payBack() {
					final Period current = Circuit.getCurrentPeriod();
					if (!current.isBefore(this.maturityDate)) {
						final Long repayment = Math.min(getAmount(), this.principal);
						if (repayment>0) {
							deposit.debit(repayment);
							this.principal -= repayment;
							debt -= repayment;
							v.assets -= repayment;
							v.capital -= repayment;
						}
						if (!current.isBefore(this.extendedDate) && this.principal!=0) {
							bankrupt=true;
						}
					}
				}

				@Override
				public void payInterest() {
					final Period currentPeriod = Circuit.getCurrentPeriod();
					if (this.lastInterestPayment!=null && !currentPeriod.isAfter(this.lastInterestPayment)) {
						throw new RuntimeException("It's already paid for.");
					}
					final long interest ;
					if (!currentPeriod.isAfter(this.maturityDate)) { 
						interest = (long) (this.principal*this.rate);
					}
					else {
						interest = (long) (this.principal*this.penaltyRate);
					}
					if (interest>0) {
						this.principal+=interest;
						debt+=interest;
						BankingSector.this.v.assets+=interest;
						BankingSector.this.v.capital+=interest;
						final long payment = Math.min(interest, deposit.getAmount());
						if (payment>0) { 
							Account.this.deposit.debit(payment);
							this.principal-=payment;
							debt-=payment;
							BankingSector.this.v.assets-=payment;
							BankingSector.this.v.capital-=payment;
						}
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
			return new Cheque(){

				/** A flag that indicates if the check is paid. */
				private boolean paid = false;

				@Override
				public long getAmount() {
					return amount;
				}

				@Override
				public boolean payment() {
					final boolean result;
					if (!paid && Account.this.getAmount()>=amount) {
						Account.this.deposit.debit(amount);
						this.paid = true;
						result = true;
					}
					else {
						result = false;
					}
					return result;
				}

				@Override
				public String toString() {
					return "Drawer: "+accountHolder.getName()+
							", amount: "+amount;
				}
			};
		}

		@Override public String toString() {
		    final StringBuilder result = new StringBuilder();
		    final String NEW_LINE = System.getProperty("line.separator");
		    result.append(this.getClass().getName() + " {" + NEW_LINE);
		    result.append(" Holder: " + this.accountHolder.getName() + NEW_LINE);
		    result.append(" Creation: " + creation + NEW_LINE);
		    result.append(" Deposit: " + deposit.getAmount() + NEW_LINE );
		    result.append(" Debt: " + debt + NEW_LINE);
		    result.append("}");
		    return result.toString();
		  }

	}

	/**
	 * Enumeration of the keys of the output messages sent by this sector.
	 */
	private static class KEY {

		/** When the sector send its data for aggregation at the end of the period. */
		public static final String putData = "putData";

		/** To select at random the owner of the bank. */
		public static final String selectCapitalOwner = "selectCapitalOwner";

	}

	/**
	 * A class to store the parameters of the sector.
	 */
	private class Parameters {

		@SuppressWarnings("javadoc")
		private static final String CAPITAL_PROP_TO_DISTRIBUTE = "capital.propensityToDistributeExcess";

		@SuppressWarnings("javadoc")
		private static final String CAPITAL_RATIO = "capital.targetedRatio";

		@SuppressWarnings("javadoc")
		private final static String EXTENDED_TERM = "term.extended";

		@SuppressWarnings("javadoc")
		private final static String PATIENCE = "patience";

		@SuppressWarnings("javadoc")
		private final static String PENALTY_RATE = "rate.penalty";

		@SuppressWarnings("javadoc")
		private final static String RATE = "rate.normal";

		@SuppressWarnings("javadoc")
		private final static String TERM = "term.normal";

		/** The extended term of credits. */
		private Integer extendedTerm = null;

		/** The normal term of credits. */
		private Integer normalTerm = null;

		/** The patience of the bank with the startups. */
		private Integer patience = null;

		/** The penalty interest rate. */
		private Float penaltyRate = null;

		/** The propensity to distribute the excess of capital. */
		private Float propensityToDistributeCapitalExcess = null;

		/** The regular interest rate. */
		private Float rate = null;

		/** The targeted capital ratio. */
		private Float targetedCapitalRatio = null;

		/**
		 * Updates the parameters.
		 */
		private void update() {
			this.extendedTerm = Integer.parseInt(circuit.getParameter(BankingSector.this.getName(),EXTENDED_TERM));
			this.normalTerm = Integer.parseInt(circuit.getParameter(BankingSector.this.getName(),TERM));
			this.patience = Integer.parseInt(circuit.getParameter(BankingSector.this.getName(),PATIENCE));
			this.penaltyRate = Float.parseFloat(circuit.getParameter(BankingSector.this.getName(),PENALTY_RATE));
			this.propensityToDistributeCapitalExcess = Float.parseFloat(circuit.getParameter(BankingSector.this.getName(),CAPITAL_PROP_TO_DISTRIBUTE));
			this.rate = Float.parseFloat(circuit.getParameter(BankingSector.this.getName(),RATE));
			this.targetedCapitalRatio = Float.parseFloat(circuit.getParameter(BankingSector.this.getName(),CAPITAL_RATIO));
		}

	}

	/**
	 * A class to store the variables of the sector.
	 */
	private class Variables {

		/** The total assets of the sector. */
		private long assets = 0l;

		/** The number of bankruptcies in the period. */
		private int bankruptcies = 0;

		/** The total capital of the sector. */
		private long capital = 0l;

		/** The total liabilities of the sector. */
		private long liabilities = 0l;

	}

	/** The list of customers accounts. */
	private final List<Account> accounts = new ArrayList<Account>(1000);

	/** The owner of the bank. */
	private CapitalOwner bankOwner = null;

	/** The circuit. */
	private final Circuit circuit;

	/** The data. */
	private final Map<String,Double> data = new HashMap<String,Double>();

	/** The keys */
	private final List<String> dataKeys = new ArrayList<String>(); // FIXME: unused

	/** The sector name. */
	private final String name;

	/** The parameters of the sector. */
	private final Parameters p = new Parameters();

	/** The variables of the sector. */
	private final Variables v = new Variables();

	/**
	 * Creates a new banking sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BankingSector(String name,Circuit circuit) {
		this.name=name;
		this.circuit=circuit;
		this.p.update();
	}

	/**
	 * Checks the consistency of the banking sector.
	 * @return <code>true</code> if the banking sector is consistent, <code>false</code> otherwise. 
	 */
	private boolean checkConsistency() {
		boolean result = true;
		long sumDeposit = 0;
		long sumDebt = 0;
		for (Account account: this.accounts) {
			if (account.isConsistent()) {
				sumDeposit+=account.getAmount();
				sumDebt+=account.getDebt();
			} 
			else {
				result = false;
				break;
			}
		}
		if (result=true) {
			if (sumDeposit!=v.liabilities || sumDebt!=v.assets || v.assets-v.liabilities!=v.capital) {
				result=false;
			}
		}
		return result;
	}

	/**
	 * Close the bank.
	 * The data of the period are computed and forwarded to the circuit.
	 */
	private void close() {
		this.updateData();
		this.circuit.forward(KEY.putData,this.data);
		if (!checkConsistency()) {
			throw new RuntimeException("Inconsistency");
		}
	}

	/**
	 * Recovers due debts and interests.
	 */
	private void debtRecovery() {
		Collections.shuffle(accounts, Circuit.getRandom());
		final Iterator<Account> iterAccount = accounts.iterator();
		final int now = Circuit.getCurrentPeriod().getValue();
		while(iterAccount.hasNext()){
			Account account = iterAccount.next();
			if(account.getDebt()>0) {
				account.payInterest();
				account.recover();
				/*if (!account.isSolvent()) {
					if (now-account.creation>this.p.patience) {
						account.bankrupt=true;
					}
				}*/
				if (account.bankrupt) {
					account.close();
					account.getAccountHolder().bankrupt();
					iterAccount.remove();
					this.v.bankruptcies++;
				};
			}
		}		
	}

	/**
	 * Returns the doubtful debt amount.
	 * @return the doubtful debt amount.
	 */
	private long getDoubtfulDebt() {
		long result=0;
		for(Account account : accounts) {
			result+=account.getDoubtfulDebt();
		}
		return result;
	}

	/**
	 * Opens the sector.
	 */
	private void open() {
		this.data.clear();
		this.v.bankruptcies=0;
	}

	/**
	 * Pays the dividend to its owner.
	 */
	private void payDividend() {
		final long requiredCapital = (long)(v.assets*p.targetedCapitalRatio );
		final long excedentCapital = Math.max(0, v.capital-requiredCapital);
		final long dividend = (long) (excedentCapital*p.propensityToDistributeCapitalExcess);
		data.put(name+".dividends", (double) dividend);
		if (dividend!=0) {
			if (BankingSector.this.bankOwner==null) {
				BankingSector.this.bankOwner=(CapitalOwner) BankingSector.this.circuit.forward(KEY.selectCapitalOwner);
				bankOwner.addAsset(this);
			}
			if (BankingSector.this.bankOwner!=null) {
				BankingSector.this.bankOwner.receiveDividend(new Cheque() {
					private boolean paid = false;
					@Override public long getAmount() {
						return dividend;
					}
					@Override
					public boolean payment() {
						final boolean result;
						if (!paid && BankingSector.this.v.capital>=dividend) {
							this.paid = true;
							result = true;
							// In this case, there is no deposit to debit: 
							// the money corresponding to the dividend will be created by the credit of the owner account. 
						}
						else {
							result = false;
						}
						return result;
					}},BankingSector.this);
			}
		}
	}

	/**
	 * Updates the data.
	 */
	private void updateData() {
		data.put(name+".doubtfulDebt", (double) this.getDoubtfulDebt());
		data.put(name+".capital", (double) v.capital);
		data.put(name+".liabilities", (double) v.liabilities);
		data.put(name+".assets", (double) v.assets);
		data.put(name+".bankruptcies", (double) v.bankruptcies);
	}

	@Override
	public boolean doPhase(String phaseName) {

		checkConsistency(); // DELETE

		if (phaseName.equals("opening")) {
			this.open();
		}

		else if (phaseName.equals("pay_dividend")) {
			this.payDividend();
		}

		else if (phaseName.equals("debt_recovery")) {
			this.debtRecovery();
		}

		else if (phaseName.equals("closure")) {
			this.close();
		}

		else {
			throw new IllegalArgumentException(this.name+": Unknown phase <"+phaseName+">");
		}

		checkConsistency(); // DELETE

		return true;

	}

	@Override
	public Object forward(String request, Object ... args) {

		final Object result;

		if (request.equals("getAssets")) {
			result = v.assets;
		}

		else if (request.equals("getCapital")) {
			result = v.capital;
		}

		else if (request.equals("getLiabilities")) {
			result = v.liabilities;
		}

		else if (request.equals("getNewAccount")) {
			final Account account = new Account((AccountHolder) args[0]);
			accounts.add(account);
			result = account;
		}

		else if (request.equals("isConsistent")) {
			result = checkConsistency();
		}

		else if (request.equals("updateParameters")) {
			p.update();
			result = null;
		}

		else if (request.equals("addDataKey")) {
			result = this.dataKeys.add((String) args[0]);
		}

		else {
			throw new IllegalArgumentException(this.name+": Unknown request <"+request+">");
		}

		return result;

	}

	@Override
	public long getCapital() {
		return v.capital;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void pause() {
		// Does nothing.
	}

}

// ***

