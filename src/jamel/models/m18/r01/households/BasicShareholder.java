package jamel.models.m18.r01.households;

import java.util.Arrays;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.models.m18.r01.data.AgentDataset;
import jamel.models.m18.r01.data.BasicAgentDataset;
import jamel.models.m18.r01.data.BasicPeriodDataset;
import jamel.models.m18.r01.data.PeriodDataset;
import jamel.models.m18.r01.firms.BasicGoods;
import jamel.models.m18.r01.util.BasicAmount;
import jamel.models.m18.r01.util.Equity;
import jamel.models.m18.r01.util.Shareholder;
import jamel.models.m18.r01.util.Tools;
import jamel.models.util.Account;
import jamel.models.util.Bank;
import jamel.models.util.Cheque;
import jamel.models.util.Commodities;
import jamel.models.util.Supplier;
import jamel.models.util.Supply;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
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
public class BasicShareholder extends JamelObject implements Shareholder, Household {

	/**
	 * Represents a set of constants.
	 */
	private class Constants {

		/**
		 * The supervision period.
		 */
		final private float supervision;

		/**
		 * The savings target ratio.
		 */
		final public String consumptionGoodsQuality;

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
			this.consumptionGoodsQuality = parameters.getString("goodMarket.quality");
		}

	}

	/**
	 * The data keys.
	 */
	private static final BasicShareholderKeys keys = BasicShareholderKeys.getInstance();

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
	final private Account account;

	/**
	 * The agent dataset.
	 */
	final private AgentDataset agentDataset;

	/**
	 * The set of constants of this worker.
	 */
	final private Constants consts;

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
	private long equitiesValue = 0;

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The dataset.
	 */
	private PeriodDataset periodDataset = null;

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
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;

		final Parameters params = this.sector.getParameters();
		if (params == null) {
			throw new RuntimeException("Parameters are null.");
		}
		this.consts = new Constants(params);

		final String bankSectorName = params.getAttribute("bankSector");
		this.account = ((Bank) this.getSimulation().getSector(bankSectorName).selectArray(1)[0]).openAccount(this);

		final Parameters goodMarketParams = params.get("goodMarket");
		this.supplierSector = this.getSimulation().getSector(goodMarketParams.getAttribute("suppliers"));
		this.suppliers = new Supplier[this.consts.supplySearch];

		this.agentDataset = new BasicAgentDataset(this);
	}

	@Override
	public void acceptDividendCheque(Cheque cheque) {
		this.dividends.plus(cheque.getAmount());
		this.account.deposit(cheque);
	}

	@Override
	public void acceptTitle(Equity title) {
		this.equities.add(title);
	}

	/**
	 * Closes the shareholder at the end of the period.
	 */
	@Override
	public void close() {
		this.periodDataset.put(keys.count, 1);
		this.periodDataset.put(keys.money, this.account.getAmount());
		this.periodDataset.put(keys.dividends, this.dividends.getAmount());
		final long newPortfolioValue = this.equities.getValue();
		final long capitalAppreciation = newPortfolioValue - this.equitiesValue;
		this.periodDataset.put(keys.capitalAppreciation, capitalAppreciation);
		this.equitiesValue = newPortfolioValue;
		this.periodDataset.put(keys.equities, this.equitiesValue);
		this.agentDataset.put(periodDataset);
	}

	/**
	 * The consumption phase.
	 */
	@Override
	public void consumption() {

		// 2018-03-02 : publique, appelée par le marché des biens.

		long budget;
		if (getPeriod() < this.consts.supervision) {
			budget = this.account.getAmount();
		} else {
			final double averageIncome = (this.agentDataset.sum(keys.dividends, 12)
					+ this.agentDataset.sum(keys.capitalAppreciation, 12)) / 12;
			final long savingsTarget = (long) (12 * averageIncome * this.consts.savingsRatioTarget);
			final long savings = (long) (this.equitiesValue + this.account.getAmount() - averageIncome);
			if (savings < savingsTarget) {
				budget = Math.min(this.account.getAmount(),
						(long) ((1. - this.consts.savingsPropensityToSave) * averageIncome));
			} else {
				budget = Math.min(this.account.getAmount(), (long) (averageIncome
						+ (savings - savingsTarget) * this.consts.savingsPropensityToConsumeExcess));
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
					if (!((BasicGoods) goods).getQuality().equals(this.consts.consumptionGoodsQuality)) {
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
		// penser à updater les chiffres de l'épargne.
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
		return "shareholder_" + this.id;
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
		this.dividends.cancel();
		this.periodDataset = new BasicPeriodDataset(this);

	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

}
