package jamelV3.jamel.households;

import jamelV3.basic.agent.BasicAgentDataset;
import jamelV3.basic.agent.AgentDataset;
import jamelV3.basic.util.Timer;
import jamelV3.jamel.sectors.HouseholdSector;
import jamelV3.jamel.util.AnachronismException;
import jamelV3.jamel.util.BasicMemory;
import jamelV3.jamel.util.ConsistencyException;
import jamelV3.jamel.util.Memory;
import jamelV3.jamel.widgets.Asset;
import jamelV3.jamel.widgets.AssetPortfolio;
import jamelV3.jamel.widgets.BankAccount;
import jamelV3.jamel.widgets.BasicAssetPortfolio;
import jamelV3.jamel.widgets.Cheque;
import jamelV3.jamel.widgets.Commodities;
import jamelV3.jamel.widgets.JobContract;
import jamelV3.jamel.widgets.JobOffer;
import jamelV3.jamel.widgets.LaborPower;
import jamelV3.jamel.widgets.Supply;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * A basic household.
 */
public class BasicHousehold implements Household {

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

	/** A flag that indicates if the agent records its history. */
	private boolean recordHistoric = false;

	/** The households sector. */
	private final  HouseholdSector sector;

	/** A map that stores the variables of the household. */
	private final Map<String,Number> variables = new HashMap<String,Number>();

	/** The memory. */
	final protected Memory memory;

	/** The random. */
	final protected Random random;

	/** The timer. */
	final protected Timer timer;

	/**
	 * Creates a household.
	 * @param name the name of the new household.
	 * @param sector the households sector.
	 */
	public BasicHousehold(String name,HouseholdSector sector) {
		this.history.add("Creation: "+name);
		this.name = name;
		this.sector = sector;
		this.timer = this.sector.getTimer();
		this.random = this.sector.getRandom();
		this.memory = new BasicMemory(timer, 12);
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

	/**
	 * Updates the data.
	 */
	private void updateData() {
		this.data.put("cash", (double) account.getAmount());
		this.data.put("wages", variables.get("wage").doubleValue());
		this.data.put("dividend", variables.get("dividend").doubleValue());
		final long capital = assetPortfolio.getNetValue();
		this.data.put("capital", (double) capital);				
		this.data.put("capital_variation", (double) (capital-variables.get("asset portfolio initial value").longValue()));				
		this.data.put("agents", 1.);		
	}

	@Override
	public void addAsset(Asset asset) {
		this.assetPortfolio.add(asset);
	}

	@Override
	public void close() {
		this.updateData();
	}

	@Override
	public void consumption() {
		this.memory.put("income", this.variables.get("wage").longValue()+this.variables.get("dividend").longValue());
		final double averageIncome = this.memory.getMean("income", this.timer.getPeriod().intValue(),12);
		final long savingsTarget = (long) (12*averageIncome*this.sector.getParam(SAV_TARGET));
		final long savings = (long) (this.account.getAmount()-averageIncome);
		long consumptionBudget;
		if (savings<savingsTarget) {
			consumptionBudget = Math.min(this.account.getAmount(), (long) ((1.-this.sector.getParam(SAV_PROP))*averageIncome));
		}
		else {
			consumptionBudget = Math.min(this.account.getAmount(), (long) (averageIncome + (savings-savingsTarget)*this.sector.getParam(SAV_PROP2_CONSUM_EXCESS)));
		}

		this.history.add("Average income: "+averageIncome);
		this.history.add("Savings Target: "+savingsTarget);
		this.history.add("Actual Savings: "+savings);
		this.history.add("Consumption Budget: "+consumptionBudget);			

		this.data.put("consumption.budget", (double) consumptionBudget);
		long consumptionValue=0;
		long consumptionVolume=0;
		if (consumptionBudget>0) {
			final Supply[] supplies = this.sector.getSupplies(sector.getParam(N_SUPPLIES).intValue());
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
			throw new ConsistencyException("Bad cheque amount.");
		}
		this.variables.put("wage", this.variables.get("wage").longValue() + paycheck.getAmount());
		this.account.deposit(paycheck);
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
			throw new ConsistencyException("Wage not paid.");			
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
	public void goBankrupt() {
		throw new RuntimeException("A household cannot be bankrupted.");
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
			if (unempDuration>this.sector.getParam(WAGE_RESIST)){
				reservationWage = (reservationWage*(1f-this.sector.getParam(WAGE_FLEX)*this.random.nextFloat()));
				this.variables.put("reservationWage", reservationWage);
				this.history.add("Reservation wage updated.");
			}
			this.history.add("Reservation wage: "+reservationWage);
			final JobOffer[] jobOffers = this.sector.getJobOffers(sector.getParam(N_JOB_OFFERS).intValue());
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
		this.history.add("Period "+this.timer.getPeriod().intValue());
		this.variables.put("dividend", 0l);
		this.variables.put("worked", 0);
		this.variables.put("wage", 0l);
		this.variables.put("asset portfolio initial value",this.assetPortfolio.getNetValue());	
		this.data = new BasicAgentDataset(this.name);
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
