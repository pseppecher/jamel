package jamel.models.modelJEE.firms;

import jamel.data.AbstractDataKeys;

/**
 * Data keys of the {@code BasicFirm}.
 */
final public class BasicFirmKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicFirmKeys}.
	 */
	private static BasicFirmKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	public static BasicFirmKeys getInstance() {
		if (instance == null) {
			instance = new BasicFirmKeys();
		}
		return instance;
	}

	/**
	 * Denotes the willing of the firm to produce.
	 */
	final public int active = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int assets = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int capacity = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int circulatingCapital = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int debtRatio = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int debtRatioTarget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int debtService = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int deltaMarkup = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int dividends = this.getNextIndex();

	/**
	 * Index to count the "empty" firms, ie, the firms with 0 machines.
	 */
	final public int empty = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int equities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int firing = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int grossProfit = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int hedge = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int highPrice = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int highWage = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int imitation = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inProcessValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int interests = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inventoriesLevel = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inventoriesNormalVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inventoriesValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inventoriesVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inventoryDesequilibria = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int investment2capacityRatio = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int investmentSize = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int investmentValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int investmentVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int isBadDebtor = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int jobOffers = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int liabilities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int liabilitiesTarget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int longTermDebt = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int lowPrice = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int lowWage = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int machineryValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int markup = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int money = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int netProfit = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int overdueDebt = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int paybackPeriod = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int ponzi = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int price = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int productionMax = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int productionTarget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int productionValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int productionVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int salesCosts = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int salesLevel = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int salesValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int salesVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int shortTermDebt = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int solvent = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int speculative = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int supplyCost = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int supplyValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int supplyVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int tangibleAssets = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int utilizationTarget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int vacancies = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int vacancyRatio = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int wage = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int wageBill = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int workforce = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int workforceTarget = this.getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private BasicFirmKeys() {
		this.init(this.getClass().getFields());
	}

}
