package jamel.jamel.households;

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

	@SuppressWarnings("javadoc")
	private static final int UNEMPLOYED = 0;

	@SuppressWarnings("javadoc")
	private final static String WAGE_FLEX = "wage.flexibility";

	@SuppressWarnings("javadoc")
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

	/** The current account. */
	private final BankAccount account;

	/** Items of property. */
	private final AssetPortfolio assetPortfolio = new BasicAssetPortfolio();

	/** The data of the agent. */
	private BasicAgentDataset data;

	/** The job contract. */
	private JobContract jobContract;

	/** The name of the household. */
	private final String name;

	/** The households sector. */
	private final HouseholdSector sector;

	/** 
	 * A map that stores the variables of the household.
	 * TODO: remplacer par des champs. 
	 */
	private final Map<String, Number> variables = new HashMap<String, Number>();

	/** The memory. */
	final protected Memory<Long> recentIncome;

	/** The random. */
	final protected Random random;

	/** The timer. */
	final protected Timer timer;

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
		this.data.put("cash", (double) account.getAmount());
		this.data.put("wages", variables.get("wage").doubleValue());
		this.data.put("dividends", variables.get("dividends").doubleValue());
		final long capital = assetPortfolio.getNetValue();
		this.data.put("capital", (double) capital);
		this.data.put("netWorth", (double) capital + account.getAmount());
		this.data.put("agents", 1.);
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
			final long error=price-shares.getBookValue();
			if (error>1) {
			throw new IllegalArgumentException(
					"Price of shares is <" + price + "> but the book value is <" + shares.getBookValue() + ">");
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
		this.recentIncome.add(this.variables.get("wage").longValue() + this.variables.get("dividends").longValue());

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
		long consumptionValue = 0;
		long consumptionVolume = 0;
		if (consumptionBudget > 0) {
			final Supply[] supplies = this.sector.getSupplies(sector.getParam(N_SUPPLIES).intValue());
			if (supplies.length > 0) {
				Arrays.sort(supplies, supplyComparator);
				for (Supply supply : supplies) {
					if (consumptionBudget < supply.getPrice()) {
						break;
					}
					final long volume;
					if (supply.getPrice(supply.getVolume()) >= consumptionBudget) {
						volume = (long) (consumptionBudget / supply.getPrice());
					} else {
						volume = supply.getVolume();
					}
					final long value = (long) (volume * supply.getPrice());
					final Commodities truc = supply.buy(volume, this.account.newCheque(value));
					if (truc.getVolume() != volume) {
						throw new RuntimeException(
								"Consumption volume expected <" + volume + "> but was <" + truc.getVolume() + ">");
					}
					truc.consume();
					consumptionBudget -= value;
					consumptionValue += value;
					consumptionVolume += volume;
				}
			}
		}
		this.data.put("consumption.val", (double) consumptionValue);
		this.data.put("consumption.vol", (double) consumptionVolume);
	}

	@Override
	public void earnWage(Cheque paycheck) {
		if (this.jobContract == null) {
			throw new RuntimeException("Job contract is null.");
		}
		if (!this.jobContract.isValid()) {
			throw new RuntimeException("Invalid job contract.");
		}
		if (this.variables.get("wage").longValue() > 0) {
			throw new AnachronismException("Wage already earned.");
		}
		if (paycheck.getAmount() != this.jobContract.getWage()) {
			throw new ConsistencyException("Bad cheque amount.");
		}
		this.variables.put("wage", this.variables.get("wage").longValue() + paycheck.getAmount());
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
		if (this.variables.get("wage").longValue() == 0) {
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
	public boolean isSolvent() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void jobSearch() {

		// Updates the status.

		Integer unempDuration = (Integer) this.variables.get("unemployement duration");
		if ((this.jobContract == null) || !(this.jobContract.isValid())) {
			this.variables.put("status", UNEMPLOYED);
			if (unempDuration < 0) {
				unempDuration = 0;
			} else {
				unempDuration++;
			}
		} else {
			this.variables.put("status", EMPLOYED);
			if (unempDuration > 0) {
				unempDuration = 0;
			} else {
				unempDuration--;
			}
		}

		// Different behaviors according the status.

		switch (this.variables.get("status").intValue()) {
		case UNEMPLOYED:
			// Attention, c'est un peu plus compliqu� dans les derni�res
			// versions de Jamel1.
			Double reservationWage = (Double) this.variables.get("reservationWage");
			if (reservationWage == null) {
				reservationWage = 0d;
				this.variables.put("reservationWage", reservationWage);
			}
			if (unempDuration > this.sector.getParam(WAGE_RESIST)) {
				reservationWage = (reservationWage * (1f - this.sector.getParam(WAGE_FLEX) * this.random.nextFloat()));
				this.variables.put("reservationWage", reservationWage);
			}
			final JobOffer[] jobOffers = this.sector.getJobOffers(sector.getParam(N_JOB_OFFERS).intValue());
			if (jobOffers.length > 0) {
				Arrays.sort(jobOffers, jobComparator);
				if (jobOffers[0].getWage() >= reservationWage) {
					this.jobContract = jobOffers[0].apply(this);
					this.variables.put("status", EMPLOYED);
					unempDuration = 0;
				}
			}
			break;
		case EMPLOYED:
			this.variables.put("reservationWage", (double) this.jobContract.getWage());
			break;
		default:
			throw new RuntimeException("Unexpected status.");
		}

		this.data.put("unemployed", 1d - this.variables.get("status").doubleValue());
		this.data.put("employed", this.variables.get("status").doubleValue());
		this.variables.put("unemployment duration", unempDuration);
	}

	@Override
	public void open() {
		this.variables.put("dividends", 0l);
		this.variables.put("worked", 0);
		this.variables.put("wage", 0l);
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
