package jamel.jamel.households;

import jamel.Jamel;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.util.ConsistencyException;
import jamel.jamel.util.Memory;
import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.AssetPortfolio;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.BasicAssetPortfolio;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.JobContract;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;
import jamel.jamel.widgets.Supply;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A basic household.
 */
public class BasicHousehold implements Household {

	@SuppressWarnings("javadoc")
	private static final int EMPLOYED = 1;

	/**
	 * The job offer comparator.
	 * <p>
	 * To compare jobs according to the wage they offer.
	 */
	private static final Comparator<JobOffer> jobComparator = new Comparator<JobOffer>() {
		@Override
		public int compare(JobOffer offer1, JobOffer offer2) {
			return (new Long(offer2.getWage()).compareTo(offer1.getWage()));
		}
	};

	@SuppressWarnings("javadoc")
	private static final String N_JOB_OFFERS = "jobs.selection";

	@SuppressWarnings("javadoc")
	private static final String N_SUPPLIES = "supplies.selection";

	@SuppressWarnings("javadoc")
	private final static String SAV_PROP = "savings.propensityToSave";

	@SuppressWarnings("javadoc")
	private final static String SAV_PROP2_CONSUM_EXCESS = "savings.propensityToConsumeExcess";

	@SuppressWarnings("javadoc")
	private final static String SAV_TARGET = "savings.ratioTarget";

	/**
	 * The supply comparator.
	 * <p>
	 * To compare supplies according to their price.
	 */
	private static final Comparator<Supply> supplyComparator = new Comparator<Supply>() {
		@Override
		public int compare(Supply offer1, Supply offer2) {
			return (-(new Double(offer2.getPrice())).compareTo(offer1.getPrice()));
		}
	};

	@SuppressWarnings("javadoc")
	private static final int UNEMPLOYED = 0;

	@SuppressWarnings("javadoc")
	private final static String WAGE_FLEX = "wage.flexibility";

	@SuppressWarnings("javadoc")
	private final static String WAGE_RESIST = "wage.resistance";

	/** The current account. */
	private final BankAccount account;

	/** Items of property. */
	private final AssetPortfolio assetPortfolio = new BasicAssetPortfolio();

	/**
	 * The value of the consumption goods bought during this period.
	 */
	private long consumptionValue = 0;

	/**
	 * The quantity of the consumption goods bought during this period.
	 */
	private long consumptionVolume = 0;

	/** The data of the agent. */
	private BasicAgentDataset data;

	/** The job contract. */
	private JobContract jobContract;

	/** The name of the household. */
	private final String name;

	/**
	 * The net worth of the household, at the end of the previous period.
	 */
	private long previousNetWorth = 0;

	/** The random. */
	final private Random random;

	/** The memory. */
	final private Memory<Long> recentIncome;

	/** The households sector. */
	private final HouseholdSector sector;

	/** The timer. */
	final private Timer timer;

	/**
	 * The type of goods the household want to consume.
	 */
	private final String typeOfConsumptionGood;

	/**
	 * Unemployement duration.
	 */
	private int unempDuration = 0;

	/**
	 * A map that stores the variables of the household. TODO: remplacer par des
	 * champs.
	 */
	private final Map<String, Number> variables = new HashMap<String, Number>();

	/**
	 * Flexibility of wages.
	 */
	private Float wageFlex = null;

	/**
	 * The wage earned by the household for the current period.
	 */
	private long wages = 0;

	/**
	 * Creates a household.
	 * 
	 * @param name
	 *            the name of the new household.
	 * @param sector
	 *            the households sector.
	 */
	public BasicHousehold(String name, HouseholdSector sector) {
		this.name = name;
		this.sector = sector;
		this.timer = this.sector.getTimer();
		this.random = this.sector.getRandom();
		this.typeOfConsumptionGood = this.sector.getTypeOfConsumptionGood();
		this.recentIncome = new Memory<Long>(12);
		this.account = sector.getNewAccount(this);
		this.variables.put("status", UNEMPLOYED);
		this.variables.put("unemployement duration", 0);
		this.data = new BasicAgentDataset(this.name);
	}

	/**
	 * Updates the data.
	 */
	private void updateData() {

		final long equities = assetPortfolio.getNetValue();
		final long netWorth = equities + account.getAmount();
		final long savings = netWorth - this.previousNetWorth;
		final long income = this.consumptionValue + savings;
		final long profits = income - this.wages;

		this.data.put("savings", savings);
		this.data.put("income", income);
		this.data.put("wages", this.wages);
		this.data.put("profits", profits);

		this.data.put("money", account.getAmount());
		this.data.put("dividends", variables.get("dividends").doubleValue());
		this.data.put("equities", equities);
		this.data.put("netWorth", netWorth);
		this.data.put("agents", 1.);
		this.data.put("consumption.val", this.consumptionValue);
		this.data.put("consumption.vol", this.consumptionVolume);

		this.previousNetWorth = netWorth;
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
		this.data.put("sharePurchase", this.data.get("sharePurchase") + price);
		return this.account.newCheque(price);
	}

	@Override
	public void close() {
		this.updateData();
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
		final long savingsTarget = (long) (12 * averageIncome * this.sector.getParam(SAV_TARGET));
		final long savings = (long) (this.account.getAmount() - averageIncome);
		long consumptionBudget;
		if (savings < savingsTarget) {
			consumptionBudget = Math.min(this.account.getAmount(),
					(long) ((1. - this.sector.getParam(SAV_PROP)) * averageIncome));
		} else {
			consumptionBudget = Math.min(this.account.getAmount(),
					(long) (averageIncome + (savings - savingsTarget) * this.sector.getParam(SAV_PROP2_CONSUM_EXCESS)));
		}

		this.data.put("consumption.budget", (double) consumptionBudget);
		if (consumptionBudget > 0) {
			final Supply[] supplies = this.sector.getSupplies(sector.getParam(N_SUPPLIES).intValue());
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
					final Commodities commod = supply.buy(volume, this.account.newCheque(value));
					if (!this.typeOfConsumptionGood.equals(commod.getType())) {
						Jamel.println("Expected: " + this.typeOfConsumptionGood);
						Jamel.println("Found: " + commod.getType());
						throw new RuntimeException("Bad type of commodities: " + commod.getType());
					}
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
	public void earnWage(Cheque paycheck) {
		if (this.jobContract == null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.wages != 0) {
			throw new AnachronismException("Wage already earned.");
		}
		if (paycheck.getAmount() != this.jobContract.getWage()) {
			throw new ConsistencyException("Bad cheque amount.");
		}
		this.wages = paycheck.getAmount();
		this.account.deposit(paycheck);
	}

	@Override
	public AgentDataset getData() {
		return this.data;
	}

	@Override
	public long getFinancialCapacity() {
		return this.account.getAmount() / 2;
	}

	@Override
	public LaborPower getLaborPower() {
		if (this.jobContract == null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.variables.get("worked").intValue() != 0) {
			throw new AnachronismException("Already worked.");
		}
		if (this.wages == 0) {
			throw new ConsistencyException("Wage not paid.");
		}
		this.variables.put("worked", 1);
		return new LaborPower() {

			final private int date = timer.getPeriod().intValue();

			private boolean exhausted = false;

			private long value = jobContract.getWage();

			private final long wage = value;

			@Override
			public void expend() {
				if (date != timer.getPeriod().intValue()) {
					throw new AnachronismException("This worforce is out of date.");
				}
				if (exhausted) {
					throw new RuntimeException("This workforce is exhausted.");
				}
				exhausted = true;
				value = 0;
			}

			@Override
			public long getValue() {
				return value;
			}

			@Override
			public long getWage() {
				return wage;
			}

			@Override
			public boolean isExhausted() {
				return exhausted;
			}

		};
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void goBankrupt() {
		throw new RuntimeException("A household cannot be bankrupted.");
	}

	@Override
	public boolean isBankrupted() {
		return false;
	}

	@Override
	public boolean isEmployed() {
		throw new RuntimeException("Not used");
	}

	@Override
	public boolean isSolvent() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void jobSearch() {

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
			// Attention, c'est un peu plus compliqu� dans les derni�res
			// versions de Jamel1.
			Double reservationWage = (Double) this.variables.get("reservationWage");
			if (reservationWage == null) {
				reservationWage = 0d;
				this.wageFlex = null;
				this.variables.put("reservationWage", reservationWage);
			}
			// if (reservationWage>0 && unempDuration >
			// this.sector.getParam(WAGE_RESIST)) {
			if (reservationWage > 0 && this.unempDuration > this.sector.getParam(WAGE_RESIST)) {
				reservationWage -= this.wageFlex * this.random.nextFloat();
				this.variables.put("reservationWage", reservationWage);
			}
			final JobOffer[] jobOffers = this.sector.getJobOffers(sector.getParam(N_JOB_OFFERS).intValue());
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
			this.wageFlex = this.sector.getParam(WAGE_FLEX) * this.jobContract.getWage();
			// TODO 0.2 should be a parameter.
			// unempDuration = 0;
			break;

		default:
			throw new RuntimeException("Unexpected status.");
		}

		this.data.put("reservationWage", this.variables.get("reservationWage").doubleValue());
		this.data.put("unemployed", 1d - this.variables.get("status").doubleValue());
		this.data.put("employed", this.variables.get("status").doubleValue());
		this.data.put("unemployment.duration", this.unempDuration);
	}

	@Override
	public void open() {
		this.consumptionValue = 0;
		this.consumptionVolume = 0;
		this.wages = 0;
		this.variables.put("dividends", 0l);
		this.variables.put("worked", 0);
		this.data = new BasicAgentDataset(this.name);
		this.data.put("sharePurchase", 0d);
	}

	@Override
	public void removeAsset(Asset asset) {
		this.assetPortfolio.remove(asset);
	}

	@Override
	public void takeDividends() {
		long dividend = 0;
		for (Asset asset : this.assetPortfolio.getList()) {
			// REMARQUE: pour le moment, le portefeuille des m�nages n'est
			// compos� que d'actions.
			final Cheque cheque = ((StockCertificate) asset).getDividend();
			if (cheque != null) {
				dividend += cheque.getAmount();
				this.account.deposit(cheque);
			}
		}
		this.variables.put("dividends", dividend);
	}
}

// ***
