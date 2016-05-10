package jamel.jamel.firms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jamel.Jamel;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.capital.BasicCapitalStock;
import jamel.jamel.capital.CapitalStock;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.firms.factory.BasicFactory;
import jamel.jamel.firms.factory.Factory;
import jamel.jamel.firms.factory.BasicMachine;
import jamel.jamel.firms.factory.FinishedGoods;
import jamel.jamel.firms.factory.Machine;
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
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;
import jamel.jamel.widgets.Supply;

/**
 * An abstract firm.
 */
public class BasicFirm implements Firm {

	/**
	 * The supply comparator.
	 * <p>
	 * To compare supplies according to their price.
	 */
	private static final Comparator<Supply> supplyComparator = new Comparator<Supply>() {
		@Override
		public int compare(Supply offer1, Supply offer2) {
			final int result;
			if (offer1 == null && offer2 == null) {
				result = 0;
			} else if (offer1 == null) {
				result = 1;
			} else if (offer2 == null) {
				result = -1;
			} else {
				result = (-(new Double(offer2.getPrice())).compareTo(offer1.getPrice()));
			}
			return result;
		}
	};

	/**
	 * Creates and returns the specified number of new machines.
	 * 
	 * @param n
	 *            the number of machines to be created.
	 * @param stuff
	 *            a heap of commodities that will serve as input for the
	 *            creation of the new machines.
	 * @param technology
	 *            the technology that will be used to create the new machines.
	 * @param timer
	 *            the timer.
	 * @param random
	 *            the random.
	 * @return the specified number of new machines.
	 */
	private static Machine[] getNewMachines(int n, Commodities stuff, Technology technology, Timer timer,
			Random random) {
		final Machine[] machines = new Machine[n];
		for (int i = 0; i < n; i++) {
			final Commodities input;
			if (stuff == null) {
				input = null;
			} else {
				input = stuff.detach(technology.getInputVolumeForANewMachine());
			}
			machines[i] = new BasicMachine(technology, input, timer, random);
		}
		return machines;
	}

	/**
	 * Returns the optimum size of the investment.
	 * 
	 * @param machinePrices
	 *            an array that contains the prices of the new machines.
	 * @param productivity
	 *            the average productivity of the investment (= the average
	 *            productivity of one machine).
	 * @param machinery
	 *            an array that contains the productivity of each existing
	 *            machines, sorted in descending order.
	 * @param demandForecast
	 *            the volume of final goods to be produced by period.
	 * @param productPrice
	 *            the expected price of one unit of product.
	 * @param wage
	 *            the expected wage.
	 * @param forecastPeriod
	 *            the number of periods to consider in evaluating the project.
	 * @param discountRate
	 *            the interest rate used to compute the net present values.
	 * @return the expected profit.
	 */
	private static int getOptimumSize(final Long[] machinePrices, final long productivity, final long[] machinery,
			final double demandForecast, final double productPrice, final double wage, final int forecastPeriod,
			float discountRate) {

		double presentValue = getPresentValue(0, 0, productivity, machinery, demandForecast,
				productPrice, wage, discountRate, forecastPeriod);
		int investmentSize = 0;

		while (true) {
			final int targetedInvestmentSize = investmentSize + 1;
			if (targetedInvestmentSize == machinePrices.length) {
				// FIXME: il faut mesurer ce phénomène pour évaluer son
				// importance.
				// Jamel.println();
				// Jamel.println("InvestorToolBox.getOptimumSize(): Not enough
				// sellers: " + machinePrices.length);
				// Jamel.println();
				break;
			}
			final long machinePrice = machinePrices[targetedInvestmentSize];

			final double presentValue2 = getPresentValue(targetedInvestmentSize, machinePrice,
					productivity, machinery, demandForecast, productPrice, wage, discountRate, forecastPeriod);
			if (presentValue2 > presentValue) {
				presentValue = presentValue2;
				investmentSize = targetedInvestmentSize;
			} else {
				break;
			}
		}

		return investmentSize;

	}

	/**
	 * Returns the expected profit of the specified investment project.
	 * 
	 * @param machines
	 *            the size of the investment (the number of machines to buy).
	 * @param initialOutlay
	 *            the initial outlay (i.e. the price of the new machines).
	 * @param productivity
	 *            the average productivity of the investment (= the average
	 *            productivity of one machine).
	 * @param machinery
	 *            an array that contains the productivity of each existing
	 *            machines, sorted in descending order.
	 * @param demandForecast
	 *            the volume of final goods to be produced by period.
	 * @param productPrice
	 *            the expected price of one unit of product.
	 * @param wage
	 *            the expected wage.
	 * @param rate
	 *            the rate of interest.
	 * @param forecastPeriod
	 *            the number of periods to consider in evaluating the project.
	 * @return the expected profit.
	 */
	private static double getPresentValue(final int machines, final long initialOutlay, final long productivity,
			final long[] machinery, double demandForecast, final double productPrice, final double wage,
			final float rate, final int forecastPeriod) {
	
		double effectiveProduction = 0;
		double wagebill = 0;
	
		// TODO: à revoir. Fusionner les productivités des machines à créer avec
		// celles des machines existantes avant de calculer.
	
		// On suppose que les machines dont l'achat est projeté sont plus
		// productives que les anciennes.
		// Donc ce sont elles qui seront utilisées en priorité.
		for (int i = 0; i < machines; i++) {
			if (effectiveProduction + productivity <= demandForecast) {
				effectiveProduction += productivity;
				wagebill += wage;
			} else {
				wagebill += (wage * (demandForecast - effectiveProduction)) / productivity;
				effectiveProduction = demandForecast;
				break;
			}
		}
	
		// Si la production des nouvelles machines est insuffisante pour faire
		// face à la demande anticipée, on fait appel aux machines existantes,
		// en commençant par les plus productives (elles devraient être rangées
		// par productivité décroissante).
		if (effectiveProduction < demandForecast) {
			for (int i = 0; i < machinery.length; i++) {
				if (effectiveProduction + machinery[i] <= demandForecast) {
					effectiveProduction += machinery[i];
					wagebill += wage;
				} else {
					wagebill += (wage * (demandForecast - effectiveProduction)) / machinery[i];
					effectiveProduction = demandForecast;
					break;
				}
			}
		}
	
		final double sales = effectiveProduction * productPrice;
		final double cashFlow = sales - wagebill;
	
		final double presentValue;
		if (rate == 0) {
			presentValue = cashFlow*forecastPeriod - initialOutlay;
		} else {
			presentValue = (cashFlow / rate)*(1-1/Math.pow(1 + rate, forecastPeriod)) - initialOutlay;
		}	
	
		return presentValue;
	}

	/**
	 * Returns an array of that contains the prices of the new machines.
	 * <code>result[1]</code> contains the price of one machine,
	 * <code>result[2]</code> contains the price of two machines, etc.
	 * 
	 * @param supplies
	 *            the list of supplies of 'raw materials'.
	 * @param realCost
	 *            the volume of 'raw materials' required for building a new
	 *            machine.
	 * @return an array that contains the prices of the new machines.
	 */
	private static Long[] getPrices(Supply[] supplies, long realCost) {
	
		final List<Long> priceList = new ArrayList<Long>();
	
		long totalPrice = 0l;
	
		priceList.add(totalPrice);
	
		long incomplete = 0;
		for (int i = 0; i < supplies.length; i++) {
			if (supplies[i] != null) {
				long remainingVolume = supplies[i].getVolume();
				final double price = supplies[i].getPrice();
				if (incomplete != 0) {
					final long need = realCost - incomplete;
					if (need <= remainingVolume) {
						totalPrice += need * price;
						priceList.add(totalPrice);
						remainingVolume -= need;
						incomplete = 0;
					} else {
						totalPrice += remainingVolume * price;
						incomplete += remainingVolume;
						remainingVolume = 0l;
					}
				}
	
				if (remainingVolume > 0) {
					if (remainingVolume >= realCost) {
						final int n = (int) (remainingVolume / realCost);
						for (int j = 0; j < n; j++) {
							totalPrice += realCost * price;
							priceList.add(totalPrice);
							remainingVolume -= realCost;
						}
					}
	
					if (remainingVolume > 0) {
						totalPrice += remainingVolume * price;
						incomplete += remainingVolume;
						remainingVolume = 0l;
					}
	
				}
			}
	
		}
	
		final Long[] prices = priceList.toArray(new Long[priceList.size()]);
	
		return prices;
	}

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

	/** The account. */
	private final BankAccount account;

	@SuppressWarnings("javadoc")
	private Double averagePrice = null;

	@SuppressWarnings("javadoc")
	private Double averageWage = null;

	/** A flag that indicates if the firm is bankrupted. */
	private boolean bankrupted = false;

	/** The capital manager. */
	private CapitalManager capitalManager;

	/** Date of creation. */
	private final int creation;

	/** The data of the agent. */
	private AgentDataset data;

	/** A flag that indicates if the data of the firm is to be exported. */
	private boolean exportData;

	/** The factory. */
	private Factory factory;

	/**
	 * The factory value. Updated at the end of the period. To compute the net
	 * investment at the end of the period.
	 */
	private long factoryValueAtTheEndOfThePeriod = 0;

	@SuppressWarnings("javadoc")
	private int imitations = 0;

	@SuppressWarnings("javadoc")
	private final int initialCapacity;

	@SuppressWarnings("javadoc")
	private final float investmentGreediness;

	@SuppressWarnings("javadoc")
	private int lastImitiation = 0;

	@SuppressWarnings("javadoc")
	private final int longTerm;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfDebtRepayment;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfGrossProfit;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfInterest;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfSalesValue;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfSalesVolume;

	@SuppressWarnings("javadoc")
	private final Memory<Long> memoryOfWagebill;

	@SuppressWarnings("javadoc")
	private final Memory<Integer> memoryOfWorkforce;

	@SuppressWarnings("javadoc")
	private final float mutation;

	/** The name of this firm. */
	private final String name;

	/** A flag that indicates if this firm is open or not. */
	private boolean open;

	@SuppressWarnings("javadoc")
	private final float ownerEquitiesMaximumShareToBeDistributedAsDividend;

	/** The current period. */
	private Integer period = null;

	@SuppressWarnings("javadoc")
	final private float priceFlexibility;

	/** The pricing manager. */
	private PricingManager pricingManager;

	/** The production manager. */
	private ProductionManager productionManager;

	/** The random. */
	final private Random random;

	/** The marketing manager. */
	private SalesManager salesManager;

	/** The sector. */
	private final IndustrialSector sector;

	@SuppressWarnings("javadoc")
	private final Integer shortTerm;

	@SuppressWarnings("javadoc")
	private final float stability;

	@SuppressWarnings("javadoc")
	private Float targetDebtRatio = null;

	@SuppressWarnings("javadoc")
	private Technology technology = null;

	/** The timer. */
	final private Timer timer;

	/** The employer behavior. */
	private WorkforceManager workforceManager;

	/**
	 * Creates a new basic firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public BasicFirm(String name, IndustrialSector sector) {
		this.name = name;
		this.sector = sector;
		this.timer = this.sector.getTimer();
		this.creation = this.timer.getPeriod().intValue();
		this.random = this.sector.getRandom();
		this.account = this.sector.getNewAccount(this);

		// 2016-04-04: rassemblement de la lecture des paramètres dans le
		// constructeur.

		final int memory = this.sector.getParam("memory").intValue();
		this.memoryOfSalesValue = new Memory<Long>(memory);
		this.memoryOfSalesVolume = new Memory<Long>(memory);
		this.memoryOfWagebill = new Memory<Long>(memory);
		this.memoryOfWorkforce = new Memory<Integer>(memory);
		this.memoryOfGrossProfit = new Memory<Long>(6);
		this.memoryOfInterest = new Memory<Long>(6);
		this.memoryOfDebtRepayment = new Memory<Long>(6);

		this.shortTerm = this.sector.getParam("credit.term.short").intValue();
		this.longTerm = this.sector.getParam("credit.term.long").intValue();
		this.ownerEquitiesMaximumShareToBeDistributedAsDividend = this.sector
				.getParam("ownerEquities.maximumShareToBeDistributedAsDividend");
		this.investmentGreediness = sector.getParam("investment.greediness").floatValue();
		final float min = sector.getParam("debtRatio.target.initialValue.min").floatValue();
		final float max = sector.getParam("debtRatio.target.initialValue.max").floatValue();
		this.targetDebtRatio = min + (max - min) * this.random.nextFloat();
		this.mutation = sector.getParam("mutation.strenght").floatValue();
		this.stability = 1f - sector.getParam("mutation.probability").floatValue();
		this.technology = sector.getTechnology();
		this.priceFlexibility = sector.getParam("price.flexibility");
		this.initialCapacity = sector.getParam("production.capacity").intValue();

		this.setCapitalManager(getNewCapitalManager());
		this.setFactory(getNewFactory());
		this.setPricingManager(getNewPricingManager());
		this.setProductionManager(getNewBasicProductionManager());
		this.setSalesManager(getNewBasicSalesManager());
		this.setWorkforceManager(getNewWorkforceManager());

	}

	/**
	 * Closes all the managers of this firm.
	 */
	private void closeManagers() {
		this.salesManager.close();
		this.pricingManager.close();
		this.capitalManager.close();
		this.workforceManager.close();
		this.factory.close();
	}

	/**
	 * Exports agent data in a csv file.
	 * 
	 * @throws IOException
	 *             in the case of an I/O exception.
	 */
	private void exportData() throws IOException {
		if (this.exportData) {
			// TODO gerer la localisation du dossier exports, son existence
			final File outputFile = new File("exports/" + sector.getSimulationID() + "-" + this.name + ".csv");
			if (!outputFile.exists()) {
				this.data.exportHeadersTo(outputFile);
			}
			this.data.exportTo(outputFile);
		}
	}

	/**
	 * Returns the age of the firm (= the number of periode since its creation).
	 * 
	 * @return the age of the firm.
	 */
	private int getAge() {
		return timer.getPeriod().intValue() - this.creation;
	}

	/**
	 * @return the capitalManager
	 */
	private CapitalManager getCapitalManager() {
		return capitalManager;
	}

	/**
	 * @return the factory
	 */
	private Factory getFactory() {
		return factory;
	}

	/**
	 * Creates and returns a new {@linkplain ProductionManager}.
	 * 
	 * @return a new {@linkplain ProductionManager}.
	 */
	private final ProductionManager getNewBasicProductionManager() {
		return new ProductionManager("ProductionManager", timer) {

			private final float utilizationRateFlexibility = sector.getParam("utilizationRate.flexibility");

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
					this.utilizationRateTargeted = sector.getParam("utilizationRate.initialValue");
				}
				return this.utilizationRateTargeted;
			}

			@Override
			public void updateCapacityUtilizationTarget() {
				checkConsistency();
				if (this.utilizationRateTargeted == null) {
					this.utilizationRateTargeted = sector.getParam("utilizationRate.initialValue");
				} else {
					final double inventoryRatio = getFactory().getInventoryRatio();
					final float alpha1 = random.nextFloat();
					final float alpha2 = random.nextFloat();
					final float delta = (alpha1 * this.utilizationRateFlexibility);
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

	/**
	 * Creates and returns a new sales manager.
	 * 
	 * @return a new {@linkplain SalesManager}.
	 */
	private final SalesManager getNewBasicSalesManager() {
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
				dataset.put("supply.vol", supplyVolume);
				dataset.put("supply.val", supplyValue);
				dataset.put("sales.vol", salesVolume);
				dataset.put("sales.val", salesValue);
				dataset.put("sales.cost", salesValueAtCost);
				dataset.put("grossProfit", grossProfit);
			}

			@Override
			public void createSupply() {
				checkConsistency();
				final int validPeriod = timer.getPeriod().intValue();
				supplyVolume = Math.min(
						(long) (sector.getParam("inventories.propensity2sell") * getFactory().getFinishedGoodsVolume()),
						(long) (sector.getParam("sales.capacity") * getFactory().getPotentialOutput()));

				if (supplyVolume == 0 && getFactory().getFinishedGoodsVolume() != 0) {
					supplyVolume = getFactory().getFinishedGoodsVolume();
					if (getFactory().getPotentialOutput() != 0 && getFactory().getFinishedGoodsVolume() != 1) {
						throw new RuntimeException("Why is it possible?");
					}
				}

				if (getPricingManager().getPrice() == null) {
					getPricingManager().updatePrice();
					// TODO: combien de fois updatePrice() est-il appel� ?
					// ne peut-il �tre appel� q'une fois, ici ?
				}
				final Double price = getPricingManager().getPrice();

				if (price == null) {
					if (supplyVolume != 0) {
						throw new RuntimeException("price is null");
					}
				} else {
					if (price <= 0) {
						Jamel.println("price: " + price);
						throw new RuntimeException("Negative price");
					}

					if (price.isNaN()) {
						Jamel.println("Volume:" + supplyVolume);
						throw new RuntimeException("Price is not a number");
					}

				}

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
							Jamel.println("Supply volume: " + this.volume);
							Jamel.println("Demand volume: " + demand);
							throw new IllegalArgumentException("Demand cannot exceed supply.");
						}
						if ((long) (this.getPrice() * demand) != cheque.getAmount()) {
							if (cheque.getAmount() != 1 && demand != 1) {
								throw new IllegalArgumentException("Cheque amount : expected <"
										+ (long) (demand * this.getPrice()) + "> but was <" + cheque.getAmount() + ">");
							}
						}
						account.deposit(cheque);
						this.volume -= demand;
						salesValue += cheque.getAmount();
						salesVolume += demand;
						final Commodities sales = getFactory().getCommodities(demand);
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
						if (vol <= 0) {
							throw new IllegalArgumentException("Bad volume: " + vol);
						}
						return Math.max((long) (price * vol), 1);
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
	 * Creates and returns a new capital manager.
	 * 
	 * Lisse la distribution des dividendes.
	 * 
	 * @return a new {@linkplain CapitalManager}.
	 */
	private CapitalManager getNewCapitalManager() {

		final CapitalManager newCapitalManager = new CapitalManager("CapitalManager", timer) {

			private CapitalStock capitalStock = null;

			/** The dividend paid. */
			private long dividend;

			/** The capital of the firm at the beginning of the period. */
			private long initialCapital;

			/**
			 * The memory of past net profits.
			 */
			private Memory<Long> netProfitMemory = new Memory<Long>(12);

			/**
			 * A flag that indicates whether the ownership of the firm is
			 * distributed or not.
			 */
			private boolean ownership = false;

			private long getCapital() {
				return getFactory().getValue() + account.getAmount() - account.getDebt();
			}

			private long getCapitalTarget() {
				final long assets = account.getAmount() + getFactory().getValue();
				return (long) ((assets) * (1f - targetDebtRatio));
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
				final long assets = account.getAmount() + getFactory().getValue();
				return assets - getCapitalTarget();
			}

			/**
			 * Issues the specified number of new shares.
			 * 
			 * @param nShares
			 *            the number of new shares to be issued.
			 * @return a {@link StockCertificate} that encapsulates the new
			 *         shares.
			 */
			private StockCertificate getNewShares(Integer nShares) {
				checkConsistency();
				return this.capitalStock.issueNewShares(nShares);
			}

			@Override
			public Object askFor(String key) {
				checkConsistency();
				final Object result;
				if (key.equals("capital")) {
					result = this.getCapital();
				} else if (key.equals("capitalTarget")) {
					result = getCapitalTarget();
				} else if (key.equals("assets")) {
					long val = getFactory().getValue() + account.getAmount();
					if (val < 0) {
						Jamel.println("getFactory().getValue()", getFactory().getValue());
						Jamel.println("account.getAmount()", account.getAmount());
						throw new RuntimeException("Assets are negative.");
					}
					result = val;
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
				imitation();
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
				final long assets = getFactory().getValue() + cash;
				final long liabilities = account.getDebt();
				final long capital = assets - liabilities;

				this.dataset.put("cash", cash);
				this.dataset.put("assets", assets);
				this.dataset.put("liabilities", liabilities);
				this.dataset.put("capital", capital);
				this.dataset.put("factory.val", getFactory().getValue());

				this.dataset.put("debt.shortTerm", account.getShortTermDebt());
				this.dataset.put("debt.longTerm", account.getLongTermDebt());

				this.dataset.put("dividends", this.dividend);
				this.dataset.put("interest", account.getInterest());
				this.dataset.put("debt.repaid", account.getRepaidDebt());

				this.dataset.put("liabilities.target", getLiabilitiesTarget());
				this.dataset.put("liabilities.excess", getLiabilitiesExcess());

				this.dataset.put("liabilities.new", account.getNewDebt());
				this.dataset.put("liabilities.repayment", account.getRepaidDebt());

				this.dataset.put("canceledDebts", account.getCanceledDebt());
				// 2016-04-03
				// this.dataset.put("canceledDeposits",
				// account.getCanceledMoney());
				final long netProfit = capital - initialCapital + dividend;
				this.netProfitMemory.add(netProfit);
				final double returnOnEquity = this.netProfitMemory.getSum() / capital;
				final double returnOnAssets = this.netProfitMemory.getSum() / assets;
				final double solvency = this.netProfitMemory.getSum() / liabilities;
				this.dataset.put("returnOnEquity", returnOnEquity);
				this.dataset.put("returnOnAssets", returnOnAssets);
				if (Double.isFinite(solvency)) {
					this.dataset.put("solvency", solvency);
				}

				memoryOfInterest.add(account.getInterest());
				memoryOfDebtRepayment.add(account.getRepaidDebt());

			}

			@Override
			public StockCertificate[] getNewShares(List<Integer> shares) {
				this.clearOwnership();
				final StockCertificate[] newShares = new StockCertificate[shares.size()];
				for (int i = 0; i < shares.size(); i++) {
					newShares[i] = this.getNewShares(shares.get(i));
				}
				return newShares;
			}

			@Override
			public boolean isConsistent() {
				return true; // FIXME LATER
				/*
				 * checkConsistency(); final boolean isConsistent; final long
				 * grossProfit = (Long) salesManager.askFor("grossProfit");
				 * final long interest = account.getInterest(); final long
				 * bankruptcy = account.getCanceledMoney() +
				 * getFactory().getInventoryLosses() -
				 * account.getCanceledDebt(); final long capital =
				 * this.getCapital();
				 * 
				 * isConsistent = (capital == this.initialCapital + grossProfit
				 * - (this.dividend + interest + bankruptcy)); if
				 * (!isConsistent) { if
				 * (this.capitalStock.getDistributedDividends() !=
				 * this.dividend) { Jamel.println("distributed dividend = " +
				 * this.capitalStock.getDistributedDividends()); Jamel.println(
				 * "expected = " + this.dividend); // throw new
				 * RuntimeException("Inconsistency"); } Jamel.println(
				 * "capital = " + capital); Jamel.println("expected = " +
				 * (this.initialCapital + grossProfit -
				 * (this.capitalStock.getDistributedDividends() + interest +
				 * bankruptcy))); // throw new RuntimeException(
				 * "Inconsistency: " + // Firm150908.this.name); // FIXME }
				 * return isConsistent;
				 */
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
				final long cash = account.getAmount();
				final long assets = cash + getFactory().getValue();
				final double capital = getCapital();
				final long capitalTarget2 = getCapitalTarget();
				final double averageIncome = this.netProfitMemory.getMean();
				long newDividend;
				if (capital > 0) {
					final double ratio = capital / capitalTarget2;
					newDividend = (long) Math.min(averageIncome * ratio,
							capital * ownerEquitiesMaximumShareToBeDistributedAsDividend);
					// newDividend = (long) (averageIncome * ratio);
					// newDividend = (long) Math.max(capital-capitalTarget2, 0);
				} else {
					newDividend = 0;
				}
				if (newDividend > cash) {
					// account.newLoan(newDividend-cash, 4, true);
					newDividend = cash;
				}
				if (newDividend < 0) {
					newDividend = 0;
				}
				dividend = newDividend;
				capitalStock.setDividend(dividend);

				this.dataset.put("payDividend.cash", cash);
				this.dataset.put("payDividend.assets", assets);
				this.dataset.put("payDividend.capital", capital);
				this.dataset.put("payDividend.capitalTarget", capitalTarget2);
				this.dataset.put("payDividend.averageIncome", averageIncome);
				this.dataset.put("payDividend.dividend", dividend);

				this.dataset.put("debt2target.ratio", (account.getDebt()) / getLiabilitiesTarget());
			}

			@Override
			public void secureFinancing(long amount) {
				checkConsistency();
				if (amount > account.getAmount()) {
					account.newLoan(amount - account.getAmount(), shortTerm, false);
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
				// TODO : 10 should be a parameter
				if (shareHolders.size() > 0) {
					this.capitalStock = new BasicCapitalStock(BasicFirm.this, shareHolders.size(), account,
							timer);
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
	 * Creates and returns a new agent dataset.
	 * 
	 * @return a new agent dataset.
	 */
	private AgentDataset getNewDataset() {
		return new BasicAgentDataset(name);
	}

	/**
	 * Creates and returns a new getFactory().
	 * 
	 * @return a new getFactory().
	 */
	private Factory getNewFactory() {
		final float depreciationRate = sector.getParam("inventories.depreciation.rate").floatValue();
		final Machine[] machines = getNewMachines(this.initialCapacity, null, this.technology, this.timer, this.random);
		final BasicFactory result = new BasicFactory(this.technology.getProductionTime(), timer, random, machines);
		result.setInventoriesDepreciationRate(depreciationRate);
		return result;
		// TODO la Factory devrait déduire elle-même productionTime des machines
		// dont elle est équipée.
	}

	/**
	 * Creates and returns a new pricing manager.
	 * 
	 * @return a new pricing manager.
	 */
	private final PricingManager getNewPricingManager() {
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
				final Double unitCost = getFactory().getUnitCost();
				if (unitCost != null) {
					if (unitCost.isNaN()) {
						throw new RuntimeException("Unit cost is not a number");
					}
					this.price = unitCost;
					this.highPrice = (1f + priceFlexibility) * this.price;
					this.lowPrice = (1f - priceFlexibility) * this.price;
				}
			}

			@Override
			public void close() {
				checkConsistency();
				final long supplyVolume = (Long) getSalesManager().askFor("supplyVolume");
				final double salesVolume = (Long) getSalesManager().askFor("salesVolume");
				if (supplyVolume > 0) {
					this.salesRatio = salesVolume / supplyVolume;
				} else {
					this.salesRatio = null;
				}
			}

			@Override
			public Double getPrice() {
				checkConsistency();
				return this.price;
			}

			@Override
			public void setPrice(double price) {
				throw new RuntimeException("Not used");
			}

			@Override
			public void updatePrice() {
				checkConsistency();
				final StringBuilder info = new StringBuilder();
				if (this.price == null) {
					this.setUnitCostPrice();
					info.append("Price is null: using the unit cost.<br>");
				}
				final float normalSalesRatio = sector.getParam("sales.normalRate");
				if (this.price != null && salesRatio != null) {
					final double inventoryRatio = getFactory().getInventoryRatio();
					if ((salesRatio >= normalSalesRatio)) {
						info.append("Sales level: high.<br>");
						this.lowPrice = this.price;
						if (inventoryRatio < 1) {
							info.append("Inventory level: low.<br>");
							info.append("Price: raising.<br>");
							this.price = getNewPrice(this.lowPrice, this.highPrice);
						} else {
							info.append("Inventory level: high.<br>");
							info.append("Price: unchanged<br>");
						}
						this.highPrice = this.highPrice * (1f + priceFlexibility);
					} else {
						info.append("Sales level: low.<br>");
						this.highPrice = this.price;
						if (inventoryRatio > 1) {
							info.append("Inventory level: high.<br>");
							info.append("Price: lowering.<br>");
							this.price = getNewPrice(this.lowPrice, this.highPrice);
						} else {
							info.append("Inventory level: low.<br>");
							info.append("Price: unchanged<br>");
						}
						this.lowPrice = this.lowPrice * (1f - priceFlexibility);
					}
				} else if (this.price != null) {
					info.append("Sales level: null.<br>");
					this.highPrice = this.highPrice * (1f + priceFlexibility);
					this.lowPrice = this.lowPrice * (1f - priceFlexibility);
				}
				this.dataset.put("prices", this.price);
				this.dataset.put("price.low", this.lowPrice);
				this.dataset.put("price.high", this.highPrice);
				info.append("New price: " + this.price + "<br>");
				this.dataset.putInfo("updatePrice", info.toString());
			}

		};
	}

	/**
	 * Returns a new {@link WorkforceManager}.
	 * 
	 * @return a new {@link WorkforceManager}.
	 */
	private WorkforceManager getNewWorkforceManager() {
		return new WorkforceManager("WorkforceManager", timer) {

			/** The wage offered. */
			private Double highWage = null;

			/** The job offer. */
			private JobOffer jobOffer = null;

			/** The wage offered. */
			private Double lowWage = null;

			/** The manpower target. */
			private Integer manpowerTarget = null;

			private final double normalVacancyRate = sector.getParam("vacancy.normalRate");

			private final int observations = sector.getParam("wage.observations").intValue();

			/** The payroll (= the anticipated wage bill) */
			private Long payroll = null;

			/** Memory of the recent vacancies. */
			private final Memory<Integer> recentVacancies = new Memory<Integer>(1);

			/** Memory of the recent workforce target. */
			private final Memory<Integer> recentWorkforceTarget = new Memory<Integer>(1);

			/** Number of job vacancies in the current period. */
			private Integer vacancies = null;

			/** The wage offered. */
			private Double wage = null;

			/**
			 * The wagebill.
			 */
			private Long wagebill;

			private final float wageFlexibility = sector.getParam("wage.flexibility");

			private final double wageInitialValue = sector.getParam("wage.initialValue");

			/** The workforce. */
			private final Workforce workforce = new Workforce();

			private Double copyWage() {
				/*
				 * 2016-03-16 : Renvoie le salaire offert par une firme de plus
				 * grande taille.
				 */
				Double result = null;
				if (this.observations > 0) {
					List<Firm> sample = sector.getSimpleRandomSample(this.observations);
					for (Firm firm : sample) {
						final int size = firm.getSize();
						if (size > getSize()) {
							final Double newWage = (Double) firm.askFor("wage");
							if (newWage != null) {
								result = newWage;
								break;
							}
						}
					}
				}
				return result;
			}

			/**
			 * Returns the vacancy rate.
			 * 
			 * @return the vacancy rate.
			 */
			private Double getVacancyRate() {
				final Double result;
				final double sumVacancies = recentVacancies.getSum();
				final double sumJobs = recentWorkforceTarget.getSum();
				if (sumJobs == 0) {
					result = null;
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
							final float min = sector.getParam("labourContract.min");
							final float max = sector.getParam("labourContract.max");
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
				} else if ("wage".equals(key)) {
					result = this.wage;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void close() {
				checkConsistency();
				recentVacancies.add(this.vacancies);
				this.dataset.put("jobVacancies", this.vacancies);
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
				this.dataset.put("wageBill", this.wagebill.doubleValue());
			}

			@Override
			public void setWage(double wage) {
				this.wage = wage;
				this.highWage = this.wage * (1f + wageFlexibility);
				this.lowWage = this.wage * (1f - wageFlexibility);
			}

			/**
			 * A trial-and-error procedure for wage adjusting.
			 */
			@Override
			public void updateWage() {
				checkConsistency();
				final StringBuilder info = new StringBuilder();
				final Double vacancyRate = getVacancyRate();
				dataset.put("vacancies.rate", vacancyRate);
				final Double vacancyRatio;
				if (vacancyRate != null) {
					vacancyRatio = getVacancyRate() / normalVacancyRate;
				} else {
					vacancyRatio = null;
				}
				if (this.wage == null) {
					info.append("Wage=null<br>");
					this.wage = sector.getRandomWage();
					if (this.wage == null) {
						this.wage = wageInitialValue;
					}
					this.highWage = this.wage * (1f + wageFlexibility);
					this.lowWage = this.wage * (1f - wageFlexibility);

				} else if (vacancyRatio == null) {
					info.append("Does nothing<br>");
				} else {

					// Imitation "non directionnelle"

					final Double newWage = copyWage();
					// final Double newWage = null;

					if (newWage != null) {
						this.dataset.put("wage.copy", 1);
					} else {
						this.dataset.put("wage.copy", 0);
					}

					if (newWage != null) {
						this.wage = newWage;
						this.highWage = this.wage * (1f + wageFlexibility);
						this.lowWage = this.wage * (1f - wageFlexibility);
					} else if (vacancyRatio < 1) {
						// On baisse le salaire
						info.append("Vacancy rate: low<br>Decision: down<br>");
						this.highWage = this.wage;
						this.wage = this.lowWage + random.nextFloat() * (this.wage - this.lowWage);
						this.lowWage = this.lowWage * (1f - wageFlexibility);
					} else {
						// On hausse le salaire
						info.append("Vacancy rate: high<br>Decision: up<br>");
						this.lowWage = this.wage;
						this.wage = this.wage + random.nextFloat() * (this.highWage - this.wage);
						this.highWage = this.highWage * (1f + wageFlexibility);
					}
				}

				// info.append("New wage: " + wage + "<br>");
				dataset.putInfo("updateWage", info.toString());

				this.dataset.put("jobVacancyRate", vacancyRate);
				this.dataset.put("wages", wage);
				this.dataset.put("wage.low", lowWage);
				this.dataset.put("wage.high", highWage);
			}

			@Override
			public void updateWorkforce() {
				checkConsistency();
				workforce.cleanUp();
				manpowerTarget = Math.min(getFactory().getCurrentMaxCapacity(),
						Math.round(getFactory().getCapacity() * getProductionManager().getTarget()));
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
				this.dataset.put("jobOpenings", jobOpenings);
				this.recentWorkforceTarget.add(manpowerTarget);
				this.dataset.put("workforceTarget", manpowerTarget);
				this.vacancies = jobOpenings;
				this.newJobOffer();
			}

		};
	}

	/**
	 * @return the pricingManager
	 */
	private PricingManager getPricingManager() {
		return pricingManager;
	}

	/**
	 * @return the productionManager
	 */
	private ProductionManager getProductionManager() {
		return productionManager;
	}

	/**
	 * @return the salesManager
	 */
	private SalesManager getSalesManager() {
		return salesManager;
	}

	/**
	 * Returns an array of supplies, sorted by price in ascending order.
	 * 
	 * @param type
	 *            the type of goods to be supplied.
	 * @param i
	 *            the number of supplies to be returned.
	 * 
	 * @return an array of supplies, sorted by price in ascending order.
	 */
	private Supply[] getSupplies(String type, int i) {
		final Supply[] supplies = this.sector.getSupplies(type, i);
		for (int j = 0; j < supplies.length; j++) {
			if (supplies[j].getSupplier() == this) {
				// Exclusion of the firm itself.
				supplies[j] = null;
			}
		}
		Arrays.sort(supplies, supplyComparator);
		return supplies;
	}

	/**
	 * @return the workforceManager
	 */
	private WorkforceManager getWorkforceManager() {
		return workforceManager;
	}

	/**
	 * Imitates an other firm (copies its target debt ratio).
	 */
	private void imitation() {
		final int now = timer.getPeriod().intValue();
		if (now > this.lastImitiation + 12) {
			List<Firm> sample = sector.getSimpleRandomSample(10);
			for (Firm firm : sample) {
				final int lastImitiation2 = (Integer) firm.askFor("lastImitation");
				if (lastImitiation2 + 12 < now) {
					// TODO cette exclusion des firmes
					// qui ont elles-mêmes imité récemment n'a rien
					// d'indispensable.
					this.lastImitiation = now;
					this.imitations++;
					this.getWorkforceManager().setWage((Double) firm.askFor("wage"));
					this.targetDebtRatio = (Float) firm.askFor("targetDebtRatio");
					mutation(this.mutation);
					break;
				}
			}
		}
	}

	@SuppressWarnings("javadoc")
	private void invest(int investmentSize, Long[] machinePrices, long input, Supply[] supplies) {
		if (investmentSize > machinePrices.length - 1) {
			throw new IllegalArgumentException("Investment size is " + investmentSize + " but there is only "
					+ (machinePrices.length - 1) + " machines available.");
		}
		long investmentCost = machinePrices[investmentSize] + 2 * investmentSize;
		final long autofinancement = (long) (investmentCost * (1f - this.targetDebtRatio));
		final long newLongTermLoan = investmentCost - autofinancement;
		if (newLongTermLoan > 0) {
			this.account.newLoan(newLongTermLoan, longTerm, true);
		}
		if (this.account.getAmount() < investmentCost) {
			this.account.newLoan(investmentCost - this.account.getAmount(), shortTerm, true);
		}

		long requiredVolume = investmentSize * input;

		final Commodities stuff = new FinishedGoods(this.technology.getTypeOfInputForMachineCreation());

		for (Supply supply : supplies) {
			final long supplyVolume = supply.getVolume();
			final long purchaseVolume;
			if (supplyVolume > requiredVolume) {
				purchaseVolume = requiredVolume;
			} else {
				purchaseVolume = supplyVolume;
			}
			final long expense = supply.getPrice(purchaseVolume);
			if (expense > this.account.getAmount()) {
				Jamel.println("this.account.getAmount(): " + this.account.getAmount());
				Jamel.println("expense: " + expense);
				throw new RuntimeException("Not enough money.");
			}
			final Commodities purchase = supply.buy(purchaseVolume, this.account.newCheque(expense));
			stuff.put(purchase);
			requiredVolume -= purchaseVolume;
			if (requiredVolume == 0) {
				break;
			}
		}
		/*
		 * investmentCost=totalExpense; if (stuff.getValue() > 1.01 *
		 * investmentCost + 1 || stuff.getValue() < investmentCost) { throw new
		 * RuntimeException("Expense is " + stuff.getValue() + ", expected was "
		 * + investmentCost); }
		 */
		investmentCost = stuff.getValue();
		if (stuff.getVolume() != investmentSize * input) {
			throw new RuntimeException("Not enough stuff.");
		}
		final long investmentVolume = stuff.getVolume();
		final Machine[] newMachines = getNewMachines(investmentSize, stuff, technology, timer, random);
		// TODO: vérifier que stuff a été consommé.
		this.getFactory().expandCapacity(newMachines);
		this.data.put("investment.size", investmentSize);
		this.data.put("investment.vol", investmentVolume);
		this.data.put("investment.val", investmentCost);

	}

	/**
	 * Mutates
	 * 
	 * @param mut
	 *            the size of the mutation.
	 */
	private void mutation(float mut) {
		this.targetDebtRatio += (float) (mut * random.nextGaussian());
		if (this.targetDebtRatio > 1) {
			this.targetDebtRatio = 1f;
		} else if (this.targetDebtRatio < 0) {
			this.targetDebtRatio = 0f;
		}
	}

	/**
	 * Opens all the managers of this firm.
	 */
	private void openManagers() {
		this.factory.open();
		this.capitalManager.open();
		this.pricingManager.open();
		this.productionManager.open();
		this.salesManager.open();
		this.workforceManager.open();
	}

	/**
	 * @param capitalManager
	 *            the capitalManager to set
	 */
	private void setCapitalManager(CapitalManager capitalManager) {
		if (this.capitalManager != null) {
			throw new RuntimeException("Not null");
		}
		this.capitalManager = capitalManager;
	}

	/**
	 * @param factory
	 *            the factory to set
	 */
	private void setFactory(Factory factory) {
		if (this.factory != null) {
			throw new RuntimeException("Not null");
		}
		this.factory = factory;
	}

	/**
	 * @param pricingManager
	 *            the pricingManager to set
	 */
	private void setPricingManager(PricingManager pricingManager) {
		if (this.pricingManager != null) {
			throw new RuntimeException("Not null");
		}
		this.pricingManager = pricingManager;
	}

	/**
	 * @param productionManager
	 *            the productionManager to set
	 */
	private void setProductionManager(ProductionManager productionManager) {
		if (this.productionManager != null) {
			throw new RuntimeException("Not null");
		}
		this.productionManager = productionManager;
	}

	/**
	 * @param salesManager
	 *            the salesManager to set
	 */
	private void setSalesManager(SalesManager salesManager) {
		if (this.salesManager != null) {
			throw new RuntimeException("Not null");
		}
		this.salesManager = salesManager;
	}

	/**
	 * @param workforceManager
	 *            the workforceManager to set
	 */
	private void setWorkforceManager(WorkforceManager workforceManager) {
		if (this.workforceManager != null) {
			throw new RuntimeException("Not null");
		}
		this.workforceManager = workforceManager;
	}

	/**
	 * Updates the data.
	 */
	private void updateData() {

		this.data.put("firms", 1);
		this.data.put("age", getAge());

		this.data.putAll(this.workforceManager.getData());
		this.data.putAll(this.pricingManager.getData());
		this.data.putAll(this.factory.getData());
		this.data.putAll(this.capitalManager.getData());
		this.data.putAll(this.salesManager.getData());

		if (bankrupted) {
			this.data.put("bankruptcies", 1);
		} else {
			this.data.put("bankruptcies", 0);
		}

		this.data.putInfo("name", this.name);
		this.data.putInfo("account", this.account.getInfo());

		// ****

		this.memoryOfSalesVolume.add((Long) this.getSalesManager().askFor("salesVolume"));
		this.memoryOfSalesValue.add((Long) this.getSalesManager().askFor("salesValue"));
		this.memoryOfWagebill.add((Long) this.getWorkforceManager().askFor("wagebill"));
		this.memoryOfWorkforce.add((Integer) this.getWorkforceManager().askFor("workforce"));
		this.memoryOfGrossProfit.add((Long) this.getSalesManager().askFor("grossProfit"));

		final long newFactoryValue = this.getFactory().getValue();
		final long netInvestment = newFactoryValue - this.factoryValueAtTheEndOfThePeriod;

		this.factoryValueAtTheEndOfThePeriod = newFactoryValue;
		this.data.put("management.change", this.imitations);
		this.data.put("netInvestment", netInvestment);
		this.data.put("targetDebtRatio", this.targetDebtRatio);

		final double grossProfit = this.memoryOfGrossProfit.getSum();
		final double repayment = this.memoryOfDebtRepayment.getSum();
		final double interest = this.memoryOfInterest.getSum();

		if (grossProfit > repayment + interest) {
			this.data.put("hedge", 1);
			this.data.put("speculative", 0);
			this.data.put("ponzi", 0);
			this.data.put("income-debt.relation", 0);
		} else if (grossProfit < interest) {
			this.data.put("hedge", 0);
			this.data.put("speculative", 0);
			this.data.put("ponzi", 1);
			this.data.put("income-debt.relation", 2);
		} else {
			this.data.put("hedge", 0);
			this.data.put("speculative", 1);
			this.data.put("ponzi", 0);
			this.data.put("income-debt.relation", 1);
		}

	}

	@Override
	public Object askFor(String key) {
		final Object result;
		if ("targetDebtRatio".equals(key)) {
			result = this.targetDebtRatio;
		} else if ("wage".equals(key)) {
			result = this.getWorkforceManager().askFor("wage");
		} else if ("lastImitation".equals(key)) {
			result = this.lastImitiation;
		} else if ("size".equals(key)) {
			result = this.getSize();
		} else {
			throw new RuntimeException("Unexpected query: " + key);
			// result = super.askFor(key);
		}
		return result;
	}

	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;

		this.closeManagers();

		this.updateData();

		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data", e);
		}
	}

	@Override
	public Long getBookValue() {
		return (Long) this.capitalManager.askFor("capital");
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

	/**
	 * Issues the specified number of new shares.
	 * 
	 * @param shares
	 *            the number of new shares to be issued.
	 * @return a {@link StockCertificate} that encapsulates the new shares.
	 */
	@Override
	public StockCertificate[] getNewShares(List<Integer> shares) {
		return this.capitalManager.getNewShares(shares);
	}

	@Override
	public int getSize() {
		return this.getFactory().getCapacity();
	}

	@Override
	public Supply getSupply() {
		return this.salesManager.getSupply();
	}

	@Override
	public long getValueOfAssets() {
		return (Long) this.capitalManager.askFor("assets");
	}

	@Override
	public long getValueOfLiabilities() {
		return (Long) this.capitalManager.askFor("liabilities");
	}

	@Override
	public void goBankrupt() {
		this.bankrupted = true;
		this.factory.delete();
	}

	@Override
	public void inputsPurchase() {

		// Ca c'est pour stocker les offres consultées sur le marché des inputs:
		final Map<String, Supply[]> superSupplies = new LinkedHashMap<String, Supply[]>();

		// On récupère les besoins exprimés par l'usine.
		final Map<String, Long> needs = this.getFactory().getNeeds();

		// Pour stocker la dépense effective à des fins statistiques.
		long inputTotalValueEffective = 0;

		// On calcule le besoin de financement total.
		long inputTotalValue = 0;
		for (String inputKey : needs.keySet()) {
			final Supply[] supplies = this.getSupplies(inputKey, 10);
			superSupplies.put(inputKey, supplies);
			// TODO: 10 should be a parameter.
			long need = needs.get(inputKey);

			if (need > 0) {
				for (Supply supply : supplies) {
					final long volume = Math.min(need, supply.getVolume());
					inputTotalValue += supply.getPrice(volume);
					need -= volume;
					if (need == 0) {
						break;
					}
				}
			}

		}

		// On assure le financement des inputs.
		final long externalFunds = Math.max(0, inputTotalValue - this.account.getAmount());
		if (externalFunds > 0) {
			this.account.newLoan(externalFunds, shortTerm, false);
		}

		for (String inputKey : superSupplies.keySet()) {

			final Supply[] supplies = superSupplies.get(inputKey);
			long need = needs.get(inputKey);
			if (need > 0) {
				for (Supply supply : supplies) {
					final long volume = Math.min(need, supply.getVolume());
					final long amount = supply.getPrice(volume);
					final Commodities input = supply.buy(volume, this.account.newCheque(amount));
					inputTotalValueEffective += amount;
					need -= input.getVolume();
					this.getFactory().putResources(input);
					if (need == 0) {
						break;
					}
				}
			}
		}

		this.data.put("inputs.val", inputTotalValueEffective);

		// TODO: Verifier cette methode.

	}

	@Override
	public void invest() {
		this.data.put("desinvestment.size", 0);
		final long capital = (Long) this.getCapitalManager().askFor("capital");
		final long capitalTarget2 = (Long) this.getCapitalManager().askFor("capitalTarget");
		this.data.put("demand12", memoryOfSalesVolume.getMean());

		final double workforce = this.memoryOfWorkforce.getSum();
		final double salesVolume = this.memoryOfSalesVolume.getSum();
		if (workforce > 0 && salesVolume > 0) {
			averagePrice = this.memoryOfSalesValue.getSum() / salesVolume;
			averageWage = this.memoryOfWagebill.getSum() / workforce;
		}

		int investmentSize = 0;
		long investmentCost = 0;
		long investmentVolume = 0;
		this.data.put("investment.size", investmentSize);
		this.data.put("investment.vol", investmentVolume);
		this.data.put("investment.val", investmentCost);

		final double anticipedDemand = this.memoryOfSalesVolume.getMean() * (1. + investmentGreediness);
		this.data.put("demand.anticipated", anticipedDemand);

		if ((averagePrice != null && averageWage != null) || (this.getFactory().getCapacity() == 0)) {

			// Il faut que la firme ait fonctionné au moins une fois au
			// cours des périodes récentes, pour qu'on puisse calculer un
			// prix moyen et un salaire moyen.

			// Sauf si le nombre de machine = 0, là il faut en racheter une
			// coute que coute.

			final long[] machinery = (long[]) this.getFactory().askFor("machinery");

			if (this.getFactory().getCapacity() == 0 || capital > capitalTarget2) {

				// Il faut que le niveau de capital de la firme soit
				// satisfaisant
				// pour qu'on puisse envisager l'achat de nouvelles machines

				// On récupère une liste d'offres.
				final Supply[] supplies = getSupplies(technology.getTypeOfInputForMachineCreation(), 10);

				if (supplies.length > 0) {

					// Il faut qu'il y ait au moins 1 offre de 'raw
					// materials'.

					// TODO pour combien de machines au max ? Il faudrait
					// déterminer ça avant l'étape ci-dessous.
					final Long[] machinePrices = getPrices(supplies,
							technology.getInputVolumeForANewMachine());

					// TODO: Il faudrait demander à la banque son taux +
					// tenir
					// compte de l'inflation + aversion au risque
					if (machinePrices.length == 1) {
						investmentSize = 0;
					} else if (this.getFactory().getCapacity() == 0) {
						imitation();
						investmentSize = 1;
					} else {
						final float discountRate = this.account.getRealRate();
						investmentSize = getOptimumSize(machinePrices, technology.getProductivity(), machinery,
								anticipedDemand, averagePrice, averageWage, (int) technology.getTimelifeMean(),
								discountRate);
					}

					if (investmentSize > 0) {
						invest(investmentSize, machinePrices, technology.getInputVolumeForANewMachine(), supplies);
					}
				}

			}
		}
	}

	@Override
	public boolean isBankrupted() {
		return this.bankrupted;
	}

	@Override
	public boolean isCancelled() {
		return this.bankrupted;
	}

	@Override
	public boolean isSolvent() {
		return this.capitalManager.isSolvent();
	}

	@Override
	public void open() {
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		if (this.period == null) {
			this.period = this.timer.getPeriod().intValue();
		} else {
			this.period++;
			if (this.period != timer.getPeriod().intValue()) {
				throw new AnachronismException("Bad period");
			}
		}
		this.data = getNewDataset();
		this.openManagers();
		getFactory().setInventoryNormalLevel(sector.getParam("inventories.normalLevel"));
		if (this.bankrupted) {
			this.capitalManager.bankrupt();
			this.workforceManager.layoff();
			this.factory.cancel();
		}
		this.imitations = 0;
		if (random.nextFloat() > stability) {
			mutation(mutation);
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

		this.capitalManager.secureFinancing(this.workforceManager.getPayroll());

	}

	@Override
	public void production() {
		if (this.bankrupted) {
			throw new RuntimeException("This firm is bankrupted.");
		}
		this.workforceManager.payWorkers();
		factory.process(this.workforceManager.getLaborPowers());
		this.salesManager.createSupply();
	}

}

// ***