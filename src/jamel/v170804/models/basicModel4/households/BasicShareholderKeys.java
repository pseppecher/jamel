package jamel.v170804.models.basicModel4.households;

import jamel.v170804.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicShareholder}.
 */
public class BasicShareholderKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicShareholderKeys}.
	 */
	private static BasicShareholderKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	static BasicShareholderKeys getInstance() {
		if (instance == null) {
			instance = new BasicShareholderKeys();
		}
		return instance;
	}

	@SuppressWarnings("javadoc")
	final public int consumptionBudget = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int consumptionVolume = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int money = this.getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private BasicShareholderKeys() {
		this.init(this.getClass().getFields());
	}

}
