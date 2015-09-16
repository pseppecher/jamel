package jamel.austrian.households;



import java.util.HashMap;
import java.util.LinkedList;

import jamel.austrian.banks.Bank;
import jamel.austrian.banks.CommercialBank;
import jamel.austrian.markets.Market;
import jamel.austrian.roles.AccountHolder;
import jamel.austrian.roles.Debtor;
import jamel.austrian.roles.Employer;
import jamel.austrian.roles.Offerer;
import jamel.austrian.roles.Seller;
import jamel.austrian.sfc.SFCAgent;
import jamel.austrian.sfc.SFCSector;
import jamel.austrian.util.Alphabet;
import jamel.austrian.widgets.Cheque;
import jamel.austrian.widgets.CreditContract;
import jamel.austrian.widgets.Offer;
import jamel.austrian.widgets.TimeDeposit;
import jamel.basic.Circuit;

/**
 * The representation of a household.
 */
public class BasicHousehold extends SFCAgent implements Household{
	
	
	@SuppressWarnings("javadoc")
	private final static String STRUCTURE_LENGTH = "prefs.structureLength";
	
	@SuppressWarnings("javadoc")
	private final static String TYPE_RANGE = "prefs.typeRange";
	
	@SuppressWarnings("javadoc")
	private final static String SKEWNESS = "prefs.skewness";
	
	@SuppressWarnings("javadoc")
	private final static String SAVINGS = "prefs.savings";
	
	@SuppressWarnings("javadoc")
	private final static String HORIZONS = "prefs.horizons";
	
	@SuppressWarnings("javadoc")
	private final static String EQUITY = "prefs.equity";
	
	@SuppressWarnings("javadoc")
	private final static String MONEY_DEMAND = "prefs.moneyDemand";
	
	@SuppressWarnings("javadoc")
	private final static String TIME_PREF = "prefs.timePref";
	
	@SuppressWarnings("javadoc")
	private final static String TIME_PREFFLEX = "prefs.timePrefFlex";
	
	@SuppressWarnings("javadoc")
	private final static String WAGE_OFFSET = "prefs.wageOffset";
	
	@SuppressWarnings("javadoc")
	private final static String WAGE_LEISUREFLEX = "prefs.wageLeisureFlex";
	
	@SuppressWarnings("javadoc")
	private final static String WAGE_WEALTHFLEX = "prefs.wageWealthFlex";	
	
	/** Enumerates the employment status of the household.*/
	public enum EmploymentStatus {

		EMPLOYED, INVOLONTARILY_UNEMPLOYED, VOLONTARILY_UNEMPLOYED;

		public boolean isEmployed() {
			return (this==EMPLOYED);
		}

		boolean isInvolontarily_unemployed() {
			return (this==INVOLONTARILY_UNEMPLOYED);
		}

		boolean isVolontarily_unemployed() {
			return (this==VOLONTARILY_UNEMPLOYED);
		}
	}

	/** The bank which manages the payments of the household. */
	private CommercialBank bank ;
	
	/** The list of time deposits. */
	private final LinkedList<TimeDeposit> timeDeposits;

	/** The preferences of the household.<p>
	 * 	Contains the keys to the markets in which the desired goods are sold. */
	private LinkedList<String> preferences ;
	
	/** The list of the companies and banks that the household holds shares of. */
	private LinkedList<Debtor> equityHoldings ;
	
	/** The expected receivable amounts of money (entries refer to successive future time periods). */
	private LinkedList<Integer> expectedFutureMoney ;
	
	/** The preferences which are updated in the course of a time period. */
	private LinkedList<String> intendedActions ;
	
	/** The flexibility of reservation prices. */
	private HashMap<String,Integer> moneyDemand; 
	
	/**  */
	private int actionCounter;
		
	/** The purchases that the household has effectuated in the current time period. */
	private HashMap<String,Integer> purchases ;
	
	/** The employment status of the last time period. */
	private EmploymentStatus employmentStatus;
	
	/** The employment status of the last time period. */
	private int employmentDuration = 0;

	/** The reservation wage. */
	private float reservationWage;

	/**  */
	private float purchasingPowerEstimate;

	private int totalBudget;

	private int jobs;

	private int newTimeDeposits;
	
	private int redeemedTimeDeposits;
	
	private int defaultedTimeDeposits;

	private int realConsumption;
	
	private int nominalConsumption;

	private HashMap<String, Integer> occurenceOfPreference;
	
	private LinkedList<Integer> equityRecord;
	
	private LinkedList<Integer> equityGainRecord;
	
	private float returnOnEquityEstimate = Float.NaN;

	private LinkedList<Offerer> rejectionList;

	private int dividendsReceived;

	private int wagesReceived;

	private int zeroVolumeDeposits;

	/**
	 * Creates a household.
	 * @param name the name of the new household.
	 * @param sector the households sector.
	 */
	public BasicHousehold(String name, Circuit aCircuit, SFCSector sector) {
		super(name, aCircuit, sector);
		this.bank = getBankingSector().selectRandomBank();
		this.bank.getNewAccount(this, initialConditions.initialMoney);
		this.timeDeposits = new LinkedList<TimeDeposit>();
		this.employmentStatus = EmploymentStatus.VOLONTARILY_UNEMPLOYED;
		this.equityHoldings = new LinkedList<Debtor>();
		this.purchases = new HashMap<String, Integer>();
		this.expectedFutureMoney = new LinkedList<Integer>();
		this.equityGainRecord = new LinkedList<Integer>();
		this.equityRecord = new LinkedList<Integer>();
		for (int i=0; i<12; i++) {
			equityGainRecord.add(0);
			equityRecord.add(0);
		}
		this.rejectionList = new LinkedList<Offerer>();
		setPreferences();
	}



	

	/**
	 * Initializes the preferences of a household.<br>
	 * The preferences must be specified as settings = "x,y,z,w";		
	 */
	private void setPreferences() {							
		int structureLength = sector.getParam(STRUCTURE_LENGTH).intValue();
		int typeRange = sector.getParam(TYPE_RANGE).intValue();
		float skewness = sector.getParam(SKEWNESS);
		int savings = sector.getParam(SAVINGS).intValue();
		int horizons = sector.getParam(HORIZONS).intValue();
		int equity = sector.getParam(EQUITY).intValue();
		int moneyDemand = sector.getParam(MONEY_DEMAND).intValue();
		
		this.preferences = new LinkedList<String>();
		this.moneyDemand = new HashMap<String, Integer>();
		this.occurenceOfPreference = new HashMap<String, Integer>();
		for (int i=0; i<horizons; i++) expectedFutureMoney.add(0);
			
		for (int i=1;i<=structureLength;i++){
			//TODO: The choice of HORIZONS=4 seems critical for the smoothness of the results. Why?
			String preference = new String();
			if (i % savings == 0){    							//every n-th action is a savings action
				int horizon = 1 + random.nextInt(horizons-1);	
				preference = "savings,"+horizon;
			}					
			else if (i % equity == 0){							//every n-th action is an equity purchase
				preference = "equity";		
			}
			else {
				float variation = (float) Math.pow(getRandom().nextFloat(),skewness);
				int numericPreference = 1 + Math.round( variation * (typeRange - 1) );
				if (numericPreference>typeRange) numericPreference = typeRange;
				String preferenceType = Alphabet.getLetter(numericPreference);
				preference = preferenceType+0;
				purchases.put(preference, 0);
			}
			int ocurrences = 1; 
			if (occurenceOfPreference.containsKey(preference)) ocurrences += occurenceOfPreference.get(preference);
			occurenceOfPreference.put(preference, ocurrences);
			preferences.add(preference);
		}
		
		for (String type:occurenceOfPreference.keySet()){
			this.moneyDemand.put(type, moneyDemand/occurenceOfPreference.get(type));
		}
	}
	
	
/*	*//**
	 * Adjusts the relative valuation between money and other goods.
	 *//*
	private void adjustMoneyDemand(String settings) {
		String[] param = settings.split("\\|");
		if (param.length!=2) throw new RuntimeException("False specification of shock.");
		if (param[0].equals("Goods")){
			int moneyDemand = Integer.parseInt(param[1]);
			for (String type:occurenceOfPreference.keySet()){
				this.moneyDemand.put(type,moneyDemand/occurenceOfPreference.get(type));
			}
		}
	}

	
	*//**
	 * Adjusts the relative valuation between money and other goods.
	 *//*
	private void adjustPreference(String settings) {
		String[] param = settings.split("\\|");
		if (param.length!=2) throw new RuntimeException("False specification of shock.");
		if (param[0].equals("Savings")){
			int position = Integer.parseInt(param[1]);
			int horizon = getRandom().nextInt(1, 5);		
			String pref = "savings,"+horizon;
			preferences.set(position, pref);
		}
		if (param[0].equals("Savings_md")){
			float tp = Float.parseFloat(param[1]);
			timePreference = tp;
		}
		if (param[0].equals("Equity")){
			int position = Integer.parseInt(param[1]);
			preferences.set(position, "equity");
		}
	}
*/
	
	
	/** 
	 * Opens the household for a new period.
	 */
	@Override
	public void open() {
		
		actionCounter = 0;
		intendedActions = new LinkedList<String>();  
		intendedActions.addAll(preferences);

		totalBudget = bank.getBalance(this);
		if (employmentStatus.isEmployed()) employmentDuration += 1;
		else employmentDuration = 0;
		employmentStatus = EmploymentStatus.INVOLONTARILY_UNEMPLOYED;
		jobs=0;
		wagesReceived = 0;
		newTimeDeposits = 0;
		redeemedTimeDeposits = 0;
		defaultedTimeDeposits = 0;
		zeroVolumeDeposits=0;
		realConsumption = 0;
		nominalConsumption = 0;
		dividendsReceived = 0;
		rejectionList.clear();
	}
	
		

	/**
	 * Sets the reservation wage
	 */
	public void setReservationWage() {
		//Expected consumption if the household abstains from work
		int expectedConsumption = (int) (totalBudget*purchasingPowerEstimate);
		reservationWage= (float) (sector.getParam(WAGE_OFFSET)					
				+ Math.log(1+sector.getParam(WAGE_WEALTHFLEX) * expectedConsumption) 
				+ sector.getParam(WAGE_LEISUREFLEX) * employmentDuration);	
	}

	

	/**
	 * Looks for a job.
	 */
	public void jobSearch() {
		
		if (employmentStatus.isEmployed()) throw new RuntimeException("Already has a job."); 
 
 		LinkedList<Offer> jobOffers = getMarket("labor").searchOffers(true, true);
 		/*LinkedList<Offer> jobOffers2 = laborMarket.searchOffers(true, true);
 		if (jobOffers2!=null) jobOffers.addAll(jobOffers2);*/
		if (jobOffers == null) return;
		
		/* Smart search	
		 * 
		 * // The worst offerer gets a rejection note.
		if (jobOffers.size() == parameters.numSearch) {
			Employer lowestOfferer = (Employer) jobOffers.getFirst().getOfferer();
			lowestOfferer.notifyRejection();
		}
		
		// If the best offer is too low then the households decides not to work.
		Offer bestOffer = jobOffers.getLast();
		if (bestOffer.getPrice() < reservationWage) {
			employmentStatus = EmploymentStatus.VOLONTARILY_UNEMPLOYED;
			return;
		}
		
		do{ 
			Offer jobOffer = jobOffers.removeLast();
			if (rejectionList.contains(jobOffer.getOfferer())) continue;
			if (jobOffer.getPrice() >= reservationWage) {
				Employer employer = (Employer) jobOffer.getOfferer();
				employer.receiveApplication(this) ;
				return;
			}
		} while (jobOffers.size()>0);
		
		// If all offers were already tested then you choose the best offer.
		Employer employer = (Employer) bestOffer.getOfferer();
		employer.receiveApplication(this) ;*/
		
		
		// Regular search
		Offer jobOffer = jobOffers.removeLast();
		if (jobOffer.getPrice() >= reservationWage) {
			Employer employer = (Employer) jobOffer.getOfferer();
			employer.receiveApplication(this) ;
		}
		else {
			employmentStatus = EmploymentStatus.VOLONTARILY_UNEMPLOYED;
			jobOffers.add(jobOffer);
		}
		
		if (jobOffers.size()<=1) return;
		
		Offer offer2 = jobOffers.removeFirst();
		Employer employer2 = (Employer) offer2.getOfferer();
		employer2.notifyRejection();
	}
	
		
	/**
	 * Takes note of an employment contract and receives the wage.<br>
	 * The wage income may be spent in the same time period.
	 */
	public void notifyHiring(Cheque cheque) {
		jobs++;
		if (jobs>1) throw new RuntimeException("Multiple jobs."); 
		bank.deposit(this,cheque) ;
		totalBudget += cheque.getAmount();
		wagesReceived += cheque.getAmount();
		employmentStatus = EmploymentStatus.EMPLOYED;
		((HouseholdSector) sector).registerNewDemand(this);
		intendedActions.addAll(preferences); // The new budgetary situation may allow for purchases which were unaffordable before.
	}
	
/*	
	*//**
	 * Takes note that the job application has been rejected and starts a new job search.
	 */
	public void notifyRejection(Offerer offerer){
		rejectionList.add(offerer); // only needed for the smart search algorithm
		jobSearch();
	}

		
	/**
	 * Spends money on consumption goods and investment vehicles.
	 */
	public boolean budgetAllocation() {
		
		makePurchase();
		actionCounter++;
		if (actionCounter > intendedActions.size()-1) actionCounter = 0;
		if (intendedActions.size()>0) return true;
		return false;
	}
	

	/**
	 * Purchases one unit of a good.
	 */
	private void makePurchase(){
		
		String preference = intendedActions.get(actionCounter);
		int money = bank.getBalance(this);
		
		if (preference.startsWith("savings")){	
			
			Market market = getMarket("savings");
			Offer offer = market.searchOffer(false);	
			if (offer == null){
				while (intendedActions.remove(preference))
				return;
			}

			String[] savings = preference.split(",");
			int duration = Integer.parseInt(savings[1]);	// duration means deposited time excluding the current period.
			int faceValue = parameters.timeDepositSize;
			int price = (int) (faceValue / Math.pow(1+offer.getPrice(),duration));
			float timePreference = sector.getParam(TIME_PREF) + getExpectedFutureMoney(duration) /  sector.getParam(TIME_PREFFLEX);
			int reservationPrice = (int) (faceValue / Math.pow(1+timePreference,duration)); 
			if (price > reservationPrice | price > money){
				float lowestPossiblePrice =  (int) (faceValue / Math.pow(1+sector.getParam(TIME_PREF),duration)); 
				if (price > lowestPossiblePrice ) {		//TODO: that's a bit direct, maybe.
					Bank bank = (Bank) offer.getOfferer();
					bank.notifyRejection();
				}
				while (intendedActions.remove(preference))
				return;
			}
			
			Debtor debtor = (Debtor) offer.getOfferer();
			TimeDeposit timeDeposit = new TimeDeposit(this, debtor, faceValue, faceValue-price, offer.getPrice(), duration, timer);
			Cheque cheque = bank.newCheque(this, price);
			debtor.acquireFunding(timeDeposit, cheque);
			timeDeposits.add(timeDeposit);
			newTimeDeposits +=1;
			
			if (timeDeposit.getRemainingDuration()!=duration) throw new RuntimeException("Duration mismatch");
			
		}

		else if (preference.equals("equity")){
			
			Market market = getMarket("equity");
			Offer offer = market.searchOffer(true);
			if (offer == null){
				while (intendedActions.remove(preference))
				return;
			}
			
			int price = (int) offer.getPrice();		
			float expectedPayoff;
			if (!Float.isNaN(returnOnEquityEstimate)) expectedPayoff = returnOnEquityEstimate;
			else expectedPayoff = 0.05f; // a placeholder
			float timePreference = sector.getParam(TIME_PREF) + getExpectedFutureMoney(1) /  sector.getParam(TIME_PREFFLEX);
			int reservationPrice = (int) (price * expectedPayoff / timePreference);
			if (price > reservationPrice | price > money){
				while (intendedActions.remove(preference))
				return;
			}
			Seller provider = (Seller) offer.getOfferer();
			provider.sell(bank.newCheque(this, price));
			
		}
		
		else {
			
			Market market = getMarket(preference);
			if (market == null){
				while (intendedActions.remove(preference))
				return;
			}
			Offer offer = market.searchOffer(true);
			if (offer == null){
				while (intendedActions.remove(preference))
				return;
			}
			int price = (int) offer.getPrice();
			if (price > money - moneyDemand.get(preference)*purchases.get(preference)){
				while (intendedActions.remove(preference))
				return;
			}

			if (money >= price){
				Seller provider = (Seller) offer.getOfferer();
				provider.sell(bank.newCheque(this, price)) ; 
				nominalConsumption += price;
				int previousPurchases = purchases.get(preference);		
				purchases.put(preference, previousPurchases+1);
			}
		}
	}
	
	
	/**
	 * Takes note of a new equity holding.
	 */
	public void newEquityHolding(Debtor newFirm){
		equityHoldings.add(newFirm);
	}
	
	
	/**
	 * Registers the budget allocation.
	 */
	public void registerExpenditure() {
		data.put("totalBudget", (double) totalBudget);
		data.put("consumptionExpenditure", (double) nominalConsumption);
		data.put("hoarding", (double) bank.getBalance(this));
		//Investments and savings are recorded on the banks's side.
	}
	
	
	/**
	 * Receives a dividend from a bank or a firm.
	 */
	public void receiveDividend(Cheque cheque) {
		final int dividend = cheque.getAmount();
		bank.deposit(this,cheque);
		totalBudget += dividend;
		dividendsReceived += dividend;
	}
	
	
	/**
	 * Takes note of the default of a firm (or bank) that he owns.
	 */
	public void notifyDefault(AccountHolder firm){
		equityHoldings.remove(firm);
	}

	
	/**
	 * Receives an interest payment from a bank.
	 */
	public void receiveInterestPayment(Cheque cheque) {
		bank.deposit(this,cheque);
	}
	
	
	/**
	 * Receives a redemption payment from a bank.
	 */
	public void receiveRedemption(Cheque cheque) {
		bank.deposit(this,cheque);
	}
	
	
	/**
	 * Takes note of a redemption.
	 */
	public void notifySettlement(TimeDeposit deposit) {
		redeemedTimeDeposits ++;
		timeDeposits.remove(deposit);
	}

	
	/**
	 * Takes note of a default.
	 */
	public void notifyDefault(TimeDeposit deposit){
		defaultedTimeDeposits ++;
		timeDeposits.remove(deposit);	
	}
	

	/**
	 * Returns the number time deposits that are due at the specified horizon.
	 */
	private int getExpectedFutureMoney(int horizon){
		int amountReceivable = (int) (returnOnEquityEstimate*equityRecord.getLast());
		for (TimeDeposit deposit : timeDeposits){
			if (deposit.getRemainingDuration() == horizon) amountReceivable+=deposit.getReceivableAmount();
		}
		return amountReceivable;
	}
	
	
	/**
	 * Returns the volume of the time deposits of the household (exclusive of interest).
	 */
	private int getTimeDeposits(){
		int deposits = 0;
		for (TimeDeposit deposit : timeDeposits) {
			if (deposit.isSettled()) throw new RuntimeException();
			deposits += deposit.getVolume();
			if (deposit.getVolume()==0) zeroVolumeDeposits++;
		}
		return deposits;
	}
	
	
	/**
	 * Returns the total value of the equity shares held by the household.
	 */
	private int getEquityValue(){
		int equityValue = 0;
		for (Debtor debtor : equityHoldings) {
			equityValue += debtor.getEquityValue(this);
		}
		return equityValue;
	}
	

	/**
	 * Closes the period.
	 */
	public void close() {
		
		// estimate PPM
		realConsumption = 0;
		for (String type : purchases.keySet()){
			realConsumption += purchases.get(type) ;
			purchases.put(type, 0); //Consumer goods are consumed here.
		}
		if (totalBudget>0) purchasingPowerEstimate = (float) realConsumption / (float) totalBudget;
		
		// calculate dividends
		int previousEquityValue = equityRecord.getLast();
		int newEquityValue = getEquityValue();
		int equityGain = dividendsReceived - (previousEquityValue-newEquityValue);

		// estimate ROE
		equityRecord.removeFirst();
		equityRecord.add(newEquityValue);
		equityGainRecord.removeFirst();
		equityGainRecord.add(equityGain);
		float totalROE=0;
		for (int i=1; i<equityRecord.size(); i++){
			float roe = (float) equityGainRecord.get(i) / (float) equityRecord.get(i-1);
			totalROE += roe;
		}
		returnOnEquityEstimate = totalROE / (equityRecord.size()-1);
		
		// update data
		data.put("realConsumption", (double) realConsumption);
		data.put("dividendsReceived",(double) dividendsReceived) ;
		data.put("moneyHoldings", (double) bank.getBalance(this));
		data.put("equityValue", (double) newEquityValue);
		data.put("equityHoldings", (double) equityHoldings.size());
		data.put("timeDeposits", (double) timeDeposits.size());	
		data.put("newTimeDeposits", (double) newTimeDeposits);
		data.put("redeemedTimeDeposits", (double) redeemedTimeDeposits);
		data.put("defaultedTimeDeposits", (double) defaultedTimeDeposits);
		data.put("timeDepositsVolume", (double) getTimeDeposits());
		if ( timeDeposits.size()==0) data.put("timeDepositsAverageSize", 0d);
		else data.put("timeDepositsAverageSize", (double) getTimeDeposits() / timeDeposits.size());
		data.put("reservationWage", (double) reservationWage);
		data.put("employmentDuration", (double) employmentDuration); 
		data.put("zeroVolumeDeposits", (double) zeroVolumeDeposits); 

		if (employmentStatus.isEmployed()){
			data.put("jobSeekers", 1d);
			data.put("employed", 1d); 
			data.put("wages",(double) wagesReceived);
			data.put("voluntarilyUnemployed", 0d);
			data.put("involuntarilyUnemployed", 0d); 
		}
		else{
			if (employmentStatus.isInvolontarily_unemployed()){
				data.put("jobSeekers", 1d);
				data.put("involuntarilyUnemployed", 1d); 
				data.put("voluntarilyUnemployed", 0d); 
			}
			else if (employmentStatus.isVolontarily_unemployed()){
				data.put("jobSeekers", 0d);
				data.put("involuntarilyUnemployed", 0d); 
				data.put("voluntarilyUnemployed", 1d); 	
			}
			data.put("employed", 0d); 
			data.put("wages", 0d);		
		}
		}

	
	/**
	 * Transfers the household's checking account to another bank.
	 */
	@Override
	public void getNewBank(CommercialBank newBank) {
		this.bank = newBank;
	}
	
		
	/**
	 * Returns the money holdings of the household.
	 */
	@Override
	public int getBalance() {
		return bank.getBalance(this);
	}
	
	
	/**
	 * Never called.
	 */
	@Override
	public Cheque acceptDebtor(CreditContract newContract){
		throw new RuntimeException("Never called.");
	}
	
	
	/**
	 * Never called. 
	 */
	@Override
	public void makeOffer() {
		
	}


	/**
	 * Never called.
	 */	
	@Override
	public void notifyRedemption(CreditContract contract) {
		
	}


	/**
	 * Never called.
	 */
	@Override
	public void notifyDefault(CreditContract contract) {
	
	}

}