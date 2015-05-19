package jamel.basic.agents.households;

import jamel.basic.agents.households.util.AssetPortfolio;
import jamel.basic.agents.roles.Asset;
import jamel.basic.agents.util.BasicMemory;
import jamel.basic.agents.util.LaborPower;
import jamel.basic.agents.util.Memory;
import jamel.basic.data.dataSets.AgentDataset;
import jamel.basic.data.dataSets.AbstractAgentDataset;
import jamel.basic.util.AnachronismException;
import jamel.basic.util.BankAccount;
import jamel.basic.util.Cheque;
import jamel.basic.util.Commodities;
import jamel.basic.util.JobContract;
import jamel.basic.util.JobOffer;
import jamel.basic.util.Supply;
import jamel.util.Circuit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A basic household.
 */
public class BasicHousehold implements Household {

	/**
	 * A basic implementation of <code>AssetPortfolio</code>.
	 */
	private final class BasicAssetPortfolio implements AssetPortfolio {

		/** The set of assets. */
		private final Set<Asset> assets = new HashSet<Asset>();

		@Override
		public void add(Asset asset) {
			if(this.assets.contains(asset)){
				throw new RuntimeException("This asset is already owned.");
			}
			this.assets.add(asset);
		}

		@Override
		public boolean contains(Asset asset) {
			return this.assets.contains(asset);
		}

		@Override
		public long getNetValue() {
			long value = 0;
			for(Asset asset:assets) {
				value += asset.getBookValue();
			}
			return value;
		}

		@Override
		public void remove(Asset asset) {
			if (!this.assets.contains(asset)) {
				throw new RuntimeException("Asset Not found.");
			}
			this.assets.remove(asset);
		}
	}

	@SuppressWarnings("javadoc")
	private static final int EMPLOYED = 1;

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
	private static final int UNEMPLOYED = 0;

	@SuppressWarnings("javadoc")
	private final static String WAGE_FLEX = "wage.flexibility";

	@SuppressWarnings("javadoc")
	private final static String WAGE_RESIST = "wage.resistance";

	/** 
	 * The job offer comparator.<p>
	 * To compare jobs according to the wage they offer.
	 */
	public static final Comparator<JobOffer>jobComparator = new Comparator<JobOffer>() {
		@Override
		public int compare(JobOffer offer1, JobOffer offer2) {
			return (new Long(offer2.getWage()).compareTo(offer1.getWage()));
		}
	};
	/** 
	 * The supply comparator.<p>
	 * To compare supplies according to their price.
	 */
	public static final Comparator<Supply> supplyComparator = new Comparator<Supply>() {
		@Override
		public int compare(Supply offer1, Supply offer2) {
			return (-(new Double(offer2.getPrice())).compareTo(offer1.getPrice()));
		}
	};

	/** The current account. */
	private final BankAccount account;
	
	/** Items of property. */
	private final AssetPortfolio assetPortfolio = new BasicAssetPortfolio();
	
	/** The data of the agent. */
	private AbstractAgentDataset data;

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

	/** A flag that indicates if the agent records its history. */
	private boolean recordHistoric = false;

	/** The households sector. */
	private final  HouseholdsSector sector;

	/** A map that stores the variables of the household. */
	private final Map<String,Number> variables = new HashMap<String,Number>();

	/** The memory. */
	final protected Memory memory = new BasicMemory(12);

	/**
	 * Creates a household.
	 * @param name the name of the new household.
	 * @param sector the households sector.
	 */
	public BasicHousehold(String name,HouseholdsSector sector) {
		this.history.add("Creation: "+name);
		this.name = name;
		this.sector = sector;
		this.account = sector.getNewAccount(this);
		this.variables.put("status", UNEMPLOYED);
		this.variables.put("unemployement duration", 0);
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
		this.assetPortfolio.add(asset);
	}

	@Override
	public void goBankrupt() {
		throw new RuntimeException("A household cannot be bankrupted.");
	}

	@Override
	public void close() {
		//this.annualIncome.add(this.variables.get("wage").longValue()+this.variables.get("dividend").longValue());
		this.data.update();
	}

	@Override
	public void consumption() {
		this.memory.put("income", this.variables.get("wage").longValue()+this.variables.get("dividend").longValue());
		final double averageIncome = this.memory.getMean("income", Circuit.getCurrentPeriod().intValue(),12);
		final long savingsTarget = (long) (12*averageIncome*this.sector.getFloatParameter(SAV_TARGET));
		final long savings = (long) (this.account.getAmount()-averageIncome);
		long consumptionBudget;
		if (savings<savingsTarget) {
			consumptionBudget = Math.min(this.account.getAmount(), (long) ((1.-this.sector.getFloatParameter(SAV_PROP))*averageIncome));
		}
		else {
			consumptionBudget = Math.min(this.account.getAmount(), (long) (averageIncome + (savings-savingsTarget)*this.sector.getFloatParameter(SAV_PROP2_CONSUM_EXCESS)));
		}

		this.history.add("Average income: "+averageIncome);
		this.history.add("Savings Target: "+savingsTarget);
		this.history.add("Actual Savings: "+savings);
		this.history.add("Consumption Budget: "+consumptionBudget);			

		this.data.put("consumption.budget", (double) consumptionBudget);
		long consumptionValue=0;
		long consumptionVolume=0;
		if (consumptionBudget>0) {
			final Supply[] supplies = this.sector.getSupplies((int) sector.getFloatParameter(N_SUPPLIES));
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
		if (this.variables.get("wage").longValue()>0) {
			throw new AnachronismException("Wage already earned.");			
		}
		if (paycheck.getAmount()!=this.jobContract.getWage()) {
			throw new IllegalArgumentException("Bad cheque amount.");
		}
		this.variables.put("wage", this.variables.get("wage").longValue() + paycheck.getAmount());
		this.account.deposit(paycheck);
	}

	/* 
	 * (non-Javadoc)
	 * @see jamel.basic.agents.roles.Agent#execute(java.lang.String, java.lang.Object[])
	 * @since 23-11-2014
	 * TODO IMPLEMENT ME
	 */
	@Override
	public Object execute(String instruction, Object... args) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public long getAssets() {
		return this.assetPortfolio.getNetValue()+this.account.getAmount();
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
		if (this.variables.get("worked").intValue()!=0) {
			throw new AnachronismException("Already worked.");			
		}
		if (this.variables.get("wage").longValue()==0) {
			throw new RuntimeException("Wage not paid.");			
		}
		this.variables.put("worked", 1);
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
		
		Integer unempDuration = (Integer) this.variables.get("unemployement duration");
		if ((this.jobContract==null)||!(this.jobContract.isValid())) {
			this.variables.put("status",UNEMPLOYED);
			if (unempDuration<0) {
				unempDuration =0;
			}
			else {
				unempDuration++;
			}
		}
		else {
			this.variables.put("status",EMPLOYED);
			if (unempDuration>0) {
				unempDuration=0;
			}
			else {
				unempDuration--;
			}
		}

		this.history.add("Status: "+this.variables.get("status"));
		this.history.add("Unemployement duration: "+unempDuration);

		// Different behaviors according the status.

		switch(this.variables.get("status").intValue()) {
		case UNEMPLOYED:
			// Attention, c'est un peu plus compliquŽ dans les dernires versions de Jamel1.
			Double reservationWage = (Double) this.variables.get("reservationWage");
			if (reservationWage==null){
				reservationWage=0d;
				this.variables.put("reservationWage", reservationWage);
			}
			if (unempDuration>this.sector.getFloatParameter(WAGE_RESIST)){
				reservationWage = (reservationWage*(1f-this.sector.getFloatParameter(WAGE_FLEX)*Circuit.getRandom().nextFloat()));
				this.variables.put("reservationWage", reservationWage);
				this.history.add("Reservation wage updated.");
			}
			this.history.add("Reservation wage: "+reservationWage);
			final JobOffer[] jobOffers = this.sector.getJobOffers((int) sector.getFloatParameter(N_JOB_OFFERS));
			if (jobOffers.length>0) {
				Arrays.sort(jobOffers,jobComparator);
				if (jobOffers[0].getWage()>=reservationWage) {
					this.jobContract=jobOffers[0].apply(this);
					this.variables.put("status",EMPLOYED);
					unempDuration=0;
					this.history.add("New job contract: "+this.jobContract.toString());
				}
			}
			break;
		case EMPLOYED:
			this.variables.put("reservationWage",(double) this.jobContract.getWage());
			break;
		default :
			throw new RuntimeException("Unexpected status."); 
		}

		this.data.put("unemployed", 1d-this.variables.get("status").doubleValue());			
		this.data.put("employed", this.variables.get("status").doubleValue());			
		this.variables.put("unemployment duration", unempDuration);
	}

	@Override
	public void open() {
		this.history.add("Period "+Circuit.getCurrentPeriod().intValue());
		this.variables.put("dividend", 0l);
		this.variables.put("worked", 0);
		this.variables.put("wage", 0l);
		this.variables.put("asset portfolio initial value",this.assetPortfolio.getNetValue());	
		this.data = new AbstractAgentDataset(this.name) {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void update() {
				this.put("cash", (double) account.getAmount());
				this.put("wages", variables.get("wage").doubleValue());
				this.put("dividend", variables.get("dividend").doubleValue());
				final long capital = assetPortfolio.getNetValue();
				this.put("capital", (double) capital);				
				this.put("capital_variation", (double) (capital-variables.get("asset portfolio initial value").longValue()));				
				this.put("agents", 1.);
			}

		};
	}

	@Override
	public void receiveDividend(Cheque cheque,Asset asset) {
		if (!this.assetPortfolio.contains(asset)) {
			throw new RuntimeException("This asset is not own.");
		}
		this.history.add("Receive dividend: "+cheque.toString());
		this.variables.put("dividend", (Long) this.variables.get("dividend") + cheque.getAmount());
		this.account.deposit(cheque);
	}

	@Override
	public void removeAsset(Asset asset) {
		this.assetPortfolio.remove(asset);
	}

}

// ***
