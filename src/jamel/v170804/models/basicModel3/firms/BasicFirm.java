package jamel.v170804.models.basicModel3.firms;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import jamel.util.Agent;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.NotUsedException;
import jamel.util.Parameters;
import jamel.util.Sector;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.data.BasicAgentDataset;
import jamel.v170804.models.basicModel3.banks.Account;
import jamel.v170804.models.basicModel3.banks.Bank;
import jamel.v170804.models.basicModel3.banks.Cheque;
import jamel.v170804.models.basicModel3.households.Shareholder;
import jamel.v170804.models.basicModel3.households.Worker;

/**
 * A basic firm.
 */
public class BasicFirm extends JamelObject implements Firm {

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = BasicFirmKeys.getInstance();

	/**
	 * To test the validity of job contracts.
	 * 
	 * @return a predicate which returns <code>true</code> for not valid
	 *         contracts.
	 */
	final private static Predicate<LaborContract> isNotValid() {
		return contract -> !contract.isValid();
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
		case "planProduction":
			action = (agent) -> {
				((BasicFirm) agent).planProduction();
			};
			break;
		case "production":
			action = (agent) -> {
				((BasicFirm) agent).production();
			};
			break;
		case "payWages":
			action = (agent) -> {
				((BasicFirm) agent).payWages();
			};
			break;
		case "payDividends":
			action = (agent) -> {
				((BasicFirm) agent).payDividends();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	/**
	 * The bank account of this firm.
	 */
	private final Account account;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The factory.
	 */
	final private Factory factory;

	/**
	 * The id of this firm.
	 */
	final private int id;

	/**
	 * The normal volume of inventories.
	 */
	final private double inventoriesNormalVolume;

	/**
	 * Maximal duration of a job contract.
	 */
	final private int jobContractMax;

	/**
	 * Minimal duration of a job contract.
	 */
	final private int jobContractMin;

	/**
	 * The job offer of this firm.
	 */
	private final BasicJobOffer jobOffer;

	/**
	 * The number of jobs offered (= the initial number of vacancies, before the
	 * matching procedure on the labour market).
	 */
	private int jobOffers;

	/**
	 * The markup.
	 */
	private Double markup = null;

	/**
	 * The flexibility of the markup.
	 */
	final private double markupFlexibility;

	/**
	 * The normal rate of utilization.
	 */
	private final Double normalUtilizationRate;

	/**
	 * The owners of the firm.
	 */
	final private List<Shareholder> owners = new LinkedList<>();

	/**
	 * The list of the labor contracts.
	 */
	private final LinkedList<BasicLaborContract> payroll = new LinkedList<>();

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * The supply of this firm.
	 */
	private final BasicSupply supply;

	/**
	 * The flexibility of the utilization rate.
	 */
	private final double utilizationFlexibility;

	/**
	 * The utilization target.
	 */
	private Double utilizationTarget = null;

	/**
	 * The flexibility of the wage.
	 */
	private final double wageFlex;

	private double averageSales = 0;

	/**
	 * Creates a new firm.
	 * 
	 * @param sector
	 *            the sector of this firm.
	 * @param id
	 *            the id of this firm.
	 */
	public BasicFirm(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;

		final Parameters params = this.sector.getParameters();
		ArgChecks.nullNotPermitted(params, "params");

		this.id = id;
		this.dataset = new BasicAgentDataset(this, keys);

		this.markup = params.get("pricing").getDoubleAttribute("initialMarkup");
		this.markupFlexibility = params.get("pricing").getDoubleAttribute("markupFlexibility");
		this.jobContractMax = params.get("workforce").get("jobContracts").getIntAttribute("max");
		this.jobContractMin = params.get("workforce").get("jobContracts").getIntAttribute("min");
		this.wageFlex = params.getDoubleValue("workforce.wage.flexibility");
		this.utilizationFlexibility = params.getDoubleValue("utilization.flexibility");
		this.utilizationTarget = params.getDoubleValue("utilization.initialValue");
		this.normalUtilizationRate = params.getDoubleValue("utilization.normalRate");

		final String bankSectorName = params.get("financing").getAttribute("bankSector");
		this.account = ((Bank) this.getSimulation().getSector(bankSectorName).select(1)[0]).openAccount(this);

		this.factory = new BasicFactory(params.get("production"), this);
		this.jobOffer = new BasicJobOffer(this);
		this.jobOffer.setWage(params.get("workforce").get("wage").getIntAttribute("initialValue"));
		this.supply = new BasicSupply(this);

		this.inventoriesNormalVolume = this.factory.getCapacity() * this.factory.getProductivity()
				* params.get("inventories").getDoubleAttribute("normalVolumeRatio");

	}

	/**
	 * Initializes the owners of this firm.
	 */
	private void initOwners() {
		final Agent[] selection = this.getSimulation().getSector("Shareholders").select(10);
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] != null) {
				this.owners.add((Shareholder) selection[i]);
			}
		}
	}

	/**
	 * The dividend payment phase.
	 */
	private void payDividends() {
		if (this.owners.isEmpty()) {
			throw new RuntimeException("No owners.");
		}

		final long cash = this.account.getAmount();
		final long assets = cash + this.factory.getValue();
		final long liabilities = this.account.getDebt();
		final long capital = assets - liabilities;
		final long capitalTarget = (long) (assets * 0.5);
		final long capitalExcess = Math.max(capital - capitalTarget, 0);
		long dividends = 0;
		if (capitalExcess > this.owners.size()) {
			final long newDividend = Math.min(this.account.getAmount(), capitalExcess) / this.owners.size();
			if (newDividend > 0) {
				for (final Shareholder shareholder : this.owners) {
					shareholder.acceptDividendCheque(this.account.issueCheque(shareholder, newDividend));
					dividends += newDividend;
				}
			}
		}
		this.dataset.put(keys.dividends, dividends);
	}

	/**
	 * The wage payment phase.
	 */
	private void payWages() {

		/*
		 * Première passe : on calcule le wagebill.
		 */

		long wages = 0;
		for (BasicLaborContract contract : this.payroll) {
			wages += contract.getWage();
		}

		/*
		 * Besoin d'un financement ?
		 */

		if (wages > this.account.getAmount()) {
			this.account.borrow(wages - this.account.getAmount(), 12, false);
		}

		/*
		 * Deuxième passe : on paie.
		 */

		for (BasicLaborContract contract : this.payroll) {
			contract.getWorker().acceptPayCheque(this.account.issueCheque(contract.getWorker(), contract.getWage()));
		}

	}

	/**
	 * Phase of production planing. Decides how much to produce.
	 */
	private void planProduction() {

		this.updateMarkup();

		// On commence par faire le ménage dans la liste des contrats de
		// travail, en retirant les contrats échus.

		this.payroll.removeIf(isNotValid());

		final double averageInventories;
		final int t = this.getPeriod();
		double sum = 0;
		int count = 0;
		for (int i = 0; i < 12; i++) {
			final Double value = this.dataset.getData(keys.inventoriesVolume, t - i);
			if (value != null) {
				sum += value;
				count++;
			}
		}
		if (count > 0) {
			averageInventories = sum / count;
		} else {
			averageInventories = 0;
		}

		final double inventoryDesequilibria = this.inventoriesNormalVolume - averageInventories;

		this.dataset.put(keys.inventoryDesequilibria, inventoryDesequilibria);

		final double productionTarget = 1.1 * averageSales + 0.2 * inventoryDesequilibria;

		this.dataset.put(keys.productionTarget, productionTarget);

		final double maxProduction = this.factory.getProductivity() * this.factory.getCapacity();

		this.dataset.put(keys.productionMax, maxProduction);

		if (productionTarget <= 0) {
			this.utilizationTarget = 0.;
		} else if (productionTarget >= maxProduction) {
			this.utilizationTarget = 1.;
		} else {
			this.utilizationTarget = productionTarget / maxProduction;
		}

		// Détermine le niveau souhaité de la production.

		// TODO: devrait être basé surtout sur le niveau des ventes passées,
		// corrigées par le niveau des stocks actuel.

		/*final double delta = this.utilizationFlexibility * this.getRandom().nextFloat();
		if (this.factory.getInventories().getVolume() < this.inventoriesNormalVolume) {
			this.utilizationTarget += delta;
			if (this.utilizationTarget > 1) {
				this.utilizationTarget = 1.;
			}
		} else {
			this.utilizationTarget -= delta;
			if (this.utilizationTarget < 0) {
				this.utilizationTarget = 0.;
			}
		}*/

		this.dataset.put(keys.utilizationTarget, this.utilizationTarget);

		final int workforceTarget = (int) Math.round(this.utilizationTarget * this.factory.getCapacity());

		// Licencier ou embaucher.

		final int firing;
		if (this.payroll.size() > workforceTarget) {
			firing = this.payroll.size() - workforceTarget;
			do {
				// Last in first out.
				this.payroll.removeLast().breach();
			} while (workforceTarget < this.payroll.size());
		} else if (this.payroll.size() < workforceTarget) {
			this.jobOffers = workforceTarget - this.payroll.size();
			this.jobOffer.setVacancies(this.jobOffers);
			firing = 0;
		} else {
			firing = 0;
		}
		this.dataset.put(keys.jobOffers, this.jobOffer.getVacancies());
		this.dataset.put(keys.workforceTarget, workforceTarget);
		this.dataset.put(keys.firing, firing);
	}

	/**
	 * The production phase.
	 */
	private void production() {
		this.updateWage();
		final double price;
		final double oldPrice;
		final int changePrice;
		this.factory.production(this.payroll);
		if (this.factory.getInventories().getVolume() > 0) {
			// Updates the price
			final double newPrice = (this.markup * this.factory.getInventories().getValue())
					/ this.factory.getInventories().getVolume();
			oldPrice = this.supply.getPrice();
			// TODO this threshold should be a parameter.
			final double threshold = 0.025;
			if (Math.abs(newPrice / oldPrice - 1.) > threshold) {
				price = newPrice;
				changePrice = 1;
			} else {
				price = oldPrice;
				changePrice = 0;
			}
			this.supply.update(this.factory.getInventories().getVolume(), price);

			this.dataset.put(keys.supplyVolume, this.factory.getInventories().getVolume());
			this.dataset.put(keys.supplyValue, this.supply.getPrice() * this.factory.getInventories().getVolume());
			this.dataset.put(keys.supplyCost, this.factory.getInventories().getValue());
		} else {
			price = this.supply.getPrice();
			oldPrice = price;
			changePrice = 0;
		}
		this.dataset.put(keys.price, price);
		this.dataset.put(keys.changePrice, changePrice);
	}

	/**
	 * Updates the markup.
	 */
	private void updateMarkup() {
		final double delta1 = this.markupFlexibility * this.getRandom().nextDouble();
		final double delta2;
		final double salesTarget = this.normalUtilizationRate * this.factory.getCapacity()
				* this.factory.getProductivity();
		if (this.averageSales < salesTarget) {
			delta2 = -delta1;
		} else if (this.averageSales > salesTarget) {
			delta2 = delta1;
		} else {
			delta2 = 0;
		}
		this.markup += delta2;
		if (this.markup < 0.1) {
			this.markup = 0.1;
		}
		this.dataset.put(keys.deltaMarkup, delta2);
	}

	/**
	 * Updates the markup.
	 */
	private void updateMarkup2() {
		final double delta1 = this.markupFlexibility * this.getRandom().nextDouble();
		final double delta2;
		if (this.factory.getInventories().getVolume() > this.inventoriesNormalVolume
				&& this.utilizationTarget < this.normalUtilizationRate) {
			delta2 = -delta1;
		} else if (this.factory.getInventories().getVolume() < this.inventoriesNormalVolume
				&& this.utilizationTarget > this.normalUtilizationRate) {
			delta2 = delta1;
		} else {
			delta2 = 0;
		}
		this.markup += delta2;
		if (this.markup < 0.1) {
			this.markup = 0.1;
		}
		this.dataset.put(keys.deltaMarkup, delta2);
	}

	/**
	 * Updates the wage.
	 */
	private void updateWage() {
		long newWage;
		final double alea = this.getRandom().nextDouble();
		if (this.jobOffer.getVacancies() > 0) {
			newWage = (long) ((1. + alea * this.wageFlex) * this.jobOffer.getWage());
		} else if (this.jobOffers > 0) {
			newWage = (long) ((1. - alea * this.wageFlex) * this.jobOffer.getWage());
		} else {
			newWage = this.jobOffer.getWage();
		}
		if (newWage < 1) {
			newWage = 1;
		}
		this.jobOffer.setWage(newWage);
	}

	/**
	 * Accepts the specified cheque.
	 * 
	 * @param cheque
	 *            a cheque to be deposited on the firm account.
	 */
	@Override
	public void accept(Cheque cheque) {
		this.account.deposit(cheque);
	}

	/**
	 * Closes the firm at the end of the period.
	 */
	@Override
	public void close() {
		// TODO revoir l'articulation avec factory
		final long assets = this.account.getAmount() + this.factory.getValue();
		final Double debtRatio;
		if (assets != 0) {
			debtRatio = (double) (this.account.getDebt()) / assets;
		} else if (assets == 0) {
			debtRatio = 0d;
		} else {
			debtRatio = null;
		}
		final int solvent;
		if (assets <= this.account.getDebt()) {
			solvent = 0;
		} else {
			solvent = 1;
		}
		/*if (this.getPeriod()>100 && solvent==0) {
			Jamel.println(this.getPeriod(),this.getName(),"insolvent");
		}*/
		this.dataset.put(keys.assets, assets);
		this.dataset.put(keys.debtRatio, debtRatio);
		this.dataset.put(keys.solvent, solvent);
		this.dataset.put(keys.count, 1);
		this.dataset.put(keys.inventoriesNormalVolume, this.inventoriesNormalVolume);
		this.dataset.put(keys.inventoriesValue, this.factory.getInventories().getValue());
		this.dataset.put(keys.inventoriesVolume, this.factory.getInventories().getVolume());
		this.dataset.put(keys.liabilities, this.account.getDebt());
		this.dataset.put(keys.debtService, this.account.getDebtService());
		this.dataset.put(keys.interests, this.account.getInterests());
		this.dataset.put(keys.markup, this.markup);
		this.dataset.put(keys.money, this.account.getAmount());
		this.dataset.put(keys.tangibleAssets, this.factory.getValue());
		this.dataset.put(keys.vacancies, this.jobOffer.getVacancies());
		this.dataset.put(keys.wage, this.jobOffer.getWage());
		this.dataset.put(keys.workforce, this.payroll.size());
		this.supply.close();
		this.jobOffer.close();
		this.factory.close();
		this.dataset.close();
		super.close();
	}

	@Override
	public long getAssetTotalValue() {
		// Called by the bank in the case of a bankruptcy.
		return this.account.getAmount() + this.factory.getValue();
	}

	@Override
	public int getBorrowerStatus() {
		throw new NotUsedException();
	}

	@Override
	public Double getData(String dataKey, int period) {
		return this.dataset.getData(dataKey, period);
	}

	/**
	 * Returns the dataset of this firm.
	 * 
	 * @return the dataset of this firm.
	 */
	@Override
	public AgentDataset getDataset() {
		return this.dataset;
	}

	@Override
	public JobOffer getJobOffer() {
		final JobOffer result;
		if (this.jobOffer.isEmpty()) {
			result = null;
		} else {
			result = this.jobOffer;
		}
		return result;
	}

	@Override
	public String getName() {
		return "Firm_" + this.id;
	}

	/**
	 * Returns a new job contract for the specified worker.
	 * 
	 * @param worker
	 *            the applicant.
	 * @return a new job contract.
	 */
	@Override
	public LaborContract getNewJobContract(Worker worker) {
		final int term = this.jobContractMin + this.getRandom().nextInt(this.jobContractMax);
		final BasicLaborContract contract = new BasicLaborContract(getSimulation(), this, worker,
				this.jobOffer.getWage(), term);
		this.payroll.add(contract);
		return contract;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

	@Override
	public Supply getSupply() {
		final Supply result;
		if (!supply.isEmpty()) {
			result = this.supply;
		} else {
			result = null;
		}
		return result;
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
		final long assets = this.account.getAmount() + this.factory.getValue();
		final long debt = this.account.getDebt();
		return assets > debt;
	}

	/**
	 * Opens the firm at the beginning of the period.
	 */
	@Override
	public void open() {
		if (this.owners.isEmpty()) {
			initOwners();
		}
		this.jobOffer.open();
		this.dataset.open();
		this.supply.open();
		this.factory.open();
		super.open();
		this.jobOffers = 0;
		this.averageSales = getAverageSales();
	}

	private double getAverageSales() {
		final double result;
		final int t = this.getPeriod();
		double sum = 0;
		int count = 0;
		for (int i = 0; i < 12; i++) {
			final Double value = this.dataset.getData(keys.salesVolume, t - i);
			if (value != null) {
				sum += value;
				count++;
			}
		}
		if (count > 0) {
			result = sum / count;
		} else {
			result = 0;
		}
		return result;
	}

	/**
	 * Returns the specified volume of goods.
	 * 
	 * @param volume
	 *            the volume of goods to be returned.
	 * @return the specified volume of goods.
	 */
	@Override
	public Goods supply(long volume) {
		return this.factory.getInventories().take(volume);
	}

}
