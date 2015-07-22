package jamel.jamel.firms;

import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Period;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.firms.managers.PricingManager;
import jamel.jamel.firms.managers.ProductionManager;
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.firms.util.BasicFactory;
import jamel.jamel.firms.util.Factory;
import jamel.jamel.firms.util.Workforce;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.roles.Supplier;
import jamel.jamel.roles.Worker;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;
import jamel.jamel.widgets.Supply;

/**
 * A basic firm.
 */
public class BasicFirm extends AbstractFirm {

	/**
	 * Creates a new firm.
	 * @param name the name.
	 * @param sector the sector.
	 */
	public BasicFirm(String name,IndustrialSector sector) {
		super(name,sector);
	}

	/**
	 * Creates and returns a new commodity supply.
	 * @return a new {@linkplain Supply}.
	 */
	@Override
	protected Supply createSupply() {
		final Supply supply;
		final Period validPeriod = timer.getPeriod();
		final long initialSize = Math.min((long) (sector.getParam(PROPENSITY2SELL)*factory.getFinishedGoodsVolume()), (long) (sector.getParam(SELLING_CAPACITY)*factory.getMaxUtilAverageProduction()));

		if (pricingManager.getPrice()==null) {
			pricingManager.updatePrice();
		}
		final Double price = pricingManager.getPrice();

		final long initialValue;

		if (initialSize>0) {
			initialValue=(long) (price*initialSize);
		}
		else {
			initialValue=0;
		}

		supply = new Supply() {

			private AgentDataset dataset = new BasicAgentDataset("Supply");

			private long grossProfit = 0;

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
				this.grossProfit = this.salesValue-this.salesValueAtCost;
				return sales;
			}

			@Override
			public void close() {
				this.dataset.put("supply.vol", (double) initialSize);
				this.dataset.put("supply.val", (double) initialValue);
				this.dataset.put("sales.vol", (double) salesVolume);
				this.dataset.put("sales.val", (double) salesValue);
				this.dataset.put("sales.costValue", (double) salesValueAtCost);
				this.dataset.put("grossProfit", (double) this.grossProfit);		
			}

			@Override
			public AgentDataset getData() {
				return this.dataset;
			}

			@Override
			public double getGrossProfit() {
				return this.grossProfit;
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
				// TODO: a revoir. Est-ce ici qu'il faut calculer a ?
				final double result;
				if (initialSize>0) {
					result = ((double) this.salesVolume)/initialSize; 
				}
				else {
					result=0;
				}
				return result;
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
		return supply;
	}


	/**
	 * Returns the age of the firm (= the number of periode since its creation).
	 * @return the age of the firm.
	 */
	@Override
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
	@Override
	protected double getInventoryRatio() {
		return this.factory.getFinishedGoodsVolume()/(sector.getParam(INVENTORY_NORMAL_LEVEL)*this.factory.getMaxUtilAverageProduction());
	}

	/**
	 * Creates and returns a new capital manager.
	 * @return a new {@linkplain CapitalManager}.
	 */
	@Override
	protected CapitalManager getNewCapitalManager() {
		return 	new CapitalManager() {

			private AgentDataset dataset;

			/** The dividend paid. */
			private long dividend;

			/** The capital of the firm at the beginning of the period.*/
			private long initialCapital;

			/** The owner. */
			private Shareholder owner;

			/**
			 * Returns the amount of debt exceeding the firm target. 
			 * @return the amount of debt exceeding the firm target.
			 */
			private double getLiabilitiesExcess() {
				final double result;
				final double excess = account.getDebt()-getLiabilitiesTarget();
				result = Math.max(0, excess);
				return result;
			}

			/**
			 * Returns the target value of the liabilities.
			 * @return the target value of the liabilities.
			 */
			private double getLiabilitiesTarget() {
				final long assets = account.getAmount()+factory.getValue();
				final long capitalTarget = (long) ((assets)*sector.getParam(CAPITAL_TARGET));
				return assets-capitalTarget;
			}

			@Override
			public void bankrupt() {
				owner.removeAsset(BasicFirm.this);				
			}

			@Override
			public void close() {

				isConsistent();

				final long cash = account.getAmount();
				final long factoryValue = factory.getValue();
				final long assets = factoryValue+cash;
				final long liabilities = account.getDebt();
				final long capital = assets-liabilities;
				final boolean insolvent = (timer.getPeriod().intValue()-creation > 12 && capital<0);  // TODO: 12 should be a parameter

				this.dataset.put("cash", (double) cash);
				this.dataset.put("assets", (double) assets);
				this.dataset.put("liabilities", (double) liabilities);
				this.dataset.put("capital", (double) capital);

				this.dataset.put("dividends", (double) this.dividend);
				this.dataset.put("interest", (double) account.getInterest());

				this.dataset.put("liabilities.target", getLiabilitiesTarget());
				this.dataset.put("liabilities.excess", getLiabilitiesExcess());

				this.dataset.put("canceledDebts", account.getCanceledDebt());
				this.dataset.put("canceledDeposits", account.getCanceledMoney());

				if (insolvent){
					this.dataset.put("insolvents", 1.);
				}
				else {
					this.dataset.put("insolvents", 0.);					
				}
			}

			@Override
			public long getCapital() {
				return factory.getValue() + account.getAmount() - account.getDebt();
			}

			@Override
			public AgentDataset getData() {
				return this.dataset;
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
					throw new RuntimeException("Inconsistency");
				}
				return isConsistent;
			}

			@Override
			public boolean isSolvent() {
				return (this.getCapital()>=0);
			}

			@Override
			public long newDividend() {
				final long dividend;
				final long cash = account.getAmount();
				final long assets = cash+factory.getValue();
				final long capital = getCapital();
				final long capitalTarget = (long) ((assets)*sector.getParam(CAPITAL_TARGET));
				if (capital<=0) {
					dividend=0l;
				}
				else {
					if (capital<=capitalTarget) {
						dividend=0l;
					}
					else {
						dividend = Math.min((long) ((capital-capitalTarget)*sector.getParam(CAPITAL_PROPENSITY2DISTRIBUTE)),cash);
					}
				}
				return dividend;
			}

			@Override
			public void open() {		
				this.dataset = new BasicAgentDataset("Capital Manager");
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
				BasicFirm.this.data.put("debt2target.ratio", (account.getDebt())/getLiabilitiesTarget());
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
	@Override
	protected AgentDataset getNewDataset() {
		return new BasicAgentDataset(name);
	}

	/**
	 * Creates and returns a new factory.
	 * @return a new factory.
	 */
	@Override
	protected Factory getNewFactory() {
		return new BasicFactory((int) sector.getParam(PRODUCTION_TIME), (int) sector.getParam(PRODUCTION_CAPACITY), sector.getParam(PRODUCTIVITY), timer);
	}

	/**
	 * Creates and returns a new pricing manager.
	 * @return a new pricing manager.
	 */
	@Override
	protected PricingManager getNewPricingManager() {
		return new PricingManager() {

			private AgentDataset dataset;

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

			/**
			 * Sets the price equal to the unit cost.
			 */
			private void setUnitCostPrice() {
				final double unitCost = factory.getUnitCost();
				if (!Double.isNaN(unitCost)) {
					final float priceFlexibility = sector.getParam(PRICE_FLEXIBILITY);
					this.price = unitCost;
					this.highPrice = (1f+priceFlexibility)*this.price;
					this.lowPrice = (1f-priceFlexibility)*this.price;
				}
			}

			@Override
			public void close() {
				this.salesRatio  = supply.getSalesRatio();
				// TODO: revoir la relation entre pricing manager et supply.
			}

			@Override
			public AgentDataset getData() {
				return this.dataset;
			}

			@Override
			public Double getPrice() {
				return this.price;
			}

			@Override
			public void open() {
				this.dataset = new BasicAgentDataset("Pricing Manager");
			}

			@Override
			public void updatePrice() {
				final double inventoryRatio = getInventoryRatio();
				if (this.price==null) {
					this.setUnitCostPrice();
				}
				if (this.price!=null && salesRatio!=null) {
					final float priceFlexibility = sector.getParam(PRICE_FLEXIBILITY);
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
				this.dataset.put("prices", this.price);
			}


		};
	}

	/**
	 * Creates and returns a new {@linkplain ProductionManager}. 
	 * @return a new {@linkplain ProductionManager}.
	 */
	@Override
	protected ProductionManager getNewProductionManager() {
		return new ProductionManager() {

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
					this.utilizationRateTargeted=sector.getParam(UTILIZATION_RATE_INITIAL_VALUE);
				}
				return this.utilizationRateTargeted;
			}

			@Override
			public void updateCapacityUtilizationTarget() {
				if (this.utilizationRateTargeted==null) {
					this.utilizationRateTargeted=sector.getParam(UTILIZATION_RATE_INITIAL_VALUE);
				}
				else {
					final double inventoryRatio = getInventoryRatio();
					final float alpha1 = random.nextFloat();
					final float alpha2 = random.nextFloat();
					final float delta = (alpha1*sector.getParam(UTILIZATION_RATE_FLEXIBILITY));
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
	}

	/**
	 * Returns a new {@link WorkforceManager}.
	 * @return a new {@link WorkforceManager}.
	 */
	@Override
	protected WorkforceManager getNewWorkforceManager() {
		return new WorkforceManager(){

			private AgentDataset dataset = null; 

			/** jobOffer */
			private JobOffer jobOffer = null;

			/** The manpower target. */
			private Integer manpowerTarget = null;

			/** The payroll (= the anticipated wage bill) */
			private Long payroll = null;

			private Integer vacancies = null;

			private Integer vacancies_initial = null;

			/** The wage. */
			private Double wage = null;

			/** The workforce. */
			private final Workforce workforce = new Workforce();

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
									final float min = sector.getParam(LABOUR_CONTRACT_MIN);
									final float max = sector.getParam(LABOUR_CONTRACT_MAX);
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
				this.dataset.put("vacancies.final",(double) this.vacancies);
			}

			@Override
			public AgentDataset getData() {
				return this.dataset;
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
			public void layoff() {
				workforce.layoff();
			}

			@Override
			public void open() {
				this.jobOffer=null;
				this.manpowerTarget=null;
				this.payroll=null;
				this.vacancies=null;
				this.vacancies_initial=null;
				this.dataset = new BasicAgentDataset("Workforce Manager");
			}

			@Override
			public void payWorkers() {
				double wageBill=0l;
				for (JobContract contract: workforce) {
					contract.payWage(account.newCheque(contract.getWage()));
					wageBill+=contract.getWage();
				}
				this.dataset.put("wageBill", wageBill);			
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
					vacancyRatio = getVacancyRate()/sector.getParam(NORMAL_VACANCY_RATE);	
				}
				else {
					vacancyRatio = null;
				}
				history.add("Current wage: "+this.wage);
				if (this.wage==null) {
					this.wage = sector.getRandomWage();
					if (this.wage==null) {
						this.wage = (double) sector.getParam(WAGE_INITIAL_VALUE);
						history.add("Update wage: using default value.");
					}
				}
				else {
					final float alpha1 = random.nextFloat();
					final float alpha2 = random.nextFloat();
					final double newWage;
					if (vacancyRatio<1-alpha1*alpha2) {
						newWage=this.wage*(1f-alpha1*sector.getParam(WAGE_FLEX_DOWN));
					}
					else if (vacancyRatio>1+alpha1*alpha2) {
						newWage=this.wage*( 1f+alpha1*sector.getParam(WAGE_FLEX_UP));
					}
					else {
						newWage=this.wage;
					}
					this.wage = Math.max(newWage, sector.getParam(WAGE_MINIMUM));
				}
				history.add("New wage: "+this.wage);
				this.dataset.put("vacancies.rate",vacancyRate);
				this.dataset.put("wages", wage);
			}

			@Override
			public void updateWorkforce() {
				workforce.cleanUp();
				manpowerTarget  = Math.round(factory.getCapacity()*productionManager.getTarget());
				if (manpowerTarget<=workforce.size()) {
					vacancies_initial = manpowerTarget-workforce.size();
					if (manpowerTarget<workforce.size()) {
						workforce.layoff(workforce.size()-manpowerTarget);
					}
					payroll = workforce.getPayroll();
					vacancies_initial = manpowerTarget-workforce.size();
					if (vacancies_initial!=0) {
						throw new RuntimeException("Inconsistency");
					}
				}
				else {
					vacancies_initial = manpowerTarget-workforce.size();
					payroll = workforce.getPayroll() + vacancies_initial* (long) ((double) this.wage);
				}
				this.dataset.put("vacancies.initial",(double) vacancies_initial);
				this.dataset.put("workforce.target",(double) manpowerTarget);
				this.vacancies=this.vacancies_initial;
				this.newJobOffer();
			}

		};
	}

}

// ***