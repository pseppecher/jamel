package jamel.models.m18.r01.households;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r01.data.AgentDataset;
import jamel.models.m18.r01.data.BasicAgentDataset;
import jamel.models.m18.r01.data.BasicPeriodDataset;
import jamel.models.m18.r01.data.PeriodDataset;
import jamel.models.m18.r01.firms.BasicGoods;
import jamel.models.m18.r01.util.BasicAmount;
import jamel.models.m18.r01.util.Tools;
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
 * Represents a worker.
 * 
 * 2018-03-10 : jamel/models/m18/r01/households/BasicWorker.java
 * 
 * 2018-03-08 : BasicWorker2
 * Un travailleur fait pour être combiné avec BasicLaborMarket2.
 * 
 * 2018-03-02 : implement Household.
 * Pour lui permettre d'être appelé par l'objet marché des biens lors de la
 * phase de consommation.
 * 
 * 2018-02-09 : BasicWorker
 * Nouveau nom du précédent.
 * 
 * 2018-01-28 : BasicWorker2
 * Ce travailleur est conçu pour être utilisé avec LaborMarket
 * 
 * 2017-11-08 : new name for "BasicWorker3"
 */
public class BasicWorker extends JamelObject implements Worker, Household {

	/**
	 * Represents a set of constants.
	 */
	private class Constants {

		/**
		 * The supervision period.
		 */
		final private float supervision;

		/**
		 * The name of the bank sector.
		 */
		final public String bankSectorName;

		/**
		 * The savings target ratio.
		 */
		final public String consumptionGoodsQuality;

		/**
		 * The wage flexibility.
		 */
		final public float flexibility;

		/**
		 * The wage resistance parameter.
		 */
		final public int resistance;

		/**
		 * The propensity to consume excess savings.
		 */
		final public float savingsPropensityToConsumeExcess;

		/**
		 * The propensity to save?
		 */
		final public float savingsPropensityToSave;

		/**
		 * The savings target ratio.
		 */
		final public float savingsRatioTarget;

		/**
		 * The name of the supplier sector.
		 */
		final public String supplierSectorName;

		/**
		 * The number of suppliers to be selected in the consumption phase.
		 */
		final public int supplySearch;

		/**
		 * Creates a new set of constants.
		 * 
		 * @param parameters
		 *            the parameters.
		 */
		public Constants(Parameters parameters) {
			this.supervision = parameters.getInt("supervision");
			this.supplySearch = parameters.getInt("goodMarket.search");
			this.savingsPropensityToConsumeExcess = parameters.getFloat("goodMarket.savingsPropensityToConsumeExcess");
			this.savingsPropensityToSave = parameters.getFloat("goodMarket.savingPropensity");
			this.savingsRatioTarget = parameters.getFloat("goodMarket.savingsRatioTarget");
			this.flexibility = parameters.getFloat("laborMarket.flexibility");
			this.resistance = parameters.getInt("laborMarket.resistance");
			this.consumptionGoodsQuality = parameters.getString("goodMarket.quality");
			this.supplierSectorName = parameters.getString("goodMarket.suppliers");
			this.bankSectorName = parameters.getString("bankSector");
		}

	}

	/**
	 * The job offer comparator.
	 * <p>
	 * To compare jobs according to the wage they offer.
	 */
	private static final Comparator<JobOffer> jobComparator = new Comparator<JobOffer>() {
		@Override
		public int compare(JobOffer offer1, JobOffer offer2) {
			final int result;
			if (offer1.isEmpty()) {
				if (offer2.isEmpty()) {
					result = 0;
				} else {
					result = 1;
				}
			} else if (offer2.isEmpty()) {
				result = -1;
			} else if (offer2.getWage() == offer1.getWage()) {
				result = 0;
			} else if (offer2.getWage() > offer1.getWage()) {
				result = 1;
			} else {
				result = -1;
			}
			return result;
		}
	};

	/**
	 * The data keys.
	 */
	private static final BasicWorkerKeys keys = BasicWorkerKeys.getInstance();

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static public Consumer<? super Agent> getAction(final String phaseName) {

		// TODO : faire le ménage ici, cette méthode devrait être simplement
		// supprimée !

		final Consumer<? super Agent> action;

		switch (phaseName) {
		case "jobSearch":
			Jamel.notUsed();
			// 2018-01-28 : il n'y a plus de phase de job search c'est le marché
			// du travail qui organise le matching
			action = null;
			break;
		case "consumption":
			// 2018-03-02 : il n'y a plus de phase de consommation c'est le
			// marché des biens
			// qui appelle lui même les agents
			Jamel.notUsed();
			action = null;
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * Returns the set of data keys for this class of agent.
	 * 
	 * @return the set of data keys for this class of agent.
	 */
	static public DataKeys getDataKeys() {
		return keys;
	}

	/**
	 * The bank account of this worker.
	 */
	private final Account account;

	/**
	 * The dataset.
	 */
	final private AgentDataset agentDataset;

	/**
	 * A flag that indicates whether this worker is exhausted or not.
	 */
	private boolean exhausted = false;

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The labor contract.
	 */
	private JobContract jobContract = null;

	/**
	 * The list of the job offers the worker received since the start of the
	 * period.
	 */
	final private List<JobOffer> jobOffers = new LinkedList<>();

	/**
	 * The constants.
	 */
	private final Constants k;

	/**
	 * The dataset.
	 */
	private PeriodDataset periodDataset = null;

	/**
	 * The reservation wage of this worker.
	 */
	private Double reservationWage = null;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * The regular suppliers of this worker.
	 */
	final private Supplier[] suppliers;

	/**
	 * The sector of the suppliers.
	 */
	private final Sector supplierSector;

	/**
	 * The duration of unemployment.
	 */
	private int unempDuration = 0;

	/**
	 * The amount of the wage received for this period.
	 */
	private final BasicAmount wage = new BasicAmount();

	/**
	 * The wage flexibility.
	 */
	private Float wageFlex = null;

	/**
	 * Creates a new worker.
	 * 
	 * @param sector
	 *            the sector of this worker.
	 * @param id
	 *            the id of this worker.
	 */
	public BasicWorker(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		this.k = new Constants(this.sector.getParameters());
		this.account = ((Bank) this.getSimulation().getSector(this.k.bankSectorName).selectArray(1)[0])
				.openAccount(this);
		this.supplierSector = this.getSimulation().getSector(this.k.supplierSectorName);
		this.suppliers = new Supplier[this.k.supplySearch];
		this.agentDataset = new BasicAgentDataset(this);
	}

	/**
	 * Updates the reservation wage of this worker.
	 * Should be called at the beginning of the period.
	 */
	private void updateReservationWage() {
		if (this.isEmployed()) {
			this.unempDuration = 0;
			this.reservationWage = (double) this.jobContract.getWage();
			this.wageFlex = this.k.flexibility * this.jobContract.getWage();
		} else {
			this.jobContract = null;
			this.unempDuration++;
			if (reservationWage == null) {
				reservationWage = 0d;
				this.wageFlex = null;
			}
			if (reservationWage > 0 && this.unempDuration > this.k.resistance) {
				reservationWage = Math.max(0, this.reservationWage - this.wageFlex * getRandom().nextFloat());
			}
		}

	}

	@Override
	public boolean acceptJob(JobOffer jobOffer) {
		// 2018-03-08 Pas utilisé, remplacé par addJobOffer() et chooseJob();
		Jamel.notUsed();
		return false;
	}

	@Override
	public void acceptPayCheque(Cheque cheque) {
		if (this.jobContract == null || this.jobContract.getWage() != cheque.getAmount()
		// || this.jobContract.getEmployer() != cheque.getDrawer()
				|| this.wage.getAmount() != 0) {
			Jamel.println("this.jobContract", this.jobContract);
			Jamel.println("this.jobContract.getWage()", this.jobContract.getWage());
			Jamel.println("cheque.getAmount()", cheque.getAmount());
			Jamel.println("this.wage.getAmount()", this.wage.getAmount());
			throw new RuntimeException("Pay cheque trouble");
		}

		this.wage.plus(cheque.getAmount());
		this.account.deposit(cheque);

		// Comptabiliser ce paiement, à des fins statisques mais aussi pour que
		// le travailleur vérifie s'il a été payé en fin de période.
	}

	/**
	 * Adds a new job offer (called by an employer).
	 * 
	 * @param jobOffer
	 *            the new job offer.
	 */
	public void addJobOffer(JobOffer jobOffer) {
		this.jobOffers.add(jobOffer);
	}

	/**
	 * Chooses a job among the received job offers.
	 */
	public void chooseJob() {
		if (jobOffers.size() > 0) {
			Collections.sort(jobOffers, jobComparator);
			final JobOffer jobOffer = jobOffers.get(0);
			if (jobOffer.size() > 0 && jobOffer.getWage() >= reservationWage) {
				this.jobContract = jobOffer.apply(this);
				this.unempDuration = 0;
			}
		}
	}

	/**
	 * Closes the worker at the end of the period.
	 */
	@Override
	public void close() {
		if (!this.exhausted && this.jobContract != null) {
			if (!this.jobContract.isValid()) {
				this.jobContract = null;
			} else {
				throw new RuntimeException("This worker is not exhausted while there is a labor contract.");
			}
		}
		if (this.exhausted && (this.wage.isEmpty() || this.jobContract == null
				|| this.jobContract.getWage() != this.wage.getAmount())) {
			Jamel.println();
			Jamel.println("this.wage.isEmpty()", this.wage.isEmpty());
			Jamel.println("this.jobContract", this.jobContract);
			Jamel.println("this.jobContract.getWage()", this.jobContract.getWage());
			Jamel.println("this.wage.getAmount()", this.wage.getAmount());
			throw new RuntimeException("This worker is exhausted but there is a problem with its labor contract.");
		}
		this.periodDataset.put(keys.count, 1);
		if (this.exhausted) {
			this.periodDataset.put(keys.employed, 1);
			this.periodDataset.put(keys.unemployed, 0);
			this.periodDataset.put(keys.employmentDuration, 1 + this.getPeriod() - this.jobContract.getStart());
		} else {
			this.periodDataset.put(keys.employed, 0);
			this.periodDataset.put(keys.unemployed, 1);
			this.periodDataset.put(keys.employmentDuration, 0);
		}
		this.periodDataset.put(keys.money, this.account.getAmount());
		this.periodDataset.put(keys.wage, this.wage.getAmount());
		this.periodDataset.put(keys.reservationWage, this.reservationWage);
		this.periodDataset.put(keys.unempDuration, this.unempDuration);
		this.agentDataset.put(this.periodDataset);
	}

	/**
	 * The consumption phase.
	 */
	@Override
	public void consumption() {

		// 2018-03-02 : publique, appelée par le marché des biens.

		long budget;
		if (getPeriod() < this.k.supervision) {
			budget = this.account.getAmount();
		} else {
			final double averageIncome = this.agentDataset.sum(keys.wage, 12) / 12;
			final long savingsTarget = (long) (12 * averageIncome * this.k.savingsRatioTarget);
			final long savings = (long) (this.account.getAmount() - averageIncome);
			if (savings < savingsTarget) {
				budget = Math.min(this.account.getAmount(),
						(long) ((1. - this.k.savingsPropensityToSave) * averageIncome));
			} else {
				budget = Math.min(this.account.getAmount(),
						(long) (averageIncome + (savings - savingsTarget) * this.k.savingsPropensityToConsumeExcess));
			}
		}
		this.periodDataset.put(keys.consumptionBudget, budget);
		long consumptionVolume = 0;
		long consumptionValue = 0;
		if (budget > 0) {
			this.suppliers[suppliers.length - 1] = (Supplier) this.supplierSector.select();

			Arrays.sort(suppliers, Tools.supplierComparator);

			for (Supplier supplier : suppliers) {
				if (supplier == null || supplier.getSupply() == null || supplier.getSupply().isEmpty()
						|| supplier.getSupply().getPrice() > budget) {
					break;
				}

				final Supply supply = supplier.getSupply();
				final long spending;
				final long consumVol;
				if (supply.getValue() <= budget) {
					consumVol = supply.getVolume();
					spending = (long) (consumVol * supply.getPrice());
					if (spending != supply.getValue()) {
						throw new RuntimeException("Inconsistency.");
					}
				} else {
					consumVol = (int) (budget / supply.getPrice());
					spending = (long) (consumVol * supply.getPrice());
				}

				if (spending != 0) {
					if (consumVol > supply.getVolume()) {
						Jamel.println("consumVol", consumVol);
						Jamel.println("supply.getVolume()", supply.getVolume());
						throw new RuntimeException("Inconsistency");
					}
					final Commodities goods = supply.purchase(consumVol,
							this.account.issueCheque(supply.getSupplier(), spending));
					if (!((BasicGoods) goods).getQuality().equals(this.k.consumptionGoodsQuality)) {
						throw new RuntimeException("Bad quality: " + ((BasicGoods) goods).getQuality());
					}
					if (goods.getVolume() != consumVol) {
						Jamel.println("goods.getVolume(): " + goods.getVolume(), "consumVol: " + consumVol);
						throw new RuntimeException("Bad volume");
					}
					budget -= spending;
					consumptionValue += spending;
					consumptionVolume += goods.getVolume();
					goods.consume();
				}

			}

		}

		this.periodDataset.put(keys.consumptionVolume, consumptionVolume);
		this.periodDataset.put(keys.consumptionValue, consumptionValue);
		// A l'occasion, updater les chiffres de l'épargne.
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
	public String getName() {
		return this.sector.getName() + "." + this.id;
	}

	@Override
	public boolean isEmployed() {
		return this.jobContract != null && this.jobContract.isValid();
	}

	@Override
	public boolean isSolvent() {
		Jamel.notUsed();
		// TODO REMOVE
		return false;
	}

	/**
	 * Opens the worker at the beginning of the period.
	 */
	@Override
	public void open() {
		this.jobOffers.clear();
		this.exhausted = false;
		this.wage.cancel();
		this.periodDataset = new BasicPeriodDataset(this);
		this.updateReservationWage();
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

	@Override
	public void work() {
		if (this.jobContract == null || this.exhausted == true) {
			Jamel.println();
			Jamel.println("this.name == " + this.getName());
			Jamel.println("this.laborContract == " + this.jobContract);
			Jamel.println("this.exhausted == " + this.exhausted);
			Jamel.println();
			throw new RuntimeException("Something went wrong while working.");
		}
		this.exhausted = true;
	}

}
