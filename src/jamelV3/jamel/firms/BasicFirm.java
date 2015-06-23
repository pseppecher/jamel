package jamelV3.jamel.firms;

import jamelV3.basic.agent.BasicAgentDataset;
import jamelV3.basic.agent.AgentDataset;
import jamelV3.basic.util.Period;
import jamelV3.basic.util.Timer;
import jamelV3.jamel.firms.managers.CapitalManager;
import jamelV3.jamel.firms.managers.PricingManager;
import jamelV3.jamel.firms.managers.ProductionManager;
import jamelV3.jamel.firms.managers.WorkforceManager;
import jamelV3.jamel.firms.util.BasicFactory;
import jamelV3.jamel.firms.util.Factory;
import jamelV3.jamel.firms.util.Workforce;
import jamelV3.jamel.roles.Shareholder;
import jamelV3.jamel.roles.Supplier;
import jamelV3.jamel.roles.Worker;
import jamelV3.jamel.util.AnachronismException;
import jamelV3.jamel.util.BasicMemory;
import jamelV3.jamel.util.Memory;
import jamelV3.jamel.widgets.BankAccount;
import jamelV3.jamel.widgets.Cheque;
import jamelV3.jamel.widgets.Commodities;
import jamelV3.jamel.widgets.JobContract;
import jamelV3.jamel.widgets.JobOffer;
import jamelV3.jamel.widgets.LaborPower;
import jamelV3.jamel.widgets.Supply;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * A basic firm.
 */
public class BasicFirm implements Firm {

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

	/** A flag that indicates if the data of the firm is to be exported. */
	private boolean exportData;

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
	protected final int creation;

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
	protected final Memory memory;

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
				final float alpha1 = random.nextFloat();
				final float alpha2 = random.nextFloat();
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

	/** The random. */
	final protected Random random;

	/** The sector. */
	protected final IndustrialSector sector;

	/** The supply. */
	protected Supply supply;

	/** The timer. */
	final protected Timer timer;

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
		private Double getVacancyRate() {
			final Double result;
			if (!memory.checkConsistency("vacancies","jobs")) {
				throw new RuntimeException("Inconsistent series.");
			}
			final Double vacancies = memory.getSum("vacancies", timer.getPeriod().intValue()-1,4);
			final Double jobs = memory.getSum("jobs", timer.getPeriod().intValue()-1,4);
			if (vacancies==null&&jobs==null) {
				result = null;
			} else if (vacancies==0) {
				result=0.;
			}
			else {
				result=vacancies/jobs;
			}
			return result;
		}

		private void newJobOffer() {
			memory.put("jobs",vacancies);
			if (vacancies<0) {
				throw new RuntimeException("Negative number of vacancies");
			}
			if (vacancies==0) {
				jobOffer = null;
			}
			else {
				final Period validPeriod = timer.getPeriod();
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

							final private Period start = timer.getPeriod();

							{
								final int term;
								final float min = sector.getFloatParameter(LABOUR_CONTRACT_MIN);
								final float max = sector.getFloatParameter(LABOUR_CONTRACT_MAX);
								if (max==min) {
									term = (int) min ; 
								}
								else {
									term = (int) (min+random.nextInt((int) (max-min))) ;
								}
								end = start.plus(term);
							}

							@Override
							public void breach() {
								this.end  = timer.getPeriod();
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
								return this.end.isAfter(timer.getPeriod());
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
					public Object getEmployerName() {
						return BasicFirm.this.getName();
					}

					@Override
					public long getWage() {
						return jobWage;
					}
				};
			}
		}

		@Override
		public void close() {
			memory.put("vacancies",this.vacancies);
			data.put("vacancies.final",(double) this.vacancies);
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

		/*
		 * TODO 
		 * 14-02-15
		 * Revoir la procedure d'ajustement des salaires de BasicFirm.
		 * L'ajustement est trop lent en cas de forte penurie de main d'oeuvre.
		 * Proposer une methode avec elargissement croissant de la zone de recherche (procedure explosive ?).
		 * Comparer le nombre d'emplois vacants avec le nombre total de postes de l'entreprise (et non pas seulement avec le nombre de postes offerts).
		 */  		
		@Override
		public void updateWage() {
			final Double vacancyRate = getVacancyRate();
			final Double vacancyRatio;
			if (vacancyRate!=null) {
				vacancyRatio = getVacancyRate()/sector.getFloatParameter(NORMAL_VACANCY_RATE);	
			}
			else {
				vacancyRatio = null;
			}
			history.add("Current wage: "+this.wage);
			if (this.wage==null) {
				this.wage = getRandomWage();
				if (this.wage==null) {
					this.wage = (double) sector.getFloatParameter(WAGE_INITIAL_VALUE);
					history.add("Update wage: using default value.");
				}
			}
			else {
				final float alpha1 = random.nextFloat();
				final float alpha2 = random.nextFloat();
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
			data.put("vacancies.rate",vacancyRate);
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
				vacancies = manpowerTarget-workforce.size();
				if (vacancies!=0) {
					throw new RuntimeException("Negative number of vacancies");
				}
			}
			else {
				vacancies = manpowerTarget-workforce.size();
				payroll = workforce.getPayroll() + vacancies* (long) ((double) this.wage);
			}
			BasicFirm.this.data.put("vacancies.initial",(double) vacancies);
			BasicFirm.this.data.put("workforce.target",(double) manpowerTarget);
			this.newJobOffer();
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
		this.timer = this.sector.getTimer();
		this.creation = this.timer.getPeriod().intValue();
		this.random = this.sector.getRandom();
		this.memory = new BasicMemory(timer, 24);
		this.account = this.sector.getNewAccount(this);
		this.factory = getNewFactory();
		this.pricingManager = getNewPricingManager();
	}

	/**
	 * Exports agent data in a csv file.
	 * @throws IOException in the case of an I/O exception.
	 */
	private void exportData() throws IOException {
		if (this.exportData) {
			// TODO gerer la localisation du dossier exports, son existence
			final File outputFile = new File("exports/"+sector.getSimulationID()+"-"+this.name+".csv");
			if (!outputFile.exists()) {
				this.data.exportHeadersTo(outputFile);
			}
			this.data.exportTo(outputFile);
		}
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
		final Period validPeriod = timer.getPeriod();
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
		return timer.getPeriod().intValue()-this.creation;
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
	
			private long dividend;
			private long initialCapital;
			/** The owner. */
			protected Shareholder owner;
	
			@Override
			public void bankrupt() {
				owner.removeAsset(BasicFirm.this);				
			}
	
			@Override
			public void close() {
				isConsistent();
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
			public boolean isConsistent() {
				final boolean isConsistent;
				final double grossProfit;
				final double inventoryLosses = factory.getInventoryLosses();
				if (supply!=null) {
					grossProfit = supply.getGrossProfit();			
				}
				else {
					grossProfit = 0;				
				}
				final double interest = account.getInterest();
				final double bankruptcy = account.getCanceledMoney()-account.getCanceledDebt();
				final long capital = this.getCapital();
				isConsistent = (capital == this.initialCapital + grossProfit - (inventoryLosses + this.dividend + interest + bankruptcy));  
				if (!isConsistent){
					System.out.println("capital = "+ capital);
					System.out.println("expected = "+ (this.initialCapital + grossProfit - (inventoryLosses + this.dividend + interest + bankruptcy)));
					throw new RuntimeException("Inconsistency");}
				return isConsistent;
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
				this.initialCapital=this.getCapital();
				this.dividend=0;
				isConsistent();
			}
	
			@Override
			public void payDividend() {
				isConsistent();
				// TODO: hou la la ! revoir ces appels ˆ history
				BasicFirm.this.history.add("cash: "+ account.getAmount());
				BasicFirm.this.history.add("assets: "+ (factory.getValue() + account.getAmount()));
				BasicFirm.this.history.add("liabilities: "+ account.getDebt());
				BasicFirm.this.history.add("capital: "+ getCapital());
				dividend = newDividend();
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
				isConsistent();
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
		return new BasicAgentDataset(name);
	}

	/**
	 * Creates and returns a new factory.
	 * @return a new factory.
	 */
	protected Factory getNewFactory() {
		return new BasicFactory((int) sector.getFloatParameter(PRODUCTION_TIME), (int) sector.getFloatParameter(PRODUCTION_CAPACITY), sector.getFloatParameter(PRODUCTIVITY), timer);
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

			/** The sales ratio observed the last period. */
			private Double salesRatio = null;

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
				return lowPrice+random.nextFloat()*(highPrice-lowPrice);
			}

			@Override
			public Double getPrice() {
				return this.price;
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
				if (this.price!=null && salesRatio!=null) {
					final float priceFlexibility = sector.getFloatParameter(PRICE_FLEXIBILITY);
					if ((salesRatio==1)) {
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

			@Override
			public void close() {
				if (supply!=null) {
					this.salesRatio  = supply.getSalesRatio();
				}
				else {
					this.salesRatio = null;
				}
			}


		};
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {
		this.data.put("firms", 1.);
		this.data.put("age", (double) getAge());
		this.data.put("prices", pricingManager.getPrice());
		this.data.put("wages", workforceManager.getWage());
		this.data.put("workforce", (double) factory.getWorkforce());
		this.data.put("inventories.inProcess.val", factory.getGoodsInProcessValue());
		this.data.put("inventories.fg.val", (double) factory.getFinishedGoodsValue());
		this.data.put("inventories.fg.vol", (double) factory.getFinishedGoodsVolume());
		this.data.put("inventories.fg.vol.normal", sector.getFloatParameter(INVENTORY_NORMAL_LEVEL)*factory.getMaxUtilAverageProduction());						
		this.data.put("production.vol", (double) factory.getProductionVolume());
		this.data.put("production.val", (double) factory.getProductionValue());
		this.data.put("productivity", factory.getProductivity());
		this.data.put("wageBill", (double) workforceManager.getWageBill());
		this.data.put("dividends", (double) memory.get(MEM_DIVIDEND).longValue());
		if (supply!=null) {
			this.data.put("supply.vol",(double) supply.getInitialVolume());
			this.data.put("supply.val",(double) supply.getPrice(supply.getInitialVolume()));
			this.data.put("sales.vol", supply.getSalesVolume());
			this.data.put("sales.val", supply.getSalesValue());
			this.data.put("sales.costValue", supply.getSalesValueAtCost());
			this.data.put("grossProfit", supply.getGrossProfit()-factory.getInventoryLosses());
		} 
		else {
			this.data.put("supply.vol", 0.);
			this.data.put("supply.val", 0.);
			this.data.put("sales.vol", 0.);
			this.data.put("sales.val", 0.);
			this.data.put("sales.costValue", 0.);
			this.data.put("grossProfit", -factory.getInventoryLosses());
		}
		if (bankrupted){
			this.data.put("bankruptcies", 1.);
		}
		else {
			this.data.put("bankruptcies", 0.);					
		}
		this.data.put("cash", (double) account.getAmount());
		this.data.put("interest", (double) account.getInterest());
		this.data.put("canceledDebts", account.getCanceledDebt());
		this.data.put("canceledDeposits", account.getCanceledMoney());
		this.data.put("assets", (double) factory.getValue() + account.getAmount());
		this.data.put("liabilities", (double) account.getDebt());
		this.data.put("liabilities.target", capitalManager.getLiabilitiesTarget());
		this.data.put("liabilities.excess", capitalManager.getLiabilitiesExcess());
		this.data.put("capital", (double) factory.getValue() + account.getAmount() - account.getDebt());
		this.data.put("capacity", (double) factory.getCapacity());
		if (timer.getPeriod().intValue()-creation > 12 // 12 should be a parameter 
				&& factory.getValue() + account.getAmount() < account.getDebt()){
			this.data.put("insolvents", 1.);
		}
		else {
			this.data.put("insolvents", 0.);					
		}

	}

	@Override
	public void close() {
		this.pricingManager.close();
		this.capitalManager.close();
		this.updateData();
		this.workforceManager.close();
		this.supply=null;
		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data",e);
		}
	}

	@SuppressWarnings("javadoc")
	public Object execute(String instruction) {
		// TODO hou la la ! revenir ici sans faute

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

		else if ("exportData.start".equals(instruction)) {
			this.exportData = true;
			result = null;
		}

		else if ("exportData.end".equals(instruction)) {
			this.exportData = false;
			result = null;
		}

		else {
			throw new RuntimeException("Unknown instruction: "+instruction);
		}

		return result;
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
	public void goBankrupt() {
		this.bankrupted = true;
		this.factory.bankrupt();
	}

	@Override
	public boolean isBankrupted() {
		return this.bankrupted;
	}

	@Override
	public void open() {
		this.data=getNewDataset();
		this.history.add("");
		this.history.add("Period: "+timer.getPeriod().intValue());
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
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