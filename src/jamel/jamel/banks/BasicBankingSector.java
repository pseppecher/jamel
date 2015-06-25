package jamel.jamel.banks;

import jamel.basic.Circuit;
import jamel.basic.agent.BasicAgentDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.sector.SectorDataset;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Period;
import jamel.basic.util.Timer;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.sectors.BankingSector;
import jamel.jamel.sectors.CapitalistSector;
import jamel.jamel.util.BasicMemory;
import jamel.jamel.util.Memory;
import jamel.jamel.util.RepresentativeAgentDataset;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	 * Represents a current account.
	 */
	private class Account implements BankAccount {

		/** The account holder. */
		private final AccountHolder accountHolder;

		/** A flag that indicates if the account holder is bankrupt or not. */
		private boolean bankrupt=false;

		/** A flag that indicates if the account is canceled or not. */
		private boolean canceled=false;

		/** canceledDebt */
		private Long canceledDebt = 0l;

		/** canceledMoney */
		private Long canceledMoney = 0l;

		/** Date of creation. */
		private final int creation = timer.getPeriod().intValue();

		/** The total debt of the account (equals the sum of the principal of the loans). */
		private long debt = 0;

		/** The deposit. */
		private final Deposit deposit = new Deposit() {

			/** The amount of money. */
			private long amount = 0 ;

			@Override
			public void cancel() {
				if (this.amount>0) {
					BasicBankingSector.this.v.add("canceledDeposits",this.amount);
					BasicBankingSector.this.v.add("liabilities", -this.amount);
					BasicBankingSector.this.v.add("capital", this.amount);
					Account.this.canceledMoney += this.amount;
					this.amount=0;
				}
			}

			@Override
			public void credit(long creditAmount) {
				if (creditAmount<=0) {
					throw new RuntimeException("Null or negative credit.");
				}
				this.amount += creditAmount ;
				BasicBankingSector.this.v.add("liabilities", creditAmount);
				BasicBankingSector.this.v.add("capital" ,-creditAmount);
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
				v.add("liabilities", -debit);
				v.add("capital", debit);
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

		/** The memory of the account. */
		private final Memory memory = new BasicMemory(timer,6);

		/**
		 * Creates a new account.
		 * @param accountHolder the account holder.
		 */
		private Account(AccountHolder accountHolder) {
			this.accountHolder = accountHolder;
		}

		/**
		 * Definitely closes the account. 
		 * Cancels all the loans associated with this account. 
		 * Called in case of a bankruptcy.
		 */
		private void cancel() {
			if (canceled) {
				throw new RuntimeException("This account is definitely closed.");
			}
			this.deposit.cancel();
			for (Loan loan:loans) {
				loan.cancel();
			}
			this.loans.clear();
			this.canceled = true;
		}

		/**
		 * Returns the doubtful debt amount.
		 * @return the doubtful debt amount.
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
			if (canceled && this.deposit.getAmount()!=0) {
				throw new RuntimeException("This account is closed but the amount is not 0.");
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
			if (interest==null) {
				result = 0;
			}
			else {
				result = interest.longValue();
			}
			return result;
		}

		@Override
		public boolean isOpen() {
			return !this.canceled;
		}

		@Override
		public void lend(final long principalAmount) {
			if (canceled) {
				throw new RuntimeException("This account is closed.");
			}
			this.loans.add(new AbstractLoan(principalAmount, params.get(RATE), params.get(PENALTY_RATE), 
					params.get(TERM).intValue(), params.get(EXTENDED_TERM).intValue(), BasicBankingSector.this.timer) {

				{					
					Account.this.debt += this.principal;
					BasicBankingSector.this.v.add("assets", this.principal);
					BasicBankingSector.this.v.add("capital", this.principal);
					Account.this.deposit.credit(this.principal);
				}

				@Override
				public void cancel() {
					BasicBankingSector.this.v.add("assets", -this.principal);
					BasicBankingSector.this.v.add("capital", - this.principal);
					BasicBankingSector.this.v.add("canceledDebts", this.principal);
					Account.this.canceledDebt += this.principal;
					Account.this.debt -= this.principal;
					this.principal = 0;
				}

				@Override
				public void payBack() {
					final Period current = timer.getPeriod();
					if (!current.isBefore(this.maturityDate)) {
						final Long repayment = Math.min(getAmount(), this.principal);
						if (repayment>0) {
							deposit.debit(repayment);
							this.principal -= repayment;
							debt -= repayment;
							v.add("assets", -repayment);
							v.add("capital", -repayment);
						}
						if (!current.isBefore(this.extendedDate) && this.principal!=0) {
							bankrupt=true;
						}
					}
				}

				@Override
				public void payInterest() {
					final Period currentPeriod = timer.getPeriod();
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
						BasicBankingSector.this.v.add("assets", +interest);
						BasicBankingSector.this.v.add("capital", +interest);
						final long payment = Math.min(interest, deposit.getAmount());
						if (payment>0) { 
							Account.this.deposit.debit(payment);
							this.principal-=payment;
							debt-=payment;
							BasicBankingSector.this.v.add("assets", -payment);
							BasicBankingSector.this.v.add("capital", -payment);
						}
						Account.this.memory.add("interest",interest);
						v.add("interest", + interest);
					}
					this.lastInterestPayment = currentPeriod;
				}

			});
		}

		@Override
		public Cheque newCheque(final long amount) {
			if (canceled) {
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
						if (paid) {
							throw new RuntimeException("Bad cheque: Already paid.");
						}
						throw new RuntimeException("Bad cheque: Non-sufficient funds.");
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

		/**
		 * Called at the beginning of the period.
		 */
		public void open() {
			this.canceledDebt=0l;
			this.canceledMoney=0l;
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
	 * An implementation of the Variable interface.
	 */
	private class BasicVariables extends HashMap<String,Long> implements Variables {

		{
			this.put("liabilities", 0);
			this.put("assets", 0);
			this.put("capital", 0);			
			this.put("bankruptcies",0);
			this.put("interest",0);
			this.put("canceledDebts",0);
			this.put("canceledDeposits",0);
			this.put("dividends",0);
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
		 * @param key key of the exisiting value with which the specified value is to be added
		 * @param value value to be added with the existing value.
		 */
		void add(String key, Long value);

		/**
		 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
		 * @param key the key whose associated value is to be returned
		 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
		 */
		Long get(String key);

		/**
		 * Associates the specified value with the specified key in this map. 
		 * If the map previously contained a mapping for the key, the old value is replaced.
		 * @param key key with which the specified value is to be associated
		 * @param value value to be associated with the specified key
		 */
		void put(String key, long value);

	}

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

	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The list of customers accounts. */
	private final List<Account> accounts = new ArrayList<Account>(1000);

	/** The owner of the bank. */
	private Shareholder bankOwner = null;
	
	/** 
	 * The capitalist sector.
	 * Used to select the initial owner of the bank.
	 */
	private CapitalistSector capitalistSector = null;

	/** The circuit. */
	private final Circuit circuit;

	/** The data. */
	private BasicAgentDataset dataset;

	/** The sector name. */
	private final String name;

	/** The parameters of the sector. */
	private final Map<String,Float> params = new HashMap<String,Float>();

	/** The random. */
	private final Random random;

	/** The timer. */
	private final Timer timer;

	/** The variables of the sector. */
	private final Variables v = new BasicVariables();

	/**
	 * Creates a new banking sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BasicBankingSector(String name,Circuit circuit) {
		this.name=name;
		this.circuit=circuit;
		this.random=circuit.getRandom();
		this.timer=circuit.getTimer();
		this.dataset = new BasicAgentDataset(this.name);
		this.updateDataset();
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
		if (result==true) {
			if (sumDeposit!=v.get("liabilities") || sumDebt!=v.get("assets") || v.get("assets")-v.get("liabilities")!=v.get("capital")) {
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
		this.updateDataset();
		if (!checkConsistency()) { // TODO faut-il vérifier la cohérence automatiquement ? réfléchir à ça.
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
		while(iterAccount.hasNext()){
			Account account = iterAccount.next();
			if(account.getDebt()>0) {
				account.payInterest();
				account.recover();
				if (!account.isSolvent()) {
					if (now-account.creation>this.params.get(PATIENCE)) {
						account.bankrupt=true;
					}
				}
				if (account.bankrupt) {
					account.cancel();
					account.getAccountHolder().goBankrupt();
					iterAccount.remove();
					this.v.add("bankruptcies",1l);
				}
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
	 * Selects a new owner.
	 */
	private void newOwner() {
		if (this.bankOwner!=null) {
			throw new RuntimeException("There is already an owner.");
		}
		this.bankOwner=this.capitalistSector.selectRandomCapitalOwner();
		if (this.bankOwner!=null) {
			bankOwner.addAsset(this);		
		} // else ? warning ? TODO test that.
	}

	/**
	 * Opens the sector.
	 */
	private void open() {
		if (this.bankOwner==null) {
			newOwner();
		}
		this.dataset = new BasicAgentDataset(this.name);
		this.v.put("bankruptcies",0);
		this.v.put("interest",0);
		this.v.put("canceledDebts",0);
		this.v.put("canceledDeposits",0);
		for(Account account:this.accounts) {
			account.open();
		}
	}

	/**
	 * Pays the dividend to its owner.
	 */
	private void payDividend() {
		final long requiredCapital = (long)(v.get("assets")*params.get(CAPITAL_RATIO));
		final long excedentCapital = Math.max(0, v.get("capital")-requiredCapital);
		final long dividend = (long) (excedentCapital*params.get(CAPITAL_PROP_TO_DISTRIBUTE));
		v.put("dividends", dividend);
		if (dividend!=0) {
			if (BasicBankingSector.this.bankOwner==null) {
				newOwner();
			}
			if (BasicBankingSector.this.bankOwner!=null) {
				BasicBankingSector.this.bankOwner.receiveDividend(new Cheque() {
					private boolean paid = false;
					@Override public long getAmount() {
						return dividend;
					}
					@Override
					public boolean payment() {
						final boolean result;
						if (!paid && BasicBankingSector.this.v.get("capital")>=dividend) {
							this.paid = true;
							result = true;
							// In this case, there is no deposit to debit: 
							// the money corresponding to the dividend will be created by the credit of the owner account. 
						}
						else {
							result = false;
						}
						return result;
					}},BasicBankingSector.this);
			}
		}
	}



	/**
	 * Updates the dataset.
	 */
	private void updateDataset() {
		this.dataset.put("doubtfulDebt", (double) getDoubtfulDebt());
		this.dataset.put("dividends", (double) v.get("dividends"));
		this.dataset.put("capital", (double) v.get("capital"));
		this.dataset.put("liabilities", (double) v.get("liabilities"));
		this.dataset.put("assets", (double) v.get("assets"));
		this.dataset.put("bankruptcies", (double) v.get("bankruptcies"));
		this.dataset.put("interest", (double) v.get("interest"));
		this.dataset.put("canceledDebts", (double) v.get("canceledDebts"));
		this.dataset.put("canceledDeposits", (double) v.get("canceledDeposits"));		
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}
	
	@Override
	public long getBookValue() {
		return v.get("capital");
	}

	@Override
	public SectorDataset getDataset() {
		return new RepresentativeAgentDataset(this.dataset);
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
		if (name.equals("opening")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicBankingSector.this.open();
				}				
			};			
		}
		else if (name.equals("pay_dividend")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicBankingSector.this.payDividend();
				}				
			};			
		}
		else if (name.equals("debt_recovery")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicBankingSector.this.debtRecovery();
				}				
			};			
		}
		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicBankingSector.this.close();
				}				
			};			
		}		
		else if (name.equals("check_consistency")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
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
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement==null) {
			throw new InitializationException("Element not found: "+DEPENDENCIES);
		}
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement==null) {
			throw new InitializationException("Element not found: "+key1);
		}
		final String capitalists = capitalistSectorElement.getAttribute("value");
		if (capitalists=="") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalistSector = (CapitalistSector) circuit.getSector(capitalists);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				try {
					this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
				} catch (NumberFormatException e) {
					throw new InitializationException("For settings attribute: "+attr.getName()+"=\""+attr.getValue()+"\"",e);
				}
			}
		}
	}

}

// ***
