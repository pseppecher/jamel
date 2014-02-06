/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher and contributors.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 */

package jamel.spheres.monetarySphere;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.roles.AccountHolder;
import jamel.agents.roles.CapitalOwner;
import jamel.util.data.PeriodDataset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Represents the single representative bank.
 * <p>
 * Encapsulates a list of accounts.
 */
public class Bank extends JamelObject implements AccountHolder {

	/**
	 * A class for basic accounts.
	 * <p>
	 * A current account encapsulates a deposit, but no debts.
	 * It offers checks methods to allow customers to distribute and to receive money. 
	 */
	private class BasicAccount implements Account {

		/**
		 * A class for regular checks.
		 */
		private class RegularCheck extends Check{

			/**
			 * Creates a new regular check.
			 * @param aAmount  the amount.
			 * @param aPayee  the payee.
			 */
			private RegularCheck(long aAmount, AccountHolder aPayee) {
				super(aAmount, aPayee);
				if (aAmount>BasicAccount.this.getAmount()) throw new RuntimeException("Not enough money.");
			}

			@Override
			protected void transferTo(Account payeeAccount) {
				if (!isValid()) 
					throw new RuntimeException("This cheque ("+this.toString()+")is not valid.");			
				if (this.getPayee()!=payeeAccount.getHolder()) 
					throw new RuntimeException("Bad payee.");
				try {
					BasicAccount.this.fDeposit.debit(this.getAmount()) ;
					((BasicAccount) payeeAccount).fDeposit.credit(this.getAmount()) ;
					this.cancel();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("The debit failed.");
				}
			}

			@Override
			public AccountHolder getDrawer() {
				return fAccountHolder;
			}

		}

		/** The account holder. */
		private final AccountHolder fAccountHolder;

		/** The deposit. */
		protected final Deposit fDeposit;

		/** 
		 * A flag that indicates whether the account is open or closed.
		 * The account must be closed definitively after a bankruptcy. 
		 */
		protected boolean open=false;

		/**
		 * Creates a new regular account. 
		 * @param aAccountHolder the holder of the account
		 */
		private BasicAccount(AccountHolder aAccountHolder) {
			this.fAccountHolder = aAccountHolder;
			this.fDeposit = new Deposit();
			this.open = true;
		}

		/**
		 * Closes the account.
		 */
		protected void close() {
			this.open=false;
		}

		@Override
		final public void deposit(Check check) {
			if (!open)
				throw new RuntimeException("This account is closed.");
			check.transferTo(this);			
		}

		@Override
		public Object get(String key) {
			Object result = null;
			if (key.equals(Labels.MONEY)) {
				result=getAmount();
			}
			else if (key.equals(Labels.DEBT)) {
				result=getDebt();
			}
			return result;
		}

		@Override
		final public long getAmount() {
			return this.fDeposit.getAmount();
		}

		/**
		 * Returns 0 (there is no debt associated with a simple account).
		 * @return the debt.
		 */
		@Override
		public long getDebt() {
			return 0;
		}

		/**
		 * Returns the debtor status <code>GOOD</code> (no debt).
		 * @return <code>GOOD</code>. 
		 */
		@Override
		public Quality getDebtorStatus() {
			return Quality.GOOD;
		}

		@Override
		final public AccountHolder getHolder() {
			return this.fAccountHolder;
		}

		@Override
		public void lend(long principal) {
			throw new RuntimeException("This account is closed.");
		}

		@Override
		final public Check newCheck(long aAmount, AccountHolder aPayee) {
			if (!open) 
				throw new RuntimeException("This account is closed.");
			return new RegularCheck(aAmount,aPayee);
		}

	}

	/**
	 * A class for current accounts.
	 * <p>
	 * A current account encapsulates a deposit together with a list of debts.
	 * It offers checks methods to allow customers to distribute and to receive money. 
	 */
	private class CurrentAccount extends BasicAccount {

		/**
		 * Represents a debt of the account holder towards the bank.
		 */
		private class Loan implements java.lang.Comparable<Loan> {

			/** The due date. */
			private int lDueDate ;

			/** The funding date. */
			private	final int lFundingDate ;												

			/** The interest rate (monthly). */
			private double lInterestRate ;

			/** The period of the last call to payInterest() (to detect and avoid two interest payments for the same period). */
			private int lLastInterestPayment;

			/** The period of the last call to payBack() (to detect and avoid two payment for the same period). */
			private int lLastRepayment;

			/** The remaining principal. */
			private long lPrincipal ;

			/** The quality. */
			private Quality lQuality ;

			/** The term. */
			private	final int lTerm ;

			/**
			 * Creates a new loan.<br>
			 * Respects the principle "Credit makes deposit".
			 * @param aPrincipal - the principal.
			 * @param aQuality - the quality.
			 * @param aInterestRate - the rate.
			 * @param aTerm - the term.
			 */
			private Loan( 
					final long aPrincipal , 
					final Quality aQuality ,
					final double aInterestRate,
					final int aTerm
					) {
				if (aPrincipal <=0) throw new RuntimeException("The principal must be strictly positive.");
				if (aTerm<=0) throw new IllegalArgumentException("The term must be strictly positive.");
				this.lPrincipal = aPrincipal;
				this.lQuality = aQuality ;
				this.lInterestRate = aInterestRate;
				this.lTerm = aTerm ;
				this.lFundingDate = getCurrentPeriod().getValue() ;
				this.lDueDate = this.lFundingDate+this.lTerm ;
				this.lLastInterestPayment = this.lFundingDate-1;
				this.lLastRepayment = this.lFundingDate-1;
				CurrentAccount.this.fDeposit.credit(this.lPrincipal);
				CurrentAccount.this.fDebtList.add(this);
			}

			/**
			 * Cancels the loan.
			 * @return <code>true</code> if the loan has been cancelled, <code>false</code> otherwise.
			 */
			private boolean cancel() {
				if (this.lPrincipal<=0)
					throw new RuntimeException("Principal is negative or zero.");
				if (Bank.this.bankAccount.getAmount()==0)
					return false;
				if (Bank.this.bankAccount.getAmount()>=this.lPrincipal) {
					Bank.this.bankAccount.fDeposit.debit(this.lPrincipal);
					this.lPrincipal=0;
					return true;
				}
				else {
					this.lPrincipal-=Bank.this.bankAccount.getAmount();
					Bank.this.bankAccount.fDeposit.debit(Bank.this.bankAccount.getAmount());
					return false;
				}
			}

			/**
			 * Downgrades the loan quality.
			 */
			private void downgrade() {
				if (this.lQuality.isGood()) {
					this.lQuality=Quality.DOUBTFUL;
					this.lInterestRate=Bank.this.penaltyRate;
					return;
				}
				if (this.lQuality.isDoubtFul()) {
					if (Bank.this.pAccommodating) {
						// If the bank is accommodating then the loan is not downgraded.
						return; 
					}
					this.lQuality=Quality.BAD;
					return;
				}
				throw new RuntimeException("Unexpected quality.");
			}

			/**
			 * Returns the remaining principal.
			 * @return the principal.
			 */
			private long getPrincipal() {
				return this.lPrincipal;
			}

			/**
			 * Returns a flag that indicates whether the loan is due or not.
			 * @return a boolean.
			 */
			private boolean isDue() {
				if (lDueDate<getCurrentPeriod().getValue()) throw new RuntimeException("Due date is past.");
				return (lDueDate==getCurrentPeriod().getValue());
			}

			/**
			 * Pays back the loan.
			 */
			private void payBack() {
				if (lLastRepayment>=getCurrentPeriod().getValue()) throw new RuntimeException("Loan already repaid for this period.");
				if (this.lQuality.isBad()) {
					// Normally, a bad loan leads immediately to the failure of the borrower.
					// Thus, a bad loan would never be repaid.
					throw new RuntimeException("Unexpected bad loan.");
				}
				if ((this.lQuality.isGood())&(!this.isDue())) 
					return; // The loan is not due.
				long repayment = 0;
				if ((this.lQuality.isDoubtFul())&(!this.isDue()))
					repayment = Math.min(getAmount(), lPrincipal);
				else { 
					repayment = lPrincipal;
					if (repayment>getAmount()) {
						repayment = getAmount();
						lDueDate = lDueDate+lTerm;
						downgrade();
					};
				}
				if (repayment==0) return;
				try {
					CurrentAccount.this.fDeposit.debit(repayment);
					lPrincipal=lPrincipal-repayment;
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Borrower deposit can't be debited.");
				}
				lLastRepayment = getCurrentPeriod().getValue();
			}

			/**
			 * Pays the interest due.
			 */
			private void payInterest() {
				if (lLastInterestPayment>=getCurrentPeriod().getValue()) 
					throw new RuntimeException("Interest already paid.");
				final long interestPayment = (long) (lPrincipal*lInterestRate);
				if (interestPayment>0) {
					final long availableBalance = getAmount();
					if (availableBalance<interestPayment) {
						if (availableBalance>0) {
							fDeposit.debit(availableBalance);
						}
						lPrincipal = lPrincipal+interestPayment-availableBalance;
					}
					else {				
						fDeposit.debit(interestPayment);
					}
					Bank.this.bankAccount.fDeposit.credit(interestPayment);
				}
				lLastInterestPayment = getCurrentPeriod().getValue();
			}

			/**
			 * Implements the Comparable interface so that loans can easily be sorted.
			 * @param otherLoan the loan to compare against.
			 * @return <code>1</code> if the priority of the other loan is less than this,
			 * <code>0</code> if both have the same priority 
			 * and <code>-1</code> this priority is less than the others.
			 */
			@Override
			public int compareTo(Loan otherLoan) {
				if (this.lQuality==otherLoan.lQuality) {
					if (this.lDueDate<otherLoan.lDueDate) return -1;
					if (this.lDueDate>otherLoan.lDueDate) return 1;
					return 0;
				}
				if (this.lQuality==Quality.BAD) return -1;
				if (this.lQuality==Quality.GOOD) return 1;
				if (otherLoan.lQuality==Quality.BAD) return 1;
				return -1;
			}

			@Override
			public String toString() {
				return
						"Principal: "+lPrincipal
						+", Interest rate: "+lInterestRate
						+", Due date: "+lDueDate
						+", Is due: "+isDue()
						+", Time before payback: "+(lDueDate-getCurrentPeriod().getValue());
			}
		}

		/** The list of debts. */
		private final LinkedList<Loan> fDebtList = new LinkedList<Loan>();

		/**
		 * Creates a new current account. 
		 * @param aAccountHolder  the holder of the account.
		 */
		private CurrentAccount(AccountHolder aAccountHolder) {
			super(aAccountHolder);
		}

		/**
		 * Cancels the debt attached to this account.
		 * @return <code>true</code> if the debt has been successfully cancelled, <code>false</code> otherwise.
		 */
		private boolean cancelDebt() {
			if (!open) 
				throw new RuntimeException("This account is closed.");
			final Collection<Loan> cancelled = new HashSet<Loan>();
			for (Loan aLoan : this.fDebtList) {
				if (aLoan.cancel())
					cancelled.add(aLoan);
			}
			this.fDebtList.removeAll(cancelled);
			if (fDebtList.isEmpty())
				return true;
			else
				return false;
		}

		/**
		 * Pays interest due for each loan.
		 */
		private void payInterest() {
			if (!open) 
				throw new RuntimeException("This account is closed.");
			for (Loan loan: fDebtList) loan.payInterest();
		}

		/**
		 * Recovers loans.<br>
		 * Empty loans are removed.
		 */
		private void recover() {
			if (!open) 
				throw new RuntimeException("This account is closed.");
			Collections.sort(this.fDebtList) ;
			final LinkedList<Loan> tempList = new LinkedList<Loan>(); 
			while(!this.fDebtList.isEmpty()) {
				final Loan aLoan = this.fDebtList.removeFirst();
				aLoan.payBack();
				if(aLoan.lPrincipal>0) tempList.add(aLoan);
			}
			this.fDebtList.addAll(tempList);
		}

		/**
		 * Returns the total outstanding debt for this account.
		 * @return the debt.
		 */
		@Override
		public long getDebt() {
			long totalDebt = 0;
			for(Loan aLoan: fDebtList) {
				totalDebt = totalDebt + aLoan.getPrincipal();
			}
			return totalDebt;
		}

		/**
		 * Computes and returns the debtor status of this account.
		 * @return <code>GOOD</code>, <code>DOUBTFUL</code> or <code>BAD</code>. 
		 */
		@Override
		public Quality getDebtorStatus() {
			Quality debtorStatus = Quality.GOOD;
			for(Loan aLoan: fDebtList) {
				if (aLoan.lQuality==Quality.BAD) return Quality.BAD;
				if (aLoan.lQuality==Quality.DOUBTFUL) debtorStatus = Quality.DOUBTFUL;
			}
			return debtorStatus;
		}

		@Override
		public void lend(long principal) {
			if (!open) 
				throw new RuntimeException("This account is closed.");
			new Loan(principal,Quality.GOOD, Bank.this.pMonthlyInterestRate, Bank.this.pNormalTerm);
		}

	}

	/**
	 * Converts the given annual rate to the corresponding monthly rate.
	 * @param annualRate - the annual rate.
	 * @return the monthly rate.
	 */
	public static double yearly2Monthly(float annualRate) {
		return Math.pow( 1+annualRate , 1f/12 ) - 1 ;
	}

	/** The list of customers accounts. */
	private final LinkedList<CurrentAccount> accountsList;

	/** The bank own account (own funds). */
	private final BasicAccount bankAccount;

	/** The dataset. */
	private BankData bankData;

	/** The owner of the bank. */
	private CapitalOwner bankOwner ;

	/** A flag that indicates whether the bank the is bankrupt or not. */
	private boolean bankrupt = false;

	/** A flag that indicates whether the bank is accommodating or not. */
	private boolean pAccommodating = true;

	/** The penalty interest rate (monthly). */
	private double penaltyRate = 0;

	/** The normal interest rate (monthly). */
	private double pMonthlyInterestRate = 0;

	/** The normal term of credits. */
	private int pNormalTerm = 0;

	/** The propensity to distribute the exceeding capital. */
	private float propensityToDistributeCapitalExcess;

	/** The capital ratio targeted by the bank. */
	private float pTargetedCapitalRatio = 0f;

	@SuppressWarnings("javadoc")
	protected final String PARAM_ACCOMODATING = "Bank.accommodating";

	@SuppressWarnings("javadoc")
	protected final String PARAM_CAPITAL_PROP_TO_DISTRIBUTE_EXCESS = "Bank.capital.propensityToDistributeExcess";

	@SuppressWarnings("javadoc")
	protected final String PARAM_CAPITAL_TARGET = "Bank.capital.targetedRatio";

	@SuppressWarnings("javadoc")
	protected final String PARAM_RATE_NORMAL = "Bank.rate.normal";

	@SuppressWarnings("javadoc")
	protected final String PARAM_RATE_PENALTY = "Bank.rate.penalty";

	@SuppressWarnings("javadoc")
	protected final String PARAM_TERM = "Bank.term";

	/**
	 * Creates a new bank.
	 */
	public Bank() { 
		this.bankAccount = new BasicAccount(this); 
		this.accountsList = new LinkedList<CurrentAccount>() ;
	}

	/**
	 * Computes and returns the total amount of the doubtful debts.
	 * @return the sum of doubtful debts
	 */
	private long getDoubtfulDebts() {
		long doubtfulDebts = 0 ;
		for (CurrentAccount account : accountsList )
			if ( account.getDebtorStatus() != Quality.GOOD) doubtfulDebts+=account.getDebt()  ;
		return doubtfulDebts ;
	}

	/**
	 * Calculates and returns the total amount of assets of the bank.<br>
	 * The total amount of assets of the bank is the sum of the debts of the non-banking agents.
	 * @return the total amount of assets.
	 */
	private long getTotalAssets() {
		long totalAssets = 0;
		for(CurrentAccount aAccount: accountsList) {
			totalAssets = totalAssets + aAccount.getDebt();
		}
		return totalAssets;
	}

	/**
	 * Calculates and returns the total amount of liabilities of the bank.<br>
	 * The total amount of liabilities of the bank is the sum of the deposits of the non-banking agents.
	 * @return the total amount of assets.
	 */ 
	private long getTotalLiabilities() {
		long totalLiabilities = 0;
		for(CurrentAccount aAccount: accountsList) {
			totalLiabilities = totalLiabilities+aAccount.getAmount();
		}
		return totalLiabilities;
	}

	/**
	 * Updates the exogenous parameters of the bank.
	 */
	private void updateParameters() {
		this.pMonthlyInterestRate = yearly2Monthly(Float.parseFloat(Circuit.getParameter(this.PARAM_RATE_NORMAL)));
		this.penaltyRate = yearly2Monthly(Float.parseFloat(Circuit.getParameter(this.PARAM_RATE_PENALTY)));
		this.pTargetedCapitalRatio = Float.parseFloat(Circuit.getParameter(this.PARAM_CAPITAL_TARGET));
		this.propensityToDistributeCapitalExcess=Float.parseFloat(Circuit.getParameter(this.PARAM_CAPITAL_PROP_TO_DISTRIBUTE_EXCESS));
		this.pNormalTerm = Integer.parseInt(Circuit.getParameter(this.PARAM_TERM));
		this.pAccommodating = Boolean.parseBoolean(Circuit.getParameter(this.PARAM_ACCOMODATING));
	}

	/**
	 * Closes the bank.<br>
	 * Updates the balance sheet and updates statistics.
	 * Must be called at the end of each period.
	 * @param macroDataset - the macro dataset.
	 */
	public void close(PeriodDataset macroDataset) {	
		bankData.setCapital(bankAccount.getAmount());
		bankData.setDeposits(this.getTotalLiabilities());
		bankData.setDoubtfulDebts(this.getDoubtfulDebts());
		bankData.setLoans(this.getTotalAssets());
		if (bankData.getLoans()-bankData.getDeposits()!=bankData.getCapital())
			throw new RuntimeException("Inconsistency ("+(bankData.getLoans()-bankData.getDeposits())+","+bankData.getCapital()+")");
		macroDataset.compileBankData(bankData);
		if (bankData.getCapital()==0)
			this.bankrupt =true;
	}

	/**
	 * Recovers the debt.<br>
	 * Traverses the list of accounts, looking for due debts and trying to recover them.
	 * A very important method that simulates the monetary reflux.  
	 */
	public void debtRecovery() {
		if (bankrupt)
			throw new RuntimeException("The bank is bankrupt.");
		Collections.shuffle(accountsList, getRandom());
		for (CurrentAccount aAccount : accountsList ) 
			aAccount.payInterest();
		final LinkedList<CurrentAccount> bankruptList = new LinkedList<CurrentAccount>();
		for(CurrentAccount account : this.accountsList){
			account.recover();
			if (account.getDebtorStatus().isBad()) {
				if (!this.pAccommodating) {
					this.bankData.addBankruptcy();
					// The bank tries to cancel the debt of the bad debtor.
					final long nPLoans = account.getDebt();
					if (account.cancelDebt()) {
						this.bankData.nPLoans+=nPLoans;
						account.close();
						account.getHolder().goBankrupt();
						bankruptList.add(account);
					}
					else {
						// Bank failure.
						this.bankrupt = true;
					}
				}
			}
		}
		this.accountsList.removeAll(bankruptList);				
	}

	@Override
	public String getName() {
		return "The bank";
	}

	/**
	 * Returns a new account for the given holder. 
	 * @param holder - the account holder.
	 * @return the new account.
	 */
	public Account getNewAccount(AccountHolder holder) {
		if (bankrupt)
			throw new RuntimeException("The bank is bankrupt.");
		CurrentAccount newAccount = new CurrentAccount(holder);
		this.accountsList.add(newAccount);
		return newAccount;
	}

	/**
	 * IMPLEMENT
	 */
	@Override
	public void goBankrupt() {
		throw new RuntimeException("Not yet implemented.");
	}

	/**
	 * Returns a flag that indicates whether the bank is bankrupt or not.
	 * @return <code>true</code> if the bank is bankrupt, <code>false</code> otherwise.
	 */
	public boolean isBankrupt() {
		return bankrupt;
	}

	/**
	 * Opens the bank.<br>
	 * Usually called at the beginning of a period.
	 * Initializes the record of bank data for the period.
	 */
	public void open() {
		if (bankrupt)
			throw new RuntimeException("The bank is bankrupt.");
		bankData = new BankData();
		updateParameters();
	}

	/**
	 * Pays the dividend to the bank owner.
	 */
	public void payDividend() {
		if (bankrupt)
			throw new RuntimeException("The bank is bankrupt.");
		final long ownCapital = bankAccount.getAmount();
		final long totalAssets = this.getTotalAssets();
		final long requiredCapital = (long)(totalAssets*this.pTargetedCapitalRatio);
		final long excedentCapital = Math.max(0, ownCapital-requiredCapital);
		long dividend = (long) (excedentCapital*propensityToDistributeCapitalExcess);	
		if (dividend<0) throw new RuntimeException("Dividend must be positive.") ;
		if (dividend>0) {
			if (this.bankOwner==null){
				this.bankOwner=(CapitalOwner) Circuit.getResource(Circuit.SELECT_A_CAPITAL_OWNER);
			}
			if (this.bankOwner!=null){
				this.bankOwner.receiveDividend( this.bankAccount.newCheck(dividend, this.bankOwner) );
			} 
			else {
				dividend=0;
			}
		}
		this.bankData.setDividend(dividend);
	}

	/**
	 * Sets the owner of the bank.
	 * @param capitalist the new bank owner.
	 */
	public void setOwner(CapitalOwner capitalist) {
		if (bankrupt)
			throw new RuntimeException("The bank is bankrupt.");
		if (this.bankOwner!=null) 
			throw new RuntimeException("The bank owner is already defined.");  
		this.bankOwner = capitalist;
	}

}