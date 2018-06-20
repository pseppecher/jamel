package jamel.models.m18.r08.households;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r08.firms.BasicGoods;
import jamel.models.m18.r08.roles.Bank;
import jamel.models.m18.r08.roles.Supplier;
import jamel.models.m18.r08.roles.Worker;
import jamel.models.m18.r08.util.AbstractAgent;
import jamel.models.m18.r08.util.Account;
import jamel.models.m18.r08.util.BasicAmount;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.Commodities;
import jamel.models.m18.r08.util.JobContract;
import jamel.models.m18.r08.util.JobOffer;
import jamel.models.m18.r08.util.Supply;
import jamel.models.m18.r08.util.Tools;
import jamel.util.Agent;
import jamel.util.Parameters;
import jamel.util.Sector;

/*
 * 2018-04-07: jamel.models.m18.r08.households.BasicWorker
 * Une nouvelle façon de déterminer le salaire de réservation.
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

/**
 * Represents a worker.
 */
public class BasicWorker2 extends AbstractAgent implements Worker, Household {

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
	protected static final WorkerKeys keys = WorkerKeys.getInstance();

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
	 * A flag that indicates whether this worker is exhausted or not.
	 */
	private boolean exhausted = false;

	/**
	 * The list of the job offers the worker received since the start of the
	 * period.
	 */
	final private List<JobOffer> jobOffers = new LinkedList<>();

	/**
	 * The regular suppliers of this worker.
	 */
	final private Supplier[] suppliers;

	/**
	 * The sector of the suppliers.
	 */
	private final Sector supplierSector;

	/**
	 * The amount of the wage received for this period.
	 */
	private final BasicAmount wage = new BasicAmount();

	/**
	 * The labor contract.
	 */
	protected JobContract jobContract = null;

	/**
	 * The constants.
	 */
	protected final WorkerConstants k;

	/**
	 * The reservation wage of this worker.
	 */
	protected Double reservationWage = null;

	/**
	 * The duration of unemployment.
	 */
	protected int unempDuration = 0;

	/**
	 * The wage flexibility.
	 */
	protected Float wageFlex = null;

	/**
	 * Creates a new worker.
	 * 
	 * @param sector
	 *            the sector of this worker.
	 * @param id
	 *            the id of this worker.
	 */
	public BasicWorker2(final Sector sector, final int id) {
		super(sector, id);
		this.k = new WorkerConstants(this.sector.getParameters());
		this.account = ((Bank) this.getSimulation().getSector(this.k.bankSectorName).selectArray(1)[0])
				.openAccount(this);
		this.supplierSector = this.getSimulation().getSector(this.k.supplierSectorName);
		this.suppliers = new Supplier[this.k.supplySearch];
	}

	/**
	 * Updates the reservation wage of this worker.
	 * Should be called at the beginning of the period.
	 */
	protected void updateReservationWage() {
		double sum = this.agentDataset.average(keys.wage, 12);
		// TODO n should be a parameter
		final int n = 3;
		for (int i = 0; i < n; i++) {
			sum += ((BasicWorker2) this.sector.select()).agentDataset.average(keys.wage, 12);
		}
		this.reservationWage = 1.15 * sum / (n + 1);

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

	@Override
	public void addJobOffer(JobOffer jobOffer) {
		this.jobOffers.add(jobOffer);
	}

	@Override
	public void chooseJob() {
		if (jobOffers.size() > 0) {
			this.updateReservationWage();
			Collections.sort(jobOffers, jobComparator);
			final JobOffer jobOffer = jobOffers.get(0);
			if (jobOffer.size() > 0 && jobOffer.getWage() >= this.reservationWage) {
				this.jobContract = jobOffer.apply(this);
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
		this.putData(keys.count, 1);
		if (this.exhausted) {
			this.unempDuration++;
			this.putData(keys.employed, 1);
			this.putData(keys.unemployed, 0);
			this.putData(keys.employmentDuration, 1 + this.getPeriod() - this.jobContract.getStart());
		} else {
			this.unempDuration=0;
			this.putData(keys.employed, 0);
			this.putData(keys.unemployed, 1);
			this.putData(keys.employmentDuration, 0);
		}
		this.putData(keys.money, this.account.getAmount());
		this.putData(keys.wage, this.wage.getAmount());
		this.putData(keys.reservationWage, this.reservationWage);
		this.putData(keys.unempDuration, this.unempDuration);
		super.close();
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
		this.putData(keys.consumptionBudget, budget);
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

		this.putData(keys.consumptionVolume, consumptionVolume);
		this.putData(keys.consumptionValue, consumptionValue);
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
		super.open();
		this.jobOffers.clear();
		this.exhausted = false;
		this.wage.cancel();
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
