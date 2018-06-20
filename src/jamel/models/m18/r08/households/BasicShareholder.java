package jamel.models.m18.r08.households;

import java.util.Arrays;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r08.firms.BasicGoods;
import jamel.models.m18.r08.roles.Bank;
import jamel.models.m18.r08.roles.Shareholder;
import jamel.models.m18.r08.roles.Supplier;
import jamel.models.m18.r08.util.AbstractAgent;
import jamel.models.m18.r08.util.Account;
import jamel.models.m18.r08.util.AccountHolder;
import jamel.models.m18.r08.util.BasicAmount;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.Commodities;
import jamel.models.m18.r08.util.Equity;
import jamel.models.m18.r08.util.Supply;
import jamel.models.m18.r08.util.Tools;
import jamel.util.Agent;
import jamel.util.Parameters;
import jamel.util.Sector;

/*
 * Represents a shareholder.
 * 
 * 2018-03-10 : jamel/models/m18/r01/households/BasicShareholder.java
 * 
 * 2018-03-02 : implement Household.
 * Pour lui permettre d'être appelé par l'objet marché des biens lors de la
 * phase de consommation.
 * 
 * 2018-02-16 : BasicShareholder2
 * On tente d'aligner le comportement d'épargne de cet agent sur celui du
 * capitaliste d'ICC
 * => prendre en compte la valeur des firmes détenues dans les calculs du revenu
 * et de la consommation.
 * 
 * 2017-11-08 : new name for "BasicShareholder2"
 */

/**
 * A basic shareholder.
 */
public class BasicShareholder extends AbstractAgent implements Shareholder, Household {

	/**
	 * The data keys.
	 */
	protected static final ShareholderKeys keys = ShareholderKeys.getInstance();

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
		case "consumption":
			Jamel.notUsed();
			// 2018-01-28 : il n'y a plus de phase de job search c'est le marché
			// du travail qui organise le matching
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
	 * The bank account of this shareholder.
	 */
	protected final Account account;

	/**
	 * The set of constants of this worker.
	 */
	protected final ShareholderConstants consts;

	/**
	 * The amount of the dividends received for this period.
	 */
	final private BasicAmount dividends = new BasicAmount();

	/**
	 * The equities owned by this shareholder.
	 */
	final private Equities equities = new Equities();

	/**
	 * The book value of equities, computed at the end of the period.
	 */
	protected long equitiesValue = 0;

	/**
	 * The value of the equity bought and to be provided.
	 */
	private Long expectedEquityAmount = null;

	/**
	 * The regular suppliers of this worker.
	 */
	final private Supplier[] suppliers;

	/**
	 * The sector of the suppliers.
	 */
	final private Sector supplierSector;

	/**
	 * Creates a new shareholder.
	 * 
	 * @param sector
	 *            the sector of this shareholder.
	 * @param id
	 *            the id of this shareholder.
	 */
	public BasicShareholder(final Sector sector, final int id) {
		super(sector, id);

		final Parameters params = this.sector.getParameters();
		if (params == null) {
			throw new RuntimeException("Parameters are null.");
		}
		this.consts = new ShareholderConstants(params);

		final String bankSectorName = params.getAttribute("bankSector");
		this.account = ((Bank) this.getSimulation().getSector(bankSectorName).selectArray(1)[0]).openAccount(this);

		final Parameters goodMarketParams = params.get("goodMarket");
		this.supplierSector = this.getSimulation().getSector(goodMarketParams.getAttribute("suppliers"));
		this.suppliers = new Supplier[this.consts.supplySearch];

	}

	/**
	 * Computes and returns the consumption budget.
	 * 
	 * @return the consumption budget.
	 */
	protected long consumptionBudget() {
		final double averageIncome = (this.agentDataset.sum(keys.dividends, 12)
				+ this.agentDataset.sum(keys.capitalAppreciation, 12)) / 12;
		final long savingsTarget = (long) (12 * averageIncome * this.consts.savingsRatioTarget);
		final long savings = (long) (this.equitiesValue + this.account.getAmount() - averageIncome);

		this.putData(keys.savingsTarget, savingsTarget);
		this.putData(keys.savings, savings);

		long budget;
		if (getPeriod() < this.consts.supervision) {
			budget = this.account.getAmount();
		} else {
			final long budget2;
			if (savings < savingsTarget) {
				budget2 = (long) ((1. - this.consts.savingsPropensityToSave) * averageIncome);
			} else {
				budget2 = (long) (averageIncome
						+ (savings - savingsTarget) * this.consts.savingsPropensityToConsumeExcess);
			}
			budget = Math.min(this.account.getAmount(), budget2);
			this.putData(keys.consumptionBudget2, budget2);
		}
		return budget;
	}

	/**
	 * Purchases and consumes the specified amount of consumption goods.
	 * 
	 * @param budget
	 *            the value of the goods to be purchased and consumed.
	 */
	protected void purchase(final long budget) {
		long consumptionVolume = 0;
		long consumptionValue = 0;
		long amount = budget;
		if (amount > 0) {
			this.suppliers[suppliers.length - 1] = (Supplier) this.supplierSector.select();

			Arrays.sort(suppliers, Tools.supplierComparator);

			for (Supplier supplier : suppliers) {
				if (supplier == null || supplier.getSupply() == null || supplier.getSupply().isEmpty()
						|| supplier.getSupply().getPrice() > amount) {
					break;
				}

				final Supply supply = supplier.getSupply();
				final long spending;
				final long consumVol;
				if (supply.getValue() <= amount) {
					consumVol = supply.getVolume();
					spending = (long) (consumVol * supply.getPrice());
					if (spending != supply.getValue()) {
						throw new RuntimeException("Inconsistency.");
					}
				} else {
					consumVol = (int) (amount / supply.getPrice());
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
					if (!((BasicGoods) goods).getQuality().equals(this.consts.consumptionGoodsQuality)) {
						throw new RuntimeException("Bad quality: " + ((BasicGoods) goods).getQuality());
					}
					if (goods.getVolume() != consumVol) {
						Jamel.println("goods.getVolume(): " + goods.getVolume(), "consumVol: " + consumVol);
						throw new RuntimeException("Bad volume");
					}
					amount -= spending;
					consumptionValue += spending;
					consumptionVolume += goods.getVolume();
					goods.consume();
				}

			}

		}

		this.putData(keys.consumptionVolume, consumptionVolume);
		this.putData(keys.consumptionValue, consumptionValue);
		// penser à updater les chiffres de l'épargne.
	}

	@Override
	public void acceptDividendCheque(Cheque cheque) {
		this.dividends.plus(cheque.getAmount());
		this.account.deposit(cheque);
	}

	@Override
	public void acceptTitle(Equity title) {
		if ((this.expectedEquityAmount != null && title.getValue() != this.expectedEquityAmount)
				|| (this.expectedEquityAmount == null && title.getValue() != 1)) {
			Jamel.println();
			Jamel.println("***");
			Jamel.println("expectedEquityAmount", expectedEquityAmount);
			Jamel.println("title.getValue()", title.getValue());
			throw new RuntimeException("Inconsistency");
		}
		this.expectedEquityAmount = null;
		this.equities.add(title);
	}

	/**
	 * Closes the shareholder at the end of the period.
	 */
	@Override
	public void close() {
		if (this.expectedEquityAmount != null) {
			throw new RuntimeException("Inconsistency");
		}
		this.putData(keys.count, 1);
		this.putData(keys.money, this.account.getAmount());
		this.putData(keys.dividends, this.dividends.getAmount());
		final long newPortfolioValue = this.equities.getValue();
		final long capitalAppreciation = newPortfolioValue - this.equitiesValue;
		this.putData(keys.capitalAppreciation, capitalAppreciation);
		this.equitiesValue = newPortfolioValue;
		this.putData(keys.equities, this.equitiesValue);
		super.close();
	}

	/**
	 * The consumption phase.
	 */
	@Override
	public void consumption() {

		// 2018-03-02 : publique, appelée par le marché des biens.

		final long budget = this.consumptionBudget();
		this.putData(keys.consumptionBudget, budget);
		this.purchase(budget);

	}

	@Override
	public Cheque contribute(final AccountHolder payee, final long amount) {
		if (this.expectedEquityAmount != null) {
			throw new RuntimeException("Inconsistency");
		}
		final Cheque result;
		if (this.account.getAmount() > amount) {
			result = this.account.issueCheque(payee, amount);
			this.expectedEquityAmount = amount;
		} else {
			result = null;
		}
		return result;
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
	public boolean isSolvent() {
		Jamel.notUsed();
		return false;
	}

	/**
	 * Opens the shareholder at the beginning of the period.
	 */
	@Override
	public void open() {
		super.open();
		this.dividends.cancel();
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
