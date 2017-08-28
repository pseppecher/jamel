package jamel.v170804.models.basicModel3.households;

import java.util.Arrays;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.util.Agent;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.NotUsedException;
import jamel.util.Parameters;
import jamel.util.Sector;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.data.BasicAgentDataset;
import jamel.v170804.models.basicModel3.banks.Account;
import jamel.v170804.models.basicModel3.banks.Amount;
import jamel.v170804.models.basicModel3.banks.Bank;
import jamel.v170804.models.basicModel3.banks.Cheque;
import jamel.v170804.models.basicModel3.firms.Employer;
import jamel.v170804.models.basicModel3.firms.Goods;
import jamel.v170804.models.basicModel3.firms.JobOffer;
import jamel.v170804.models.basicModel3.firms.LaborContract;
import jamel.v170804.models.basicModel3.firms.Supplier;
import jamel.v170804.models.basicModel3.firms.Supply;

/**
 * Represents a worker.
 */
public class BasicWorker extends JamelObject implements Agent, Worker {

	/**
	 * The 'employed' status.
	 */
	private static final int EMPLOYED = 1;

	/**
	 * The data keys.
	 */
	private static final BasicWorkerKeys keys = BasicWorkerKeys.getInstance();

	/**
	 * THe 'unemployed' status.
	 */
	private static final int UNEMPLOYED = 0;

	/**
	 * Selects the best offer among the selection.
	 * 
	 * @param employerSector
	 *            the employer sector.
	 * @param n
	 *            the number of offers to be selected.
	 * @return the best offer, or <code>null</code> if a valid offer could not
	 *         be found.
	 */
	private static JobOffer selectJobOffer(Sector employerSector, int n) {
		final Agent[] employers = employerSector.select(n);
		JobOffer result = null;
		// On retient la meilleur des offres.
		for (int i = 0; i < employers.length; i++) {
			if (employers[i] != null) {
				final JobOffer jobOffer = ((Employer) employers[i]).getJobOffer();
				if (jobOffer != null) {
					if (result == null || jobOffer.getWage() > result.getWage()) {
						result = jobOffer;
					}
				}
			}
		}

		return result;
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
		case "jobSearch":
			action = (agent) -> {
				((BasicWorker) agent).jobSearch();
			};
			break;
		case "consumption":
			action = (agent) -> {
				((BasicWorker) agent).consumption();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * The bank account of this worker.
	 */
	private final Account account;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The sector of the employers.
	 */
	private final Sector employerSector;

	/**
	 * A flag that indicates whether this worker is exhausted or not.
	 */
	private boolean exhausted = false;

	/**
	 * Indicates if the worker has been hired during the current period (=1 if
	 * hired, 0 if not).
	 */
	private int hiring;

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The labor contract.
	 */
	private LaborContract jobContract = null;

	/**
	 * The wage flexibility parameter.
	 */
	private final double param_flexibility;

	/**
	 * The number of employers to be selected in the job search phase.
	 */
	private final int param_jobSearch;

	/**
	 * The wage resistance parameter.
	 */
	private final int param_resistance;

	/**
	 * The saving propensity.
	 */
	private final double param_savingPropensity;

	/**
	 * The number of suppliers to be selected in the consumption phase.
	 */
	private final int param_supplySearch;

	/**
	 * The reservation wage of this worker.
	 */
	private double reservationWage = 0;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * The selected suppliers.
	 */
	final private Agent[] selection;

	/**
	 * The employment status of this worker.
	 */
	private int status = UNEMPLOYED;

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
	private final Amount wage = new Amount();

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

		final Parameters params = this.sector.getParameters();
		ArgChecks.nullNotPermitted(params, "params");

		final String bankSectorName = params.getAttribute("bankSector");
		this.account = ((Bank) this.getSimulation().getSector(bankSectorName).select(1)[0]).openAccount(this);

		final Parameters laborMarketParams = params.get("laborMarket");
		final String employerSectorName = laborMarketParams.getAttribute("employers");
		this.employerSector = this.getSimulation().getSector(employerSectorName);
		this.param_jobSearch = laborMarketParams.getIntAttribute("search");
		this.param_flexibility = laborMarketParams.getDoubleAttribute("flexibility");
		this.param_resistance = laborMarketParams.getIntAttribute("resistance");

		final Parameters goodMarketParams = params.get("goodMarket");
		final String supplierSectorName = goodMarketParams.getAttribute("suppliers");
		this.supplierSector = this.getSimulation().getSector(supplierSectorName);
		this.param_savingPropensity = Double.parseDouble(goodMarketParams.getAttribute("savingPropensity"));
		this.param_supplySearch = Integer.parseInt(goodMarketParams.getAttribute("search"));
		this.selection = new Agent[param_supplySearch];

		this.dataset = new BasicAgentDataset(this, keys);
	}

	/**
	 * The consumption phase.
	 */
	private void consumption() {
		long consumptionVolume = 0;
		long consumptionValue = 0;
		long budget = (long) (this.account.getAmount() * (1. - this.param_savingPropensity));
		this.dataset.put(keys.consumptionBudget, budget);
		if (budget > 0) {
			// final Agent[] selection =
			/*if (selection[selection.length-1]==null) {
				
			}*/
			selection[selection.length - 1] = this.supplierSector.select(1)[0];
			Arrays.sort(selection, Households.supplierComparator);

			for (Agent agent : selection) {
				if (agent == null || ((Supplier) agent).getSupply() == null || ((Supplier) agent).getSupply().isEmpty()
						|| ((Supplier) agent).getSupply().getPrice() > budget) {
					break;
				}

				final Supply supply = ((Supplier) agent).getSupply();
				final long spending;
				final long consumVol;
				if (supply.getTotalValue() <= budget) {
					consumVol = supply.getVolume();
					spending = (long) (consumVol * supply.getPrice());
					if (spending != supply.getTotalValue()) {
						throw new RuntimeException("Inconsistency.");
					}
				} else {
					consumVol = (int) (budget / supply.getPrice());
					spending = (long) (consumVol * supply.getPrice());
				}

				final Goods goods = supply.purchase(consumVol,
						this.account.issueCheque(supply.getSupplier(), spending));
				if (goods.getVolume() != consumVol) {
					throw new RuntimeException("Bad volume");
				}
				budget -= spending;
				consumptionValue += spending;
				consumptionVolume += goods.getVolume();
				goods.consume();
			}

		}

		this.dataset.put(keys.consumptionVolume, consumptionVolume);
		this.dataset.put(keys.consumptionValue, consumptionValue);
		// TODO updater les chiffres de l'épargne.
	}

	/**
	 * The job search phase.
	 */
	private void jobSearch() {

		if (this.jobContract == null || !this.jobContract.isValid()) {
			this.status = UNEMPLOYED;
			this.unempDuration++;
			this.jobContract = null;
		} else {
			this.status = EMPLOYED;
			this.unempDuration = 0;
		}

		// Different behaviors according the status.

		switch (this.status) {

		case UNEMPLOYED:

			if (this.reservationWage > 0 && this.unempDuration > param_resistance) {
				this.reservationWage -= this.param_flexibility * this.reservationWage * getRandom().nextFloat();
			}
			final JobOffer jobOffer = selectJobOffer(this.employerSector, this.param_jobSearch);
			if (jobOffer != null && jobOffer.getWage() >= this.reservationWage) {
				this.jobContract = jobOffer.apply(this);
				this.status = EMPLOYED;
				this.unempDuration = 0;
				this.hiring = 1;
				this.reservationWage = this.jobContract.getWage();
			}
			break;

		case EMPLOYED:
			break;

		default:
			throw new RuntimeException("Unexpected status: " + this.status);
		}
	}

	@Override
	public void acceptPayCheque(Cheque cheque) {
		if (this.jobContract == null || !this.exhausted || this.jobContract.getWage() != cheque.getAmount()
				|| this.jobContract.getEmployer() != cheque.getDrawer() || this.wage.getAmount() != 0) {
			throw new RuntimeException("Pay cheque trouble");
		}

		this.wage.plus(cheque.getAmount());
		this.account.deposit(cheque);

		// Comptabiliser ce paiement, à des fins statisques mais aussi pour que
		// le travailleur vérifie s'il a été payé en fin de période.
	}

	/**
	 * Closes the worker at the end of the period.
	 */
	@Override
	public void close() {
		if (!this.exhausted && this.jobContract != null) {
			throw new RuntimeException("This worker is not exhausted while there is a labor contract.");
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
		this.dataset.put(keys.count, 1);
		if (this.exhausted) {
			this.dataset.put(keys.employed, 1);
			this.dataset.put(keys.employmentDuration, 1 + this.getPeriod() - this.jobContract.getStart());
		} else {
			this.dataset.put(keys.employed, 0);
			this.dataset.put(keys.employmentDuration, 0);
		}
		this.dataset.put(keys.money, this.account.getAmount());
		this.dataset.put(keys.wage, this.wage.getAmount());
		this.dataset.put(keys.reservationWage, this.reservationWage);
		this.dataset.put(keys.hiring, this.hiring);
		this.dataset.put(keys.unempDuration, this.unempDuration);
		this.dataset.close();
		super.close();
	}

	@Override
	public long getAssetTotalValue() {
		throw new NotUsedException();
	}

	@Override
	public int getBorrowerStatus() {
		throw new NotUsedException();
	}

	@Override
	public Double getData(String dataKey, int period) {
		return this.dataset.getData(dataKey, period);
	}

	@Override
	public String getName() {
		return "Worker_" + this.id;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

	@Override
	public void goBankrupt() {
		throw new NotUsedException();
	}

	@Override
	public boolean isBankrupted() {
		throw new NotUsedException();
	}

	@Override
	public boolean isSolvent() {
		throw new NotUsedException();
	}

	/**
	 * Opens the worker at the beginning of the period.
	 */
	@Override
	public void open() {
		this.exhausted = false;
		this.wage.cancel();
		this.dataset.open();
		super.open();
		this.hiring = 0;
		// ***
		// TODO REMOVE ME
		final int isValid;
		final int isNull;
		final int isInvalid;
		int sinceStart = 0;
		if (this.jobContract != null && this.jobContract.isValid()) {
			isValid = 1;
			isInvalid = 0;
			isNull = 0;
		} else {
			isValid = 0;
			if (this.jobContract != null) {
				isInvalid = 1;
				isNull = 0;
				sinceStart = this.getPeriod() - this.jobContract.getStart();
			} else {
				isInvalid = 0;
				isNull = 1;
			}
		}
		this.dataset.put(keys.isValid, isValid);
		this.dataset.put(keys.isInvalid, isInvalid);
		this.dataset.put(keys.isNull, isNull);
		this.dataset.put(keys.sinceStart, sinceStart);
		// ***
	}

	@Override
	public boolean work() {
		if (this.jobContract == null || this.exhausted == true) {
			Jamel.println();
			Jamel.println("this.name == " + this.getName());
			Jamel.println("this.laborContract == " + this.jobContract);
			Jamel.println("this.exhausted == " + this.exhausted);
			Jamel.println();
			throw new RuntimeException("Something went wrong while working.");
		}
		this.exhausted = true;
		return true;
	}

}
