package jamel.v170804.models.basicModel2.banks;

import jamel.v170804.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicBank}.
 */
public class BasicBankKeys extends AbstractDataKeys {

	@SuppressWarnings("javadoc")
	final public int assets = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int liabilities = this.getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	BasicBankKeys() {
		init(this.getClass().getFields());
	}

}
