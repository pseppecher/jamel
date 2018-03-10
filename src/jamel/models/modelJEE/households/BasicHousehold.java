package jamel.models.modelJEE.households;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import jamel.Jamel;
import jamel.data.AgentDataset;
import jamel.data.BasicAgentDataset;
import jamel.data.DataKeys;
import jamel.models.modelJEE.capital.StockCertificate;
import jamel.models.modelJEE.util.Asset;
import jamel.models.modelJEE.util.Memory;
import jamel.models.util.Account;
import jamel.models.util.Bank;
import jamel.models.util.Cheque;
import jamel.models.util.Commodities;
import jamel.models.util.Employer;
import jamel.models.util.JobContract;
import jamel.models.util.JobOffer;
import jamel.models.util.Supplier;
import jamel.models.util.Supply;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Sector;

/**
 * A basic household.
 */
@SuppressWarnings("javadoc")
public class BasicHousehold extends JamelObject implements Household {

	/**
	 * A class to parse and store the constant parameters of the firm.
	 */
	private class Constants {

		final private int jobsSelection;

		final private float savingsPropensityToConsumeExcess;

		final private float savingsPropensityToSave;

		final private float savingsRatioTarget;

		final private int suppliesSelection;

		private final float wageFlexibility;

		private final float wageResistance;

		/**
		 * Creates a new set of parameters by parsing the specified
		 * {@code Parameters}.
		 * 
		 * @param params
		 *            the parameters to be parsed.
		 */
		private Constants(Parameters params) {
			this.jobsSelection = params.getInt(N_JOB_OFFERS);
			this.suppliesSelection = params.getInt(N_SUPPLIES);
			this.savingsPropensityToSave = params.getFloat(SAV_PROP);
			this.savingsPropensityToConsumeExcess = params.getFloat(SAV_PROP2_CONSUM_EXCESS);
			this.savingsRatioTarget = params.getFloat(SAV_TARGET);
			this.wageFlexibility = params.getFloat(WAGE_FLEX);
			this.wageResistance = params.getFloat(WAGE_RESIST);
		}
	}

	private static final int EMPLOYED = 1;

	/**
	 * The data keys.
	 */
	private static final BasicHouseholdKeys keys = BasicHouseholdKeys.getInstance();

	private static final String N_JOB_OFFERS = "jobs.selection";

	private static final String N_SUPPLIES = "supplies.selection";

	private final static String SAV_PROP = "savings.propensityToSave";

	private final static String SAV_PROP2_CONSUM_EXCESS = "savings.propensityToConsumeExcess";

	private final static String SAV_TARGET = "savings.ratioTarget";

	private static final int UNEMPLOYED = 0;

	private final static String WAGE_FLEX = "wage.flexibility";

	private final static String WAGE_RESIST = "wage.resistance";

	/**
	 * The job offer comparator.
	 * <p>
	 * To compare jobs according to the wage they offer.
	 */
	public static final Comparator<JobOffer> jobComparator = new Comparator<JobOffer>() {
		@Override
		public int compare(JobOffer offer1, JobOffer offer2) {
			return (new Long(offer2.getWage()).compareTo(offer1.getWage()));
		}
	};

	/**
	 * The supply comparator.
	 * <p>
	 * To compare supplies according to their price.
	 */
	public static final Comparator<Supply> supplyComparator = new Comparator<Supply>() {
		@Override
		public int compare(Supply offer1, Supply offer2) {
			return (-(new Double(offer2.getPrice())).compareTo(offer1.getPrice()));
		}
	};

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
				((BasicHousehold) agent).open();
			};
			break;
		case "take_dividends":
			action = (agent) -> {
				((BasicHousehold) agent).takeDividends();
			};
			break;
		case "job_search":
			action = (agent) -> {
				((BasicHousehold) agent).jobSearch();
			};
			break;
		case "consumption":
			action = (agent) -> {
				((BasicHousehold) agent).consumption();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicHousehold) agent).close();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}

		return action;

	}

	static public DataKeys getDataKeys() {
		return keys;
	}

	/** The current account. */
	private final Account account;

	/** Items of property. */
	private final AssetPortfolio assetPortfolio = new BasicAssetPortfolio();

	private final Constants consts;

	private long consumptionValue = 0;

	private long consumptionVolume = 0;

	/** The data of the agent. */
	private AgentDataset dataset = new BasicAgentDataset(this, keys);

	/** The job contract. */
	private JobContract jobContract;

	/** The name of the household. */
	private final String name;

	private long netWorth = 0;

	/** The memory. */
	final private Memory<Long> recentIncome;

	/** The households sector. */
	private final Sector sector;

	private int unempDuration = 0;

	/**
	 * A map that stores the variables of the household. TODO: remplacer par des
	 * champs.
	 */
	private final Map<String, Number> variables = new HashMap<String, Number>();

	private Float wageFlex = null;

	private long wages = 0;

	/**
	 * Creates a household.
	 * 
	 * @param name
	 *            the name of the new household.
	 * @param sector
	 *            the households sector.
	 */
	public BasicHousehold(String name, Sector sector) {
		super(sector.getSimulation());
		this.name = name;
		this.sector = sector;
		this.consts = new Constants(this.sector.getParameters());
		this.recentIncome = new Memory<Long>(12);
		this.account = ((Bank) this.getSector("Banks").selectAll().get(0)).openAccount(this);
		this.variables.put("status", UNEMPLOYED);
		this.variables.put("unemployement duration", 0);
	}

	private JobOffer[] getJobOffers(int nOffers) {
		final ArrayList<JobOffer> jobOffersList = new ArrayList<JobOffer>(nOffers);
		final Sector firms = this.getSector("Firms");
		for (final Agent firm : firms.selectList(nOffers)) {
			final JobOffer jobOffer = ((Employer) firm).getJobOffer();
			if (jobOffer != null) {
				jobOffersList.add(jobOffer);
			}
		}
		// Useless shuffle. For historical reasons (conserver intacts les
		// résultats du papier "Is the market").
		Collections.shuffle(jobOffersList, getRandom());
		return jobOffersList.toArray(new JobOffer[jobOffersList.size()]);
	}

	private Supply[] getSupplies(int nSupply) {
		final ArrayList<Supply> list = new ArrayList<Supply>(nSupply);
		final Sector firms = this.getSector("Firms");
		for (final Agent firm : firms.selectList(nSupply)) {
			final Supply supply = ((Supplier) firm).getSupply();
			if (supply != null) {
				list.add(supply);
			}
		}
		return list.toArray(new Supply[list.size()]);
	}

	private void jobSearch() {

		// Updates the status.

		// unempDuration = (Integer) this.variables.get("unemployement
		// duration");
		if ((this.jobContract == null) || !(this.jobContract.isValid())) {
			this.variables.put("status", UNEMPLOYED);
		} else {
			this.variables.put("status", EMPLOYED);
		}

		// Different behaviors according the status.

		switch (this.variables.get("status").intValue()) {

		case UNEMPLOYED:
			this.unempDuration++;
			Double reservationWage = (Double) this.variables.get("reservationWage");
			if (reservationWage == null) {
				reservationWage = 0d;
				this.wageFlex = null;
				this.variables.put("reservationWage", reservationWage);
			}
			if (reservationWage > 0 && this.unempDuration > this.consts.wageResistance) {
				reservationWage -= this.wageFlex * getRandom().nextFloat();
				this.variables.put("reservationWage", reservationWage);
			}
			final JobOffer[] jobOffers = this.getJobOffers(consts.jobsSelection);
			if (jobOffers.length > 0) {
				Arrays.sort(jobOffers, jobComparator);
				if (jobOffers[0].getWage() >= reservationWage) {
					this.jobContract = jobOffers[0].apply(this);
					this.variables.put("status", EMPLOYED);
					this.unempDuration = 0;
				}
			}
			break;

		case EMPLOYED:
			this.unempDuration = 0;
			this.variables.put("reservationWage", (double) this.jobContract.getWage());
			this.wageFlex = this.consts.wageFlexibility * this.jobContract.getWage();
			break;

		default:
			throw new RuntimeException("Unexpected status.");
		}

		this.dataset.put(keys.reservationWage, this.variables.get("reservationWage").doubleValue());
		this.dataset.put(keys.unemployed, 1d - this.variables.get("status").doubleValue());
		this.dataset.put(keys.employed, this.variables.get("status").doubleValue());
		this.dataset.put(keys.unempDuration, this.unempDuration);
	}

	/**
	 * Updates the data.
	 */
	private void updateData() {

		final long equities = assetPortfolio.getNetValue();
		final long previousNetWorth = this.netWorth;
		this.netWorth = equities + account.getAmount();
		final long savings = this.netWorth - previousNetWorth;
		final long income = this.consumptionValue + savings;
		final long profits = income - this.wages;

		this.dataset.put(keys.count, 1);
		this.dataset.put(keys.savings, savings);
		this.dataset.put(keys.income, income);
		this.dataset.put(keys.wage, this.wages);
		this.dataset.put(keys.profit, profits);

		this.dataset.put(keys.money, account.getAmount());
		this.dataset.put(keys.dividends, variables.get("dividends").doubleValue());
		this.dataset.put(keys.equities, equities);
		this.dataset.put(keys.netWorth, this.netWorth);
		this.dataset.put(keys.consumptionValue, this.consumptionValue);
		this.dataset.put(keys.consumptionVolume, this.consumptionVolume);
	}

	@Override
	public boolean acceptJob(JobOffer jobOffer) {
		Jamel.notUsed();
		return false;
	}

	@Override
	public void acceptPayCheque(Cheque paycheck) {
		if (this.jobContract == null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.wages > 0) {
			throw new RuntimeException("Wage already earned.");
		}
		if (paycheck.getAmount() != this.jobContract.getWage()) {
			throw new RuntimeException("Bad cheque amount.");
		}
		this.wages += paycheck.getAmount();
		this.account.deposit(paycheck);
	}

	@Override
	public void addAsset(Asset asset) {
		this.assetPortfolio.add(asset);
	}

	@Override
	public Cheque buy(StockCertificate shares, long price) {
		if (price > getFinancialCapacity()) {
			throw new IllegalArgumentException("Price of shares exceeds the financial capacity of this household.");
		}
		if (price > shares.getBookValue()) {
			final long error = price - shares.getBookValue();
			if (error > 1) {
				/*
				 * throw new IllegalArgumentException(
				 *
				 * "Price of shares is <" + price + "> but the book value is <"
				 * + shares.getBookValue() + ">");
				 */
				// Ca c'est le cas où le prix d'une action a atteint le cours
				// plancher de 1.
			}
		}
		this.assetPortfolio.add(shares);
		// this.dataset.put("sharePurchase", this.dataset.get("sharePurchase") +
		// price);
		return this.account.issueCheque(null, price);
	}

	@Override
	public void close() {
		this.updateData();
		this.dataset.close();
	}

	@Override
	public void consumption() {
		// TODO BEURK: mettre à jour recentIncome à la fin de la période.
		this.recentIncome.add(this.wages + this.variables.get("dividends").longValue());

		final double averageIncome;

		if (this.recentIncome.getSize() == 0) {
			averageIncome = 0;
		} else {
			averageIncome = this.recentIncome.getSum() / this.recentIncome.getSize();
		}
		final long savingsTarget = (long) (12 * averageIncome * this.consts.savingsRatioTarget);
		final long savings = (long) (this.account.getAmount() - averageIncome);
		long consumptionBudget;
		if (savings < savingsTarget) {
			consumptionBudget = Math.min(this.account.getAmount(),
					(long) ((1. - this.consts.savingsPropensityToSave) * averageIncome));
		} else {
			consumptionBudget = Math.min(this.account.getAmount(),
					(long) (averageIncome + (savings - savingsTarget) * this.consts.savingsPropensityToConsumeExcess));
		}

		this.dataset.put(keys.consumptionBudget, (double) consumptionBudget);
		if (consumptionBudget > 0) {
			// final Supply[] supplies =
			// this.sector.getSupplies(consts.N_SUPPLIES).intValue());
			final Supply[] supplies = this.getSupplies(consts.suppliesSelection);

			if (supplies.length > 0) {
				Arrays.sort(supplies, supplyComparator);
				for (Supply supply : supplies) {
					if (consumptionBudget < supply.getPrice()) {
						break;
					}
					final long volume;
					if (supply.getPrice(supply.getVolume()) == consumptionBudget) {
						volume = supply.getVolume();
						// Jamel.println("Case 0: volume="+supply.getVolume());
					} else if (supply.getPrice(supply.getVolume()) > consumptionBudget) {
						volume = (long) (consumptionBudget / supply.getPrice());
						// Jamel.println("Case 1");
						// Jamel.println("supply.getPrice(supply.getVolume()):
						// "+supply.getPrice(supply.getVolume()));
						// Jamel.println("consumptionBudget:
						// "+consumptionBudget);
					} else {
						volume = supply.getVolume();
						// Jamel.println("Case 2: volume="+volume);
					}
					long value = supply.getPrice(volume);// (long) (volume *
					// supply.getPrice());
					/*
					 * if (value==0) { value=1; }
					 */
					if (value <= 0) {
						Jamel.println("consumptionBudget: " + consumptionBudget);
						Jamel.println("Price: " + supply.getPrice());
						Jamel.println("Volume: " + volume);
						throw new RuntimeException("Negative value: " + value);
					}
					final Commodities commod = supply.purchase(volume,
							this.account.issueCheque(supply.getSupplier(), value));
					if (commod.getVolume() != volume) {
						throw new RuntimeException(
								"Consumption volume expected <" + volume + "> but was <" + commod.getVolume() + ">");
					}
					commod.consume();
					consumptionBudget -= value;
					consumptionValue += value;
					consumptionVolume += volume;
				}
			}
		}
	}

	@Override
	public void doEvent(Parameters event) {
		Jamel.notUsed();
	}

	@Override
	public Double getData(int dataIndex, int t) {
		return this.dataset.getData(dataIndex, t);
	}

	@Override
	public long getFinancialCapacity() {
		return this.account.getAmount() / 2;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isEmployed() {
		Jamel.notUsed();
		return false;
	}

	@Override
	public boolean isSolvent() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void open() {
		this.dataset.open();
		this.consumptionValue = 0;
		this.consumptionVolume = 0;
		this.wages = 0;
		this.variables.put("dividends", 0l);
		this.variables.put("worked", 0);
	}

	@Override
	public void removeAsset(Asset asset) {
		this.assetPortfolio.remove(asset);
	}

	@Override
	public boolean satisfy(String criteria) {
		Jamel.notUsed();
		return false;
	}

	@Override
	public void takeDividends() {
		long dividend = 0;
		for (Asset asset : this.assetPortfolio.getList()) {
			// REMARQUE: pour le moment, le portefeuille des m�nages n'est
			// compos� que d'actions.
			final Cheque cheque = ((StockCertificate) asset).getDividend(this);
			if (cheque != null) {
				dividend += cheque.getAmount();
				this.account.deposit(cheque);
			}
		}
		this.variables.put("dividends", dividend);
	}

	@Override
	public void work() {
		if (this.jobContract == null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.variables.get("worked").intValue() != 0) {
			throw new RuntimeException("Already worked.");
		}
		if (this.wages == 0) {
			throw new RuntimeException("Wage not paid.");
		}
		this.variables.put("worked", 1);

	}

}
