package jamel.models.modelJEE.firms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.AgentDataset;
import jamel.data.BasicAgentDataset;
import jamel.data.DataKeys;
import jamel.models.modelJEE.capital.BasicCapitalStock;
import jamel.models.modelJEE.capital.CapitalStock;
import jamel.models.modelJEE.capital.StockCertificate;
import jamel.models.modelJEE.firms.factory.BasicFactory;
import jamel.models.modelJEE.firms.factory.BasicMachine;
import jamel.models.modelJEE.firms.factory.FinishedGoods;
import jamel.models.modelJEE.firms.factory.Machine;
import jamel.models.modelJEE.firms.factory.Technology;
import jamel.models.modelJEE.roles.Shareholder;
import jamel.models.modelJEE.util.Memory;
import jamel.models.util.Account;
import jamel.models.util.Bank;
import jamel.models.util.Cheque;
import jamel.models.util.Commodities;
import jamel.models.util.JobContract;
import jamel.models.util.JobOffer;
import jamel.models.util.Supplier;
import jamel.models.util.Supply;
import jamel.models.util.Worker;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
 * A basic firm.
 */
@SuppressWarnings("javadoc")
public class BasicFirm extends JamelObject implements Firm {

	private class CapitalManager {

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

		private void clearOwnership() {
			imitation();
			final boolean isOpen = capitalStock.isOpen();
			this.capitalStock.cancel();
			this.capitalStock = new BasicCapitalStock(BasicFirm.this, account, getSimulation());
			if (isOpen) {
				this.capitalStock.open();
			}
		}

		private void close() {

			this.capitalStock.close();

			final long cash = account.getAmount();
			final long assets = factory.getValue() + cash;
			final long liabilities = account.getDebt();
			final long capital = assets - liabilities;

			BasicFirm.this.dataset.put(keys.debtRatio, ((double) liabilities) / assets);
			BasicFirm.this.dataset.put(keys.money, cash);
			BasicFirm.this.dataset.put(keys.assets, assets);
			BasicFirm.this.dataset.put(keys.liabilities, liabilities);
			BasicFirm.this.dataset.put(keys.equities, capital);
			BasicFirm.this.dataset.put(keys.tangibleAssets, factory.getValue());

			BasicFirm.this.dataset.put(keys.shortTermDebt, account.getShortTermDebt());
			BasicFirm.this.dataset.put(keys.longTermDebt, account.getLongTermDebt());

			BasicFirm.this.dataset.put(keys.dividends, this.dividend);
			BasicFirm.this.dataset.put(keys.interests, account.getInterests());
			// BasicFirm.this.newDataset.put(keys.debtService,
			// account.getRepaidDebt());

			BasicFirm.this.dataset.put(keys.liabilitiesTarget, getLiabilitiesTarget());
			// BasicFirm.this.newDataset.put(keys.lia, getLiabilitiesExcess());

			// BasicFirm.this.newDataset.put("liabilities.new",
			// account.getNewDebt());
			// BasicFirm.this.newDataset.put("liabilities.repayment",
			// account.getRepaidDebt());

			// BasicFirm.this.newDataset.put("canceledDebts",
			// account.getCanceledDebt());

			// ***

			final long netProfit = capital - initialCapital + dividend;
			this.netProfitMemory.add(netProfit);
			final double returnOnEquity = this.netProfitMemory.getSum() / capital;
			final double returnOnAssets = this.netProfitMemory.getSum() / assets;
			final double solvency = this.netProfitMemory.getSum() / liabilities;
			// BasicFirm.this.oldDataset.put("returnOnEquity", returnOnEquity);
			// BasicFirm.this.oldDataset.put("returnOnAssets", returnOnAssets);
			if (Double.isFinite(solvency)) {
				// BasicFirm.this.oldDataset.put("solvency", solvency);
			}

			memoryOfInterest.add(account.getInterests());
			memoryOfDebtRepayment.add(account.getRepaidDebt());

		}

		private long getCapital() {
			return factory.getValue() + account.getAmount() - account.getDebt();
		}

		private long getCapitalTarget() {
			final long assets = account.getAmount() + factory.getValue();
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
			final long assets = account.getAmount() + factory.getValue();
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
			return this.capitalStock.issueNewShares(nShares);
		}

		private StockCertificate[] getNewShares(List<Integer> shares) {
			this.clearOwnership();
			final StockCertificate[] newShares = new StockCertificate[shares.size()];
			for (int i = 0; i < shares.size(); i++) {
				newShares[i] = this.getNewShares(shares.get(i));
			}
			return newShares;
		}

		private boolean isSolvent() {
			return (this.getCapital() >= 0);
		}

		private void open() {
			this.initialCapital = this.getCapital();
			this.dividend = 0;
			this.capitalStock.open();
		}

		private void payDividend() {
			final long cash = account.getAmount();
			final long assets = cash + factory.getValue();
			final double capital = getCapital();
			final long capitalTarget2 = getCapitalTarget();
			final double averageIncome = this.netProfitMemory.getMean();
			long newDividend;
			if (capital > 0) {
				final double ratio = capital / capitalTarget2;
				newDividend = (long) Math.min(averageIncome * ratio,
						capital * consts.ownerEquitiesMaximumShareToBeDistributedAsDividend);
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

			/*
			BasicFirm.this.oldDataset.put("payDividend.cash", cash);
			BasicFirm.this.oldDataset.put("payDividend.assets", assets);
			BasicFirm.this.oldDataset.put("payDividend.capital", capital);
			BasicFirm.this.oldDataset.put("payDividend.capitalTarget", capitalTarget2);
			BasicFirm.this.oldDataset.put("payDividend.averageIncome", averageIncome);
			BasicFirm.this.oldDataset.put("payDividend.dividend", dividend);
			
			BasicFirm.this.oldDataset.put("debt2target.ratio", (account.getDebt()) / getLiabilitiesTarget());
			*/
		}

		private void secureFinancing(long amount) {
			if (amount > account.getAmount()) {
				account.borrow(amount - account.getAmount(), consts.shortTerm, false);
			}
			if (account.getAmount() < amount) {
				throw new RuntimeException("Production is not financed.");
			}
		}

		private void updateOwnership() {
			if (ownership) {
				throw new RuntimeException("The ownership of this firm is already fixed.");
			}
			// TODO Households should be a parameter.
			final List<? extends Agent> shareHolders = getSector("Households").selectList(10);
			// final List<Shareholder> shareHolders =
			// sector.selectCapitalOwner(10);
			// final List<Shareholder> shareHolders = (List<Shareholder>)
			// sector.select(10);
			// TODO : 10 should be a parameter
			if (shareHolders.size() > 0) {
				this.capitalStock = new BasicCapitalStock(BasicFirm.this, shareHolders.size(), account,
						getSimulation());
				List<StockCertificate> certificates = this.capitalStock.getCertificates();
				for (int id = 0; id < certificates.size(); id++) {
					final StockCertificate certif = certificates.get(id);
					final Shareholder shareHolder = (Shareholder) shareHolders.get(id);
					shareHolder.addAsset(certif);
				}
				ownership = true;
			} else {
				throw new RuntimeException("No shareholder.");
			}
		}
	}

	/**
	 * A class to parse and store the constant parameters of the firm.
	 */
	private class Constants {

		private final int initialCapacity;

		private final Float initialUtilizationRate;

		final private float inventoryNormalLevel;

		private final float investmentGreediness;

		final private int jobContractMax;

		final private int jobContractMin;

		final private int longTerm;

		private final float mutation;

		private final float ownerEquitiesMaximumShareToBeDistributedAsDividend;

		final private float priceFlexibility;

		private final float propensity2sellInventories;

		private final float salesCapacity;

		final private int shortTerm;

		private final float stability;

		/**
		 * Creates a new set of parameters by parsing the specified
		 * {@code Parameters}.
		 * 
		 * @param params
		 *            the parameters to be parsed.
		 */
		private Constants(Parameters params) {
			this.jobContractMax = params.getInt("workforce.jobContracts.max");
			this.jobContractMin = params.getInt("workforce.jobContracts.min");
			this.shortTerm = params.getInt("credit.term.short");
			this.longTerm = params.getInt("credit.term.long");
			this.inventoryNormalLevel = params.getFloat("inventories.normalLevel");
			this.ownerEquitiesMaximumShareToBeDistributedAsDividend = params
					.getFloat("ownerEquities.maximumShareToBeDistributedAsDividend");
			this.investmentGreediness = params.getFloat("investment.greediness").floatValue();
			this.mutation = params.getFloat("mutation.strenght").floatValue();
			this.stability = 1f - params.getFloat("mutation.probability").floatValue();
			this.priceFlexibility = params.getFloat("price.flexibility");
			this.initialCapacity = params.getInt("production.capacity").intValue();
			this.initialUtilizationRate = params.getFloat("utilizationRate.initialValue");
			this.propensity2sellInventories = params.getFloat("inventories.propensity2sell");
			this.salesCapacity = params.getFloat("sales.capacity");
		}
	}

	private class PricingManager {

		/** The higher price. */
		private Double highPrice = null;

		/** The lower price. */
		private Double lowPrice = null;

		/** The price. */
		private Double price;

		/** The sales ratio observed the last period. */
		private Double salesRatio = null;

		private void close() {
			final long supplyVolume = salesManager.supplyVolume;
			final double salesVolume = salesManager.salesVolume;
			if (supplyVolume > 0) {
				this.salesRatio = salesVolume / supplyVolume;
			} else {
				this.salesRatio = null;
			}
		}

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
			return lowerBound + getRandom().nextFloat() * (upperBound - lowerBound);
		}

		private Double getPrice() {
			return this.price;
		}

		/**
		 * Sets the price equal to the unit cost.
		 */
		private void setUnitCostPrice() {
			final Double unitCost = factory.getUnitCost();
			if (unitCost != null) {
				if (unitCost.isNaN()) {
					throw new RuntimeException("Unit cost is not a number");
				}
				this.price = unitCost;
				this.highPrice = (1f + consts.priceFlexibility) * this.price;
				this.lowPrice = (1f - consts.priceFlexibility) * this.price;
			}
		}

		private void updatePrice() {
			final StringBuilder info = new StringBuilder();
			if (this.price == null) {
				this.setUnitCostPrice();
				info.append("Price is null: using the unit cost.<br>");
			}
			if (this.price != null && salesRatio != null) {
				final double inventoryRatio = factory.getInventoryRatio();
				if ((salesRatio >= 1)) {
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
					this.highPrice = this.highPrice * (1f + consts.priceFlexibility);
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
					this.lowPrice = this.lowPrice * (1f - consts.priceFlexibility);
				}
			} else if (this.price != null) {
				info.append("Sales level: null.<br>");
				this.highPrice = this.highPrice * (1f + consts.priceFlexibility);
				this.lowPrice = this.lowPrice * (1f - consts.priceFlexibility);
			}
			BasicFirm.this.dataset.put(keys.price, this.price);
			BasicFirm.this.dataset.put(keys.lowPrice, this.lowPrice);
			BasicFirm.this.dataset.put(keys.highPrice, this.highPrice);
		}

	}

	private class ProductionManager {

		private Float utilizationRateFlexibility = null;

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

		private float getTarget() {
			if (this.utilizationRateTargeted == null) {
				this.utilizationRateTargeted = consts.initialUtilizationRate;
			}
			return this.utilizationRateTargeted;
		}

		private void updateCapacityUtilizationTarget() {
			if (this.utilizationRateTargeted == null) {
				this.utilizationRateTargeted = consts.initialUtilizationRate;
			} else {
				final double inventoryRatio = factory.getInventoryRatio();
				final float alpha1 = getRandom().nextFloat();
				final float alpha2 = getRandom().nextFloat();
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

	}

	private class SalesManager {

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

		private void createSupply() {
			final int validPeriod = getPeriod();
			supplyVolume = Math.min((long) (consts.propensity2sellInventories * factory.getFinishedGoodsVolume()),
					(long) (consts.salesCapacity * factory.getPotentialOutput()));

			if (supplyVolume == 0 && factory.getFinishedGoodsVolume() != 0) {
				supplyVolume = factory.getFinishedGoodsVolume();
				if (factory.getPotentialOutput() != 0 && factory.getFinishedGoodsVolume() != 1) {
					throw new RuntimeException("Why is it possible?");
				}
			}

			if (pricingManager.getPrice() == null) {
				pricingManager.updatePrice();
				// TODO: combien de fois updatePrice() est-il appel� ?
				// ne peut-il �tre appel� q'une fois, ici ?
			}
			final Double price = pricingManager.getPrice();

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
					if (validPeriod != getPeriod()) {
						throw new RuntimeException("Out of date.");
					}
				}

				@Override
				public Double getPrice() {
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
				public long getValue() {
					return (long) (price * volume);
				}

				@Override
				public long getVolume() {
					anachronismDetection();
					return this.volume;
				}

				@Override
				public boolean isEmpty() {
					return this.volume == 0;
				}

				@Override
				public Commodities purchase(long demand, Cheque cheque) {
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
					final Commodities sales = factory.getCommodities(demand);
					salesValueAtCost += sales.getValue();
					grossProfit = salesValue - salesValueAtCost;
					sales.setValue(cheque.getAmount());
					return sales;
				}

				@Override
				public String toString() {
					anachronismDetection();
					return "Supply by " + name + ": price <" + price + ">, volume <" + volume + ">";
				}

			};
		}

		private Supply getSupply() {
			final Supply result;
			if (this.supply.getVolume() > 0) {
				result = this.supply;
			} else {
				result = null;
			}
			return result;
		}

		private void open() {
			this.supply = null;
			this.grossProfit = 0;
			this.salesValue = 0;
			this.salesValueAtCost = 0;
			this.salesVolume = 0;
			this.supplyValue = 0;
			this.supplyVolume = 0;
		}

	}

	private class WorkforceManager {

		/** The wage offered. */
		private Double highWage = null;

		/** The job offer. */
		private JobOffer jobOffer = null;

		private Integer jobOpenings;

		/** The wage offered. */
		private Double lowWage = null;

		/** The manpower target. */
		private Integer manpowerTarget = null;

		private Double normalVacancyRate = null;

		private Integer observations = null;

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

		private Float wageFlexibility = null;

		private Double wageInitialValue = null;

		/** The workforce. */
		private final Workforce workforce = new Workforce();

		private void close() {
			recentVacancies.add(this.vacancies);
			BasicFirm.this.dataset.put(keys.vacancies, this.vacancies);
			BasicFirm.this.dataset.put(keys.vacancyRatio,
					(factory.getCapacity() != 0) ? ((double) this.vacancies) / this.jobOpenings : null);
		}

		private Double copyWage() {
			/*
			 * 2016-03-16 : Renvoie le salaire offert par une firme de plus
			 * grande taille.
			 */
			Double result = null;
			if (this.observations > 0) {
				@SuppressWarnings("unchecked")
				List<Firm> sample = (List<Firm>) sector.selectList(this.observations);
				for (Firm firm : sample) {
					final int size = firm.getSize();
					if (size > getSize()) {
						final Double newWage = ((BasicFirm) firm).workforceManager.wage;
						if (newWage != null) {
							result = newWage;
							break;
						}
					}
				}
			}
			return result;
		}

		private JobOffer getJobOffer() {

			final JobOffer result;
			if (this.vacancies > 0) {
				result = this.jobOffer;
			} else {
				result = null;
			}
			return result;
		}

		private long getPayroll() {

			return this.payroll;
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
				final int validPeriod = getPeriod();
				jobOffer = new JobOffer() {

					private final long jobWage = (long) Math.floor(wage);

					@Override
					public JobContract apply(final Worker worker) {
						if (!(vacancies > 0)) {
							throw new RuntimeException("No vacancy.");
						}
						vacancies--;

						final int term;
						if (consts.jobContractMax == consts.jobContractMin) {
							term = consts.jobContractMin;
						} else {
							term = consts.jobContractMin
									+ getRandom().nextInt(consts.jobContractMax - consts.jobContractMin);
						}

						final JobContract jobContract = newJobContract(worker, jobWage, term);
						workforce.add(jobContract);
						return jobContract;

					}

					@Override
					public long getWage() {
						return jobWage;
					}

					@Override
					public boolean isEmpty() {
						return vacancies == 0;
					}

					@Override
					public int size() {
						Jamel.notUsed();
						return 0;
					}
				};
			}
		}

		private void open() {
			this.jobOffer = null;
			this.manpowerTarget = null;
			this.payroll = null;
			this.vacancies = null;
			this.wagebill = null;
			this.jobOpenings = null;
		}

		private void payWorkers() {
			this.wagebill = 0l;
			for (JobContract contract : workforce) {
				if (!contract.isValid()) {
					throw new RuntimeException("Invalid job contract.");
				}
				contract.getWorker().acceptPayCheque(account.issueCheque(contract.getWorker(), contract.getWage()));
				this.wagebill += contract.getWage();
			}
			BasicFirm.this.dataset.put(keys.wageBill, this.wagebill.doubleValue());
		}

		private void setWage(double wage) {
			this.wage = wage;
			this.highWage = this.wage * (1f + wageFlexibility);
			this.lowWage = this.wage * (1f - wageFlexibility);
		}

		/**
		 * A trial-and-error procedure for wage adjusting.
		 */
		private void updateWage() {
			final StringBuilder info = new StringBuilder();
			final Double vacancyRate = getVacancyRate();
			final Double vacancyRatio;
			if (vacancyRate != null) {
				vacancyRatio = getVacancyRate() / normalVacancyRate;
			} else {
				vacancyRatio = null;
			}
			if (this.wage == null) {
				/*info.append("Wage=null<br>");
				this.wage = sector.getRandomWage();
				if (this.wage == null) {
					this.wage = wageInitialValue;
				}*/
				this.wage = wageInitialValue;
				this.highWage = this.wage * (1f + wageFlexibility);
				this.lowWage = this.wage * (1f - wageFlexibility);

			} else if (vacancyRatio == null) {
				info.append("Does nothing<br>");
			} else {

				// Imitation "non directionnelle"

				final Double newWage = copyWage();
				// final Double newWage = null;

				/*if (newWage != null) {
					BasicFirm.this.dataset.put("wage.copy", 1);
				} else {
					BasicFirm.this.dataset.put("wage.copy", 0);
				*/

				if (newWage != null) {
					this.wage = newWage;
					this.highWage = this.wage * (1f + wageFlexibility);
					this.lowWage = this.wage * (1f - wageFlexibility);
				} else if (vacancyRatio < 1) {
					// On baisse le salaire
					info.append("Vacancy rate: low<br>Decision: down<br>");
					this.highWage = this.wage;
					this.wage = this.lowWage + getRandom().nextFloat() * (this.wage - this.lowWage);
					this.lowWage = this.lowWage * (1f - wageFlexibility);
				} else {
					// On hausse le salaire
					info.append("Vacancy rate: high<br>Decision: up<br>");
					this.lowWage = this.wage;
					this.wage = this.wage + getRandom().nextFloat() * (this.highWage - this.wage);
					this.highWage = this.highWage * (1f + wageFlexibility);
				}
			}

			BasicFirm.this.dataset.put(keys.wage, wage);
			BasicFirm.this.dataset.put(keys.lowWage, lowWage);
			BasicFirm.this.dataset.put(keys.highWage, highWage);
		}

		private void updateWorkforce() {
			workforce.cleanUp();
			manpowerTarget = Math.min(factory.getCapacity(),
					Math.round(factory.getCapacity() * productionManager.getTarget()));
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
			BasicFirm.this.dataset.put(keys.jobOffers, jobOpenings);
			this.recentWorkforceTarget.add(manpowerTarget);
			BasicFirm.this.dataset.put(keys.workforceTarget, manpowerTarget);
			this.vacancies = jobOpenings;
			this.newJobOffer();
		}

	}

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = BasicFirmKeys.getInstance();

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
	private static int getOptimumSize(final Long[] machinePrices, final long productivity, final int machinery,
			final double demandForecast, final double productPrice, final double wage, final int forecastPeriod,
			float discountRate) {

		double presentValue = getPresentValue(machinery, 0, productivity, demandForecast, productPrice, wage,
				discountRate, forecastPeriod);
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

			final double presentValue2 = getPresentValue(machinery + targetedInvestmentSize, machinePrice, productivity,
					demandForecast, productPrice, wage, discountRate, forecastPeriod);
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
			double demandForecast, final double productPrice, final double wage, final float rate,
			final int forecastPeriod) {
		final double effectiveProduction = Math.min(machines * productivity, demandForecast);
		final double wagebill = wage * effectiveProduction / productivity;
		final double sales = effectiveProduction * productPrice;
		final double cashFlow = sales - wagebill;
		final double presentValue;
		if (rate == 0) {
			presentValue = cashFlow * forecastPeriod - initialOutlay;
		} else {
			presentValue = (cashFlow / rate) * (1 - 1 / Math.pow(1 + rate, forecastPeriod)) - initialOutlay;
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
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static public Consumer<? super Agent> getAction(final String phaseName) {

		final Consumer<? super Agent> action;

		switch (phaseName) {
		case "opening":
			action = (agent) -> {
				((BasicFirm) agent).openning();
			};
			break;
		case "pay_dividend":
			action = (agent) -> {
				((BasicFirm) agent).capitalManager.payDividend();
			};
			break;
		case "plan_production":
			action = (agent) -> {
				final BasicFirm firm = (BasicFirm) agent;
				firm.pricingManager.updatePrice();
				firm.workforceManager.updateWage();
				firm.productionManager.updateCapacityUtilizationTarget();
				firm.workforceManager.updateWorkforce();
				firm.capitalManager.secureFinancing(firm.workforceManager.getPayroll());
			};
			break;
		case "production":
			action = (agent) -> {
				final BasicFirm firm = (BasicFirm) agent;
				firm.workforceManager.payWorkers();
				firm.factory.process(firm.workforceManager.workforce);
				firm.salesManager.createSupply();
			};
			break;
		case "investment":
			action = (agent) -> {
				((BasicFirm) agent).invest();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicFirm) agent).closure();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	static public DataKeys getDataKeys() {
		return keys;
	}

	/** The account. */
	private final Account account;

	private Double averagePrice = null;

	private Double averageWage = null;

	/** The capital manager. */
	final private CapitalManager capitalManager = new CapitalManager();

	final private Constants consts;

	/** The data of the agent. */
	private AgentDataset dataset = new BasicAgentDataset(this, keys);

	/** A flag that indicates if the data of the firm is to be exported. */
	private boolean exportData;

	/** The factory. */
	final private BasicFactory factory;

	/**
	 * The factory value. Updated at the end of the period. To compute the net
	 * investment at the end of the period.
	 */
	private long factoryValueAtTheEndOfThePeriod = 0;

	private int imitations = 0;

	private int lastImitiation = 0;

	private final Memory<Long> memoryOfDebtRepayment;

	private final Memory<Long> memoryOfGrossProfit;

	private final Memory<Long> memoryOfInterest;

	private final Memory<Long> memoryOfSalesValue;

	private final Memory<Long> memoryOfSalesVolume;

	private final Memory<Long> memoryOfWagebill;

	private final Memory<Integer> memoryOfWorkforce;

	/** The name of this firm. */
	private final String name;

	/** A flag that indicates if this firm is open or not. */
	private boolean open;

	final private Parameters parameters;

	/** The current period. */
	private Integer period = null;

	/** The pricing manager. */
	final private PricingManager pricingManager = new PricingManager();

	/** The production manager. */
	final private ProductionManager productionManager = new ProductionManager();

	/** The marketing manager. */
	final private SalesManager salesManager = new SalesManager();

	/** The sector. */
	private final Sector sector;

	private Float targetDebtRatio = null;

	final private Technology technology;

	/** The employer behavior. */
	final private WorkforceManager workforceManager = new WorkforceManager();

	/**
	 * Creates a new basic firm.
	 * 
	 * @param name
	 *            the name.
	 * @param sector
	 *            the sector.
	 */
	public BasicFirm(String name, Sector sector) {
		super(sector.getSimulation());
		this.name = name;
		this.sector = sector;

		this.parameters = this.sector.getParameters();

		this.consts = new Constants(this.parameters);

		// TODO final String bankSectorName =
		// parameters.get("financing").getAttribute("bankSector");
		final String bankSectorName = "Banks";
		this.account = ((Bank) this.getSimulation().getSector(bankSectorName).selectList(1).get(0)).openAccount(this);
		// this.account = this.sector.getNewAccount(this);

		// 2016-04-04: rassemblement de la lecture des paramètres dans le
		// constructeur.

		this.memoryOfSalesValue = new Memory<Long>(12);
		this.memoryOfSalesVolume = new Memory<Long>(12);
		this.memoryOfWagebill = new Memory<Long>(12);
		this.memoryOfWorkforce = new Memory<Integer>(12);
		this.memoryOfGrossProfit = new Memory<Long>(6);
		this.memoryOfInterest = new Memory<Long>(6);
		this.memoryOfDebtRepayment = new Memory<Long>(6);

		this.technology = this.getTechnology(parameters.get("technology"));

		final float min = this.parameters.getFloat("debtRatio.target.initialValue.min");
		final float max = this.parameters.getFloat("debtRatio.target.initialValue.max");
		this.targetDebtRatio = min + (max - min) * this.getRandom().nextFloat();

		this.workforceManager.normalVacancyRate = this.parameters.getDoubleValue("vacancy.normalRate");
		this.workforceManager.observations = this.parameters.getInt("wage.observations").intValue();
		this.workforceManager.wageFlexibility = this.parameters.getFloat("wage.flexibility");
		this.workforceManager.wageInitialValue = this.parameters.getDoubleValue("wage.initialValue");
		this.productionManager.utilizationRateFlexibility = this.parameters.getFloat("utilizationRate.flexibility");
		this.capitalManager.updateOwnership();

		final Machine[] machines = getNewMachines(this.consts.initialCapacity, null, this.technology);
		this.factory = new BasicFactory(this.dataset, this.technology.getProductionTime(), getSimulation(), getRandom(),
				machines);

	}

	private void closure() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;

		this.pricingManager.close();
		this.capitalManager.close();
		this.workforceManager.close();
		this.factory.close();

		this.updateData();
		this.dataset.close();

		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data", e);
		}
	}

	/**
	 * Exports agent data in a csv file.
	 * 
	 * @throws IOException
	 *             in the case of an I/O exception.
	 */
	private void exportData() throws IOException {
		/*if (this.exportData) { TODO IMPLEMENT ME
			// TODO gerer la localisation du dossier exports, son existence
			final File outputFile = new File("exports/" + sector.getSimulationID() + "-" + this.name + ".csv");
			if (!outputFile.exists()) {
				this.dataset.exportHeadersTo(outputFile);
			}
			this.dataset.exportTo(outputFile);
		}*/
	}

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
	 *            the getRandom().
	 * @return the specified number of new machines.
	 */
	private Machine[] getNewMachines(int n, Commodities stuff, Technology technology) {
		final Machine[] machines = new Machine[n];
		for (int i = 0; i < n; i++) {
			final Commodities input;
			if (stuff == null) {
				input = null;
			} else {
				input = stuff.take(technology.getInputVolumeForANewMachine());
			}
			machines[i] = new BasicMachine(technology, input, this.getSimulation(), getRandom());
		}
		return machines;
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
	private Supply[] getSupplies(int i) {
		final ArrayList<Supply> list = new ArrayList<Supply>(i);
		final List<? extends Agent> suppliers = this.sector.selectList(i);
		for (final Agent firm : suppliers) {
			final Supply supply = ((Firm) firm).getSupply();
			if (!firm.equals(this) && supply != null) {
				list.add(supply);
			}
		}
		final Supply[] supplies = list.toArray(new Supply[list.size()]);
		Arrays.sort(supplies, supplyComparator);
		return supplies;
	}

	private Technology getTechnology(final Parameters params) {

		final long inputVolumeForANewMachine = params.getIntAttribute("machine.creation.input.volume");
		final int getProductionTime = params.getIntAttribute("production.time");
		final long getProductivity = params.getIntAttribute("production.productivity");
		final double getTimelifeMean = params.getIntAttribute("machine.timelife.mean");
		final double getTimelifeStDev = params.getIntAttribute("machine.timelife.stDev");

		return new Technology() {

			@Override
			public long getInputVolumeForANewMachine() {
				return inputVolumeForANewMachine;
			}

			@Override
			public int getProductionTime() {
				return getProductionTime;
			}

			@Override
			public long getProductivity() {
				return getProductivity;
			}

			@Override
			public double getTimelifeMean() {
				return getTimelifeMean;
			}

			@Override
			public double getTimelifeStDev() {
				return getTimelifeStDev;
			}

		};
	}

	/**
	 * Imitates an other firm (copies its target debt ratio).
	 */
	private void imitation() {
		final int now = getPeriod();
		if (now > this.lastImitiation + 12) {
			@SuppressWarnings("unchecked")
			List<Firm> sample = (List<Firm>) sector.selectList(10);
			for (Firm firm : sample) {
				final int lastImitiation2 = ((BasicFirm) firm).lastImitiation;
				if (lastImitiation2 + 12 < now) {
					// TODO cette exclusion des firmes
					// qui ont elles-mêmes imité récemment n'a rien
					// d'indispensable.
					this.lastImitiation = now;
					this.imitations++;
					this.workforceManager.setWage(((BasicFirm) firm).workforceManager.wage);
					this.targetDebtRatio = ((BasicFirm) firm).targetDebtRatio;
					mutation(this.consts.mutation);
					break;
				}
			}
		}
	}

	private void invest() {
		final long capital = this.capitalManager.getCapital();
		final long capitalTarget2 = this.capitalManager.getCapitalTarget();
		// this.dataset.put("demand12", memoryOfSalesVolume.getMean());

		final double workforce = this.memoryOfWorkforce.getSum();
		final double salesVolume = this.memoryOfSalesVolume.getSum();
		if (workforce > 0 && salesVolume > 0) {
			averagePrice = this.memoryOfSalesValue.getSum() / salesVolume;
			averageWage = this.memoryOfWagebill.getSum() / workforce;
		}

		int investmentSize = 0;
		long investmentCost = 0;
		long investmentVolume = 0;
		// this.dataset.put("investment.size", investmentSize);
		// this.dataset.put("investment.vol", investmentVolume);
		// this.dataset.put("investment.val", investmentCost);

		final double anticipedDemand = this.memoryOfSalesVolume.getMean() * (1. + consts.investmentGreediness);
		// this.dataset.put("demand.anticipated", anticipedDemand);

		if ((averagePrice != null && averageWage != null) || (this.factory.getCapacity() == 0)) {

			// Il faut que la firme ait fonctionné au moins une fois au
			// cours des périodes récentes, pour qu'on puisse calculer un
			// prix moyen et un salaire moyen.

			// Sauf si le nombre de machine = 0, là il faut en racheter une
			// coute que coute.

			final int capacity = this.factory.getCapacity();

			if (this.factory.getCapacity() == 0 || capital > capitalTarget2) {

				// Il faut que le niveau de capital de la firme soit
				// satisfaisant
				// pour qu'on puisse envisager l'achat de nouvelles machines

				// On récupère une liste d'offres.
				final Supply[] supplies = getSupplies(10);

				if (supplies.length > 0) {

					// Il faut qu'il y ait au moins 1 offre de 'raw
					// materials'.

					// TODO pour combien de machines au max ? Il faudrait
					// déterminer ça avant l'étape ci-dessous.
					final Long[] machinePrices = getPrices(supplies, technology.getInputVolumeForANewMachine());

					// ***
					// Ca c'est juste pour l'affichage
					final Double paybackPeriod;
					if (machinePrices.length > 1) {
						paybackPeriod = machinePrices[1] / (averagePrice * technology.getProductivity() - averageWage);
						dataset.put(keys.paybackPeriod, paybackPeriod);
					} else {
						paybackPeriod = null;
					}
					// ***

					// TODO: Il faudrait demander à la banque son taux +
					// tenir
					// compte de l'inflation + aversion au risque
					if (machinePrices.length == 1) {
						investmentSize = 0;
					} else if (this.factory.getCapacity() == 0) {
						imitation();
						investmentSize = 1;
					} else {
						final float discountRate = this.account.getRealRate();
						investmentSize = getOptimumSize(machinePrices, technology.getProductivity(), capacity,
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

	private void invest(int investmentSize, Long[] machinePrices, long input, Supply[] supplies) {
		if (investmentSize > machinePrices.length - 1) {
			throw new IllegalArgumentException("Investment size is " + investmentSize + " but there is only "
					+ (machinePrices.length - 1) + " machines available.");
		}
		long investmentCost = machinePrices[investmentSize] + 2 * investmentSize;
		final long autofinancement = (long) (investmentCost * (1f - this.targetDebtRatio));
		final long newLongTermLoan = investmentCost - autofinancement;
		if (newLongTermLoan > 0) {
			this.account.borrow(newLongTermLoan, consts.longTerm, true);
		}
		if (this.account.getAmount() < investmentCost) {
			this.account.borrow(investmentCost - this.account.getAmount(), consts.shortTerm, true);
		}

		long requiredVolume = investmentSize * input;

		final Commodities stuff = new FinishedGoods();

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
			final Commodities purchase = supply.purchase(purchaseVolume,
					this.account.issueCheque(supply.getSupplier(), expense));
			stuff.add(purchase);
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
		final Machine[] newMachines = getNewMachines(investmentSize, stuff, technology);
		// TODO: vérifier que stuff a été consommé.
		this.factory.expandCapacity(newMachines);
		this.dataset.put(keys.investmentSize, investmentSize);
		this.dataset.put(keys.investmentVolume, investmentVolume);
		this.dataset.put(keys.investmentValue, investmentCost);

	}

	/**
	 * Mutates
	 * 
	 * @param mut
	 *            the size of the mutation.
	 */
	private void mutation(float mut) {
		this.targetDebtRatio += (float) (mut * getRandom().nextGaussian());
		if (this.targetDebtRatio > 1) {
			this.targetDebtRatio = 1f;
		} else if (this.targetDebtRatio < 0) {
			this.targetDebtRatio = 0f;
		}
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
	private JobContract newJobContract(final Worker worker, final long wage, final int term) {
		final JobContract jobContract = new JobContract() {

			/**
			 * The end of the contract.
			 */
			private int end = getPeriod() + term;

			/**
			 * The start of this contract.
			 */
			final private int start = getPeriod();

			@Override
			public void breach() {
				end = getPeriod();
			}

			@Override
			public int getStart() {
				return this.start;
			}

			@Override
			public long getWage() {
				return wage;
			}

			@Override
			public Worker getWorker() {
				return worker;
			}

			@Override
			public boolean isValid() {
				return this.end > getPeriod();
			}

		};
		return jobContract;
	}

	private void openning() {
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		if (this.period == null) {
			this.period = this.getPeriod();
		} else {
			this.period++;
			if (this.period != getPeriod()) {
				throw new RuntimeException("Bad period");
			}
		}
		this.dataset.open();
		this.factory.open();
		this.capitalManager.open();
		this.salesManager.open();
		this.workforceManager.open();
		this.factory.setInventoryNormalLevel(consts.inventoryNormalLevel);
		this.imitations = 0;
		if (getRandom().nextFloat() > consts.stability) {
			mutation(consts.mutation);
		}
	}

	/**
	 * Updates the data.
	 */
	private void updateData() {

		this.dataset.put(keys.count, 1);

		this.dataset.put(keys.capacity, this.factory.getCapacity());
		this.dataset.put(keys.productionMax, this.factory.getPotentialOutput());

		this.dataset.put(keys.salesValue, this.salesManager.salesValue);
		this.dataset.put(keys.salesVolume, this.salesManager.salesVolume);
		this.dataset.put(keys.supplyVolume, this.salesManager.supplyVolume);
		this.dataset.put(keys.supplyValue, this.salesManager.supplyValue);
		this.dataset.put(keys.salesCosts, this.salesManager.salesValueAtCost);
		this.dataset.put(keys.grossProfit, this.salesManager.grossProfit);

		// ****

		this.memoryOfSalesVolume.add(this.salesManager.salesVolume);
		this.memoryOfSalesValue.add(this.salesManager.salesValue);
		this.memoryOfWagebill.add(this.workforceManager.wagebill);
		this.memoryOfWorkforce.add(this.workforceManager.workforce.size());
		this.memoryOfGrossProfit.add(this.salesManager.grossProfit);

		final long newFactoryValue = this.factory.getValue();
		final long netInvestment = newFactoryValue - this.factoryValueAtTheEndOfThePeriod;

		this.factoryValueAtTheEndOfThePeriod = newFactoryValue;
		this.dataset.put(keys.imitation, this.imitations);
		// this.dataset.put("netInvestment", netInvestment);
		// this.dataset.put("targetDebtRatio", this.targetDebtRatio);

		this.dataset.put(keys.debtRatioTarget, this.targetDebtRatio);

		final double grossProfit = this.memoryOfGrossProfit.getSum();
		final double repayment = this.memoryOfDebtRepayment.getSum();
		final double interest = this.memoryOfInterest.getSum();

		if (grossProfit > repayment + interest) {
			this.dataset.put(keys.hedge, 1);
			this.dataset.put(keys.speculative, 0);
			this.dataset.put(keys.ponzi, 0);
		} else if (grossProfit < interest) {
			this.dataset.put(keys.hedge, 0);
			this.dataset.put(keys.speculative, 0);
			this.dataset.put(keys.ponzi, 1);
		} else {
			this.dataset.put(keys.hedge, 0);
			this.dataset.put(keys.speculative, 1);
			this.dataset.put(keys.ponzi, 0);
		}

	}

	@Override
	public void close() {
		Jamel.notUsed();
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Long getBookValue() {
		return this.capitalManager.getCapital();
	}

	@Override
	public Double getData(int dataIndex, int t) {
		return this.dataset.getData(dataIndex, t);
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
		return this.factory.getCapacity();
	}

	@Override
	public Supply getSupply() {
		return this.salesManager.getSupply();
	}

	@Override
	public long getValueOfAssets() {
		return factory.getValue() + account.getAmount();
	}

	@Override
	public long getValueOfLiabilities() {
		return this.account.getDebt();
	}

	@Override
	public boolean isCancelled() {
		Jamel.notUsed();
		return false;
	}

	@Override
	public boolean isSolvent() {
		return this.capitalManager.isSolvent();
	}

	@Override
	public void open() {
		Jamel.notUsed();
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}

// ***