package jamel.models.modelJEE.households;

import jamel.data.AbstractDataKeys;

/**
 * Data keys of the {@code BasicFirm}.
 */
final class BasicHouseholdKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicFirmKeys}.
	 */
	private static BasicHouseholdKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	static BasicHouseholdKeys getInstance() {
		if (instance == null) {
			instance = new BasicHouseholdKeys();
		}
		return instance;
	}

	@SuppressWarnings("javadoc")
	final public int consumptionBudget = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionValue = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionVolume = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int dividends = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int employed = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int employmentDuration = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int equities = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int hiring = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int income = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int money = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int netWorth = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int profit = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int reservationWage = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int savings = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int unempDuration = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int unemployed = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int wage = getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private BasicHouseholdKeys() {
		this.init(this.getClass().getFields());
	}

}
