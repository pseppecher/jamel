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
import jamel.util.Agent;
import jamel.util.AgentDataset;
import jamel.util.JamelObject;
import jamel.util.NotUsedException;
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
	 * The wage flexibility parameter.
	 */
	private static final int wageFlexParam = 0;

	/**
	 * THe wage resistance parameter.
	 */
	private static final int wageResistanceParam = 0;

	/**
	 * Selects the best offer among the selection.
	 * 
	 * @param employerSector
	 *            the employer sector.
	 * @return the best offer, or <code>null</code> if a valid offer could not
	 *         be found.
	 */
	private static JobOffer selectJobOffer(Sector employerSector) {
		final Agent[] employers = employerSector.select(10);
		// TODO 10 should be an argument of this method
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
		case "opening":
			action = (agent) -> {
				((BasicWorker) agent).open();
			};
			break;
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
		case "closure":
			action = (agent) -> {
				((BasicWorker) agent).close();
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
	 * A flag that indicates whether this worker is open or not.
	 */
	private boolean open;

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
	 * THe flexibility of the wage.
	 */
	private float wageFlex;

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
		this.account = ((Bank) this.getSimulation().getSector("Banks").select(1)[0]).openAccount(this);
		this.employerSector = this.getSimulation().getSector("Firms");
		// TODO "Firms" sould be a parameter
		if (this.employerSector == null) {
			throw new RuntimeException("Employer sector is missing.");
		}
		this.supplierSector = this.getSimulation().getSector("Firms");
		// TODO "Firms" sould be a parameter
		if (this.supplierSector == null) {
			throw new RuntimeException("Supplier sector is missing.");
		}
		this.dataset = new AgentDataset(this);
	}

	/**
	 * Closes the worker at the end of the period.
	 */
	private void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
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
		this.open = false;
	}

	/**
	 * The consumption phase.
	 */
	private void consumption() {
		if (!this.open) {
			throw new RuntimeException("Closed.");
		}
		long consumptionVolume = 0;
		long consumptionValue = 0;
		long budget = this.account.getAmount();
		this.dataset.put("consumptionBudget", budget);
		if (budget > 0) {
			final Agent[] selection = this.supplierSector.select(10);
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
				final int consumVol;
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
	 * Search for job opportunities.
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

			if (this.reservationWage > 0 && this.unempDuration > wageResistanceParam) {
				this.reservationWage -= this.wageFlex * getRandom().nextFloat();
			}
			final JobOffer jobOffer = selectJobOffer(this.employerSector);
			if (jobOffer != null && jobOffer.getWage() >= this.reservationWage) {
				// Jamel.println(this.getName(), " I've got a job!");
				this.jobContract = jobOffer.apply(this);
				this.status = EMPLOYED;
				this.unempDuration = 0;
			}
			break;

		case EMPLOYED:
			this.unempDuration = 0;
			this.reservationWage = this.jobContract.getWage();
			this.wageFlex = wageFlexParam * this.jobContract.getWage();
			break;

		default:
			throw new RuntimeException("Unexpected status: " + this.status);
		}
	}

	/**
	 * Opens the worker at the beginning of the period.
	 */
	private void open() {
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		this.exhausted = false;
		this.wage.cancel();
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

	@Override
	public Long getAssetTotalValue() {
		throw new NotUsedException();
	}

	@Override
	public int getBorrowerStatus() {
		throw new NotUsedException();
	}

	@Override
	public Double getData(String dataKey, String period) {
		return this.dataset.getData(dataKey);
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
