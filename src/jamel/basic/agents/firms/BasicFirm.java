package jamel.basic.agents.firms;

import jamel.basic.agents.firms.util.BasicFactory;
import jamel.basic.agents.firms.util.Factory;
import jamel.basic.agents.firms.util.Workforce;
import jamel.basic.agents.roles.CapitalOwner;
import jamel.basic.agents.roles.Supplier;
import jamel.basic.agents.roles.Worker;
import jamel.basic.agents.util.Memory;
import jamel.basic.agents.util.LaborPower;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
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

/**
 * A basic firm.
 */
public class BasicFirm implements Firm {

	/**
	 * The capital manager.
	 */
	private interface CapitalManager {

		/**
		 * Updates the dividend.
		 */
		void updateDividend();

		/**
		 * Returns the dividend.
		 * @return the dividend paid.
		 */
		long getDividend();

	}

	/**
	 * A class to store the parameters of the firm.
	 */
	private class Parameters {

		@SuppressWarnings("javadoc")
		private final static String CAPITAL_PROPENSITY2DISTRIBUTE = "capital.propensityToDistribute";

		@SuppressWarnings("javadoc")
		private final static String CAPITAL_TARGET = "capital.target";

		@SuppressWarnings("javadoc")
		private static final String INVENTORY_NORMAL_LEVEL = "inventory.normalLevel";

		@SuppressWarnings("javadoc")
		private static final String LABOUR_CONTRACT_MAX = "labourContract.max";

		@SuppressWarnings("javadoc")
		private static final String LABOUR_CONTRACT_MIN = "labourContract.min";

		@SuppressWarnings("javadoc")
		private static final String NORMAL_VACANCY_RATE = "vacancy.normalRate";

		@SuppressWarnings("javadoc")
		private final static String PRICE_FLEXIBILITY = "price.flexibility";

		@SuppressWarnings("javadoc")
		private static final String PRODUCTION_CAPACITY = "production.capacity";

		@SuppressWarnings("javadoc")
		private static final String PRODUCTION_TIME = "production.time";

		@SuppressWarnings("javadoc")
		private static final String PRODUCTIVITY = "production.productivity";

		@SuppressWarnings("javadoc")
		private static final String PROPENSITY2SELL = "inventory.propensity2sell";

		@SuppressWarnings("javadoc")
		private static final String SELLING_CAPACITY = "sales.capacity";

		@SuppressWarnings("javadoc")
		private final static String UTILIZATION_RATE_FLEXIBILITY = "utilizationRate.flexibility";

		@SuppressWarnings("javadoc")
		private static final String UTILIZATION_RATE_INITIAL_VALUE = "utilizationRate.initialValue";

		@SuppressWarnings("javadoc")
		private static final String WAGE_FLEX_DOWN = "wage.flexibility.downward";

		@SuppressWarnings("javadoc")
		private static final String WAGE_FLEX_UP = "wage.flexibility.upward";

		@SuppressWarnings("javadoc")
		private static final String WAGE_INITIAL_VALUE = "wage.initialValue";

		@SuppressWarnings("javadoc")
		private static final String WAGE_MINIMUM = "wage.minimum";

		/** The ratio of capital targeted. */
		private float capitalRatioTarget;

		/** 
		 * The normal level of the inventory, as a number of periods of production
		 * (= targeted volume of finished goods/volume of production at full capacity utilization).
		 */
		private float inventoryNormalLevel;

		/** The maximum duration of a job contract.*/
		private int labourContractMax;

		/** The minimum duration of a job contract.*/
		private int labourContractMin;

		/** The normal rate of vacancy. */
		private float normalVacancyRate;

		/** The price flexibility */
		private float priceFlexibility;

		/** The production capacity. */
		private int productionCapacity;

		/** The production time. */
		private int productionTime;

		/** The productivity. */
		private float productivity;

		/** The propensity to distribute the capital. */
		private float propensity2DistributeCapital;

		/** The propensity to sell the inventories. */
		private float propensity2Sell;

		/** The selling capacity. */
		private float sellingCapacity;

		/** The flexibility of the utilization rate. */
		private float utilizationRateFlexibility;

		/** The initial value of the utilization rate. */
		private float utilizationRateInitialValue;

		/** The downward wage flexibility. */
		private float wageFlexDown;

		/** The upward wage flexibility. */
		private float wageFlexUp;

		/** The initial value of the wage. */
		private int wageInitialValue;

		/** The minimum wage. */
		private int wageMinimum;

		/**
		 * Returns the float value of the specified parameter.
		 * @param key the key of the parameter.
		 * @return a float.
		 */
		private float getFloat(String key) {
			try {
				return Float.parseFloat(getParameter(key));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new RuntimeException("Parameter "+key+": a float was expected but I found: "+getParameter(key));
			}
		}

		/**
		 * Returns the int value of the specified parameter.
		 * @param key the key of the parameter.
		 * @return an int.
		 */
		private int getInteger(String key) {
			try {
				return Integer.parseInt(getParameter(key));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new RuntimeException("Parameter "+key+": an integer was expected but I found: "+getParameter(key));
			}
		}

		/**
		 * Returns a string containing the value of the specified parameter.
		 * @param key key of the parameter.
		 * @return a string.
		 */
		private String getParameter(String key) {
			final String result = sector.getParameter(key);
			if (result==null) {
				throw new RuntimeException("Parameter not found: "+key );
			}
			return result;
		}

		/**
		 * Updates the parameters.<p>
		 * Called when the firm is created.
		 * (Should be called after an exogenous event.)
		 */
		private void updateCapacityUtilizationTarget() {
			this.capitalRatioTarget = getFloat(CAPITAL_TARGET);
			this.inventoryNormalLevel = getFloat(INVENTORY_NORMAL_LEVEL);
			this.labourContractMax = getInteger(LABOUR_CONTRACT_MAX);
			this.labourContractMin = getInteger(LABOUR_CONTRACT_MIN);
			this.normalVacancyRate = getFloat(NORMAL_VACANCY_RATE);
			this.priceFlexibility = getFloat(PRICE_FLEXIBILITY);
			this.productionCapacity= getInteger(PRODUCTION_CAPACITY);
			this.productionTime = getInteger(PRODUCTION_TIME);
			this.productivity = getFloat(PRODUCTIVITY);
			this.propensity2DistributeCapital = getFloat(CAPITAL_PROPENSITY2DISTRIBUTE);
			this.propensity2Sell = getFloat(PROPENSITY2SELL);
			this.sellingCapacity = getFloat(SELLING_CAPACITY);
			this.utilizationRateFlexibility = getFloat(UTILIZATION_RATE_FLEXIBILITY);
			this.utilizationRateInitialValue = getFloat(UTILIZATION_RATE_INITIAL_VALUE);
			this.wageInitialValue = getInteger(WAGE_INITIAL_VALUE);
			this.wageFlexDown = getFloat(WAGE_FLEX_DOWN);
			this.wageFlexUp = getFloat(WAGE_FLEX_UP);
			this.wageMinimum = getInteger(WAGE_MINIMUM);
		}

	}

	/**
	 * The pricing manager.
	 */
	private interface PricingManager {

		/**
		 * Returns the price.
		 * @return the price.
		 */
		Double getPrice();

		/**
		 * Sets the price equal to the unit cost.
		 */
		void setUnitCostPrice();

		/**
		 * Updates the price.
		 */
		void updatePrice();

	}

	/**
	 * The production manager.
	 */
	private interface ProductionManager {

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
	private interface WorkforceManager {

		/**
		 * Computes some technical operations at the end of the period. 
		 */
		void close();

		/**
		 * Returns the job offer.
		 * @return the job offer.
		 */
		JobOffer getJobOffer();

		/**
		 * Returns the wage.
		 * @return the wage.
		 */
		double getWage();

		/**
		 * Creates a new job offer.
		 * @param jobs the number of vacancies.
		 */
		void updateJobOffer(int jobs);

		/**
		 * Updates the wage.
		 */
		public void updateWage();

	}

	/** The account. */
	private final BankAccount account;

	/** A flag that indicates if the firm is bankrupted. */
	private boolean bankrupted = false;

	/** The capital manager. */
	private final  CapitalManager capitalManager = new CapitalManager() {

		/** The dividend. */
		private Long dividend = null;
		
		@Override
		public void updateDividend() {
			final long cash = account.getAmount();
			final long assets=cash+factory.getValue();
			final long capital=assets-account.getDebt();
			final long capitalTarget = (long) ((assets)*p.capitalRatioTarget );
			if (capital<=0) {
				dividend=0l;
			}
			else {
				if (capital<=capitalTarget) {
					dividend=0l;
				}
				else {
					dividend = Math.min((long) ((capital-capitalTarget)*p.propensity2DistributeCapital),cash);
				}
			}
		}

		@Override
		public long getDividend() {
			return dividend;
		}
	};

	/** Date of creation. */
	private final int creation = Circuit.getCurrentPeriod().getValue();

	/** The data of the agent. */
	private AgentDataset data;

	/** The dividend. */
	//private long dividend;

	/** The factory. */
	private final Factory factory;

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

	/** The data series for the income. */
	private Memory incomeSeries = new Memory(12);// 4 should be a parameter.

	/** The name. */
	private final String name;

	/** The owner. */
	private CapitalOwner owner;

	/** The parameters. */
	private final Parameters p=new Parameters();

	/** The pricing manager. */
	private final PricingManager pricingManager = new PricingManager() {

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

		@Override
		public void setUnitCostPrice() {
			final double unitCost = factory.getUnitCost();
			if (!Double.isNaN(unitCost)) {
				this.price = unitCost;
				this.highPrice = (1f+p.priceFlexibility)*this.price;
				this.lowPrice = (1f-p.priceFlexibility)*this.price;
			}
		}

		@Override
		public void updatePrice() {
			final double inventoryRatio = getInventoryRatio();
			if (this.price==null) {
				this.setUnitCostPrice();
			}
			if (this.price!=null && salesRatio!=null) {
				if ((salesRatio==1)) {
					this.lowPrice = this.price;
					if (inventoryRatio<1) {
						this.price = getNewPrice(this.lowPrice,this.highPrice);
					}
					this.highPrice =  this.highPrice*(1f+p.priceFlexibility);
				}
				else {
					this.highPrice = this.price;
					if (inventoryRatio>1) {
						this.price = getNewPrice(this.lowPrice,this.highPrice);
					}					
					this.lowPrice =  this.lowPrice*(1f-p.priceFlexibility);
				}
			}
		}

	};;

	/** The production manager. */
	private final ProductionManager productionManager = new ProductionManager() {

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
				this.utilizationRateTargeted=p.utilizationRateInitialValue;
			}
			return this.utilizationRateTargeted;
		}

		@Override
		public void updateCapacityUtilizationTarget() {
			if (this.utilizationRateTargeted==null) {
				this.utilizationRateTargeted=p.utilizationRateInitialValue;
			}
			else {
				final double inventoryRatio = getInventoryRatio();
				final float alpha1 = Circuit.getRandom().nextFloat();
				final float alpha2 = Circuit.getRandom().nextFloat();
				final float delta = (alpha1*p.utilizationRateFlexibility);
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

	/** A flag that indicates if the agent records its history. */
	private boolean recordHistoric = false;

	/** The sales ratio. */
	private Double salesRatio = null;

	/** The sector. */
	private final IndustrialSector sector;

	/** The supply. */
	private Supply supply;

	/** The wage bill */
	private long wageBill;

	/** The payroll. */
	private final Workforce workforce = new Workforce();

	/** The employer behavior. */
	private final WorkforceManager workforceManager = new WorkforceManager() {

		/** jobOffer */
		private JobOffer jobOffer;

		/** The data series for the newJobs. */
		private Memory newJobsSeries = new Memory(4);// 4 should be a parameter.

		/** The data series for the vacancies. */
		private Memory vacanciesSeries = new Memory(4);// 4 should be a parameter.

		/** The wage. */
		private Double wage;

		/**
		 * Returns the vacancy rate.
		 * @return the vacancy rate.
		 */
		private double getVacancyRate() {
			final double result;
			if (vacanciesSeries.size()!=newJobsSeries.size()) {
				throw new RuntimeException("Inconsistent series.");
			}
			final double vacancies = vacanciesSeries.getSum();
			final double jobs = newJobsSeries.getSum();
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
			if (this.jobOffer==null) {
				this.vacanciesSeries.add(0);
			}
			else {
				this.vacanciesSeries.add(this.jobOffer.getVacancies());
			}			
			this.jobOffer=null;
		}

		@Override
		public JobOffer getJobOffer() {
			final JobOffer result;
			if (this.jobOffer!=null && this.jobOffer.getVacancies()>0) {
				result=this.jobOffer;
			}
			else {
				result=null;
			}
			return result;
		}

		/**
		 * Returns the wage.
		 * @return the wage.
		 */
		public double getWage() {
			if (this.wage==null) {
				this.wage = (double) p.wageInitialValue;
			}
			return this.wage;
		}

		@Override
		public void updateJobOffer(final int jobs) {
			//final JobOffer newJobOffer;
			newJobsSeries.add(jobs);
			if (jobs==0) {
				jobOffer = null;
			}
			else {
				final Period validPeriod = Circuit.getCurrentPeriod();
				jobOffer = new JobOffer() {
					
					private final long jobWage = (long) Math.floor(wage);
					
					private int vacancies = jobs;
		
					@Override
					public JobContract apply(final Worker worker) {
						if (!validPeriod.isPresent()) {
							throw new AnachronismException("Out of date.");					
						}
						if (!(this.vacancies>0)) {
							throw new RuntimeException("No vacancy.");
						}
						this.vacancies--;
						final JobContract jobContract = new JobContract() {
		
							private Period end;
		
							final private Period start = Circuit.getCurrentPeriod();
		
							{
								final int term;
								if (p.labourContractMax==p.labourContractMin) { 
									term = p.labourContractMin ; 
								}
								else {
									term = p.labourContractMin+Circuit.getRandom().nextInt(p.labourContractMax-p.labourContractMin) ;
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
										", start: "+start.getValue()+
										", end: "+end.getValue()+
										", wage: "+wage;
							}
		
						};
						workforce.add(jobContract);
						return jobContract;
		
					}
		
					@Override
					public int getVacancies() {
						return this.vacancies;
					}
		
					@Override
					public long getWage() {
						return jobWage;
					}
				};
			}
		}

		/**
		 * Updates the wage.
		 */
		@Override
		public void updateWage() {
			if (this.wage==null) {
				this.wage = (double) p.wageInitialValue;
			}
			else {
				final float alpha1 = Circuit.getRandom().nextFloat();
				final float alpha2 = Circuit.getRandom().nextFloat();
				final double vacancyRatio = getVacancyRate()/p.normalVacancyRate;
				final double newWage;
				if (vacancyRatio<1-alpha1*alpha2) {
					newWage=this.wage*(1f-alpha1*p.wageFlexDown);
				}
				else if (vacancyRatio>1+alpha1*alpha2) {
					newWage=this.wage*( 1f+alpha1*p.wageFlexUp);
				}
				else {
					newWage=this.wage;
				}
				this.wage = Math.max(newWage, p.wageMinimum);
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
		this.p.updateCapacityUtilizationTarget();
		this.account = this.sector.getNewAccount(this);
		this.factory = new BasicFactory(p.productionTime,p.productionCapacity, p.productivity);
		//this.productionBehavior = new ProductionBehavior(p.utilizationRateInitialValue);
	}

	/**
	 * Creates a new commodity supply.
	 * @return a commodity supply.
	 */
	private Supply createSupply() {
		final Supply supply;
		final Period validPeriod = Circuit.getCurrentPeriod();
		final long initialSize = Math.min((long) (p.propensity2Sell*factory.getFinishedGoodsVolume()), (long) (p.sellingCapacity*factory.getMaxUtilAverageProduction()));
		if (initialSize==0) {
			supply = null;
		}
		else {
			if (pricingManager.getPrice()==null) {
				pricingManager.setUnitCostPrice();
			}
			final double price = pricingManager.getPrice();
			this.salesRatio=0.;
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
					salesRatio = ((double) this.salesVolume)/initialSize;
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
	 * Returns the inventory ratio.<br>
	 * If inventoryRatio > 1 : the volume of finished goods exceeds the normal volume,<br>
	 * If inventoryRatio = 1 : the volume of finished goods meets the normal volume,<br>
	 * If inventoryRatio < 1 : the volume of finished goods is under the normal volume.
	 * @return the inventory ratio.
	 */
	private double getInventoryRatio() {
		return this.factory.getFinishedGoodsVolume()/(p.inventoryNormalLevel*this.factory.getMaxUtilAverageProduction());
	}

	/**
	 * Returns the data of the period.
	 * @return the data.
	 */
	@SuppressWarnings("serial") private AgentDataset newData() {
		return new BasicAgentDataset(this.name) {
			{
				this.put("firms", 1.);
				this.put("prices", pricingManager.getPrice());
				this.put("wages", workforceManager.getWage());
				this.put("workforce", (double) factory.getWorkforce());
				this.put("inventories.fg.vol", (double) factory.getFinishedGoodsVolume());
				this.put("inventories.total.val", (double) factory.getValue());
				this.put("inventories.fg.val", (double) factory.getFinishedGoodsValue());
				this.put("inventories.fg.vol.normal", (double) (p.inventoryNormalLevel*factory.getMaxUtilAverageProduction()));						
				this.put("production.vol", (double) factory.getProductionVolume());
				this.put("production.val", (double) factory.getProductionValue());
				this.put("wageBill", (double) wageBill);
				this.put("dividends", (double) capitalManager.getDividend());
				if (supply!=null) {
					this.put("supply.vol",(double) supply.getInitialVolume());
					this.put("supply.val",(double) supply.getPrice(supply.getInitialVolume()));
					this.put("sales.vol", (double) supply.getSalesVolume());
					this.put("sales.val", (double) supply.getSalesValue());
					this.put("sales.costValue", (double) supply.getSalesValueAtCost());
					this.put("grossProfit", (double) supply.getGrossProfit()+factory.getInventoryLosses());
					this.put("income", (double) wageBill+supply.getGrossProfit());
				} 
				else {
					this.put("supply.vol", 0.);
					this.put("supply.val", 0.);
					this.put("sales.vol", 0.);
					this.put("sales.val", 0.);
					this.put("sales.costValue", 0.);
					this.put("grossProfit", (double) factory.getInventoryLosses());
					this.put("income", (double) wageBill);
				}
				if (bankrupted){
					this.put("bankruptcies", 1.);
				}
				else {
					this.put("bankruptcies", 0.);					
				}
				this.put("cash", (double) account.getAmount());
				this.put("assets", (double) factory.getValue() + account.getAmount());
				this.put("liabilities", (double) account.getDebt());
				this.put("capital", (double) factory.getValue() + account.getAmount() - account.getDebt());
				this.put("capacity", (double) factory.getCapacity());
				if (Circuit.getCurrentPeriod().getValue()-creation > 12 // 12 should be a parameter 
						&& factory.getValue() + account.getAmount() < account.getDebt()){
					this.put("insolvents", 1.);
				}
				else {
					this.put("insolvents", 0.);					
				}

			}};
	}

	/**
	 * Prints the history of the firm.
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
	public void bankrupt() {
		this.bankrupted = true;
		this.owner.removeAsset(this);
		this.factory.bankrupt();
	}

	@Override
	public void close() {
		this.workforceManager.close();
		if (supply==null) {
			this.incomeSeries.add((double) wageBill);
		}
		else {
			this.incomeSeries.add((double) wageBill+supply.getGrossProfit());
		}

		this.data=newData();

		// ***

		this.supply=null;
	}

	@Override
	public long getAssets() {
		return factory.getValue() + account.getAmount();
	}

	@Override
	public long getCapital() {
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
	public boolean isBankrupted() {
		return this.bankrupted;
	}

	@Override
	public void open() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.data=null;
		this.history.add("");
		this.history.add("Period: "+Circuit.getCurrentPeriod().getValue());
	}

	@Override
	public void payDividend() {
		if (this.owner==null){
			this.owner=this.sector.selectCapitalOwner();
			this.owner.addAsset(this);
			this.history.add("New capital owner: "+this.owner.getName());
		}
		this.history.add("cash: "+ account.getAmount());
		this.history.add("assets: "+ (factory.getValue() + account.getAmount()));
		this.history.add("liabilities: "+ account.getDebt());
		this.history.add("capital: "+ (factory.getValue() + account.getAmount() - account.getDebt()));
		capitalManager.updateDividend();
		final long dividend = capitalManager.getDividend();
		if (dividend>0) {
			this.owner.receiveDividend( this.account.newCheque(dividend), this) ;
			this.history.add("dividend: "+ dividend);
			this.history.add("cash: "+ account.getAmount());
			this.history.add("assets: "+ (factory.getValue() + account.getAmount()));
			this.history.add("liabilities: "+ account.getDebt());
			this.history.add("capital: "+ (factory.getValue() + account.getAmount() - account.getDebt()));
		}
	}

	@Override
	public void prepareProduction() {

		// *** Updates the price.

		this.pricingManager.updatePrice();

		// *** Updates the wage.

		this.workforceManager.updateWage();

		// *** Updates the targeted production level.

		this.productionManager.updateCapacityUtilizationTarget();

		// *** Updates the workforce and computes the payroll.

		this.workforce.cleanUp();
		final int manpowerTarget = Math.round(this.factory.getCapacity()*this.productionManager.getTarget());
		final long payroll;
		final int newJobs;
		if (manpowerTarget<=workforce.size()) {
			newJobs = manpowerTarget-workforce.size();
			payroll = workforce.getPayroll();
			if (manpowerTarget<workforce.size()) {
				workforce.layoff(workforce.size()-manpowerTarget);
			}
		}
		else {
			newJobs = manpowerTarget-workforce.size();
			payroll = workforce.getPayroll() + newJobs*(long) (this.workforceManager.getWage());
		}

		// *** Secures financing.

		if ( payroll>this.account.getAmount() ) {
			account.lend(payroll-this.account.getAmount()) ;
		}
		if (account.getAmount() < payroll) {
			throw new RuntimeException("Production is not financed.") ;
		}

		this.workforceManager.updateJobOffer(newJobs);

	}

	@Override
	public void production() {
		this.wageBill=0;
		for (JobContract contract: this.workforce) {
			contract.payWage(this.account.newCheque(contract.getWage()));
			wageBill+=contract.getWage();
		}
		factory.process(this.workforce.getLaborPowers()) ;
		this.supply = createSupply();
	}

	@Override
	public void updateParameters() {
		this.p.updateCapacityUtilizationTarget();
		this.factory.setProductivity(p.productivity);
	}

}

// ***
