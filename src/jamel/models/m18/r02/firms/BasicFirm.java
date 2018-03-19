package jamel.models.m18.r02.firms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r02.data.AgentDataset;
import jamel.models.m18.r02.data.BasicAgentDataset;
import jamel.models.m18.r02.data.BasicPeriodDataset;
import jamel.models.m18.r02.data.PeriodDataset;
import jamel.models.m18.r02.markets.BasicLaborMarket;
import jamel.models.m18.r02.util.Equity;
import jamel.models.m18.r02.util.Shareholder;
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

/*
 * 2018-03-10
 * jamel/models/m18/r01/firms/BasicFirm.java
 * 
 * 2018-03-05
 * jamel/models/m18/q06/firms/BasicFirm5.java
 * Une firme faite pour travailler avec BasicLaborMarket3 :
 * Elle connait son marché du travail, elle fait appel à lui quand elle veut
 * copier les salaires,
 * ce qui lui permet de copier les salaires d'une firme d'un autre secteur.
 * 
 * 2018-03-04
 * jamel/models/m18/q06/firms/BasicFirm4.java
 * On désactive la supervision des salaires.
 * But: comprendre la dynamique de la courbe de Beveridge.
 * 
 * 2018-03-02
 * jamel/models/m18/q05/firms/BasicFirm3.java
 * Modification du comportement de fixation des salaires.
 * On tente de supprimer le taux normal d'emplois vacants.
 * 
 * 2018-02-16
 * jamel/models/m18/q04/firms/BasicFirm2.java
 * On essaie de mettre les actionnaires à contribution après les faillites.
 * Introduction de titres de propriété.
 * 
 * 2018-02-09
 * jamel/models/m18/q03/firms/BasicFirm2.java
 * Copie de la firme précédente dans laquelle on va essayer d'insérer des
 * éléments de la firme du modèle icc.
 * 
 * 2018-02-09
 * jamel/models/m18/q03/firms/BasicFirm.java
 * Nouveau nom de la firme précédente.
 * 
 * 2018-02-02
 * jamel/models/m18/q02/firms/BasicFirm32.java
 * Ajout d'une mémoire des suppliers de biens capitaux.
 * 
 * 2018-01-27 jamel/models/m18/q01/firms/BasicFirm3.java
 * Mutation de BasicFirm.
 * On essaie les effets d'un markup évolutionniste.
 * 
 * 2018-01-19 jamel/models/m18/q01/firms/BasicFirm.java
 * 
 * 2017-11-08: refactoring
 * 
 * 2017-11-06: Mutation de BasicFirm5
 * Procède à la dépréciation dans la phase d'ouverture.
 * 
 * 2017-10-16: Mutation de BasicFirm3.
 * Pour permettre une mise sous tutelle pendant les première périodes.
 */

/**
 * A basic firm.
 */
public class BasicFirm extends JamelObject implements Firm {

	/**
	 * The sales manager.
	 */
	@SuppressWarnings("javadoc")
	private class SalesManager {

		private long salesValue = 0;

		private long salesValueAtCost = 0;

		private long salesVolume = 0;

		private Supply supply = null;

		private long supplyValue = 0;

		private long supplyVolume = 0;

		private void createSupply() {
			final int validPeriod = getPeriod();
			supplyVolume = factory.getInventories().getVolume();

			updatePrice();

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
				if (price == null) {
					throw new RuntimeException("Price is null.");
				}
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
					if (price == null) {
						throw new RuntimeException("Price is null.");
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
					if (price == null) {
						throw new RuntimeException("Price is null.");
					}
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
					this.volume -= demand;
					salesValue += cheque.getAmount();
					salesVolume += demand;
					final Commodities sales = factory.getInventories().take(demand);
					salesValueAtCost += sales.getValue();
					sales.setValue(cheque.getAmount());
					account.deposit(cheque);
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
			this.salesValue = 0;
			this.salesValueAtCost = 0;
			this.salesVolume = 0;
			this.supplyValue = 0;
			this.supplyVolume = 0;
		}

	}

	/**
	 * A workforce manager.
	 */
	@SuppressWarnings("javadoc")
	class WorkforceManager {

		/** The wage offered. */
		private Double highWage = null;

		/** The job offer. */
		private JobOffer jobOffer = null;

		private Integer jobOpenings;

		/** The wage offered. */
		private Double lowWage = null;

		/** The manpower target. */
		private Integer manpowerTarget = null;

		/** Number of job vacancies in the current period. */
		private Integer vacancies = null;

		/** The wage offered. */
		Double wage = null;

		private void close() {
			BasicFirm.this.periodDataset.put(keys.workforce, workforce.size());
			BasicFirm.this.periodDataset.put(keys.vacancies, this.vacancies);
			BasicFirm.this.periodDataset.put(keys.vacancyRatio,
					(factory.getCapacity() != 0) ? ((double) this.vacancies) / this.jobOpenings : null);
		}

		private Double copyWage() {
			/*
			 * 2016-03-16 : Renvoie le salaire offert par une firme de plus
			 * grande taille.
			 */
			Double result = null;
			if (cons.observations > 0) {
				// @SuppressWarnings("unchecked")
				for (int i = 0; i < cons.observations; i++) {
					final BasicFirm firm = (BasicFirm) laborMarket.selectEmployer();
					if (firm.factory.getCapacity() > factory.getCapacity()) {
						final Double newWage = firm.workforceManager.wage;
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

		/**
		 * Creates a new job offer.
		 */
		private void newJobOffer() {
			if (vacancies < 0) {
				throw new RuntimeException("Negative number of vacancies");
			}
			if (jobOffer != null) {
				throw new RuntimeException("Job offer should be null");
			}

			if (vacancies != 0) {
				jobOffer = new JobOffer() {

					final private long jobWage = (long) Math.floor(wage);

					final private int validity = getPeriod();

					@Override
					public JobContract apply(final Worker worker) {
						if (validity != getPeriod()) {
							throw new RuntimeException("This job offer is out of date.");
						}
						if (!(vacancies > 0)) {
							throw new RuntimeException("No vacancy.");
						}
						vacancies--;

						final int term;
						if (cons.jobContractMax == cons.jobContractMin) {
							term = cons.jobContractMin;
						} else {
							term = cons.jobContractMin + getRandom().nextInt(cons.jobContractMax - cons.jobContractMin);
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
						return vacancies;
					}
				};
			}
		}

		private void open() {
			this.jobOffer = null;
			this.manpowerTarget = null;
			this.vacancies = null;
			this.jobOpenings = null;
		}

		private void payWorkers() {
			long wagebill = 0;

			if (workforce.getPayroll() > account.getAmount()) {
				account.borrow(workforce.getPayroll() - account.getAmount(), cons.shortTerm, false);
			}
			if (account.getAmount() < workforce.getPayroll()) {
				throw new RuntimeException("Production is not financed.");
			}

			for (JobContract contract : workforce) {
				if (!contract.isValid()) {
					throw new RuntimeException("Invalid job contract.");
				}
				if (contract.getWage() > account.getAmount()) {
					final String message = "Not enough money.";
					Jamel.println("***");
					Jamel.println(message);
					Jamel.println("Wage to be paid: " + contract.getWage());
					Jamel.println("Account amount: " + account.getAmount());
					Jamel.println();
					throw new RuntimeException(message);
				}
				contract.getWorker().acceptPayCheque(account.issueCheque(contract.getWorker(), contract.getWage()));
				wagebill += contract.getWage();
			}
			BasicFirm.this.periodDataset.put(keys.wageBill, wagebill);
		}

		/**
		 * A trial-and-error procedure for wage adjusting.
		 */
		private void updateWage() {

			final Double recentVacancies;

			// 2018-03-02: Suppression du taux normal d'emplois vacants.
			// on désactive le salaire fixe pendant la période de supervision.
			// if (getPeriod() < K.supervision || this.wage == null) {
			if (this.wage == null) {
				this.wage = cons.wageInitialValue;
				this.highWage = this.wage * (1f + cons.wageFlexibility);
				this.lowWage = this.wage * (1f - cons.wageFlexibility);
				recentVacancies = null;

			} else {

				recentVacancies = agentDataset.sum(keys.vacancies, 3);

				// Imitation "non directionnelle"

				final Double newWage = copyWage();

				if (newWage != null) {
					this.wage = newWage;
					this.highWage = this.wage * (1f + cons.wageFlexibility);
					this.lowWage = this.wage * (1f - cons.wageFlexibility);
				} else if (recentVacancies == 0) {
					this.highWage = this.wage;
					this.wage = this.lowWage + getRandom().nextFloat() * (this.wage - this.lowWage);
					this.lowWage = this.lowWage * (1f - cons.wageFlexibility);
				} else {
					this.lowWage = this.wage;
					this.wage = this.wage + getRandom().nextFloat() * (this.highWage - this.wage);
					this.highWage = this.highWage * (1f + cons.wageFlexibility);
				}

			}

			periodDataset.put(keys.wage, wage);
			periodDataset.put(keys.lowWage, lowWage);
			periodDataset.put(keys.highWage, highWage);
			periodDataset.put(keys.recentVacancies, recentVacancies);
		}

		private void updateWorkforce() {
			workforce.cleanUp();
			if (utilizationRateTargeted == null) {
				utilizationRateTargeted = cons.initialUtilizationRate;
			}
			manpowerTarget = Math.min(factory.getCapacity(),
					Math.round(factory.getCapacity() * utilizationRateTargeted));
			if (manpowerTarget <= workforce.size()) {
				if (manpowerTarget < workforce.size()) {
					workforce.layoff(workforce.size() - manpowerTarget);
				}
				jobOpenings = manpowerTarget - workforce.size();
				if (jobOpenings != 0) {
					throw new RuntimeException("Inconsistency");
				}
			} else {
				jobOpenings = manpowerTarget - workforce.size();
			}
			BasicFirm.this.periodDataset.put(keys.jobOffers, jobOpenings);
			BasicFirm.this.periodDataset.put(keys.workforceTarget, manpowerTarget);
			this.vacancies = jobOpenings;
			this.newJobOffer();
		}

		void setWage(double wage) {
			this.wage = wage;
			this.highWage = this.wage * (1f + cons.wageFlexibility);
			this.lowWage = this.wage * (1f - cons.wageFlexibility);
		}

	}

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
	 * The data keys.
	 */
	protected static final FirmKeys keys = FirmKeys.getInstance();

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

		final List<Long> priceList = new ArrayList<>();

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
				((BasicFirm) agent).open();
			};
			break;
		case "pay_dividend":
		case "payDividends":
			action = (agent) -> {
				((BasicFirm) agent).payDividend();
			};
			break;
		case "plan_production":
		case "planProduction":
			action = (agent) -> {
				final BasicFirm firm = (BasicFirm) agent;
				firm.checkSolvency();
				firm.workforceManager.updateWage();
				firm.updateCapacityUtilizationTarget();
				firm.workforceManager.updateWorkforce();
			};
			break;
		case "payWages":
			action = (agent) -> {
				final BasicFirm firm = (BasicFirm) agent;
				firm.workforceManager.payWorkers();
			};
			break;
		case "production":
			action = (agent) -> {
				final BasicFirm firm = (BasicFirm) agent;
				firm.factory.production(firm.workforce);
				firm.salesManager.createSupply();
			};
			break;
		case "investment":
			Jamel.notUsed();
			// 2018-03-10: l'investissement est appelé par le marché des biens
			// d'investissement
			action = (agent) -> {
				((BasicFirm) agent).invest();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicFirm) agent).close();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * Returns the data keys.
	 * 
	 * @return the data keys.
	 */
	static public DataKeys getDataKeys() {
		return keys;
	}

	/**
	 * 
	 */
	private long canceledDebt = 0;

	/**
	 * To count the number of debt cancellation since the start of the period (0
	 * or 1).
	 */
	private int debtCancellation = 0;

	/**
	 * To count the number of debt cancellation since the start of this firm.
	 */
	private int debtCancellationCount = 0;

	/**
	 * Is the dividend distribution authorized?
	 */
	private boolean divid = true;

	/** The dividend paid. */
	private Long dividend;

	/** A flag that indicates if the data of this firm is to be exported. */
	@SuppressWarnings("unused")
	private boolean exportData;

	/**
	 * The labor market.
	 */
	final private BasicLaborMarket laborMarket;

	/** The name of this firm. */
	final private String name;

	/** A flag that indicates if this firm is open or not. */
	private boolean open;

	/**
	 * The title of ownership of this firm.
	 */
	final private List<Equity> ownership = new LinkedList<>();

	/** The current period. */
	private Integer period = null;

	/** The price. */
	private Double price;

	/**
	 * The volume of the production targeted.
	 */
	private double productionVolumeTarget = 0;

	/**
	 * The regular suppliers of investment goods.
	 */
	final private LinkedList<Supplier> regularSuppliers = new LinkedList<>();

	/** The marketing manager. */
	final private SalesManager salesManager = new SalesManager();

	/** The supplier sector. */
	private Sector supplierSector = null;

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

	/** The workforce. */
	final private Workforce workforce = new Workforce();

	/** The account. */
	protected final Account account;

	/** The dataset of the agent. */
	protected final AgentDataset agentDataset = new BasicAgentDataset(this);

	/**
	 * The constants.
	 */
	final protected FirmConstants cons;

	/** The factory. */
	protected final BasicFactory factory;

	/**
	 * To count the imitations since the start of the period.
	 */
	protected int imitations = 0;

	/**
	 * The date of the last imitation.
	 */
	protected int lastImitiation = 0;

	/**
	 * The markup
	 */
	protected float markup = 0;

	/** The period data of the agent. */
	protected PeriodDataset periodDataset = null;

	/** The sector. */
	protected final Sector sector;

	/**
	 * The target debt ratio.
	 */
	protected Float targetDebtRatio = null;

	/** The employer behavior. */
	protected final WorkforceManager workforceManager = new WorkforceManager();

	/**
	 * Creates a new basic firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public BasicFirm(Sector sector, int id) {

		// TODO this(sector, id, new
		// BasicFactory(sector.getParameters().get("production"),
		// sector.getSimulation()));
		// Je ne le fais pas ici pour garder intacts les résultats mais c'est à
		// faire dans le prochain modèle.

		super(sector.getSimulation());
		this.name = "Firm_" + id;
		this.sector = sector;

		final Parameters parameters = this.sector.getParameters();

		this.cons = new FirmConstants(parameters);

		this.markup = 1.f + this.cons.initialMarkupMin
				+ getRandom().nextFloat() * (this.cons.initialMarkupMax - this.cons.initialMarkupMin);

		this.account = ((Bank) this.getSimulation().getSector(this.cons.banks).selectList(1).get(0)).openAccount(this);

		final float min = parameters.getFloat("debtRatio.target.initialValue.min");
		final float max = parameters.getFloat("debtRatio.target.initialValue.max");
		this.targetDebtRatio = min + (max - min) * this.getRandom().nextFloat();

		this.factory = new BasicFactory(parameters.get("production"), this.getSimulation());

		this.laborMarket = (BasicLaborMarket) this.getSimulation().getSector(this.cons.laborMarket);

	}

	/**
	 * Creates a new basic firm.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 * @param factory
	 *            the factory.
	 */
	BasicFirm(Sector sector, int id, BasicFactory factory) {
		super(sector.getSimulation());
		this.name = "Firm_" + id;
		this.sector = sector;

		final Parameters parameters = this.sector.getParameters();

		this.cons = new FirmConstants(parameters);

		this.markup = 1.f + this.cons.initialMarkupMin
				+ getRandom().nextFloat() * (this.cons.initialMarkupMax - this.cons.initialMarkupMin);

		this.account = ((Bank) this.getSimulation().getSector(this.cons.banks).selectList(1).get(0)).openAccount(this);

		final float min = parameters.getFloat("debtRatio.target.initialValue.min");
		final float max = parameters.getFloat("debtRatio.target.initialValue.max");
		this.targetDebtRatio = min + (max - min) * this.getRandom().nextFloat();

		this.factory = factory;

		this.laborMarket = (BasicLaborMarket) this.getSimulation().getSector(this.cons.laborMarket);

	}

	/**
	 * Checks the solvency of this firm.
	 */
	private void checkSolvency() {
		if (this.canceledDebt != 0) {
			throw new RuntimeException("Cancelled debt should be nil.");
		}
		final long assets = account.getAmount() + factory.getValue();
		final long debt = account.getDebt();
		// if (debt != 0 && debt > assets & getPeriod() >= this.K.supervision) {
		if (debt != 0 && debt > assets && this.getPeriod() > 36) {
			// TODO 36 should be a parameter (=patience)

			this.debtCancellation++;
			this.debtCancellationCount++;
			final long excess = debt - assets;
			try {
				account.cancelDebt(excess);
				this.canceledDebt = excess;
			} catch (Exception e) {
				Jamel.println();
				Jamel.println("assets", assets);
				Jamel.println("account.getAmount()", account.getAmount());
				Jamel.println("factory.getValue()", factory.getValue());
				Jamel.println("debt", debt);
				Jamel.println("excess", excess);
				Jamel.println();
				throw new RuntimeException(e);
			}

			imitation();
			// On interdit à la firme de distribuer des dividendes.
			this.divid = false;
		} else {
			// On autorise la firme à distribuer des dividendes.
			this.divid = true;
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
	 * Computes and returns the capital of this firm.
	 * 
	 * @return the capital of this firm.
	 */
	private long getCapital() {
		return factory.getValue() + account.getAmount() - account.getDebt();
	}

	/**
	 * Computes and returns the capital target of this firm.
	 * 
	 * @return the capital target of this firm.
	 */
	private long getCapitalTarget() {
		final long assets = account.getAmount() + factory.getValue();
		final long capitalTarget = (targetDebtRatio == 0) ? assets
				: (targetDebtRatio == 1) ? 0 : (long) (assets * (1f - targetDebtRatio));
		if (capitalTarget < 0 || capitalTarget > assets) {
			Jamel.println();
			Jamel.println("assets", assets);
			Jamel.println("targetDebtRatio", targetDebtRatio);
			Jamel.println("capitalTarget", capitalTarget);
			throw new RuntimeException("Bad capital target");
		}
		return capitalTarget;
	}

	/**
	 * Computes and returns the normal volume of the inventories.
	 * 
	 * @return the normal volume of the inventories.
	 */
	private double getInventoryNomalVolume() {
		return cons.inventoryNormalLevel * factory.getProductionAtFullCapacity();
	}

	/**
	 * Returns the target value of the liabilities.
	 * 
	 * @return the target value of the liabilities.
	 */
	private long getLiabilitiesTarget() {
		final long assets = account.getAmount() + factory.getValue();
		final long liabilitiesTarget = assets - getCapitalTarget();
		if (liabilitiesTarget < 0) {
			throw new RuntimeException("liabilitiesTarget < 0");
		}
		return liabilitiesTarget;
	}

	/**
	 * Invests.
	 * 
	 * @param investmentSize
	 *            the number of new machines to be created.
	 * @param machinePrices
	 *            the prices of the machines.
	 * @param input
	 *            the volume of stuff required as input in the process of
	 *            machine creation.
	 * @param supplies
	 *            the supplies of investment goods.
	 */
	private void invest(int investmentSize, Long[] machinePrices, long input, Supply[] supplies) {
		if (investmentSize > machinePrices.length - 1) {
			throw new IllegalArgumentException("Investment size is " + investmentSize + " but there is only "
					+ (machinePrices.length - 1) + " machines available.");
		}
		long investmentCost = machinePrices[investmentSize] + 2 * investmentSize;
		final long autofinancement = (long) (investmentCost * (1f - this.targetDebtRatio));
		final long newLongTermLoan = investmentCost - autofinancement;
		if (newLongTermLoan > 0) {
			this.account.borrow(newLongTermLoan, cons.longTerm, true);
		}
		if (this.account.getAmount() < investmentCost) {
			this.account.borrow(investmentCost - this.account.getAmount(), cons.shortTerm, true);
		}

		long requiredVolume = investmentSize * input;

		final Commodities stuff = new BasicGoods(this.factory.getQualityOfInputForTheCreationOfANewMachine());

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
				this.account.borrow(expense - this.account.getAmount(), cons.shortTerm, true);
				/*Jamel.println("this.account.getAmount(): " + this.account.getAmount());
				Jamel.println("expense: " + expense);
				throw new RuntimeException("Not enough money.");*/
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
		this.factory.expandCapacity(investmentSize, stuff);
		// TODO: vérifier que stuff a été consommé.
		this.periodDataset.put(keys.investmentSize, investmentSize);
		this.periodDataset.put(keys.investmentVolume, investmentVolume);
		this.periodDataset.put(keys.investmentValue, investmentCost);

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

	/**
	 * Pays the dividends to the owners of the firm.
	 */
	private void payDividend() {
		final long cash = account.getAmount();
		final double capital = getCapital();
		final double averageIncome = agentDataset.average(keys.netProfit, 12);

		long toBeDistributed = (long) (averageIncome * this.cons.retentionRate);

		if (capital < 0 || toBeDistributed < 0 || divid == false) {
			toBeDistributed = 0;
		} else if (toBeDistributed > cash) {
			toBeDistributed = cash;
		}

		if (toBeDistributed > ownership.size()) {
			final long newDividend = toBeDistributed / ownership.size();
			for (final Equity title : ownership) {
				final Shareholder shareholder = title.getOwner();
				shareholder.acceptDividendCheque(account.issueCheque(shareholder, newDividend));
			}
			dividend = newDividend * ownership.size();
		} else {
			dividend = 0l;
		}
	}

	/**
	 * Updates the capacity utilization target.
	 */
	private void updateCapacityUtilizationTarget() {

		if (this.getPeriod() < cons.supervision) {
			this.utilizationRateTargeted = 0.9f;
			this.productionVolumeTarget = factory.getProductionAtFullCapacity() * this.utilizationRateTargeted;
		} else {

			final double salesAverage = this.agentDataset.average(keys.salesVolume, 12);
			final double inventoriesVolume = factory.getInventories().getVolume();
			final double inventoriesVolumeNormal = factory.getProductionAtFullCapacity()
					* this.cons.inventoryNormalLevel;
			this.productionVolumeTarget = salesAverage
					+ (inventoriesVolumeNormal - inventoriesVolume) / this.cons.productionAdjustment;

			final double potentialOutput = factory.getProductionAtFullCapacity();
			final double ratio;
			if (potentialOutput != 0) {
				ratio = this.productionVolumeTarget / potentialOutput;
			} else {
				ratio = 0;
			}

			if (ratio > 1) {
				this.utilizationRateTargeted = 1f;
			} else if (ratio < 0) {
				this.utilizationRateTargeted = 0f;
			} else {
				this.utilizationRateTargeted = (float) ratio;
			}
		}

		this.periodDataset.put(keys.utilizationTarget, this.utilizationRateTargeted);
	}

	/**
	 * Updates the price.
	 */
	private void updatePrice() {
		final Double unitCost = factory.getInventories().valuePerUnit();
		if (unitCost != null) {
			if (unitCost.isNaN()) {
				throw new RuntimeException("Unit cost is not a number");
			}
			this.price = this.markup * unitCost;
		}
		this.periodDataset.put(keys.price, this.price);
		this.periodDataset.put(keys.unitCost, unitCost);
	}

	/**
	 * Returns an array of supplies, sorted by price in ascending order.
	 * 
	 * @return an array of supplies, sorted by price in ascending order.
	 */
	protected Supply[] getSupplies() {

		// On commence par initialiser (si besoin est) les secteur qui va
		// fournir les biens capitaux.
		if (this.supplierSector == null) {
			this.supplierSector = this.getSimulation().getSector(this.cons.suppliers);
			// Supplier sector ne peut pas être créé à la création de la firme,
			// car il n'existe peut-être pas encore.
		}

		// On crée une liste d'offres vides.
		final ArrayList<Supply> list = new ArrayList<>(this.cons.nSupplier);

		// On récupère les offres des fournisseurs habituels.

		for (final Agent supplier : this.regularSuppliers) {
			final Supply supply = ((Supplier) supplier).getSupply();
			if (!supplier.equals(this) && supply != null && !supply.isEmpty()) {
				list.add(supply);
			}
		}

		final int i = cons.nSupplier - list.size();

		if (i > 0) {
			@SuppressWarnings("unchecked")
			final List<? extends Supplier> suppliers = (List<? extends Supplier>) this.supplierSector.selectList(i);
			for (final Agent supplier : suppliers) {
				final Supply supply = ((Supplier) supplier).getSupply();
				if (!supplier.equals(this) && supply != null && !supply.isEmpty() && !list.contains(supply)) {
					list.add(supply);
				}
			}
		}

		final Supply[] supplies = list.toArray(new Supply[list.size()]);
		Arrays.sort(supplies, supplyComparator);
		this.regularSuppliers.clear();
		for (Supply supply : supplies) {
			this.regularSuppliers.add(supply.getSupplier());
		}
		if (this.regularSuppliers.size() == this.cons.nSupplier) {
			this.regularSuppliers.removeLast();
		}
		return supplies;
	}

	/**
	 * Imitates an other firm (copies its target debt ratio).
	 */
	protected void imitation() {
		final int now = this.getPeriod();
		if (now > cons.supervision) {
			if (now > this.lastImitiation + 12) {
				BasicFirm firm = (BasicFirm) sector.select();
				this.lastImitiation = now;
				this.imitations = 1;
				this.workforceManager.setWage(firm.workforceManager.wage);
				this.targetDebtRatio = firm.targetDebtRatio;
				this.markup = firm.markup;
			}
		}
	}

	/**
	 * Mutates.
	 */
	protected void mutation() {
		if (this.getPeriod() > cons.supervision) {
			this.targetDebtRatio += (float) (cons.mutation * getRandom().nextGaussian());
			this.markup += (float) (cons.mutation * getRandom().nextGaussian());
			if (this.targetDebtRatio > 1) {
				this.targetDebtRatio = 1f;
			} else if (this.targetDebtRatio < 0) {
				this.targetDebtRatio = 0f;
			}
		}
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {

		final long grossProfit = this.salesManager.salesValue - this.salesManager.salesValueAtCost;

		this.periodDataset.put(keys.count, 1);

		this.periodDataset.put(keys.markup, this.markup - 1);

		this.periodDataset.put(keys.inventoriesVolume, this.factory.getInventories().getVolume());
		this.periodDataset.put(keys.inventoriesNormalVolume, getInventoryNomalVolume());
		this.periodDataset.put(keys.inventoriesLevel,
				this.factory.getInventories().getVolume() / getInventoryNomalVolume());
		this.periodDataset.put(keys.inProcessValue, this.factory.getInProcessValue());
		this.periodDataset.put(keys.inventoriesValue, this.factory.getInventories().getValue());
		this.periodDataset.put(keys.machineryValue, this.factory.getMachineryValue());
		this.periodDataset.put(keys.productionMax, this.factory.getProductionAtFullCapacity());

		this.periodDataset.put(keys.salesValue, this.salesManager.salesValue);
		this.periodDataset.put(keys.salesVolume, this.salesManager.salesVolume);
		this.periodDataset.put(keys.supplyVolume, this.salesManager.supplyVolume);
		this.periodDataset.put(keys.supplyValue, this.salesManager.supplyValue);
		this.periodDataset.put(keys.salesCosts, this.salesManager.salesValueAtCost);
		this.periodDataset.put(keys.grossProfit, grossProfit);

		this.periodDataset.put(keys.canceledDebt, this.canceledDebt);
		this.periodDataset.put(keys.debtCancellation, this.debtCancellation);
		this.periodDataset.put(keys.debtCancellationCount, this.debtCancellationCount);

		// ***

		final long cash = account.getAmount();
		final long assets = factory.getValue() + cash;
		final long liabilities = account.getDebt();
		final long capital = assets - liabilities;

		this.periodDataset.put(keys.debtRatio, ((double) liabilities) / assets);
		this.periodDataset.put(keys.weightedDebt, ((double) liabilities * liabilities) / assets);

		this.periodDataset.put(keys.money, cash);
		this.periodDataset.put(keys.assets, assets);
		this.periodDataset.put(keys.liabilities, liabilities);
		this.periodDataset.put(keys.equities, capital);
		this.periodDataset.put(keys.tangibleAssets, factory.getValue());

		this.periodDataset.put(keys.shortTermDebt, account.getShortTermDebt());
		this.periodDataset.put(keys.longTermDebt, account.getLongTermDebt());
		this.periodDataset.put(keys.overdueDebt, account.getOverdueDebt());

		this.periodDataset.put(keys.dividends, this.dividend);
		this.periodDataset.put(keys.interests, account.getInterests());
		this.periodDataset.put(keys.debtService, account.getDebtService());

		this.periodDataset.put(keys.liabilitiesTarget, this.getLiabilitiesTarget());

		// ***

		final long netProfit = grossProfit - account.getInterests() - factory.getDepreciation();
		this.periodDataset.put(keys.netProfit, netProfit);

		/*
		final double returnOnEquity = this.netProfitMemory.getSum() / capital;
		final double returnOnAssets = this.netProfitMemory.getSum() / assets;
		final double solvency = this.netProfitMemory.getSum() / liabilities;
		*/

		// ****

		this.periodDataset.put(keys.imitation, this.imitations);
		this.periodDataset.put(keys.debtRatioTarget, this.targetDebtRatio);

		final double grossProfit6 = this.agentDataset.sum(keys.grossProfit, 6);
		final double interest6 = this.agentDataset.sum(keys.interests, 6);
		final double debtService6 = this.agentDataset.sum(keys.debtService, 6);

		if (grossProfit6 > debtService6) {
			this.periodDataset.put(keys.hedge, 1);
			this.periodDataset.put(keys.speculative, 0);
			this.periodDataset.put(keys.ponzi, 0);
		} else if (grossProfit6 < interest6) {
			this.periodDataset.put(keys.hedge, 0);
			this.periodDataset.put(keys.speculative, 0);
			this.periodDataset.put(keys.ponzi, 1);
		} else {
			this.periodDataset.put(keys.hedge, 0);
			this.periodDataset.put(keys.speculative, 1);
			this.periodDataset.put(keys.ponzi, 0);
		}

	}

	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;

		this.account.close();

		this.workforceManager.close();

		this.updateData();
		this.agentDataset.put(periodDataset);

		try {
			this.exportData();
		} catch (IOException e) {
			throw new RuntimeException("Error while exporting firm data", e);
		}
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Double getData(int dataIndex, int t) {
		return this.agentDataset.getData(dataIndex, t);
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
		return this.salesManager.getSupply();
	}

	@Override
	public void invest() {

		/*
		 * 2018-03-10 -> public
		 * Permet au marché des biens d'investissement d'appeler cette methode.
		 * Permet de brasser les firmes de différents secteurs dans la phase d'investissement.
		 */

		boolean invest = false;
		final long capital = this.getCapital();
		final long capitalTarget2 = this.getCapitalTarget();

		final double manpower = this.agentDataset.sum(keys.workforce, 12);
		final double salesVolume = this.agentDataset.sum(keys.salesVolume, 12);
		Double averagePrice = null;
		Double averageWage = null;
		if (manpower > 0 && salesVolume > 0) {
			averagePrice = this.agentDataset.sum(keys.salesValue, 12) / salesVolume;
			averageWage = this.agentDataset.sum(keys.wageBill, 12) / manpower;
		}

		int investmentSize = 0;

		final int capacity = this.factory.getCapacity();

		if ((averagePrice != null && averageWage != null) || (capacity == 0)
				|| (this.getPeriod() < cons.supervision && capacity < this.cons.initialCapacity)) {

			// Il faut que la firme ait fonctionné au moins une fois au
			// cours des périodes récentes, pour qu'on puisse calculer un
			// prix moyen et un salaire moyen.

			// Sauf si le nombre de machine = 0, là il faut en racheter une
			// coute que coute.

			if (capacity == 0 || capital > capitalTarget2
					|| (this.getPeriod() < cons.supervision && capacity < this.cons.initialCapacity)) {

				// Il faut que le niveau de capital de la firme soit
				// satisfaisant
				// pour qu'on puisse envisager l'achat de nouvelles machines

				// On récupère une liste d'offres.
				final Supply[] supplies = getSupplies();

				if (supplies.length > 0) {

					// Il faut qu'il y ait au moins 1 offre de 'raw
					// materials'.

					// TODO pour combien de machines au max ? Il faudrait
					// déterminer ça avant l'étape ci-dessous.
					final Long[] machinePrices = getPrices(supplies, this.factory.getInputVolumeForANewMachine());

					if (this.getPeriod() < cons.supervision) {
						investmentSize = Math.min(machinePrices.length - 1,
								capacity < this.cons.initialCapacity ? this.cons.initialCapacity - capacity : 0);
						this.periodDataset.put(keys.r1, investmentSize);
					} else if (machinePrices.length == 1) {
						investmentSize = 0;
					} else if (this.factory.getCapacity() == 0) {
						imitation();
						investmentSize = 1;
					} else {
						if (averagePrice == null || averageWage == null) {
							throw new RuntimeException("Inconsistency");
						}
						final float discountRate = this.account.getRealRate();
						investmentSize = getOptimumSize(machinePrices, (long) this.factory.getProductivity(), capacity,
								this.productionVolumeTarget, averagePrice, averageWage,
								this.factory.getMachineTimeLife(), discountRate);

						// 2018-01-24
						// ici un calcul alternatif de la taille de
						// l'investissement

						// profit brut annuel par unité de production / valeur
						// d'achat d'une unité d production
						// niveau des ventes des 12 derniers mois rapporté à la
						// capacité de production actuelle.
						// taux d'intérêt réel

						// TODO prévoir le cas où les profits sont nuls (ou
						// négatifs), ou la capacité de production est nulle.

						// final double profit = (dataset.sum(keys.salesValue,
						// 12) - dataset.sum(keys.salesCosts, 12));

						/*final double profit = dataset.sum(keys.salesValue, 12) - dataset.sum(keys.salesCosts, 12)
								- dataset.sum(keys.dividends, 12) - dataset.sum(keys.interests, 12);
						if (profit <= 0) {
							investmentSizeAlt = 0;
						} else {
							final double machineryPrice = capacity * getAveragePrice(machinePrices);
							final double profitRate = profit / machineryPrice;
							final double utilisation = dataset.average(keys.salesVolume, 12)
									/ this.factory.getProductionAtFullCapacity() - 0.8;
						
							final double interestCommitments = dataset.sum(keys.interests, 12) / profit;
						
							final float a = 1f;
							final float b = 0f;
							final float c = -0.f;
						
							investmentSizeAlt = Math.min(machinePrices.length - 1, Math.max(0, aleaRound(
									capacity * (a * utilisation + b * profitRate + c * interestCommitments))));
						
						}*/

					}

					/*if (investmentSizeAlt > 0) {
						invest(investmentSizeAlt, machinePrices, this.factory.getInputVolumeForANewMachine(), supplies);
						invest = true;
					}*/

					if (investmentSize > 0) {
						invest(investmentSize, machinePrices, this.factory.getInputVolumeForANewMachine(), supplies);
						invest = true;
					}

					this.periodDataset.put(keys.r2, (investmentSize == machinePrices.length - 1) ? 1 : 0);

				}

			}
		}

		if (!invest) {
			this.periodDataset.put(keys.investmentSize, 0);
			this.periodDataset.put(keys.investmentVolume, 0);
			this.periodDataset.put(keys.investmentValue, 0);
		}

		// this.dataset.put(keys.investmentSizeAlt, investmentSizeAlt);
	}

	@Override
	public boolean isSolvent() {
		return (this.getCapital() >= 0);
	}

	@Override
	public void open() {
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

		this.periodDataset = new BasicPeriodDataset(this);
		this.account.open();
		this.factory.open(periodDataset);
		this.factory.depreciation();

		if (ownership.isEmpty()) {
			final List<? extends Agent> selection = getSector(cons.shareholders).selectList(10);
			for (final Agent agent : selection) {
				final Equity title = new Equity() {

					@Override
					public String getCompanyName() {
						return getName();
					}

					@Override
					public Shareholder getOwner() {
						return (Shareholder) agent;
					}

					@Override
					public long getValue() {
						/*
						 * FIXME plusieurs problèmes ici.
						 * 1- c'est con de recalculer la valeur à chaque fois. Il faudrait la calculer une fois et la mettre en cache.
						 * Problème : quand la calculer ? Il faut que ce soit en fin de période. Mais c'est aussi en fin de période que les
						 * propriétaires ont besoin de la connaître.
						 * Il faudrait donc que ce soit juste avant la fin de la période...
						 * 2- il y a le problème de l'arrondi. Il va y avoir un reste. Ce reste va empêcher la cloture exacte (à l'unité près)
						 *  des matrices de stocks et de flux...
						 */
						return getCapital() / ownership.size();
					}

				};
				((Shareholder) agent).acceptTitle(title);
				ownership.add(title);
			}
		}

		this.dividend = null;
		this.canceledDebt = 0;
		this.debtCancellation = 0;
		this.salesManager.open();
		this.workforceManager.open();
		this.imitations = 0;
		mutation();
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
