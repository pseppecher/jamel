/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
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
 * aint with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.banks.goldstandard;

import jamel.austrian.roles.AccountHolder;
import jamel.austrian.roles.Shareholder;
import jamel.austrian.roles.Creditor;
import jamel.austrian.sfc.SFCAgent;
import jamel.austrian.banks.CommercialBank;
import jamel.austrian.banks.AbstractBankingSector;
import jamel.austrian.markets.Market;
import jamel.austrian.widgets.RegularAccount;
import jamel.austrian.widgets.AbstractCheque;
import jamel.austrian.widgets.CreditContract;
import jamel.austrian.widgets.InvestmentProject;
import jamel.austrian.widgets.Offer;
import jamel.austrian.widgets.Quality;
import jamel.austrian.widgets.TimeDeposit;
import jamel.basic.Circuit;
import jamel.basic.data.BasicAgentDataset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Represents the single representative bank.
 * <p>
 * Encapsulates a list of accounts.
 * <p>
 */
public class BasicCommercialBank extends SFCAgent implements CommercialBank {
		
	/** The bank's own account (own funds). */
	private final RegularAccount reserves;
	
	/** The loanable funds. */
	private final RegularAccount loanableFunds;

	/** The list of the customers' checking accounts. */
	private final HashMap<AccountHolder,RegularAccount> checkingAccounts;
	
	/** The list of the customers' savings accounts. */
	private final LinkedList<TimeDeposit> savingsAccounts;
	
	/** The loan book. */
	private final LinkedList<CreditContract> loanBook;
	
	/** New credit contracts. */
	private final LinkedList<CreditContract> newCreditContracts;
	
	/** The historic duration of loans. */
	private final LinkedList<Integer> loanDurations;

	/** The ownership structure of the firm (in percentages of ownership). */
	private HashMap<Shareholder, Number> owners ;

	/** The market for savings. */
	private final Market savingsMarket ;
	
	/** The market for loans. */
	private final Market loanMarket ;
	
	/** The bank's offer in the market for loans. */
	private Offer creditOffer;
	
	/** The interest rate on loans (monthly). */
	private float lendingRate;
	
	/** The interest rate on loans in the next time period. */
	private float nextLendingRate;
	
	/** The offered credit volume */
	private int offeredCredit;
	
	/** The targeted credit volume */
	private int targetedLendingVolume;
	
	/** The newly issued credit volume. */
	private int newCredit;

	/** The bank's offer in the market for savings. */
	private Offer savingsOffer;
	
	/** New time deposits */
	private int newSavings;
	
	/** The interest rate on savings (monthly). */
	private float savingsRate;
	
	/** The interest rate on savings in the previous time period. */
	private float lastSavingsRate;
	
	/** The interest rate on savings in the next time period. */
	private float nextSavingsRate;
	
	/** Interest payments effectuated. */
	private int interestPaid;
	
	/** Interest payments received. */
	private int interestReceived;
	
	/** Redemption payments effectuated. */
	private int redemptionPaid;
	
	/** Redemption payments received. */
	private int redemptionReceived;
	
	/** The dividend. */
	private int dividend;

	private float averageRevenue;
	
	private float averageCost;

	private int writedowns;
	
	private int profit;

	private Quality debtorQuality;

	private int equity;

	private int loans;

	private int timeDepositVolume;
	
	private final int subscribedCapital;

	@SuppressWarnings("unused")
	private int requiredReserves;

	private int fundingGap;
	
	private boolean illiquid;

	@SuppressWarnings("unused")
	private boolean isbankrupt;

	/** At one point a firm said: "you are too expensive!" */
	private boolean rejectedSavingsOffer;

	private int transactionVolume;

		

	/**
	 * Creates a new bank with a money endowment.
	 */
	public BasicCommercialBank(String aName, Circuit aCircuit, BankingSector aSector, Float lendingRate, Float savingsRate, Integer money) { 
		super(aName, aCircuit, aSector);
		reserves = new RegularAccount(this);
		reserves.credit(money);
		subscribedCapital = money;
		loanableFunds = new RegularAccount(this);
		checkingAccounts = new HashMap<AccountHolder, RegularAccount>() ;
		savingsAccounts = new LinkedList<TimeDeposit>() ;
		loanBook = new LinkedList<CreditContract>() ;
		newCreditContracts = new LinkedList<CreditContract>() ;
		loanDurations = new LinkedList<Integer>();
		for (int i=0; i<12; i++) loanDurations.add(1);
		savingsMarket = getMarket("savings");
		loanMarket = getMarket("loans");
		debtorQuality = Quality.GOOD;
		nextLendingRate = lendingRate; 
		nextSavingsRate = savingsRate;
		fundingGap = 0;
	}

	
	
	/**
	 * Defines the ownership of banks after initialization.
	 */
	public void setOwnershipStructure() {
		owners = getHouseholdsSector().selectRandomShareholders(parameters.numOwners);
	}
	
	
	/**
	 * Creates a new bank out of existing funds.
	 */
	public BasicCommercialBank(InvestmentProject project, Circuit aCircuit,BankingSector aSector, Float lendingRate, Float savingsRate) { 
		super("Bank"+project.getSpecificID(), aCircuit, aSector);
		reserves = new RegularAccount(this);
		AbstractCheque cheque = sector.getInvestmentBank().underwrite(project.getID());
		reserves.deposit(cheque);
		subscribedCapital = cheque.getAmount();
		owners = project.getOwners();
		for (Shareholder owner:owners.keySet()) owner.newEquityHolding(this);
		loanableFunds = new RegularAccount(this);
		checkingAccounts = new HashMap<AccountHolder, RegularAccount>() ;
		savingsAccounts = new LinkedList<TimeDeposit>() ;
		loanBook = new LinkedList<CreditContract>() ;
		newCreditContracts = new LinkedList<CreditContract>() ;
		loanDurations = new LinkedList<Integer>();
		for (int i=0; i<12; i++) loanDurations.add(1);
		savingsMarket = getMarket("savings");
		loanMarket = getMarket("loans");
		debtorQuality = Quality.GOOD;
		nextLendingRate = lendingRate; 
		nextSavingsRate = savingsRate;
		fundingGap = 0;
	}
	
	
	/**
	 * Creates a new account for the given holder. 
	 * @param holder - the account holder.
	 * @param money - the initial account balance.
	 */
	public void getNewAccount(AccountHolder holder, int money) {
		RegularAccount newAccount = new RegularAccount(holder);
		checkingAccounts.put(holder, newAccount);
		newAccount.credit(money);
	}

	
	/** 
	 * Initiates a money transaction.
	 */
	public AbstractCheque newCheque(AccountHolder customer, int amount){
		if (!checkingAccounts.containsKey(customer)) throw new RuntimeException("You don't have an account here.");
		return checkingAccounts.get(customer).newCheque(amount, this);
	}
	
	
	/** 
	 * Finalizes a money transaction.
	 */
	public void deposit(AccountHolder customer, AbstractCheque cheque){
		if (!checkingAccounts.containsKey(customer)) throw new RuntimeException("You don't have an account here.");
		checkingAccounts.get(customer).deposit(cheque);
		transactionVolume +=  cheque.getAmount();
	}
	
	
	/** 
	 * Informs a customer about his account balance.
	 */
	public int getBalance(AccountHolder customer){
		if (!checkingAccounts.containsKey(customer)) throw new RuntimeException("You don't have an account here.");
		return checkingAccounts.get(customer).getBalance();		
	}
	

	/** 
	 * Informs a customer about his account balance.
	 */
	public void closeAccount(AccountHolder customer){
		if (!checkingAccounts.containsKey(customer)) throw new RuntimeException("You don't have an account here.");
		if (checkingAccounts.get(customer).getBalance()>0) throw new RuntimeException("You still have money.");
		checkingAccounts.remove(customer);		
	}
	
	
	/**
	 * Opens the bank.
	 */
	public void open() {


		newSavings = 0;
		newCredit = 0;
		newCreditContracts.clear();
		interestReceived = 0;
		interestPaid = 0;
		redemptionPaid = 0;
		redemptionReceived = 0;
		writedowns = 0;
		transactionVolume = 0;
		profit = 0;
		
		lendingRate = nextLendingRate;
		savingsRate = nextSavingsRate;

		targetedLendingVolume = 0;
		offeredCredit = 0;		
		creditOffer = null;
		savingsOffer = null;
		rejectedSavingsOffer = false;
		illiquid= false;
		this.data = new BasicAgentDataset(this.name);
	}
	
	
	
	/** 
	 * Makes offers in the market for savings and loans. 
	 */
	public void makeOffer(){
		
		if (debtorQuality==Quality.BAD) return;
		
		savingsOffer = new Offer(this, 1, savingsRate);
		savingsMarket.newOffer(savingsOffer);
		
		offeredCredit = setTargetCreditVolume();
		if (offeredCredit==0) return;
		creditOffer = new Offer(this,offeredCredit,lendingRate);	
		loanMarket.newOffer(creditOffer);	
	}
	
	
	/**
	 * Sets the quantity target for the lending operations.
	 * @return the offered credit volume.
	 */
	private int setTargetCreditVolume(){
		
		// Sets the target
		if (loanBook.size()>0){
			int totalDuration = 0;
			for (int duration : loanDurations) totalDuration += duration;
			int averageLoanDuration = totalDuration  / loanDurations.size();
			float targetFactor = (averageLoanDuration+1) / 2f;		
			targetedLendingVolume = (int) (timeDepositVolume * parameters.lendingBuffer / targetFactor);  // >0
		}
		else {
			//TODO: makes sense?
			targetedLendingVolume = (int) (timeDepositVolume * parameters.lendingBuffer); 
		}
		
		// Looks if the target is feasible
		if (targetedLendingVolume >= loanableFunds.getBalance()){
			targetedLendingVolume = loanableFunds.getBalance();
		}
		
		return targetedLendingVolume;
		
	}

	
	/** 
	 * Adds a new lending contract and pays out the sum to the debtor.<br>
	 * Called by the firm who gets the loan. 
	 */
	public AbstractCheque acceptDebtor(CreditContract newContract){
		
		loanBook.add(newContract);
		newCreditContracts.add(newContract);
		loanDurations.removeFirst();
		loanDurations.add(newContract.getTerm());
		AbstractCheque cheque = loanableFunds.newCheque(newContract.getVolume(), this);
		newCredit +=newContract.getVolume();
		loanMarket.updateOffer(creditOffer, newContract.getVolume());
		loanMarket.registerSale(creditOffer, newContract.getVolume(), false);
		return cheque;
	}
	
	
	/**
	 * Takes note that his credit offer has been rejected (due to prices).
	 */
	public void notifyRejection() {
		rejectedSavingsOffer = true;
	}
	
	
	/**
	 * Updates the interest rates.<br>
	 */
	public void updateInterestRates(){
		
		setLendingRate();
		setSavingsRate();
	}

	/** 
	 * Sets the interest rate for loans to a perceived revenue-maximizing level. 
	 */
	private void setLendingRate(){
		
		if (creditOffer==null) return;
		float alpha = getRandom().nextFloat();
		float markup = parameters.bankMarkup;
		
		if (newCredit<targetedLendingVolume) {
			nextLendingRate -= parameters.rFlex * alpha;
			if (nextLendingRate < nextSavingsRate + markup) nextLendingRate =  nextSavingsRate + markup;
		}
		else if (newCredit>=targetedLendingVolume) {
			nextLendingRate = lendingRate + parameters.rFlex * alpha;
			//if (nextLendingRate>1) nextLendingRate = 1;
		}	
	}
	
	
	/** 
	 * Sets the interest rate for savings to a perceived profit-maximizing level. 
	 */
	private void setSavingsRate(){
		
		if (savingsOffer==null) return;
		
		 //Computes the average cost and the average revenue of the bank (per time period).
		float averageRevenue = 0;
		for (CreditContract loan:loanBook){
			int duration = loan.getTerm();
			for (int t=0;t<duration;t++){ //TODO: maybe use volume and interpret it as prospective average profits.
				averageRevenue += loan.getFaceValue()*loan.getInterestRate()*(1-t/duration); //formula simplified.
			}			
		}
		float averageCost = 0;
		for (TimeDeposit deposit : savingsAccounts){  
			averageCost += ( deposit.getVolume() * (Math.pow(1+deposit.getInterestRate(),deposit.getTerm())-1) ) / deposit.getTerm();
		}
		
		
		if (this.averageCost!=0){
			
			float alpha = getRandom().nextFloat(); 
			float delta = alpha* parameters.rFlex;
			float currentProfitability = averageRevenue/averageCost;
			float lastProfitability = this.averageRevenue/this.averageCost;
			float deltaS = savingsRate - lastSavingsRate;
			float D1profitability = (currentProfitability-lastProfitability)/deltaS;
			float minSavingsRate = parameters.minSavingsRate;
			float markup = parameters.bankMarkup;
			
			if (D1profitability>0 | rejectedSavingsOffer){
				// either: higher profitability and higher rate
				// or: lower profitability and lower rate
				lastSavingsRate = savingsRate;
				nextSavingsRate = savingsRate + delta;
				if (nextSavingsRate > nextLendingRate-markup) nextSavingsRate = nextLendingRate-markup;
				if (nextSavingsRate < minSavingsRate) nextSavingsRate = minSavingsRate;
			}
			else if (D1profitability<0){
				// either: lower profitability and higher rate
				// or: higher profitability and lower rate
				lastSavingsRate = savingsRate;
				nextSavingsRate = savingsRate - delta;
				if (nextSavingsRate < minSavingsRate) nextSavingsRate =minSavingsRate;
			}
		}
		else{
			if (rejectedSavingsOffer){
				float alpha = getRandom().nextFloat(); 
				float delta = alpha* parameters.rFlex;
				float minSavingsRate = parameters.minSavingsRate;
				float markup = parameters.bankMarkup;
				lastSavingsRate = savingsRate;
				nextSavingsRate = savingsRate + delta;
				if (nextSavingsRate > nextLendingRate-markup) nextSavingsRate = nextLendingRate-markup;
				if (nextSavingsRate < minSavingsRate) nextSavingsRate = minSavingsRate;
			}
		}
		
		this.averageRevenue = averageRevenue;
		this.averageCost = averageCost;	
	}
	
	
	/**
	 * Receives money from a saver.
	 */
	public void acquireFunding(TimeDeposit timeDeposit, AbstractCheque cheque){
		
		loanableFunds.deposit(cheque);		
		savingsAccounts.add(timeDeposit);
		newSavings +=cheque.getAmount();
		requiredReserves += timeDeposit.getInterest();
	}
	
	
	/**
	 * Receives interest payments from a firm.
	 */
	public void receiveInterestPayment(AbstractCheque cheque){
		
		reserves.deposit(cheque);		
		interestReceived += cheque.getAmount();
	}
	
	
	/**
	 * Receives redemption payments from a firm and reduces the volume of outstanding debt.
	 */
	public void receiveRedemption(AbstractCheque cheque){
		
		loanableFunds.deposit(cheque);	
		redemptionReceived += cheque.getAmount() ;	
		
	}
	
	
	/**
	 * Takes note of a default.
	 */
	public void notifyDefault(CreditContract contract){
		if (!loanBook.remove(contract)) throw new RuntimeException("The contract does not exist.");
		writedowns += contract.getVolume();
	}

	
	/**
	 * Pays debt obligations, files bankruptcy if obligations can't be paid.<br>
	 * If the bank does not have liquidity problems it separates redemption and interest payments
	 * in the sense that it uses loanable funds for the former and reserves for the latter. It does so
	 * to facilitate its strategic reasoning.<br>
	 * In situations of liquidity shortage, however, cash is cash.
	 */
	public void payInterest() {

		// Bestandsaufnahme
		int totalInterestObligations = 0, totalRedemptionObligations = 0;
		int timeDepositVolume = 0, outstandingLoans = 0, excessLoanableFunds = 0;
		LinkedList<TimeDeposit> interestObligations = new LinkedList<TimeDeposit>();
		LinkedList<TimeDeposit> redemptionObligations = new LinkedList<TimeDeposit>();
		
		for (TimeDeposit deposit : savingsAccounts) {
			timeDepositVolume += deposit.getVolume();
			if (deposit.isDue()){
				if (deposit.getInterest()>0) {
					interestObligations.add(deposit);
					totalInterestObligations += deposit.getInterest();
				}
				if (deposit.getVolume()>0){
					redemptionObligations.add(deposit);
					totalRedemptionObligations += deposit.getVolume();
				}
			}
		}
		for (CreditContract loan : loanBook){
			outstandingLoans += loan.getVolume();
		}
		
		// Does some internal clearing.
		excessLoanableFunds = outstandingLoans + loanableFunds.getBalance() - timeDepositVolume;
		if (excessLoanableFunds>0){
			if (loanableFunds.getBalance()>=excessLoanableFunds){
				loanableFunds.debit(excessLoanableFunds);
				reserves.credit(excessLoanableFunds);
			}
			else{
				reserves.credit(loanableFunds.getBalance());
				loanableFunds.reset();
			}
		}
				
		if ( interestObligations.size() + redemptionObligations.size() == 0 ) return;
	
		
	
		// Makes the interest payments.
		int reserves = this.reserves.getBalance();
		float creditorShare = 1; //The percentage of the interest obligations which can be paid.
		int remainder = 0;
		if (reserves < totalInterestObligations){
			illiquid = true;
			debtorQuality = Quality.BAD;
			//The bank has insufficient reserves. They are distributed proportionally among the creditors.
			creditorShare = (float) (reserves) / (float) (totalInterestObligations);
			int i = 0;
			for ( TimeDeposit deposit : interestObligations ) {
				int requiredInterest =  deposit.getInterest();
				int creditorPayment = (int)(creditorShare * requiredInterest); 
				i += creditorPayment;			
			}
			remainder = reserves - i; // < obligations.size().
		}

		int j = 0;
		for ( TimeDeposit deposit : interestObligations ) {
			j++;
			Creditor creditor = deposit.getCreditor();
			int requiredInterest = deposit.getInterest();
			int interestPayment =  (int) (creditorShare * requiredInterest);
			if (j<=remainder) interestPayment += 1;
			
			if (interestPayment>requiredInterest) throw new RuntimeException("Too much interest.");
			
			if (interestPayment>0) {
				AbstractCheque cheque = this.reserves.newCheque(interestPayment, this);
				creditor.receiveInterestPayment(cheque);
				interestPaid +=interestPayment;
				requiredReserves -= interestPayment;
				deposit.interestPayment(interestPayment);		
			}
			if (deposit.isSettled()) creditor.notifySettlement(deposit);
			else deposit.deferPayment();
		}
		
	
		if (illiquid==false & interestPaid!=totalInterestObligations) throw new RuntimeException("False computation of payments");
		if (illiquid==true & this.reserves.getBalance()!=0) throw new RuntimeException("False computation of payments");
		if (interestPaid > totalInterestObligations) throw new RuntimeException("False computation of payments");


		// Does some internal clearing.
		
		clearing();
		
				
		// Makes the redemption payments.

		int loanableFunds = this.loanableFunds.getBalance();
		float creditorShare_2 = 1; //The percentage of the redemption obligations which can be paid.
		int remainder_2 = 0;
		if (loanableFunds < totalRedemptionObligations){
			if (this.reserves.getBalance()>=totalRedemptionObligations-loanableFunds){
				this.reserves.debit(totalRedemptionObligations-loanableFunds);
				this.loanableFunds.credit(totalRedemptionObligations-loanableFunds);
			}
			else{
				illiquid = true;
				debtorQuality = Quality.BAD;
				this.loanableFunds.credit(this.reserves.getBalance());
				this.reserves.reset();
				loanableFunds = this.loanableFunds.getBalance();
				//The bank has insufficient loanable funds. They are distributed proportionally among the creditors.
				creditorShare_2 = (float) (loanableFunds) / (float) (totalRedemptionObligations);
				int i = 0;
				for ( TimeDeposit deposit : redemptionObligations ) {
					int requiredRedemption =  deposit.getVolume();
					int creditorPayment = (int)(creditorShare_2 * requiredRedemption); 
					i += creditorPayment;			
				}
				remainder_2 = loanableFunds - i; // < obligations.size().
			}
		}
		
		int k = 0;
		for ( TimeDeposit deposit : redemptionObligations ) {
			k++;
			Creditor creditor = deposit.getCreditor();
			int requiredRedemption = deposit.getVolume();
			int redemptionPayment = (int) (creditorShare_2 * requiredRedemption);
			if (k<=remainder_2) redemptionPayment += 1;
						
			if (redemptionPayment>0) {
				AbstractCheque cheque = this.loanableFunds.newCheque(redemptionPayment, this);
				creditor.receiveRedemption(cheque);
				redemptionPaid += redemptionPayment;
				deposit.redemptionPayment(redemptionPayment);
			}
			if (deposit.isSettled()) creditor.notifySettlement(deposit);
			else deposit.deferPayment();
		}
			
		if (illiquid==false & redemptionPaid!=totalRedemptionObligations) throw new RuntimeException("False computation of payments");
		if (redemptionPaid > totalRedemptionObligations) throw new RuntimeException("False computation of payments");

		if (creditorShare_2 < 1 & this.reserves.getBalance()!=0) throw new RuntimeException("False computation of payments");
		if (creditorShare_2 < 1 & this.loanableFunds.getBalance()!=0) throw new RuntimeException("False computation of payments");

	}

	
	/** 
	 * The bank replenishes the loanable funds by transferring cash from the reserves to the loanable funds.
	 */
	private void clearing(){
		
		if (fundingGap>0){
			if (fundingGap<=reserves.getBalance()){
				reserves.debit(fundingGap);
				loanableFunds.credit(fundingGap);
				fundingGap = 0;
			}
			else{
				fundingGap -= reserves.getBalance();
				loanableFunds.credit(reserves.getBalance());
				reserves.reset();
			}
		}
		
		if (writedowns>0){
			if (writedowns<=reserves.getBalance()){
				reserves.debit(writedowns);
				loanableFunds.credit(writedowns);
			}
			else{
				fundingGap += writedowns-reserves.getBalance();
				loanableFunds.credit(reserves.getBalance());
				reserves.reset();
			}
		}
	}
	


	/** Does the liquidity management.<br>
	 * 	Must be called after interest and redemption have been paid.<br>
	 *  Makes the dividend payments. */
	public void updateProfits(){

		profit = interestReceived 
				- interestPaid
				- writedowns;
				
		int requiredReserves = subscribedCapital; 
		if (reserves.getBalance() > requiredReserves) dividend = reserves.getBalance()-requiredReserves;
		else dividend = 0;
		
		if (dividend > 0){
			
			int dividendPaid=0;
			for (Shareholder owner:owners.keySet()){
				int dividend = (int) (this.dividend * owners.get(owner).floatValue());
				if (dividend>0) owner.receiveDividend(reserves.newCheque(dividend, this));
				dividendPaid+=dividend;
			}
											
			int dividendRemainder = dividend - dividendPaid;
			if (dividendRemainder>owners.size()) throw new RuntimeException("Problem with dividend calculation.");
			if (dividendRemainder>0) {
				LinkedList<Shareholder> copyOfOwners = new LinkedList<Shareholder>();
				copyOfOwners.addAll(owners.keySet());
				Collections.shuffle(copyOfOwners);
				while (dividendRemainder>0){
					copyOfOwners.removeFirst().receiveDividend(reserves.newCheque(1, this));
					dividendPaid+=1;
					dividendRemainder-=1;
				}
			}
			if (dividendPaid!=dividend) throw new RuntimeException("False dividend calculation.");	
		}
		
		
		isbankrupt = false;  //TODO: remove?
		if (debtorQuality == Quality.BAD){
			if (loanBook.size()==0){
				Iterator<TimeDeposit> iter = savingsAccounts.iterator();
				while (iter.hasNext()) if (iter.next().isSettled()) iter.remove();
				if (savingsAccounts.size()>0 && loanableFunds.getBalance()+reserves.getBalance() == 0){//The bank cannot pay its obligations.
					for (Shareholder owner:owners.keySet()) owner.notifyDefault(this);
					for (TimeDeposit deposit : savingsAccounts) deposit.getCreditor().notifyDefault(deposit);
					savingsAccounts.clear();
					((AbstractBankingSector) sector).bankruptcy(this);
					isbankrupt = true;
				}
				else if (loanableFunds.getBalance() + reserves.getBalance()==0){	//The bank has no capital to resume business.
					//TODO: for (Shareholder owner:owners.keySet()) owner.notifyDefault(this);
					// ?
					((AbstractBankingSector) sector).bankruptcy(this);
					isbankrupt= true;
					
				}
				else{															//The bank was only temporarily illiquid.
					debtorQuality = Quality.GOOD;
					nextLendingRate = getMarket("loans").getPriceLevel();
					savingsRate = getMarket("savings").getPriceLevel();
				}
			}
		}
		
		
	}
	
	
	
	/**
	 * Closes the bank and updates the balance sheet.
	 */
	public void close() {	

		Iterator<TimeDeposit> iter = savingsAccounts.iterator();
		while (iter.hasNext()) if (iter.next().isSettled()) iter.remove();
		Iterator<CreditContract> iter2 = loanBook.iterator();
		while (iter2.hasNext()) if (iter2.next().isSettled()) iter2.remove();
		
		
		loans = 0;
		timeDepositVolume = 0;
		for(TimeDeposit contract : savingsAccounts)	timeDepositVolume += contract.getVolume();
		for(CreditContract contract : loanBook) loans += contract.getVolume();
		
		equity = reserves.getBalance()
				+loanableFunds.getBalance()
				+loans
				-timeDepositVolume;
		
		updateData();
		
	}
	
	
	/**
	 * Updates data at the end of a period.
	 */
	private void updateData() {
		

		data.put("newSavings" , (double) newSavings);
		data.put("offeredCredit" , (double) offeredCredit);
		data.put("lendingTarget" , (double) targetedLendingVolume);
		data.put("newLoans" , (double) newCredit);
		data.put("creditAllocationRatio" , (double) newCredit / (double) offeredCredit);
		data.put("newLoanContracts" , (double) newCreditContracts.size());

		data.put("savingsRedemptions" , (double) redemptionPaid);
		data.put("loanRedemptions" , (double) redemptionReceived);
		data.put("interestPaidOnLoans" , (double) interestReceived);
		data.put("interestPaidOnSavings" , (double) interestPaid);
		
		data.put("moneyHoldings" , (double) loanableFunds.getBalance()+reserves.getBalance());
		data.put("transactionVolume", (double) transactionVolume);
		data.put("timeDeposits" , (double) savingsAccounts.size());
		data.put("timeDepositsVolume" , (double) timeDepositVolume);
		data.put("outstandingLoans" , (double) loans);
		data.put("loanableFunds" , (double) loanableFunds.getBalance());
		data.put("reserves" , (double) reserves.getBalance());
		
		if (illiquid) data.put("illiquidBanks" , 1d);
		else data.put("illiquidBanks" , 0d);
		
		if (profit>0) {
			data.put("profits" , (double) profit);
			data.put("losses" , 0d);
		}
		else {
			data.put("profits" , 0d);
			data.put("losses" , (double) profit);
		}
		if (averageCost!=0) data.put("profitability" , (double) averageRevenue / (double) averageCost);
		data.put("dividendsPaid" , (double) dividend);
		data.put("equity" , (double) equity);
		data.put("registeredCapital" , (double) subscribedCapital);
		
	}
	
	
	public void terminateOperations() {
		CommercialBank otherBank;
		do otherBank = ((AbstractBankingSector) sector).selectRandomBank(); while (otherBank==this);
		for (AccountHolder accountHolder : checkingAccounts.keySet()){
			otherBank.getNewAccount(accountHolder, 0);
			accountHolder.getNewBank(otherBank);
			int deposit = checkingAccounts.get(accountHolder).getBalance();
			if (deposit>0){
				AbstractCheque transfer = checkingAccounts.get(accountHolder).newCheque(deposit, this);
				otherBank.deposit(accountHolder, transfer);		
			}
		}	
	}
		
	/**
	 * Returns the equity value of a specific owner.
	 */
	public int getEquityValue(Shareholder owner) {
		if (!owners.containsKey(owner)) throw new RuntimeException("Invalid ownership");
		float share = owners.get(owner).floatValue();
		return (int) (equity*share); 
	}

	
	@Override
	public void notifyRedemption(CreditContract contract) {
		//Never called
	}
	
	@Override
	public void getNewBank(CommercialBank bank) {
		//Never called
	}
	
	@Override
	public void notifyDefault(TimeDeposit timeDeposit) {
		//Never called
	}

	@Override
	public void notifySettlement(TimeDeposit timeDeposit) {
		//Never called
	}

}