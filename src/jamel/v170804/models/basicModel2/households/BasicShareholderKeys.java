package jamel.v170804.models.basicModel2.households;

import jamel.v170804.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicShareholder}.
 */
public class BasicShareholderKeys extends AbstractDataKeys {

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
	BasicShareholderKeys() {
		this.init(this.getClass().getFields());
	}

}
