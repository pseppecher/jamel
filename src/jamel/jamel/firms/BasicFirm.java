package jamel.jamel.firms;

import java.util.List;

import jamel.basic.util.Timer;
import jamel.jamel.capital.BasicCapitalStock;
import jamel.jamel.capital.CapitalStock;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.factory.BasicFactory;
import jamel.jamel.firms.factory.Factory;
import jamel.jamel.firms.managers.CapitalManager;
import jamel.jamel.firms.managers.PricingManager;
import jamel.jamel.firms.managers.ProductionManager;
import jamel.jamel.firms.managers.SalesManager;
import jamel.jamel.firms.managers.WorkforceManager;
import jamel.jamel.firms.util.Workforce;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.roles.Supplier;
import jamel.jamel.roles.Worker;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;
import jamel.jamel.widgets.Supply;

/**
 * A basic firm.
 * <p>
 * The ownership of the firm is shared by several shareholders.
 */
public class BasicFirm extends AbstractFirm {

	@SuppressWarnings("javadoc")
	protected static final String JOB_OPENINGS = "jobOpenings";

	@SuppressWarnings("javadoc")
	protected static final String JOB_VACANCIES = "jobVacancies";

	@SuppressWarnings("javadoc")
	protected static final String VACANCIES_RATE = "jobVacancyRate";

	@SuppressWarnings("javadoc")
	protected static final String WAGE_BILL = "wageBill";

	@SuppressWarnings("javadoc")
	protected static final String WAGES = "wages";

	@SuppressWarnings("javadoc")
	protected static final String WORKFORCE_TARGET = "workforceTarget";

	/**
	 * Creates and returns a new {@link JobContract}.
	 * 
	 * @param worker
	 *            the worker.
	 * @param wage
	 *            the wage.
	 * @param term
	 *            the term.
	 * @param timer
	 *            the timer.
	 * @return a new {@link JobContract}.
	 */
	private static JobContract newJobContract(final Worker worker, final long wage, final int term, final Timer timer) {
		final JobContract jobContract = new JobContract() {

			private int end = timer.getPeriod().intValue() + term;

			@Override
			public void breach() {
				end = timer.getPeriod().intValue();
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
				return wage;
			}

			@Override
			public boolean isValid() {
				return this.end > timer.getPeriod().intValue();
			}

			@Override
			public void payWage(Cheque paycheck) {
				if (!isValid()) {
					throw new RuntimeException("Invalid job contract.");
				}
				worker.earnWage(paycheck);
			}

		};
		return jobContract;
	}

	/**
	 * Creates a new firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public BasicFirm(String name, IndustrialSector sector) {
		super(name, sector);
	}

	/**
	 * Creates and returns a new capital manager.
	 * 
	 * @return a new {@linkplain CapitalManager}.
	 */
	@Override
	protected CapitalManager getNewCapitalManager() {

		final CapitalManager newCapitalManager = new CapitalManager("CapitalManager", timer) {

			private CapitalStock capitalStock = null;

			/** The dividend paid. */
			private long dividend;

			/** The capital of the firm at the beginning of the period. */
			private long initialCapital;

			/**
			 * A flag that indicates whether the ownership of the firm is
			 * distributed or not.
			 */
			private boolean ownership = false;

			private long getCapital() {
				return factory.getValue() + account.getAmount() - account.getDebt();
			}

			/**
			 * Returns the amount of debt exceeding the firm target.
			 * 
			 * @return the amount of debt exceeding the firm target.
			 */
			private double getLiabilitiesExcess() {
				final double result;
				final double excess = account.getDebt() - getLiabilitiesTarget();
				result = Math.max(0, excess);
				return result;
			}

			/**
			 * Returns the target value of the liabilities.
			 * 
			 * @return the target value of the liabilities.
			 */
			private double getLiabilitiesTarget() {
				final long assets = account.getAmount() + factory.getValue();
				final long capitalTarget = (long) ((assets) * sector.getParam(CAPITAL_TARGET));
				return assets - capitalTarget;
			}

			/**
			 * Determines and returns the amount that will be paid as dividend for the
			 * current period.
			 * 
			 * @return the amount of the dividend for the current period.
			 */
			private long newDividend() {
				checkConsistency();
				final long newDividend;
				final long cash = account.getAmount();
				final long assets = cash + factory.getValue();
				final long capital = getCapital();
				final long capitalTarget = (long) ((assets) * sector.getParam(CAPITAL_TARGET));
				if (capital <= 0) {
					newDividend = 0l;
				} else {
					if (capital <= capitalTarget) {
						newDividend = 0l;
					} else {
						newDividend = Math.min(
								(long) ((capital - capitalTarget) * sector.getParam(CAPITAL_PROPENSITY2DISTRIBUTE)),
								cash);
					}
				}
				return newDividend;
			}

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if (key.equals("capital")) {
					result = this.getCapital();
				} else if (key.equals("assets")) {
					result = factory.getValue() + account.getAmount();
				} else if (key.equals("liabilities")) {
					result = account.getDebt();
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void bankrupt() {
				checkConsistency();
				capitalStock.cancel();
			}

			@Override
			public void clearOwnership() {
				checkConsistency();
				final boolean isOpen = capitalStock.isOpen();
				this.capitalStock.cancel();
				this.capitalStock = new BasicCapitalStock(BasicFirm.this, account, timer);
				if (isOpen) {
					this.capitalStock.open();
				}
			}

			@Override
			public void close() {
				checkConsistency();

				this.capitalStock.close();

				isConsistent();

				final long cash = account.getAmount();
				final long factoryValue = factory.getValue();
				final long assets = factoryValue + cash;
				final long liabilities = account.getDebt();
				final long capital = assets - liabilities;
				final boolean insolvent = (timer.getPeriod().intValue() - creation > 12 && capital < 0);
				// TODO: 12 should be a parameter

				this.dataset.put("cash", (double) cash);
				this.dataset.put("assets", (double) assets);
				this.dataset.put("liabilities", (double) liabilities);
				this.dataset.put("capital", (double) capital);

				this.dataset.put("dividends", (double) this.dividend);
				this.dataset.put("interest", (double) account.getInterest());

				this.dataset.put("liabilities.target", getLiabilitiesTarget());
				this.dataset.put("liabilities.excess", getLiabilitiesExcess());

				this.dataset.put("liabilities.new", account.getNewDebt());
				this.dataset.put("liabilities.repayment", account.getRepaidDebt());

				this.dataset.put("canceledDebts", (double) account.getCanceledDebt());
				this.dataset.put("canceledDeposits", (double) account.getCanceledMoney());
				
				// TODO: alimenter les statistiques avec quelques ratios financiers.

				//final long netProfit = capital-initialCapital+dividend;
				//final float returnOnEquity = netProfit/capital;
				//this.dataset.put("returnOnEquity", (double) 1);
				
				if (insolvent) {
					this.dataset.put("insolvents", 1.);
				} else {
					this.dataset.put("insolvents", 0.);
				}
			}

			@Override
			public StockCertificate getNewShares(Integer nShares) {
				checkConsistency();
				return this.capitalStock.issueNewShares(nShares);
			}

			@Override
			public boolean isConsistent() {
				checkConsistency();
				final boolean isConsistent;
				final long grossProfit = (Long) salesManager.askFor("grossProfit");
				final long interest = account.getInterest();
				final long bankruptcy = account.getCanceledMoney()
						+ factory.getInventoryLosses() - account.getCanceledDebt();
				final long capital = this.getCapital();
				isConsistent = (capital == this.initialCapital + grossProfit
						- (this.capitalStock.getDistributedDividends() + interest + bankruptcy));
				if (!isConsistent) {
					System.out.println("capital = " + capital);
					System.out.println("expected = "
							+ (this.initialCapital + grossProfit - (this.dividend + interest + bankruptcy)));
					throw new RuntimeException("Inconsistency");
				}
				return isConsistent;
			}

			@Override
			public boolean isSolvent() {
				checkConsistency();
				return (this.getCapital() >= 0);
			}

			@Override
			public void open() {
				super.open();
				this.initialCapital = this.getCapital();
				this.dividend = 0;
				this.capitalStock.open();
			}

			@Override
			public void payDividend() {
				checkConsistency();
				isConsistent();
				dividend = newDividend();
				capitalStock.setDividend(dividend);
				this.dataset.put("debt2target.ratio", (account.getDebt()) / getLiabilitiesTarget());
				isConsistent();
			}

			@Override
			public void secureFinancing(long amount) {
				checkConsistency();
				if (amount > account.getAmount()) {
					account.newShortTermLoan(amount - account.getAmount());
				}
				if (account.getAmount() < amount) {
					throw new RuntimeException("Production is not financed.");
				}
			}

			@Override
			public void updateOwnership() {
				// checkChronologicalConsistency();
				if (ownership) {
					throw new RuntimeException("The ownership of this firm is already fixed.");
				}
				final List<Shareholder> shareHolders = sector.selectCapitalOwner(10);
				if (shareHolders.size() > 0) {
					this.capitalStock = new BasicCapitalStock(BasicFirm.this, shareHolders.size(), account, timer);
					List<StockCertificate> certificates = this.capitalStock.getCertificates();
					for (int id = 0; id < certificates.size(); id++) {
						final StockCertificate certif = certificates.get(id);
						final Shareholder shareHolder = shareHolders.get(id);
						shareHolder.addAsset(certif);
					}
					ownership = true;
				} else {
					throw new RuntimeException("No shareholder.");
				}
			}
		};
		newCapitalManager.updateOwnership();
		return newCapitalManager;
	}

	/**
	 * Creates and returns a new factory.
	 * 
	 * @return a new factory.
	 */
	@Override
	protected Factory getNewFactory() {
		return new BasicFactory((int) sector.getParam(PRODUCTION_TIME), (int) sector.getParam(PRODUCTION_CAPACITY),
				(long) sector.getParam(PRODUCTIVITY), timer, random);
	}

	/**
	 * Creates and returns a new pricing manager.
	 * 
	 * @return a new pricing manager.
	 */
	@Override
	protected PricingManager getNewPricingManager() {
		return new PricingManager("PricingManager", timer) {

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
			 * 
			 * @param lowerBound
			 *            the lower price
			 * @param upperBound
			 *            the higher price
			 * @return the new price.
			 */
			private double getNewPrice(Double lowerBound, Double upperBound) {
				if (lowerBound > upperBound) {
					throw new IllegalArgumentException("lowPrice > highPrice.");
				}
				return lowerBound + random.nextFloat() * (upperBound - lowerBound);
			}

			/**
			 * Sets the price equal to the unit cost.
			 */
			private void setUnitCostPrice() {
				final double unitCost = factory.getUnitCost();
				if (!Double.isNaN(unitCost)) {
					final float priceFlexibility = sector.getParam(PRICE_FLEXIBILITY);
					this.price = unitCost;
					this.highPrice = (1f + priceFlexibility) * this.price;
					this.lowPrice = (1f - priceFlexibility) * this.price;
				}
			}

			@Override
			public void close() {
				checkConsistency();
				final long supplyVolume = (Long) salesManager.askFor("supplyVolume");
				final double salesVolume = (Long) salesManager.askFor("salesVolume");
				if (supplyVolume > 0) {
					this.salesRatio = salesVolume / supplyVolume;
				} else {
					this.salesRatio = 0d;
				}
			}

			@Override
			public Double getPrice() {
				checkConsistency();
				return this.price;
			}

			@Override
			public void updatePrice() {
				checkConsistency();
				final double inventoryRatio = factory.getInventoryRatio(sector.getParam(INVENTORY_NORMAL_LEVEL));
				if (this.price == null) {
					this.setUnitCostPrice();
				}
				if (this.price != null && salesRatio != null) {
					final float priceFlexibility = sector.getParam(PRICE_FLEXIBILITY);
					if ((salesRatio == 1)) {
						this.lowPrice = this.price;
						if (inventoryRatio < 1) {
							this.price = getNewPrice(this.lowPrice, this.highPrice);
						}
						this.highPrice = this.highPrice * (1f + priceFlexibility);
					} else {
						this.highPrice = this.price;
						if (inventoryRatio > 1) {
							this.price = getNewPrice(this.lowPrice, this.highPrice);
						}
						this.lowPrice = this.lowPrice * (1f - priceFlexibility);
					}
				}
				this.dataset.put("prices", this.price);
			}

		};
	}

	/**
	 * Creates and returns a new {@linkplain ProductionManager}.
	 * 
	 * @return a new {@linkplain ProductionManager}.
	 */
	@Override
	protected ProductionManager getNewProductionManager() {
		return new ProductionManager("ProductionManager", timer) {

			/**
			 * The capacity utilization rate targeted.
			 * <p>
			 * Capacity utilization rate: "A metric used to measure the rate at
			 * which potential output levels are being met or used. Displayed as
			 * a percentage, capacity utilization levels give insight into the
			 * overall slack that is in the economy or a firm at a given point
			 * in time. If a company is running at a 70% capacity utilization
			 * rate, it has room to increase production up to a 100% utilization
			 * rate without incurring the expensive costs of building a new
			 * plant or facility. Also known as "operating rate". (<a href=
			 * "http://www.investopedia.com/terms/c/capacityutilizationrate.asp"
			 * >Investopedia</a>)
			 */
			private Float utilizationRateTargeted = null;

			@Override
			public void close() {
				// Does nothing.
			}

			@Override
			public float getTarget() {
				checkConsistency();
				if (this.utilizationRateTargeted == null) {
					this.utilizationRateTargeted = sector.getParam(UTILIZATION_RATE_INITIAL_VALUE);
				}
				return this.utilizationRateTargeted;
			}

			@Override
			public void updateCapacityUtilizationTarget() {
				checkConsistency();
				if (this.utilizationRateTargeted == null) {
					this.utilizationRateTargeted = sector.getParam(UTILIZATION_RATE_INITIAL_VALUE);
				} else {
					final double inventoryRatio = factory.getInventoryRatio(sector.getParam(INVENTORY_NORMAL_LEVEL));
					final float alpha1 = random.nextFloat();
					final float alpha2 = random.nextFloat();
					final float delta = (alpha1 * sector.getParam(UTILIZATION_RATE_FLEXIBILITY));
					if (inventoryRatio < 1 - alpha1 * alpha2) { // Low level
						this.utilizationRateTargeted += delta;
						if (this.utilizationRateTargeted > 1) {
							this.utilizationRateTargeted = 1f;
						}
					} else if (inventoryRatio > 1 + alpha1 * alpha2) { // High
						// level
						this.utilizationRateTargeted -= delta;
						if (this.utilizationRateTargeted < 0) {
							this.utilizationRateTargeted = 0f;
						}
					}
				}
			}

		};
	}

	@Override
	protected SalesManager getNewSalesManager() {
		return new SalesManager("SalesManager", timer) {

			/**
			 * The gross profit of the period.
			 * <p>
			 * "In accounting, gross profit or sales profit or 'credit sales' is
			 * the difference between revenue and the cost of making a product
			 * or providing a service, before deducting overhead, payroll,
			 * taxation, and interest payments (...) Gross profit = Net sales -
			 * Cost of goods sold"
			 * 
			 * (ref:
			 * <a href="https://en.wikipedia.org/wiki/Gross_profit">wikipedia.
			 * org</a>)
			 */
			private long grossProfit = 0;

			private long salesValue = 0;

			private long salesValueAtCost = 0;

			private long salesVolume = 0;

			private Supply supply = null;

			private long supplyValue = 0;

			private long supplyVolume = 0;

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if (key.equals("grossProfit")) {
					result = this.grossProfit;
				} else if (key.equals("supplyVolume")) {
					result = this.supplyVolume;
				} else if (key.equals("salesVolume")) {
					result = this.salesVolume;
				} else if (key.equals("salesValue")) {
					result = this.salesValue;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void close() {
				checkConsistency();
				dataset.put("supply.vol", (double) supplyVolume);
				dataset.put("supply.val", (double) supplyValue);
				dataset.put("sales.vol", (double) salesVolume);
				dataset.put("sales.val", (double) salesValue);
				dataset.put("sales.cost", (double) salesValueAtCost);
				dataset.put("grossProfit", (double) grossProfit);
			}

			@Override
			public void createSupply() {
				checkConsistency();
				final int validPeriod = timer.getPeriod().intValue();
				supplyVolume = Math.min((long) (sector.getParam(PROPENSITY2SELL) * factory.getFinishedGoodsVolume()),
						(long) (sector.getParam(SELLING_CAPACITY) * factory.getMaxUtilAverageProduction()));

				if (pricingManager.getPrice() == null) {
					pricingManager.updatePrice();// TODO: combien de fois update
					// price est-il appel� ? ne
					// peut-il �tre appel� q'une
					// fois, ici ?
				}
				final Double price = pricingManager.getPrice();

				if (supplyVolume > 0) {
					supplyValue = (long) (price * supplyVolume);
				} else {
					supplyValue = 0;
				}

				supply = new Supply() {

					private long volume = supplyVolume;

					private void anachronismDetection() {
						if (validPeriod != timer.getPeriod().intValue()) {
							throw new AnachronismException("Out of date.");
						}
					}

					@Override
					public Commodities buy(long demand, Cheque cheque) {
						anachronismDetection();
						if (demand > this.volume) {
							throw new IllegalArgumentException("Demand cannot exceed supply.");
						}
						if ((long) (this.getPrice() * demand) != cheque.getAmount()) {
							throw new IllegalArgumentException("Cheque amount : expected <"
									+ (long) (demand * this.getPrice()) + "> but was <" + cheque.getAmount() + ">");
						}
						account.deposit(cheque);
						this.volume -= demand;
						salesValue += cheque.getAmount();
						salesVolume += demand;
						final Commodities sales = factory.getCommodities(demand);
						salesValueAtCost += sales.getValue();
						grossProfit = salesValue - salesValueAtCost;
						sales.setValue(cheque.getAmount());
						return sales;
					}

					@Override
					public double getPrice() {
						anachronismDetection();
						return price;
					}

					@Override
					public long getPrice(long vol) {
						anachronismDetection();
						return (long) (price * vol);
					}

					@Override
					public Supplier getSupplier() {
						anachronismDetection();
						return BasicFirm.this;
					}

					@Override
					public long getVolume() {
						anachronismDetection();
						return this.volume;
					}

					@Override
					public String toString() {
						anachronismDetection();
						return "Supply by " + name + ": price <" + price + ">, volume <" + volume + ">";
					}

				};
			}

			@Override
			public Supply getSupply() {
				checkConsistency();
				final Supply result;
				if (this.supply.getVolume() > 0) {
					result = this.supply;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void open() {
				super.open();
				this.supply = null;
				this.grossProfit = 0;
				this.salesValue = 0;
				this.salesValueAtCost = 0;
				this.salesVolume = 0;
				this.supplyValue = 0;
				this.supplyVolume = 0;
			}

		};
	}

	/**
	 * Returns a new {@link WorkforceManager}.
	 * 
	 * @return a new {@link WorkforceManager}.
	 */
	@Override
	protected WorkforceManager getNewWorkforceManager() {
		return new WorkforceManager("WorkforceManager", timer) {

			/** The job offer. */
			private JobOffer jobOffer = null;

			/** The manpower target. */
			private Integer manpowerTarget = null;

			/** The payroll (= the anticipated wage bill) */
			private Long payroll = null;

			/** Memory of the recent job openings. */
			private final Memory<Integer> recentJobOpenings = new Memory<Integer>(4);

			/** Memory of the recent vacancies. */
			private final Memory<Integer> recentVacancies = new Memory<Integer>(4);

			/** Number of job vacancies in the current period. */
			private Integer vacancies = null;

			/** The wage offered. */
			private Double wage = null;

			/**
			 * The wagebill.
			 */
			private Long wagebill;

			/** The workforce. */
			private final Workforce workforce = new Workforce();

			/**
			 * Returns the vacancy rate.
			 * 
			 * @return the vacancy rate.
			 */
			private Double getVacancyRate() {
				final Double result;
				final double sumVacancies = recentVacancies.getSum();
				final double sumJobs = recentJobOpenings.getSum();
				if (sumVacancies == 0) {
					result = 0d;
				} else {
					result = sumVacancies / sumJobs;
				}
				return result;
			}

			/**
			 * Creates a new job offer.
			 */
			private void newJobOffer() {
				if (vacancies < 0) {
					throw new RuntimeException("Negative number of vacancies");
				}
				if (vacancies == 0) {
					jobOffer = null;
				} else {
					final int validPeriod = timer.getPeriod().intValue();
					jobOffer = new JobOffer() {

						private final long jobWage = (long) Math.floor(wage);

						@Override
						public JobContract apply(final Worker worker) {
							timer.checkConsistency(validPeriod);
							if (!(vacancies > 0)) {
								throw new RuntimeException("No vacancy.");
							}
							vacancies--;

							final int term;
							final float min = sector.getParam(LABOUR_CONTRACT_MIN);
							final float max = sector.getParam(LABOUR_CONTRACT_MAX);
							if (max == min) {
								term = (int) min;
							} else {
								term = (int) (min + random.nextInt((int) (max - min)));
							}

							final JobContract jobContract = newJobContract(worker, jobWage, term, timer);
							workforce.add(jobContract);
							return jobContract;

						}

						@Override
						public Object getEmployerName() {
							timer.checkConsistency(validPeriod);
							return BasicFirm.this.getName();
						}

						@Override
						public long getWage() {
							timer.checkConsistency(validPeriod);
							return jobWage;
						}
					};
				}
			}

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if ("wagebill".equals(key)) {
					result = this.wagebill;
				} else if ("workforce".equals(key)) {
					result = this.workforce.size();
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void close() {
				checkConsistency();
				recentVacancies.add(this.vacancies);
				this.dataset.put(JOB_VACANCIES, (double) this.vacancies);
			}

			@Override
			public JobOffer getJobOffer() {
				checkConsistency();
				final JobOffer result;
				if (this.vacancies > 0) {
					result = this.jobOffer;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public LaborPower[] getLaborPowers() {
				checkConsistency();
				return this.workforce.getLaborPowers();
			}

			@Override
			public long getPayroll() {
				checkConsistency();
				return this.payroll;
			}

			@Override
			public void layoff() {
				checkConsistency();
				workforce.layoff();
			}

			@Override
			public void open() {
				super.open();
				this.jobOffer = null;
				this.manpowerTarget = null;
				this.payroll = null;
				this.vacancies = null;
				this.wagebill = null;
			}

			@Override
			public void payWorkers() {
				checkConsistency();
				this.wagebill = 0l;
				for (JobContract contract : workforce) {
					contract.payWage(account.newCheque(contract.getWage()));
					this.wagebill += contract.getWage();
				}
				this.dataset.put(WAGE_BILL, this.wagebill.doubleValue());
			}

			/*
			 * TODO 14-02-15 Revoir la procedure d'ajustement des salaires de
			 * BasicFirm. L'ajustement est trop lent en cas de forte penurie de
			 * main d'oeuvre. Proposer une methode avec elargissement croissant
			 * de la zone de recherche (procedure explosive ?). Comparer le
			 * nombre d'emplois vacants avec le nombre total de postes de
			 * l'entreprise (et non pas seulement avec le nombre de postes
			 * offerts).
			 */
			@Override
			public void updateWage() {
				checkConsistency();
				final Double vacancyRate = getVacancyRate();
				final Double vacancyRatio;
				if (vacancyRate != null) {
					vacancyRatio = getVacancyRate() / sector.getParam(NORMAL_VACANCY_RATE);
				} else {
					vacancyRatio = null;
				}
				if (this.wage == null) {
					this.wage = sector.getRandomWage();
					if (this.wage == null) {
						this.wage = (double) sector.getParam(WAGE_INITIAL_VALUE);
					}
				} else {
					final float alpha1 = random.nextFloat();
					final float alpha2 = random.nextFloat();
					final double newWage;
					if (vacancyRatio < 1 - alpha1 * alpha2) {
						newWage = this.wage * (1f - alpha1 * sector.getParam(WAGE_FLEX_DOWN));
					} else if (vacancyRatio > 1 + alpha1 * alpha2) {
						newWage = this.wage * (1f + alpha1 * sector.getParam(WAGE_FLEX_UP));
					} else {
						newWage = this.wage;
					}
					this.wage = Math.max(newWage, sector.getParam(WAGE_MINIMUM));
				}
				this.dataset.put(VACANCIES_RATE, vacancyRate);
				this.dataset.put(WAGES, wage);
			}

			@Override
			public void updateWorkforce() {
				checkConsistency();
				workforce.cleanUp();
				manpowerTarget = Math.round(factory.getCapacity() * productionManager.getTarget());
				final int jobOpenings;
				if (manpowerTarget <= workforce.size()) {
					if (manpowerTarget < workforce.size()) {
						workforce.layoff(workforce.size() - manpowerTarget);
					}
					payroll = workforce.getPayroll();
					jobOpenings = manpowerTarget - workforce.size();
					if (jobOpenings != 0) {
						throw new RuntimeException("Inconsistency");
					}
				} else {
					jobOpenings = manpowerTarget - workforce.size();
					payroll = workforce.getPayroll() + jobOpenings * (long) ((double) this.wage);
				}
				this.dataset.put(JOB_OPENINGS, (double) jobOpenings);
				this.recentJobOpenings.add(jobOpenings);
				this.dataset.put(WORKFORCE_TARGET, (double) manpowerTarget);
				this.vacancies = jobOpenings;
				this.newJobOffer();
			}

		};
	}

}

// ***