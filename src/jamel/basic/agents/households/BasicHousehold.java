package jamel.basic.agents.households;

import jamel.Simulator;
import jamel.basic.agents.roles.Asset;
import jamel.basic.agents.util.LaborPower;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.data.DataSeries;
import jamel.basic.util.AnachronismException;
import jamel.basic.util.BankAccount;
import jamel.basic.util.Cheque;
import jamel.basic.util.Commodities;
import jamel.basic.util.JobContract;
import jamel.basic.util.JobOffer;
import jamel.basic.util.Supply;
import jamel.util.Circuit;
import jamel.util.Period;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A basic household.
 */
public class BasicHousehold implements Household {

	/**
	 * A class to store the parameters of the household.
	 */
	private class Parameters {

		@SuppressWarnings("javadoc")
		private static final String N_JOB_OFFERS = "jobs.selection";

		@SuppressWarnings("javadoc")
		private static final String N_SUPPLIES = "supplies.selection";

		@SuppressWarnings("javadoc")
		private final static String SAV_PROP = "savings.propensityToSave";

		@SuppressWarnings("javadoc")
		private final static String SAV_PROP2_CONSUM_EXCESS = "savings.propensityToConsumeExcess";

		@SuppressWarnings("javadoc")
		private final static String SAV_TARGET = "savings.ratioTarget";

		@SuppressWarnings("javadoc")
		private final static String WAGE_FLEX = "wage.flexibility";

		@SuppressWarnings("javadoc")
		private final static String WAGE_RESIST = "wage.resistance";

		/** The flexibility of the reservation wage. */
		private float flexibility;

		/** The number of job offers selected. */
		private int nJobOffers;

		/** The number of supplies selected. */
		private int nSupplies;

		/** The propensity to consume excess saving. */
		private float propensityToConsumeExcessSaving;

		/** The resistance to a cut of the reservation wage. */
		private int resistance;

		/** The saving propensity. */
		private float savingPropensity;

		/** The ratio of targeted savings to annual income. */
		private float savingRatioTarget;

		/**
		 * Updates the parameters.
		 * Called when the household is created.
		 */
		private void update() {
			try {
			this.flexibility = Float.parseFloat(BasicHousehold.this.sector.getParameter(WAGE_FLEX));
			this.nJobOffers=Integer.parseInt(BasicHousehold.this.sector.getParameter(N_JOB_OFFERS));
			this.nSupplies=Integer.parseInt(BasicHousehold.this.sector.getParameter(N_SUPPLIES));
			this.propensityToConsumeExcessSaving = Float.parseFloat(BasicHousehold.this.sector.getParameter(SAV_PROP2_CONSUM_EXCESS));
			this.resistance = Integer.parseInt(BasicHousehold.this.sector.getParameter(WAGE_RESIST));
			this.savingPropensity = Float.parseFloat(BasicHousehold.this.sector.getParameter(SAV_PROP));
			this.savingRatioTarget = Float.parseFloat(BasicHousehold.this.sector.getParameter(SAV_TARGET));
			}
			catch (NumberFormatException e) {
				Simulator.showErrorDialog("BasicHousehold: NumberFormatException while updating parameters");
				e.printStackTrace();
				throw new RuntimeException("BasicHousehold: NumberFormatException while updating parameters");
			}
		}

	}

	/**
	 * Enumeration of the social status of the household.
	 */
	private static enum STATUS {

		@SuppressWarnings("javadoc")
		employed,

		@SuppressWarnings("javadoc")
		unemployed;

	}

	/**
	 * A class to store the set of variables that are used to describe the state of the household.
	 */
	private class Variables {

		/** The dividends of the current period. */
		private long dividend = 0;

		/** The earnings of the current period. */
		private long income = 0;

		/** The last time period in which the household earned a wage.  */
		private Period lastWage = null;

		/** The last time period in which the household worked.  */
		private Period lastWork = null;

		/** The reservation wage. */
		private double reservationWage = 0;

		/** The social status of the household. */
		private STATUS status = STATUS.unemployed;

		/** The unemployment duration / employment duration when negative. */
		private int unempDuration = 0;

		/** The wage. */
		private long wage = 0;

	}

	/**
	 * The collection of possessions (company) that the household owns.
	 */
	public interface Possessions {

		/**
		 * Adds a company to the list.
		 * @param asset the company to be added.
		 */
		void add(Asset asset);

		/**
		 * Returns <code>true</code> if the given asset is in these possessions, <code>false</code> otherwise.
		 * @param asset the asset.
		 * @return a boolean.
		 */
		boolean contain(Asset asset);

		/**
		 * Returns the net value of the possessions (the sum of the capital of each company owned).
		 * @return the net value of the possessions.
		 */
		long getNetValue();

		/**
		 * Removes the specified company.
		 * @param asset the company to be removed.
		 */
		void remove(Asset asset);

	}

	/** 
	 * The job offer comparator.<p>
	 * To compare jobs according to the wage they offer.
	 */
	static final Comparator<JobOffer>jobComparator = new Comparator<JobOffer>() {
		@Override
		public int compare(JobOffer offer1, JobOffer offer2) {
			return (new Long(offer2.getWage()).compareTo(offer1.getWage()));
		}
	};

	/** 
	 * The supply comparator.<p>
	 * To compare supplies according to their price.
	 */
	static final Comparator<Supply> supplyComparator = new Comparator<Supply>() {
		@Override
		public int compare(Supply offer1, Supply offer2) {
			return (-(new Double(offer2.getPrice())).compareTo(offer1.getPrice()));
		}
	};

	/** The current account. */
	private final BankAccount account;

	/** The annual income. */
	private DataSeries annualIncome = new DataSeries(12);

	/** The data of the agent. */
	private BasicAgentDataset data;

	/** The history of the household. */
	private final LinkedList<String> history = new LinkedList<String>() {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		@Override
		public boolean add(String string) {
			final boolean result;
			if (recordHistoric) {
				result=super.add(string);
			}
			else {
				result=false;
			}
			return result;
		}
	};

	/** The job contract. */
	private JobContract jobContract;

	/** The name of  the household. */
	private final String name;

	/** The parameters of the household. */
	private final Parameters p = new Parameters();

	/** Items of property. */
	private final Possessions possessions = new Possessions(){

		/** The items of property. */
		private final Set<Asset> assets = new HashSet<Asset>();

		@Override
		public void add(Asset asset) {
			if(this.assets.contains(asset)){
				throw new RuntimeException("This asset is already owned.");
			}
			this.assets.add(asset);
		}

		@Override
		public boolean contain(Asset asset) {
			return this.assets.contains(asset);
		}

		@Override
		public long getNetValue() {
			long value = 0;
			for(Asset asset:assets) {
				value += asset.getCapital();
			}
			return value;
		}
		
		@Override
		public void remove(Asset asset) {
			if (!this.assets.contains(asset)) {
				throw new RuntimeException("Company "+asset.getName()
						+ " Not found.");
			}
			this.assets.remove(asset);
		}
	
	};

	/** A flag that indicates if the agent records its history. */
	private boolean recordHistoric = false;

	/** The households sector. */
	private final  HouseholdsSector sector;

	/** The set of variables that are used to describe the state of the household. */
	private final Variables v = new Variables();

	/**
	 * Creates a household.
	 * @param name the name of the new household.
	 * @param sector the households sector.
	 */
	public BasicHousehold(String name,HouseholdsSector sector) {
		this.history.add("Creation: "+name);
		this.name = name;
		this.sector = sector;
		this.p.update();
		this.account = sector.getNewAccount(this);
	}

	/**
	 * Prints the history of the household.
	 * @return <code>true</code>
	 */
	@SuppressWarnings("unused")
	private boolean printHistory() {
		for (String string:this.history) {
			System.out.println(string);
		}
		return true;
	}

	@Override
	public void addAsset(Asset asset) {
		this.possessions.add(asset);
	}

	@Override
	public void bankrupt() {
		throw new RuntimeException("A household cannot be bankrupted.");
	}

	@Override
	public void close() {
		this.annualIncome.add(this.v.income);
		this.data.put("cash", (double) this.account.getAmount());
		this.data.put("wages", (double) this.v.wage);
		this.data.put("dividend", (double) this.v.dividend);
		this.data.put("income", (double) this.v.income);
		this.data.put("capital", (double) this.possessions.getNetValue());
	}

	@Override
	public void consumption() {
		final double averageIncome = annualIncome.getMean();
		final long savingsTarget = (long) (12*averageIncome*this.p.savingRatioTarget);
		final long savings = (long) (this.account.getAmount()-averageIncome);
		long consumptionBudget;
		if (savings<savingsTarget) {
			consumptionBudget = Math.min(this.account.getAmount(), (long) ((1.-this.p.savingPropensity)*averageIncome));
		}
		else {
			consumptionBudget = Math.min(this.account.getAmount(), (long) (averageIncome + (savings-savingsTarget)*this.p.propensityToConsumeExcessSaving));
		}

		this.history.add("Average income: "+averageIncome);
		this.history.add("Savings Target: "+savingsTarget);
		this.history.add("Actual Savings: "+savings);
		this.history.add("Consumption Budget: "+consumptionBudget);			

		this.data.put("consumption.budget", (double) consumptionBudget);
		long consumptionValue=0;
		long consumptionVolume=0;
		if (consumptionBudget>0) {
			final Supply[] supplies = this.sector.getSupplies(p.nSupplies);
			this.history.add("Supplies: "+supplies.length);			
			if (supplies.length>0) {
				double supplyVolume=0;
				for(Supply supply:supplies) {
					supplyVolume+=supply.getVolume();
				}
				this.history.add("Supplies volume: "+supplyVolume);			
				Arrays.sort(supplies,supplyComparator);
				for(Supply supply: supplies) {
					this.history.add(supply.toString());			
					if (consumptionBudget<supply.getPrice()) {
						this.history.add("Consumption budget exhausted <" + consumptionBudget + ">");			
						break;
					}
					final long volume;
					if (supply.getPrice(supply.getVolume()) >= consumptionBudget) {
						volume = (long) (consumptionBudget/supply.getPrice());
					}
					else {
						volume = supply.getVolume();
					}
					final long value = (long) (volume*supply.getPrice());
					final Commodities truc = supply.buy(volume, this.account.newCheque(value));
					if (truc.getVolume()!=volume) {
						throw new RuntimeException("Consumption volume expected <"+volume+"> but was <"+truc.getVolume()+">"); 
					}
					truc.consume();
					consumptionBudget -= value;
					consumptionValue += value;
					consumptionVolume += volume;
					this.history.add("Purchase: volume <" + volume + ">, value <" + value + ">");			
					this.history.add("Consumption Budget: "+consumptionBudget);			
				}
			}
		}
		this.data.put("consumption.val", (double) consumptionValue);
		this.data.put("consumption.vol", (double) consumptionVolume);
		this.history.add("Total consumption: volume <" + consumptionVolume + ">, value <" + consumptionValue + ">");			
	}

	@Override
	public void earnWage(Cheque paycheck) {
		this.history.add("Paychek: "+paycheck.toString());
		if (this.jobContract==null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.v.lastWage!=null && !this.v.lastWage.isBefore(Circuit.getCurrentPeriod())) {
			throw new AnachronismException();			
		}
		if (paycheck.getAmount()!=this.jobContract.getWage()) {
			throw new IllegalArgumentException("Bad cheque amount.");
		}
		this.v.lastWage= Circuit.getCurrentPeriod();
		this.v.income += paycheck.getAmount();
		this.v.wage += paycheck.getAmount();
		this.account.deposit(paycheck);
	}

	@Override
	public long getAssets() {
		return this.possessions.getNetValue()+this.account.getAmount();
	}

	@Override
	public AgentDataset getData() {
		return this.data;
	}

	@Override
	public LaborPower getLaborPower() {
		if (this.jobContract==null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.v.lastWork!=null && !this.v.lastWork.isBefore(Circuit.getCurrentPeriod())) {
			throw new AnachronismException();			
		}
		if (this.v.lastWage == null || !this.v.lastWage.isPresent()) {
			throw new RuntimeException("Wage not paid.");			
		}
		this.v.lastWork=Circuit.getCurrentPeriod();
		return new LaborPower(){

			private float energy = 1;

			private long value = jobContract.getWage();

			private final long wage = value;

			@Override
			public void expend() {
				history.add("Work.");
				energy=0;
				value=0;
			}

			@Override
			public void expend(float work) {
				history.add("Work: "+work);
				if (work>energy) {
					if ((work-energy)<0.001) {
						energy=0;
						value=0;
					} else {
						throw new IllegalArgumentException();
					}
				} else {
					value-=wage*work;
					energy-=work;
					if (energy<0.001) {
						energy=0;
						value=0;
					}
				}
			}

			@Override
			public float getEnergy() {
				return energy;
			}

			@Override
			public long getValue() {
				return value;
			}

			@Override
			public long getWage() {
				return wage;
			}

			@Override
			public boolean isExhausted() {
				return energy==0;
			}

		};
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isBankrupted() {
		return false;
	}

	@Override
	public void jobSearch() {

		// Updates the status.

		if ((this.jobContract==null)||!(this.jobContract.isValid())) {
			this.v.status=STATUS.unemployed;
			if (this.v.unempDuration<0) {
				this.v.unempDuration =0;
			}
			else {
				this.v.unempDuration++;
			}
		}
		else {
			this.v.status=STATUS.employed;
			if (this.v.unempDuration>0) {
				this.v.unempDuration=0;
			}
			else {
				this.v.unempDuration--;
			}
		}

		this.history.add("Status: "+v.status.toString());
		this.history.add("Unemployement duration: "+this.v.unempDuration);

		// Different behaviors according the status.

		switch(this.v.status) {
		case unemployed:
			// Attention, c'est un peu plus compliquŽ dans les dernires versions de Jamel1.
			if (this.v.unempDuration>this.p.resistance){
				this.v.reservationWage = (this.v.reservationWage*(1f-this.p.flexibility*Circuit.getRandom().nextFloat()));
				this.history.add("Reservation wage updated.");
			}
			this.history.add("Reservation wage: "+this.v.reservationWage);
			final JobOffer[] jobOffers = this.sector.getJobOffers(p.nJobOffers);
			if (jobOffers.length>0) {
				Arrays.sort(jobOffers,jobComparator);
				if (jobOffers[0].getWage()>=this.v.reservationWage) {
					this.jobContract=jobOffers[0].apply(this);
					this.v.status=STATUS.employed;
					this.v.unempDuration=0;
					this.history.add("New job contract: "+this.jobContract.toString());
				}
			}
			break;
		case employed:
			this.v.reservationWage  = this.jobContract.getWage();
			break;
		default :
			throw new RuntimeException("Unexpected status."); 
		}

		if(this.v.status.equals(STATUS.unemployed)) {
			this.data.put("unemployed", 1.);			
			this.data.put("employed", 0.);			
		}
		else {			
			this.data.put("unemployed", 0.);			
			this.data.put("employed", 1.);			
		}
	}

	@Override
	public void open() {
		this.history.add("Period "+Circuit.getCurrentPeriod().getValue());
		this.v.income=0;
		this.v.wage=0;
		this.v.dividend=0;
		this.data = new BasicAgentDataset(this.name);
		this.data.put("households", 1.);
	}

	@Override
	public void receiveDividend(Cheque cheque,Asset asset) {
		if (!this.possessions.contain(asset)) {
			throw new RuntimeException("This asset is not own.");
		};
		this.history.add("Receive dividend: "+cheque.toString());
		this.v.income +=cheque.getAmount();
		this.v.dividend +=cheque.getAmount();
		this.account.deposit(cheque);
	}

	@Override
	public void removeAsset(Asset asset) {
		this.possessions.remove(asset);
	}

	@Override
	public void updateParameters() {
		this.p.update();
	}

}

// ***
