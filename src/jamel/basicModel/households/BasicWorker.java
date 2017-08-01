package jamel.basicModel.households;

import java.util.function.Consumer;

import jamel.Jamel;
import jamel.basicModel.banks.Account;
import jamel.basicModel.banks.Amount;
import jamel.basicModel.banks.Bank;
import jamel.basicModel.banks.Cheque;
import jamel.basicModel.firms.Employer;
import jamel.basicModel.firms.Goods;
import jamel.basicModel.firms.JobOffer;
import jamel.basicModel.firms.LaborContract;
import jamel.basicModel.firms.Supplier;
import jamel.basicModel.firms.Supply;
import jamel.data.AgentDataset;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.NotUsedException;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
 * Represents a worker.
 */
public class BasicWorker extends JamelObject implements Agent, Worker {

	/**
	 * The 'employed' status.
	 */
	private static final int EMPLOYED = 1;

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
	 * The flexibility of the wage.
	 */
	private double wageFlex;

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
		if (params == null) {
			throw new RuntimeException("Parameters are null.");
		}

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

		this.dataset = new AgentDataset(this);
	}

	/**
	 * The consumption phase.
	 */
	private void consumption() {
		long consumptionVolume = 0;
		long consumptionValue = 0;
		long budget = (long) (this.account.getAmount() * (1.-this.param_savingPropensity));
		this.dataset.put("consumptionBudget", budget);
		if (budget > 0) {
			final Agent[] selection = this.supplierSector.select(param_supplySearch);
			while (budget > 0) {
				Agent supplier = null;
				for (int i = 0; i < selection.length; i++) {
					if (selection[i] != null) {
						final Supply supply_i = ((Supplier) selection[i]).getSupply();
						if (supply_i == null || supply_i.getPrice() > budget || supply_i.getVolume() == 0) {
							selection[i] = null;
						} else if (supplier == null) {
							supplier = selection[i];
							selection[i] = null;
						} else if (((Supplier) supplier).getSupply().getPrice() > supply_i.getPrice()) {
							final Agent disappointing = supplier;
							supplier = selection[i];
							selection[i] = disappointing;
						}
					}
				}
				if (supplier == null) {
					break;
				}

				final Supply supply = ((Supplier) supplier).getSupply();
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
		this.dataset.put("consumptionVolume", consumptionVolume);
		this.dataset.put("consumptionValue", consumptionValue);
		// TODO updater les chiffres de la consommation et de l'épargne.
	}

	/**
	 * The job search phase.
	 */
	private void jobSearch() {

		if ((this.jobContract == null) || !(this.jobContract.isValid())) {
			this.status = UNEMPLOYED;
		} else {
			this.status = EMPLOYED;
		}

		// Different behaviors according the status.

		switch (this.status) {

		case UNEMPLOYED:
			this.unempDuration++;

			if (this.reservationWage > 0 && this.unempDuration > param_resistance) {
				this.reservationWage -= this.wageFlex * getRandom().nextFloat();
			}
			final JobOffer jobOffer = selectJobOffer(this.employerSector, this.param_jobSearch);
			if (jobOffer != null && jobOffer.getWage() >= this.reservationWage) {
				this.jobContract = jobOffer.apply(this);
				this.status = EMPLOYED;
				this.unempDuration = 0;
			}
			break;

		case EMPLOYED:
			this.unempDuration = 0;
			this.reservationWage = this.jobContract.getWage();
			this.wageFlex = param_flexibility * this.jobContract.getWage();
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

		this.account.deposit(cheque);
		this.wage.plus(cheque.getAmount());

		// Comptabiliser ce paiement, à des fins statisques mais aussi pour que
		// le travailleur vérifie s'il a été payé en fin de période.
	}

	/**
	 * Closes the worker at the end of the period.
	 */
	@Override
	public void close() {
		if (this.exhausted && (this.wage.isEmpty() || this.jobContract == null
				|| this.jobContract.getWage() != this.wage.getAmount())) {
			Jamel.println();
			Jamel.println("this.wage.isEmpty()", this.wage.isEmpty());
			Jamel.println("this.jobContract", this.jobContract);
			Jamel.println("this.jobContract.getWage()", this.jobContract.getWage());
			Jamel.println("this.wage.getAmount()", this.wage.getAmount());
			Jamel.println();
			throw new RuntimeException("This worker is exhausted but there is a problem with its labor contract.");
		}
		this.dataset.put("countAgent", 1);
		if (this.exhausted) {
			this.dataset.put("employed", 1);
		} else {
			this.dataset.put("employed", 0);
		}
		this.dataset.put("money", this.account.getAmount());
		this.dataset.close();
		super.close();
	}

	@Override
	public Long getAssetTotalValue() {
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
