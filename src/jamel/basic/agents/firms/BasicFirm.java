package jamel.basic.agents.firms;

import jamel.basic.agents.firms.util.BasicFactory;
import jamel.basic.agents.firms.util.CapitalManager;
import jamel.basic.agents.firms.util.Factory;
import jamel.basic.agents.firms.util.Workforce;
import jamel.basic.agents.roles.Shareholder;
import jamel.basic.agents.roles.Supplier;
import jamel.basic.agents.roles.Worker;
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
import jamel.util.Period;

import java.util.LinkedList;
import java.util.List;

/**
 * A basic firm.
 */
public class BasicFirm implements Firm {

	/**
	 * The pricing manager.
	 */
	public interface PricingManager {

		/**
		 * Returns the price.
		 * @return the price.
		 */
		Double getPrice();

		/**
		 * Updates the price.
		 */
		void updatePrice();

	}

	/**
	 * The production manager.
	 */
	public interface ProductionManager {

		/**
		 * Returns the capacity utilization targeted.
		 * @return a float in [0,1].
		 */
		float getTarget();

		/**
		 * Updates the target of capacity utilization.
		 */
		void updateCapacityUtilizationTarget();

	}

	/**
	 * The workforce manager.
	 */
	public interface WorkforceManager {

		/**
		 * Closes the manager.
		 */
		void close();

		/**
		 * Returns the average wage paid of the current workforce.
		 * @return the average wage paid of the current workforce.
		 */
		Double getAverageWage();

		/**
		 * Returns the job offer.
		 * @return the job offer.
		 */
		JobOffer getJobOffer();

		/**
		 * Returns an array containing all of the labor powers of the workforce. 
		 * @return an array of labor powers.
		 */
		LaborPower[] getLaborPowers();

		/**
		 * Returns the payroll (= the future wage bill).
		 * @return the payroll (= the future wage bill).
		 */
		long getPayroll();

		/**
		 * Returns the wage.
		 * @return the wage.
		 */
		Double getWage();

		/**
		 * Returns the wageBill of the period.
		 * @return the wageBill of the period.
		 */
		long getWageBill();

		/**
		 * Returns the size of the workforce (the number of employees)
		 * @return the size of the workforce (the number of employees)
		 */
		int getWorkforceSize();

		/**
		 * Layoffs all the workforce.
		 */
		void layoff();

		/**
		 * Pays the workers.
		 */
		void payWorkers();

		/**
		 * Creates a new job offer.
		 */
		void updateJobOffer();

		/**
		 * Updates the data about vacancies. 
		 */
		void updateVacancies();

		/**
		 * Updates the workforce according to the current production target.
		 */
		void updateWorkforce();

		/**
		 * Updates the wage.
		 */
		public void updateWage();

	}

	@SuppressWarnings("javadoc")
	public final static String CAPITAL_PROPENSITY2DISTRIBUTE = "capital.propensityToDistribute";

	@SuppressWarnings("javadoc")
	public final static String CAPITAL_TARGET = "capital.target";

	@SuppressWarnings("javadoc")
	public final static String INVENTORY_NORMAL_LEVEL = "inventory.normalLevel";

	@SuppressWarnings("javadoc")
	public final static String LABOUR_CONTRACT_MAX = "labourContract.max";

	@SuppressWarnings("javadoc")
	public final static String LABOUR_CONTRACT_MIN = "labourContract.min";

	@SuppressWarnings("javadoc")
	public static final String MEM_DIVIDEND = "dividend";

	@SuppressWarnings("javadoc")
	public final static String NORMAL_VACANCY_RATE = "vacancy.normalRate";

	@SuppressWarnings("javadoc")
	public final static String PRICE_FLEXIBILITY = "price.flexibility";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTION_CAPACITY = "production.capacity";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTION_TIME = "production.time";

	@SuppressWarnings("javadoc")
	public final static String PRODUCTIVITY = "production.productivity";

	@SuppressWarnings("javadoc")
	public final static String PROPENSITY2SELL = "inventory.propensity2sell";

	@SuppressWarnings("javadoc")
	public final static String SELLING_CAPACITY = "sales.capacity";

	@SuppressWarnings("javadoc")
	public final static String UTILIZATION_RATE_FLEXIBILITY = "utilizationRate.flexibility";

	@SuppressWarnings("javadoc")
	public final static String UTILIZATION_RATE_INITIAL_VALUE = "utilizationRate.initialValue";

	@SuppressWarnings("javadoc")
	public final static String WAGE_FLEX_DOWN = "wage.flexibility.downward";

	@SuppressWarnings("javadoc")
	public final static String WAGE_FLEX_UP = "wage.flexibility.upward";

	@SuppressWarnings("javadoc")
	public final static String WAGE_INITIAL_VALUE = "wage.initialValue";

	@SuppressWarnings("javadoc")
	public final static String WAGE_MINIMUM = "wage.minimum";

	/** The name. */
	private final String name;

	/** A flag that indicates if the agent records its history. */
	private boolean recordHistoric = false;

	/** The account. */
	protected final BankAccount account;

	/** A flag that indicates if the firm is bankrupted. */
	protected boolean bankrupted = false;

	/** The capital manager. */
	protected final CapitalManager capitalManager = getNewCapitalManager();

	/** Date of creation. */
	protected final int creation = Circuit.getCurrentPeriod().intValue();

	/** The data of the agent. */
	protected AgentDataset data;

	/** The factory. */
	protected final Factory factory;

	/** The history of the firm. */
	protected final LinkedList<String> history = new LinkedList<String>() {
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

	/** The memory. */
	protected final Memory memory = new BasicMemory(24);

	/** The pricing manager. */
	protected final PricingManager pricingManager;

	/** The production manager. */
	protected final ProductionManager productionManager = new ProductionManager() {

		/** The capacity utilization rate targeted.<p>
		 * Capacity utilization rate: "A metric used to measure the rate at which 
		 * potential output levels are being met or used. Displayed as a percentage, 
		 * capacity utilization levels give insight into the overall slack that is 
		 * in the economy or a firm at a given point in time. If a company is running 
		 * at a 70% capacity utilization rate, it has room to increase production up 
		 * to a 100% utilization rate without incurring the expensive costs of 
		 * building a new plant or facility.
		 * Also known as "operating rate".
		 * (<a href="http://www.investopedia.com/terms/c/capacityutilizationrate.asp">Investopedia</a>)  
		 */
		private Float utilizationRateTargeted = null;

		@Override
		public float getTarget() {
			if (this.utilizationRateTargeted==null) {
				this.utilizationRateTargeted=sector.getFloatParameter(UTILIZATION_RATE_INITIAL_VALUE);
			}
			return this.utilizationRateTargeted;
		}

		@Override
		public void updateCapacityUtilizationTarget() {
			if (this.utilizationRateTargeted==null) {
				this.utilizationRateTargeted=sector.getFloatParameter(UTILIZATION_RATE_INITIAL_VALUE);
			}
			else {
				final double inventoryRatio = getInventoryRatio();
				final float alpha1 = Circuit.getRandom().nextFloat();
				final float alpha2 = Circuit.getRandom().nextFloat();
				final float delta = (alpha1*sector.getFloatParameter(UTILIZATION_RATE_FLEXIBILITY));
				if (inventoryRatio<1-alpha1*alpha2) { // Low level
					this.utilizationRateTargeted += delta;
					if (this.utilizationRateTargeted>1) {
						this.utilizationRateTargeted = 1f;
					}
				}
				else if (inventoryRatio>1+alpha1*alpha2) { // High level
					this.utilizationRateTargeted -= delta;
					if (this.utilizationRateTargeted<0) {
						this.utilizationRateTargeted = 0f;
					}
				}
			}
		}

	};
	
	/** The sector. */
	protected final IndustrialSector sector;

	/** The supply. */
	protected Supply supply;

	/** The employer behavior. */
	protected final WorkforceManager workforceManager = new WorkforceManager() {

		/** jobOffer */
		private JobOffer jobOffer;

		/** The payroll (= the anticipated wage bill) */
		private long payroll;

		private int vacancies;

		/** The wage. */
		private Double wage;

		/** The wage bill */
		private long wageBill;

		/** The workforce. */
		private final Workforce workforce = new Workforce();

		/**
		 * Returns the average wage from a random sample of 3 firms.
		 * @return the average wage from a random sample of 3 firms.
		 * @since 23-11-2014
		 */
		private Double getRandomWage() {
			final Double result;
			final List<Firm> sample = sector.getSimpleRandomSample(3);
			if (!sample.isEmpty()) {
				double sum=0;
				int count=0;
				for (Firm firm: sample) {
					final Double wage1 = firm.getWage();
					if (wage1!=null) {
						sum += wage1;
						count ++;
					}
				}
				if (count!=0) {
					result = sum/count;					
				}
				else {
					result=null;
				}
			}
			else {
				result = null;
			}
			history.add("Random wage: "+result);
			return result;
		}

		/**
		 * Returns the vacancy rate.
		 * @return the vacancy rate.
		 */
		private double getVacancyRate() {
			final double result;
			if (!memory.checkConsistency("vacancies","jobs")) {
				throw new RuntimeException("Inconsistent series.");
			}
			final double vacancies = memory.getSum("vacancies", Circuit.getCurrentPeriod().intValue()-1,4);
			final double jobs = memory.getSum("jobs", Circuit.getCurrentPeriod().intValue()-1,4);
			if (vacancies==0) {
				result=0;
			}
			else {
				result=vacancies/jobs;
			}
			return result;
		}

		@Override
		public void close() {
			this.jobOffer=null;
			this.wageBill=0;
			this.payroll=0;
			this.vacancies=0;
		}

		@Override
		public Double getAverageWage() {
			return this.workforce.getAverageWage();
		}

		@Override
		public JobOffer getJobOffer() {
			final JobOffer result;
			if (this.vacancies>0) {
				result=this.jobOffer;
			}
			else {
				result=null;
			}
			return result;
		}

		@Override
		public LaborPower[] getLaborPowers() {
			return this.workforce.getLaborPowers();
		}

		@Override
		public long getPayroll() {
			return this.payroll;
		}

		@Override
		public Double getWage() {
			return this.wage;
		}

		@Override
		public long getWageBill() {
			return this.wageBill;
		}

		@Override
		public int getWorkforceSize() {
			return this.workforce.size();
		}

		@Override
		public void layoff() {
			workforce.layoff();
		}

		@Override
		public void payWorkers() {
			this.wageBill=0;
			for (JobContract contract: workforce) {
				contract.payWage(account.newCheque(contract.getWage()));
				wageBill+=contract.getWage();
			}
		}

		@Override
		public void updateJobOffer() {
			memory.put("jobs",vacancies);
			if (vacancies==0) {
				jobOffer = null;
			}
			else {
				final Period validPeriod = Circuit.getCurrentPeriod();
				jobOffer = new JobOffer() {

					private final long jobWage = (long) Math.floor(wage);

					@Override
					public JobContract apply(final Worker worker) {
						if (!validPeriod.isPresent()) {
							throw new AnachronismException("Out of date.");					
						}
						if (!(vacancies>0)) {
							throw new RuntimeException("No vacancy.");
						}
						vacancies--;
						final JobContract jobContract = new JobContract() {

							private Period end;

							final private Period start = Circuit.getCurrentPeriod();

							{
								final int term;
								final float min = sector.getFloatParameter(LABOUR_CONTRACT_MIN);
								final float max = sector.getFloatParameter(LABOUR_CONTRACT_MAX);
								if (max==min) {
									term = (int) min ; 
								}
								else {
									term = (int) (min+Circuit.getRandom().nextInt((int) (max-min))) ;
								}
								end = start.plus(term);
							}

							@Override
							public void breach() {
								this.end  = Circuit.getCurrentPeriod();
								// TODO Inform the worker ?
							}

							@Override
							public LaborPower getLaborPower() {
								if (!isValid()) {
									throw new RuntimeException("Invalid job contract.");
								}
								return worker.getLaborPower();
							}

							@Override
							public long getWage() {
								return jobWage;
							}

							@Override
							public boolean isValid() {
								return this.end.isAfter(Circuit.getCurrentPeriod());
							}

							@Override
							public void payWage(Cheque paycheck) {
								if (!isValid()) {
									throw new RuntimeException("Invalid job contract.");
								}
								worker.earnWage(paycheck);
							}

							@Override
							public String toString() {
								return "Employer: "+name+
										", Employee: "+worker.getName()+
										", start: "+start.intValue()+
										", end: "+end.intValue()+
										", wage: "+wage;
							}

						};
						workforce.add(jobContract);
						return jobContract;

					}

					@Override
					public long getWage() {
						return jobWage;
					}
				};
			}
		}

		@Override
		public void updateVacancies() {
			memory.put("vacancies",this.vacancies);			
		}

		/**
		 * Updates the wage.
		 */
		@Override
		public void updateWage() {
			history.add("Current wage: "+this.wage);
			if (this.wage==null) {
				this.wage = getRandomWage();
				if (this.wage==null) {
					this.wage = (double) sector.getFloatParameter(WAGE_INITIAL_VALUE);
					history.add("Update wage: using default value.");
				}
			}
			else {
				final float alpha1 = Circuit.getRandom().nextFloat();
				final float alpha2 = Circuit.getRandom().nextFloat();
				final double vacancyRatio = getVacancyRate()/sector.getFloatParameter(NORMAL_VACANCY_RATE);
				final double newWage;
				if (vacancyRatio<1-alpha1*alpha2) {
					newWage=this.wage*(1f-alpha1*sector.getFloatParameter(WAGE_FLEX_DOWN));
				}
				else if (vacancyRatio>1+alpha1*alpha2) {
					newWage=this.wage*( 1f+alpha1*sector.getFloatParameter(WAGE_FLEX_UP));
				}
				else {
					newWage=this.wage;
				}
				this.wage = Math.max(newWage, sector.getFloatParameter(WAGE_MINIMUM));
			}
			history.add("New wage: "+this.wage);
		}

		@Override
		public void updateWorkforce() {
			workforce.cleanUp();
			final int manpowerTarget = Math.round(factory.getCapacity()*productionManager.getTarget());
			if (manpowerTarget<=workforce.size()) {
				vacancies = manpowerTarget-workforce.size();
				if (manpowerTarget<workforce.size()) {
					workforce.layoff(workforce.size()-manpowerTarget);
				}
				payroll = workforce.getPayroll();
			}
			else {
				vacancies = manpowerTarget-workforce.size();
				payroll = workforce.getPayroll() + vacancies* (long) ((double) this.wage);
			}			
		}

	};

	/**
	 * Creates a new firm.
	 * @param name the name.
	 * @param sector the sector.
	 */
	public BasicFirm(String name,IndustrialSector sector) {
		this.history.add("Creation: "+name);
		this.name=name;
		this.sector=sector;
		this.account = this.sector.getNewAccount(this);
		this.factory = getNewFactory();
		this.pricingManager = getNewPricingManager();
	}

	/**
	 * Prints the history of the firm.
	 * @return <code>true</code>
	 */
	private boolean printHistory() {
		for (String string:this.history) {
			System.out.println(string);
		}
		return true;
	}

	/**
	 * Creates and return a new commodity supply.
	 * @return a new commodity supply.
	 */
	protected Supply createSupply() {
		final Supply supply;
		final Period validPeriod = Circuit.getCurrentPeriod();
		final long initialSize = Math.min((long) (sector.getFloatParameter(PROPENSITY2SELL)*factory.getFinishedGoodsVolume()), (long) (sector.getFloatParameter(SELLING_CAPACITY)*factory.getMaxUtilAverageProduction()));
		if (initialSize==0) {
			supply = null;
		}
		else {
			if (pricingManager.getPrice()==null) {
				pricingManager.updatePrice();
			}
			final double price = pricingManager.getPrice();
			supply = new Supply() {

				private long salesValue=0;

				private long salesValueAtCost=0;

				private long salesVolume=0;

				private long volume=initialSize;

				@Override
				public Commodities buy(long demand,Cheque cheque) {
					if (!validPeriod.isPresent()) {
						throw new AnachronismException("Bad period.");
					}
					if (demand>this.volume) {
						throw new IllegalArgumentException("Demand cannot exceed supply.");
					}
					if ((long)(this.getPrice()*demand)!=cheque.getAmount()) {
						throw new IllegalArgumentException("Cheque amount : expected <"+(long) (demand*this.getPrice())+"> but was <"+cheque.getAmount()+">");
					}
					account.deposit(cheque);
					this.volume-=demand;
					this.salesValue+=cheque.getAmount();
					this.salesVolume+=demand;
					final Commodities sales = factory.getCommodities(demand);
					this.salesValueAtCost += sales.getValue(); 
					return sales;
				}

				@Override
				public double getGrossProfit() {
					return this.salesValue-this.salesValueAtCost;
				}

				@Override
				public long getInitialVolume() {
					return initialSize;
				}

				@Override
				public double getPrice() {
					return price;
				}

				@Override
				public long getPrice(long volume) {
					return (long) (price*volume);
				}

				@Override
				public double getSalesRatio() {
					return ((double) this.salesVolume)/initialSize;
				}

				@Override
				public double getSalesValue() {
					return this.salesValue;
				}

				@Override
				public double getSalesValueAtCost() {
					return this.salesValueAtCost;
				}

				@Override
				public double getSalesVolume() {
					return this.salesVolume;
				}

				@Override
				public Supplier getSupplier() {
					return BasicFirm.this;
				}

				@Override
				public long getVolume() {
					return this.volume;
				}

				@Override
				public String toString() {
					return "Supply by "+name+": price <"+price+">, volume <"+volume+">"; 
				}

			};
		}
		return supply;
	}

	/**
	 * Returns the age of the agent (= the number of periode since its creation).
	 * @return the age of the agent.
	 */
	protected int getAge() {
		return Circuit.getCurrentPeriod().intValue()-this.creation;
	}

	/**
	 * Returns the inventory ratio.<br>
	 * If inventoryRatio > 1 : the volume of finished goods exceeds the normal volume,<br>
	 * If inventoryRatio = 1 : the volume of finished goods meets the normal volume,<br>
	 * If inventoryRatio < 1 : the volume of finished goods is under the normal volume.
	 * @return the inventory ratio.
	 */
	protected double getInventoryRatio() {
		return this.factory.getFinishedGoodsVolume()/(sector.getFloatParameter(INVENTORY_NORMAL_LEVEL)*this.factory.getMaxUtilAverageProduction());
	}

	/**
	 * Creates and returns a new capital manager.
	 * @return a new capital manager.
	 */
	protected CapitalManager getNewCapitalManager() {
		return 	new CapitalManager() {

			/** The dividend. */
			//protected Long dividend = null;

			/** The owner. */
			protected Shareholder owner;

			@Override
			public void bankrupt() {
				owner.removeAsset(BasicFirm.this);				
			}

			@Override
			public void close() {
				// Does nothing.
			}

			@Override
			public long getCapital() {
				return factory.getValue() + account.getAmount() - account.getDebt();
			}

			@Override
			public double getLiabilitiesExcess() {
				final double result;
				final double excess = account.getDebt()-getLiabilitiesTarget();
				result = Math.max(0, excess);
				return result;
			}

			@Override
			public double getLiabilitiesTarget() {
				final long assets = account.getAmount()+factory.getValue();
				final long capitalTarget = (long) ((assets)*sector.getFloatParameter(CAPITAL_TARGET));
				return assets-capitalTarget;
			}

			@Override
			public boolean isSatisfacing() {
				final long cash = account.getAmount();
				final long assets = cash+factory.getValue();
				final long capital = getCapital();
				final long capitalTarget = (long) ((assets)*sector.getFloatParameter(CAPITAL_TARGET));
				return (capital>=capitalTarget);
			}

			@Override
			public long newDividend() {
				final long dividend;
				final long cash = account.getAmount();
				final long assets = cash+factory.getValue();
				final long capital = getCapital();
				final long capitalTarget = (long) ((assets)*sector.getFloatParameter(CAPITAL_TARGET));
				if (capital<=0) {
					dividend=0l;
				}
				else {
					if (capital<=capitalTarget) {
						dividend=0l;
					}
					else {
						dividend = Math.min((long) ((capital-capitalTarget)*sector.getFloatParameter(CAPITAL_PROPENSITY2DISTRIBUTE)),cash);
					}
				}
				return dividend;
			}

			@Override
			public void open() {
				this.updateOwnership();
			}

			@Override
			public void payDividend() {
				BasicFirm.this.history.add("cash: "+ account.getAmount());
				BasicFirm.this.history.add("assets: "+ (factory.getValue() + account.getAmount()));
				BasicFirm.this.history.add("liabilities: "+ account.getDebt());
				BasicFirm.this.history.add("capital: "+ getCapital());
				final long dividend = newDividend();
				memory.put(MEM_DIVIDEND, dividend);
				if (dividend>0) {
					this.owner.receiveDividend( BasicFirm.this.account.newCheque(dividend), BasicFirm.this) ;
					BasicFirm.this.history.add("dividend: "+ dividend);
					BasicFirm.this.history.add("cash: "+ account.getAmount());
					BasicFirm.this.history.add("assets: "+ (factory.getValue() + account.getAmount()));
					BasicFirm.this.history.add("liabilities: "+ account.getDebt());
					BasicFirm.this.history.add("capital: "+ (factory.getValue() + account.getAmount() - account.getDebt()));
				}
				BasicFirm.this.data.put("debt2target.ratio", (account.getDebt())/capitalManager.getLiabilitiesTarget());
			}

			@Override
			public void updateOwnership() {
				if (this.owner==null){
					this.owner=BasicFirm.this.sector.selectCapitalOwner();
					this.owner.addAsset(BasicFirm.this);
					BasicFirm.this.history.add("New capital owner: "+this.owner.getName());
				}
			}
		};
	}

	/**
	 * Creates and returns a new agent dataset.
	 * @return a new agent dataset.
	 */
	protected AgentDataset getNewDataset() {
		return new AbstractAgentDataset(name) {

			@Override
			public void update() {
				this.put("firms", 1.);
				this.put("age", (double) getAge());
				this.put("prices", pricingManager.getPrice());
				this.put("wages", workforceManager.getWage());
				this.put("workforce", (double) factory.getWorkforce());
				this.put("inventories.inProcess.val", factory.getGoodsInProcessValue());
				this.put("inventories.fg.vol", (double) factory.getFinishedGoodsVolume());
				this.put("inventories.fg.val", (double) factory.getFinishedGoodsValue());
				this.put("inventories.fg.vol.normal", sector.getFloatParameter(INVENTORY_NORMAL_LEVEL)*factory.getMaxUtilAverageProduction());						
				this.put("production.vol", (double) factory.getProductionVolume());
				this.put("production.val", (double) factory.getProductionValue());
				this.put("productivity", factory.getProductivity());
				this.put("wageBill", (double) workforceManager.getWageBill());
				this.put("dividends", (double) memory.get(MEM_DIVIDEND).longValue());
				if (supply!=null) {
					this.put("supply.vol",(double) supply.getInitialVolume());
					this.put("supply.val",(double) supply.getPrice(supply.getInitialVolume()));
					this.put("sales.vol", supply.getSalesVolume());
					this.put("sales.val", supply.getSalesValue());
					this.put("sales.costValue", supply.getSalesValueAtCost());
					this.put("grossProfit", supply.getGrossProfit()+factory.getInventoryLosses());
				} 
				else {
					this.put("supply.vol", 0.);
					this.put("supply.val", 0.);
					this.put("sales.vol", 0.);
					this.put("sales.val", 0.);
					this.put("sales.costValue", 0.);
					this.put("grossProfit", factory.getInventoryLosses());
				}
				this.put("interest", (double) account.getInterest());
				if (bankrupted){
					this.put("bankruptcies", 1.);
				}
				else {
					this.put("bankruptcies", 0.);					
				}
				this.put("cash", (double) account.getAmount());
				this.put("assets", (double) factory.getValue() + account.getAmount());
				this.put("liabilities", (double) account.getDebt());
				this.put("liabilities.target", capitalManager.getLiabilitiesTarget());
				this.put("liabilities.excess", capitalManager.getLiabilitiesExcess());
				this.put("capital", (double) factory.getValue() + account.getAmount() - account.getDebt());
				this.put("capacity", (double) factory.getCapacity());
				if (Circuit.getCurrentPeriod().intValue()-creation > 12 // 12 should be a parameter 
						&& factory.getValue() + account.getAmount() < account.getDebt()){
					this.put("insolvents", 1.);
				}
				else {
					this.put("insolvents", 0.);					
				}

			}
		};
	}

	/**
	 * Creates and returns a new factory.
	 * @return a new factory.
	 * @since 23-11-2014
	 */
	protected Factory getNewFactory() {
		return new BasicFactory((int) sector.getFloatParameter(PRODUCTION_TIME), (int) sector.getFloatParameter(PRODUCTION_CAPACITY), sector.getFloatParameter(PRODUCTIVITY));
	}

	/**
	 * Creates and returns a new pricing manager.
	 * @return a new pricing manager.
	 */
	protected PricingManager getNewPricingManager() {
		return new PricingManager() {

			/** The higher price. */
			private Double highPrice = null;

			/** The lower price. */
			private Double lowPrice = null;

			/** The price. */
			private Double price;

			/**
			 * Returns a new price chosen at random in the given interval.
			 * @param lowPrice  the lower price
			 * @param highPrice  the higher price
			 * @return the new price.
			 */
			private double getNewPrice(Double lowPrice, Double highPrice) {
				if (lowPrice>highPrice) {
					throw new IllegalArgumentException("lowPrice > highPrice.");
				}
				return lowPrice+Circuit.getRandom().nextFloat()*(highPrice-lowPrice);
			}

			@Override
			public Double getPrice() {
				final Double result;
				if (this.price==null) {
					result=null;
				}
				else {
					result=new Double(this.price);
				}
				return result;
			}

			/**
			 * Sets the price equal to the unit cost.
			 */
			public void setUnitCostPrice() {
				final double unitCost = factory.getUnitCost();
				if (!Double.isNaN(unitCost)) {
					final float priceFlexibility = sector.getFloatParameter(PRICE_FLEXIBILITY);
					this.price = unitCost;
					this.highPrice = (1f+priceFlexibility)*this.price;
					this.lowPrice = (1f-priceFlexibility)*this.price;
				}
			}

			@Override
			public void updatePrice() {
				final double inventoryRatio = getInventoryRatio();
				if (this.price==null) {
					this.setUnitCostPrice();
				}
				if (this.price!=null && supply!=null) {
					final float priceFlexibility = sector.getFloatParameter(PRICE_FLEXIBILITY);
					if ((supply.getSalesRatio()==1)) {
						this.lowPrice = this.price;
						if (inventoryRatio<1) {
							this.price = getNewPrice(this.lowPrice,this.highPrice);
						}
						this.highPrice =  this.highPrice*(1f+priceFlexibility);
					}
					else {
						this.highPrice = this.price;
						if (inventoryRatio>1) {
							this.price = getNewPrice(this.lowPrice,this.highPrice);
						}					
						this.lowPrice =  this.lowPrice*(1f-priceFlexibility);
					}
				}
			}


		};
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {
		this.data.update();
	}

	@Override
	public void bankrupt() {
		this.bankrupted = true;
	}

	@Override
	public void close() {
		this.capitalManager.close();
		this.workforceManager.updateVacancies();
		this.updateData();
		this.workforceManager.close();
	}

	/* 
	 * @since 22-11-2014.
	 */
	@Override
	public Object execute(String instruction, Object... args) {

		final Object result;

		if ("history.start".equals(instruction)) {
			// Starts recording the history of the firm.
			// (since 22-11-2014).
			this.recordHistoric=true;
			result = null;
		}

		else if ("history.stop".equals(instruction)) {
			// Stops recording the history of the firm.
			// (since 22-11-2014).
			this.recordHistoric=false;
			result = null;
		}

		else if ("history.print".equals(instruction)) {
			// Prints the history of the firm.
			// (since 22-11-2014).
			this.printHistory();
			result = null;
		}

		else if ("productivityShock".equals(instruction)) {
			this.factory.setProductivity((Float) args[0]);
			result = null;
		}

		else {
			throw new RuntimeException("Unknown instruction: "+instruction);
		}

		return result;
	}

	@Override
	public Object get(String string) {
		return null;
	}

	@Override
	public long getAssets() {
		return factory.getValue() + account.getAmount();
	}

	@Override
	public long getBookValue() {
		return getAssets() - account.getDebt();
	}

	@Override
	public AgentDataset getData() {
		return this.data;
	}

	@Override
	public JobOffer getJobOffer() {
		return this.workforceManager.getJobOffer();
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Supply getSupply() {
		final Supply result;
		if (this.supply!=null && this.supply.getVolume()>0){
			result=this.supply;
		}
		else {
			result=null;
		}
		return result;
	}

	@Override
	public Double getWage() {
		return this.workforceManager.getWage();
	}

	@Override
	public boolean isBankrupted() {
		return this.bankrupted;
	}

	@Override
	public void open() {
		this.data=getNewDataset();
		this.history.add("");
		this.history.add("Period: "+Circuit.getCurrentPeriod().intValue());
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
			this.factory.bankrupt();
			this.workforceManager.layoff();
		}
		else {
			this.capitalManager.open();
		}
	}

	@Override
	public void payDividend() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.capitalManager.payDividend();
	}

	@Override
	public void prepareProduction() {

		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		// *** Updates the price.

		this.pricingManager.updatePrice();

		// *** Updates the wage.

		this.workforceManager.updateWage();

		// *** Updates the targeted production level.

		this.productionManager.updateCapacityUtilizationTarget();

		// *** Updates the workforce and computes the payroll.

		this.workforceManager.updateWorkforce();

		// *** Secures financing.

		final long payroll = this.workforceManager.getPayroll();

		if ( payroll>this.account.getAmount() ) {
			account.lend(payroll-this.account.getAmount()) ;
		}
		if (account.getAmount() < payroll) {
			throw new RuntimeException("Production is not financed.") ;
		}

		this.workforceManager.updateJobOffer();

	}

	@Override
	public void production() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.workforceManager.payWorkers();
		factory.process(this.workforceManager.getLaborPowers()) ;
		this.supply = createSupply();
	}
	
}

// ***
