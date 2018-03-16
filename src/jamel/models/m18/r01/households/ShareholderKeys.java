package jamel.models.m18.r01.households;

import jamel.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicShareholder}.
 */
public class ShareholderKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicShareholderKeys}.
	 */
	private static ShareholderKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	static ShareholderKeys getInstance() {
		if (instance == null) {
			instance = new ShareholderKeys();
		}
		return instance;
	}

	@SuppressWarnings("javadoc")
	final public int capitalAppreciation = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionBudget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int dividends = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int equities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int money = this.getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private ShareholderKeys() {
		this.init(this.getClass().getFields());
	}

}
