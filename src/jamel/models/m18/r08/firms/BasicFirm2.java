package jamel.models.m18.r08.firms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r08.markets.BasicLaborMarket;
import jamel.models.m18.r08.roles.Bank;
import jamel.models.m18.r08.roles.Shareholder;
import jamel.models.m18.r08.roles.Supplier;
import jamel.models.m18.r08.roles.Worker;
import jamel.models.m18.r08.util.Account;
import jamel.models.m18.r08.util.BasicOwnership;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.Commodities;
import jamel.models.m18.r08.util.Equity;
import jamel.models.m18.r08.util.JobContract;
import jamel.models.m18.r08.util.JobOffer;
import jamel.models.m18.r08.util.Supply;
import jamel.util.Agent;
import jamel.util.Parameters;
import jamel.util.Sector;

/*
 * 
 * 2018-04-11
 * jamel/models/m18/r07/firms/BasicFirm2.java
 * On essaie d'introduire les recapitalisations.
 * 
 * 
 * 2018-04-10
 * jamel/models/m18/r05/firms/BasicFirm2.java
 * - meilleure gestion des stocks (lisse l'offre de marchandises)
 * - ajustement des salaires en référence au salaire moyen d'un échantillon
 * - dépréciation des stocks en cas de faillite
 * - ajuste les dividendes versés en fonction du niveau d'endettement
 * 
 * 2018-04-06
 * jamel/models/m18/r05/firms/BasicFirm.java
 * La même que la précédente (BasicFirm4), mais dont certaines fonctions
 * basiques ont été placée dans AbstractFirm.
 * 
 * 2018-04-06
 * jamel/models/m18/r04/firms/BasicFirm4.java
 * Une firme qui limite (1) la distribution de dividendes (2) l'investissement
 * lorsqu'elle est en dessous de son objectif de fonds propres.
 * 
 * 2018-04-01
 * jamel/models/m18/r04/firms/BasicFirm3.java
 * Prend en compte l'overhead expense dans le calcul du cash flow (method
 * invest()).
 * 
 * 2018-03-30
 * jamel/models/m18/r04/firms/BasicFirm2.java
 * Une firme qui a besoin d'overheadlabour.
 * 
 * 2018-03-29
 * jamel/models/m18/r04/firms/BasicFirm.java
 * Fusion de BasicFirm et BasicFirm2.
 * C'est donc une firme qui intègre un comportement d'investissement
 * post-keynésien.
 * 
 * 2018-03-28
 * jamel/models/m18/r03/firms/BasicFirm.java
 * Intègre l'objet Technology, qui permet d'introduire un progrès techologique
 * basique.
 * 
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
public class BasicFirm2 extends AbstractFirm {

	/**
	 * The bank ownership.
	 */
	class FirmOwnership extends BasicOwnership {

		/**
		 * Creates a new ownership for this firm.
		 */
		private FirmOwnership() {
			super(BasicFirm2.this);
		}

		/**
		 * Pays the dividends to the owners.
		 * 
		 * @param amount
		 *            the total amount to be distributed.
		 * @return the effective amount distributed.
		 */
		private long payDividends(long amount) {
			long result = 0;
			if (this.getTotalValue() > 0 && amount > this.size()) {
				for (final Equity title : this.equities) {
					final long newDividend = (amount * title.getValue()) / this.getTotalValue();
					if (newDividend > 1) {
						final Shareholder shareholder = title.getOwner();
						// Jamel.println("BasicFirm2.payDividends",amount,
						// ((float) title.getValue()) / this.getTotalValue(),
						// newDividend);
						shareholder.acceptDividendCheque(BasicFirm2.this.account.issueCheque(shareholder, newDividend));
						result += newDividend;
						if (result > amount) {
							throw new RuntimeException("Inconsistency");
						}
					}
				}
			}
			return result;
		}

	}

	/**
	 * The sales manager.
	 */
	@SuppressWarnings("javadoc")
	class SalesManager {

		private long salesValue = 0;

		private long salesValueAtCost = 0;

		private long salesVolume = 0;

		private Supply supply = null;

		private long supplyValue = 0;

		private long supplyVolume = 0;

		private void createSupply() {
			final int validPeriod = getPeriod();
			// TODO 3 should be a parameter !
			supplyVolume = factory.getInventories().getVolume() / 3;

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
					return BasicFirm2.this;
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
					return "Supply by " + getName() + ": price <" + price + ">, volume <" + volume + ">";
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

		void open() {
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

		/** The job offer. */
		private JobOffer jobOffer = null;

		private Integer jobOpenings;

		/** The manpower target. */
		private Integer manpowerTarget = null;

		/** Number of job vacancies in the current period. */
		private Integer vacancies = null;

		/** The wage offered. */
		Double wage = null;

		float wageFactor = 1;

		private void close() {
			BasicFirm2.this.putData(keys.workforce, workforce.size());
			BasicFirm2.this.putData(keys.vacancies, this.vacancies);
			BasicFirm2.this.putData(keys.jobOpenings, this.jobOpenings);
			BasicFirm2.this.putData(keys.vacancyRatio,
					(factory.getCapacity() != 0) ? ((double) this.vacancies) / this.jobOpenings : null);
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

		void open() {
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
			BasicFirm2.this.putData(keys.wageBill, wagebill);
		}

		/**
		 * The procedure for wage adjusting.
		 */
		private void updateWage() {

			// 2019-04-06
			// Le salaire est ajusté en fonction de la moyenne d'un échantillon.

			if (this.wage == null) {
				this.wage = cons.wageInitialValue;
			} else {

				final Double vacancies1 = agentDataset.getData(keys.vacancies, getPeriod() - 1);
				final Double vacancies2 = agentDataset.getData(keys.vacancies, getPeriod() - 2);
				final Double vacancies3 = agentDataset.getData(keys.vacancies, getPeriod() - 3);
				final Double jobOpenings1 = agentDataset.getData(keys.jobOpenings, getPeriod() - 1);
				final Double jobOpenings2 = agentDataset.getData(keys.jobOpenings, getPeriod() - 2);
				final Double jobOpenings3 = agentDataset.getData(keys.jobOpenings, getPeriod() - 3);

				if (vacancies1 != null && jobOpenings1 != null) {
					if (vacancies1 > 0 && jobOpenings1 > 0) {
						this.wageFactor += 0.0025;
						if (vacancies2 != null && vacancies2 > 0 && jobOpenings2 != null && jobOpenings2 > 0) {
							this.wageFactor += 0.005;
							if (vacancies3 != null && vacancies3 > 0 && jobOpenings3 != null && jobOpenings3 > 0) {
								this.wageFactor += 0.01;
							}
						}
					} else {
						this.wageFactor -= 0.0025;
						if (vacancies2 != null && vacancies2 == 0 && jobOpenings2 != null && jobOpenings2 > 0) {
							this.wageFactor -= 0.005;
							if (vacancies3 != null && vacancies3 == 0 && jobOpenings3 != null && jobOpenings3 > 0) {
								this.wageFactor -= 0.01;
							}
						}
					}
				}

				if (this.wageFactor < 0.75) {
					this.wageFactor = 0.75f;
				} else if (this.wageFactor > 1.5) {
					this.wageFactor = 1.5f;
				}

				// on calcule le salaire moyen du "voisinage"

				double wages = agentDataset.sum(keys.wageBill, 12);
				double employees = agentDataset.sum(keys.workforce, 12);
				for (int i = 0; i < cons.observations; i++) {
					final BasicFirm2 firm = (BasicFirm2) laborMarket.selectEmployer();
					wages += firm.agentDataset.sum(keys.wageBill, 12);
					employees += firm.agentDataset.sum(keys.workforce, 12);
				}
				if (employees > 0) {
					final double averageWage = wages / employees;
					this.wage = averageWage * this.wageFactor;
				}

			}

			putData(keys.wage, wage);
		}

		private void updateWorkforce() {
			workforce.cleanUp();
			if (utilizationRateTargeted == null) {
				utilizationRateTargeted = cons.initialUtilizationRate;
			}
			manpowerTarget = factory.getOverhead()
					+ Math.min(factory.getCapacity(), Math.round(factory.getCapacity() * utilizationRateTargeted));
			if (factory.getCapacity() > 0 && utilizationRateTargeted > 0 && manpowerTarget == 0) {
				manpowerTarget = 1;
			}
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
			BasicFirm2.this.putData(keys.jobOffers, jobOpenings);
			BasicFirm2.this.putData(keys.workforceTarget, manpowerTarget);
			this.vacancies = jobOpenings;
			this.newJobOffer();
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

	static public Consumer<? super Agent> getAction(final String phaseName) {
		return AbstractFirm.getAction(phaseName);
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
	protected long canceledDebt = 0;

	/**
	 * To count the number of debt cancellation since the start of the period (0
	 * or 1).
	 */
	protected int debtCancellation = 0;

	/**
	 * To count the number of debt cancellation since the start of this firm.
	 */
	private int debtCancellationCount = 0;

	/** The dividend paid. */
	protected Long dividends;

	/** A flag that indicates if the data of this firm is to be exported. */
	@SuppressWarnings("unused")
	private boolean exportData;

	/**
	 * The labor market.
	 */
	final private BasicLaborMarket laborMarket;

	/**
	 * The ownership of this firm.
	 */
	protected final FirmOwnership ownership = new FirmOwnership();

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
	protected final SalesManager salesManager = new SalesManager();

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

	/**
	 * The normal rate of utilization of capacities TODO should be a parameter.
	 */
	protected float normalUtilizationRate = 0.85f;

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
	public BasicFirm2(Sector sector, int id) {
		super(sector, id);

		this.factory = new BasicFactory(sector.getParameters().get("production"), sector.getSimulation());
		final Parameters parameters = this.sector.getParameters();

		this.cons = new FirmConstants(parameters);

		this.markup = 1.f + this.cons.initialMarkupMin
				+ getRandom().nextFloat() * (this.cons.initialMarkupMax - this.cons.initialMarkupMin);

		this.account = ((Bank) this.getSimulation().getSector(this.cons.banks).selectList(1).get(0)).openAccount(this);

		final float min = parameters.getFloat("debtRatio.target.initialValue.min");
		final float max = parameters.getFloat("debtRatio.target.initialValue.max");
		this.targetDebtRatio = min + (max - min) * this.getRandom().nextFloat();

		this.laborMarket = (BasicLaborMarket) this.getSimulation().getSector(this.cons.laborMarket);

	}

	private void changeOwnerShip(final long capitalRequirement) {
		// On commence par se débarasser des propriétaires actuels.
		this.ownership.clear();
		/*
		Jamel.println();
		Jamel.println("***");
		Jamel.println("Recapitalisation, objectif: " + capitalRequirement);
		*/
		long remain = capitalRequirement;
		int sampleSize = 5;
		for (int count = 0; count < 5; count++) {
			final Agent[] selection = this.getSimulation().getSector(cons.shareholders).selectArray(sampleSize);
			final long amount = Math.max(1, remain / sampleSize);
			// Jamel.println("Passe " + count, "Select " + sampleSize, "Amount "
			// + amount);
			for (int i = 0; i < selection.length; i++) {
				if (selection[i] != null) {
					final Shareholder shareholder = (Shareholder) selection[i];
					final Cheque cheque = shareholder.contribute(this, amount);
					if (cheque != null) {
						if (cheque.getAmount() != amount) {
							throw new RuntimeException("Bad amount");
						}
						this.account.deposit(cheque);
						shareholder.acceptTitle(ownership.issue(shareholder, amount));
						remain -= amount;
						// Jamel.println(shareholder.getName(), amount, "remain
						// " + remain);
					}
				}
			}
			if (remain < capitalRequirement / 10) {
				// Ca suffit, ce qui reste à recolter est négligeable, on sort.
				break;
			}
			sampleSize *= 2;
		}
		if (ownership.isEmpty()) {
			this.initOwnership();
			// throw new RuntimeException("Ownership is empty");
		}
		/*
		Jamel.println("Recapitalisation: attendu: " + capitalRequirement, "obtenu: " + this.getCapital(),
				"taux de satisfaction: " + ((float) this.getCapital()) / capitalRequirement);
		Jamel.println();
		*/
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
	 * Computes and returns the average utilization rate on the last 12
	 * periods.
	 * 
	 * @return the average utilization rate on the last 12 periods.
	 */
	private Float getUtilizationRate() {
		final double salesVolume = this.agentDataset.sum(keys.salesVolume, 12);
		final double productionMax = this.agentDataset.sum(keys.productionMax, 12);
		return (productionMax == 0) ? null : (float) (salesVolume / productionMax);
	}

	/**
	 * Initializes the owners of this bank.
	 */
	protected void initOwnership() {
		final Agent[] selection = this.getSimulation().getSector(cons.shareholders).selectArray(10);
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] != null) {
				final Shareholder agent = (Shareholder) selection[i];
				final Equity title = ownership.issue(agent, 1);
				agent.acceptTitle(title);
			}
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
		this.putData(keys.price, this.price);
		this.putData(keys.unitCost, unitCost);
		/*TODO REMOVE ME 
		 if (this.getPeriod()>1500 && this.price<250) {
			Jamel.println(this.getName(),this.price);
		}*/
	}

	/**
	 * Buys the investment goods required for the creation of specified number
	 * of machines.
	 * 
	 * @param targetMachines
	 *            the number of machines to be created.
	 * @param supplies
	 *            the suplies of investment goods.
	 * @return the goods purchased.
	 */
	protected Commodities buy(final int targetMachines, Supply[] supplies) {

		if (targetMachines == 0) {
			throw new IllegalArgumentException();
		}

		final Commodities purchase = new BasicGoods(
				this.factory.getTechnology().getQualityOfInputForTheCreationOfANewMachine());

		// Ici on doit renouveller exactement les machines
		// manquantes.
		// L'investissement est donc déterminé en termes réels

		final int targetVolume = (int) (targetMachines * this.factory.getTechnology().getInputVolumeForANewMachine()
				- this.factory.getInputVolume());
		this.putData(keys.targetVolume, targetVolume);
		// Maintenant il s'agit d'acheter ce volume, et de financer cet
		// achat.

		for (Supply supply : supplies) {
			final long value;
			final long volume;
			if (supply.getVolume() < targetVolume - purchase.getVolume()) {
				volume = supply.getVolume();
				value = supply.getValue();
			} else {
				volume = targetVolume - purchase.getVolume();
				value = supply.getPrice(volume);
			}
			if (this.account.getAmount() < value) {
				this.account.borrow(value - this.account.getAmount(), 0, false);
			}
			purchase.add(supply.purchase(volume, account.issueCheque(supply.getSupplier(), value)));
			if (purchase.getVolume() == targetVolume) {
				break;
			}
			if (purchase.getVolume() > targetVolume) {
				throw new RuntimeException("Inconsistency");
			}
		}

		return purchase;

	}

	/**
	 * Checks the solvency of this firm.
	 */
	protected void checkSolvency() {
		if (this.canceledDebt != 0) {
			throw new RuntimeException("Cancelled debt should be nil.");
		}
		long assets = account.getAmount() + factory.getValue();
		final long debt = account.getDebt();
		// if (debt != 0 && debt > assets & getPeriod() >= this.K.supervision) {
		if (debt != 0 && debt > assets && this.getPeriod() > 36) {
			// TODO 36 should be a parameter (=patience)

			imitation();
			// 2018-04-09 : réévaluation des assets, car ils ont pu être
			// dépréciés au moment de l'imitation.
			assets = account.getAmount() + factory.getValue();

			this.debtCancellation++;
			this.debtCancellationCount++;
			this.canceledDebt = debt - assets;
			account.cancelDebt(this.canceledDebt);

			if (this.getCapital() != 0) {
				throw new RuntimeException("Inconsistency");
			}

			final long capitalRequirement = this.getCapitalTarget();

			this.changeOwnerShip(capitalRequirement);

		}
	}

	protected long computesInvestmentBudget(float utilizationRate) {
		long budget;
		final double sales = this.agentDataset.sum(keys.salesValue, 12);
		final double costs = this.agentDataset.sum(keys.salesCosts, 12);
		final double overhead = this.agentDataset.sum(keys.overheadExpense, 12);
		final double interests = this.agentDataset.sum(keys.interests, 12);
		final double cashFlow = sales - (costs + interests + overhead);
		if (cashFlow > 0) {
			budget = (long) ((utilizationRate - normalUtilizationRate) * cashFlow);
			final double capital = this.getCapital();
			final double capitalTarget = this.getCapitalTarget();
			if (capital < capitalTarget) {
				budget *= capital / capitalTarget;
			}
		} else {
			budget = 0;
		}
		return budget;
	}

	/**
	 * Computes and returns the capital of this firm.
	 * 
	 * @return the capital of this firm.
	 */
	protected long getCapital() {
		return factory.getValue() + account.getAmount() - account.getDebt();
	}

	/**
	 * Computes and returns the capital target of this firm.
	 * 
	 * @return the capital target of this firm.
	 */
	protected long getCapitalTarget() {
		final long assets = account.getAmount() + factory.getValue();
		long capitalTarget = (targetDebtRatio == 0) ? assets
				: (targetDebtRatio == 1) ? 0 : (long) (assets * (1f - targetDebtRatio));
		if (capitalTarget > assets && targetDebtRatio < 0.001) {
			capitalTarget = assets;
		} else if (capitalTarget < 0 && targetDebtRatio > 0.999) {
			capitalTarget = 0;
		} else if (capitalTarget < 0 || capitalTarget > assets) {
			Jamel.println();
			Jamel.println("assets", assets);
			Jamel.println("targetDebtRatio", targetDebtRatio);
			Jamel.println("capitalTarget", capitalTarget);
			throw new RuntimeException("Bad capital target");
		}
		return capitalTarget;
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
				BasicFirm2 firm = (BasicFirm2) sector.select();
				this.lastImitiation = now;
				this.imitations = 1;
				this.targetDebtRatio = firm.targetDebtRatio;
				this.markup = firm.markup;
				this.workforceManager.wage = firm.workforceManager.wage;
				this.workforceManager.wageFactor = firm.workforceManager.wageFactor;

				/*
				 * 2018-04-09
				 * Si nécessaire, on déprécie les stock de bien finis.
				 */
				if (!this.factory.getInventories().isEmpty()) {
					final Double volume = firm.agentDataset.average(keys.salesVolume, 3);
					if (volume != 0) {
						final double costs = firm.agentDataset.average(keys.salesCosts, 3);
						final double unitCost = costs / volume;
						if (this.factory.getInventories().valuePerUnit() > unitCost) {
							this.factory.depreciateInventories(unitCost);
						}
					}
				}
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
	 * Pays the dividends to the owners of the firm.
	 */
	@Override
	protected void payDividend() {
		if (this.ownership.isEmpty()) {
			throw new RuntimeException("No owners.");
		}
		final long cash = account.getAmount();
		final double capital = getCapital();
		final double capitalTarget = getCapitalTarget();
		final double averageIncome = agentDataset.average(keys.netProfit, 12);

		// TODO 12 should be a parameter
		final double modif = (capital - capitalTarget) / 12;
		long toBeDistributed = (long) (averageIncome * this.cons.retentionRate + modif);
		/*if (capital < capitalTarget) {
			toBeDistributed = (long) (toBeDistributed * capital / capitalTarget);
		}*/

		if (capital <= 0 || toBeDistributed < 0) {
			toBeDistributed = 0;
		} else if (toBeDistributed > cash) {
			// toBeDistributed = cash;
			this.account.borrow(toBeDistributed - cash, 12, false);
		}

		dividends = this.ownership.payDividends(toBeDistributed);
		// TODO CLEAN UP
		/*if (toBeDistributed > ownership.size()) {
			final long newDividend = toBeDistributed / ownership.size();
			for (final Equity title : ownership) {
				final Shareholder shareholder = title.getOwner();
				shareholder.acceptDividendCheque(account.issueCheque(shareholder, newDividend));
			}
			dividends = newDividend * ownership.size();
		} else {
			dividends = 0l;
		}*/
	}

	@Override
	protected void payWorkers() {
		this.workforceManager.payWorkers();
	}

	@Override
	protected void planProduction() {
		this.checkSolvency();
		this.workforceManager.updateWage();
		this.updateCapacityUtilizationTarget();
		this.workforceManager.updateWorkforce();
	}

	@Override
	protected void production() {
		this.factory.production(this.workforce);
		this.salesManager.createSupply();
	}

	/**
	 * Updates the capacity utilization target.
	 */
	protected void updateCapacityUtilizationTarget() {

		if (false && this.getPeriod() < cons.supervision) {
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

		this.putData(keys.utilizationTarget, this.utilizationRateTargeted);
	}

	/**
	 * Updates the data.
	 */
	protected void updateData() {

		final long grossProfit = this.salesManager.salesValue - this.salesManager.salesValueAtCost;

		this.putData(keys.count, 1);

		this.putData(keys.markup, this.markup - 1);

		this.putData(keys.inventoriesVolume, this.factory.getInventories().getVolume());
		this.putData(keys.inventoriesNormalVolume, getInventoryNomalVolume());
		this.putData(keys.inventoriesLevel, this.factory.getInventories().getVolume() / getInventoryNomalVolume());
		this.putData(keys.inProcessValue, this.factory.getInProcessValue());
		this.putData(keys.inventoriesValue, this.factory.getInventories().getValue());
		this.putData(keys.machineryValue, this.factory.getMachineryValue());
		this.putData(keys.productionMax, this.factory.getProductionAtFullCapacity());

		this.putData(keys.normalUtilizationRate, this.normalUtilizationRate);

		this.putData(keys.salesValue, this.salesManager.salesValue);
		this.putData(keys.salesVolume, this.salesManager.salesVolume);
		this.putData(keys.supplyVolume, this.salesManager.supplyVolume);
		this.putData(keys.supplyValue, this.salesManager.supplyValue);
		this.putData(keys.salesCosts, this.salesManager.salesValueAtCost);
		this.putData(keys.grossProfit, grossProfit);
		this.putData(keys.overheadExpense, factory.getOverheadWages());

		this.putData(keys.canceledDebt, this.canceledDebt);
		this.putData(keys.debtCancellation, this.debtCancellation);
		this.putData(keys.debtCancellationCount, this.debtCancellationCount);

		// ***

		final long cash = account.getAmount();
		final long assets = factory.getValue() + cash;
		final long liabilities = account.getDebt();
		final long capital = assets - liabilities;

		this.ownership.updateValue(capital);

		this.putData(keys.debtRatio, ((double) liabilities) / assets);
		this.putData(keys.weightedDebt, ((double) liabilities * liabilities) / assets);

		this.putData(keys.money, cash);
		this.putData(keys.assets, assets);
		this.putData(keys.liabilities, liabilities);
		this.putData(keys.equities, capital);
		this.putData(keys.tangibleAssets, factory.getValue());

		this.putData(keys.shortTermDebt, account.getShortTermDebt());
		this.putData(keys.longTermDebt, account.getLongTermDebt());
		this.putData(keys.overdueDebt, account.getOverdueDebt());

		this.putData(keys.dividends, this.dividends);
		this.putData(keys.interests, account.getInterests());
		this.putData(keys.debtService, account.getDebtService());

		this.putData(keys.liabilitiesTarget, this.getLiabilitiesTarget());

		// ***

		final long netProfit = grossProfit - account.getInterests() - factory.getDepreciation()
				- factory.getOverheadWages();
		this.putData(keys.netProfit, netProfit);

		this.putData(keys.retainedEarnings, netProfit - this.dividends);

		/*
		final double returnOnEquity = this.netProfitMemory.getSum() / capital;
		final double returnOnAssets = this.netProfitMemory.getSum() / assets;
		final double solvency = this.netProfitMemory.getSum() / liabilities;
		*/

		// ****

		this.putData(keys.imitation, this.imitations);
		this.putData(keys.debtRatioTarget, this.targetDebtRatio);

		final double grossProfit6 = this.agentDataset.sum(keys.grossProfit, 6);
		final double interest6 = this.agentDataset.sum(keys.interests, 6);
		final double debtService6 = this.agentDataset.sum(keys.debtService, 6);

		if (grossProfit6 > debtService6) {
			this.putData(keys.hedge, 1);
			this.putData(keys.speculative, 0);
			this.putData(keys.ponzi, 0);
		} else if (grossProfit6 < interest6) {
			this.putData(keys.hedge, 0);
			this.putData(keys.speculative, 0);
			this.putData(keys.ponzi, 1);
		} else {
			this.putData(keys.hedge, 0);
			this.putData(keys.speculative, 1);
			this.putData(keys.ponzi, 0);
		}

	}

	@Override
	public void close() {

		this.account.close();

		this.workforceManager.close();

		this.updateData();

		super.close();
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
	public Supply getSupply() {
		return this.salesManager.getSupply();
	}

	/* (non-Javadoc)
	 * @see jamel.models.m18.r06.firms.Firm#invest()
	 */
	@Override
	public void invest() {

		/*
		 * 2018-03-10 -> public
		 * Permet au marché des biens d'investissement d'appeler cette methode.
		 * Permet de brasser les firmes de différents secteurs dans la phase d'investissement.
		 */

		final Commodities purchase;
		final Supply[] supplies = getSupplies();

		// ***
		// For debugging purpose :
		int newMachinesNeed = 0;
		this.putData(keys.suppliesLength, supplies.length);
		// ***

		final Float utilizationRate = getUtilizationRate();
		final long budget;
		if (utilizationRate == null) {
			budget = 0;
		} else {
			if (utilizationRate > normalUtilizationRate) {
				budget = computesInvestmentBudget(utilizationRate);
			} else {
				budget = 0;
			}
		}
		this.putData(keys.investmentBudget, budget);

		if (supplies.length > 0) {

			final int capacity = this.factory.getCapacity();

			if (this.getPeriod() < cons.supervision) {
				if (capacity < this.cons.initialCapacity) {
					purchase = buy(this.cons.initialCapacity - capacity, supplies);
				} else {
					purchase = new BasicGoods(
							this.factory.getTechnology().getQualityOfInputForTheCreationOfANewMachine());
				}
				newMachinesNeed = this.cons.initialCapacity - capacity;
			} else {
				if (capacity == 0) {
					purchase = buy(1, supplies);
				} else {

					purchase = new BasicGoods(
							this.factory.getTechnology().getQualityOfInputForTheCreationOfANewMachine());

					// on détermine le budget
					/*
					final Float utilizationRate = getUtilizationRate();
					final long budget;
					if (utilizationRate == null) {
						budget = 0;
					} else {
						if (utilizationRate > normalUtilizationRate) {
							budget = computesInvestmentBudget(utilizationRate);
						} else {
							budget = 0;
						}
					}
					*/

					// puis on achète.

					if (budget > 0) {
						for (Supply supply : supplies) {
							if (budget - purchase.getValue() < supply.getPrice()) {
								break;
							}
							final long value;
							final long volume;
							if (supply.getValue() < budget - purchase.getValue()) {
								volume = supply.getVolume();
								value = supply.getValue();
							} else {
								volume = (long) ((budget - purchase.getValue()) / supply.getPrice());
								value = supply.getPrice(volume);
							}
							if (this.account.getAmount() < value) {
								this.account.borrow(value - this.account.getAmount(), 0, false);
							}
							purchase.add(supply.purchase(volume, account.issueCheque(supply.getSupplier(), value)));
						}
					}
				}
			}

		} else {
			purchase = new BasicGoods(this.factory.getTechnology().getQualityOfInputForTheCreationOfANewMachine());
		}

		if (purchase.getVolume() > 0) {

			// C'est le moment de rectifier le financement

			final long purchaseValue = purchase.getValue();
			final long longTerm = (long) (purchaseValue * this.targetDebtRatio);
			final long shortTerm = purchaseValue - longTerm;

			if (longTerm > 0) {
				this.account.borrow(longTerm, cons.longTerm, true);
			}
			if (shortTerm > 0) {
				this.account.borrow(shortTerm, cons.shortTerm, true);
			}

			// TODO IMPLEMENT this.putData(keys.investmentSize, 0);
			this.putData(keys.investmentVolume, purchase.getVolume());
			this.putData(keys.investmentValue, purchase.getValue());

			// On envoie le stuff à la factory.

			this.factory.expandCapacity(purchase);
		} else {
			this.putData(keys.investmentVolume, 0);
			this.putData(keys.investmentValue, 0);
		}

		this.putData(keys.inputVolume, this.factory.getInputVolume());
		// TODO : vérifier ici que purchase est bien vide

		// For debugging purpose :
		this.putData(keys.newMachinesNeed, newMachinesNeed);
	}

	@Override
	public boolean isSolvent() {
		return (this.getCapital() >= 0);
	}

	@Override
	public void open() {
		super.open();
		this.account.open();
		this.factory.open(periodDataset);
		this.factory.depreciation();

		if (ownership.isEmpty()) {
			initOwnership();
		}

		this.dividends = null;
		this.canceledDebt = 0;
		this.debtCancellation = 0;
		this.salesManager.open();
		this.workforceManager.open();
		this.imitations = 0;
		this.mutation();
		if (this.factory.getCapacity() == 0) {
			this.imitation();
		}
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
