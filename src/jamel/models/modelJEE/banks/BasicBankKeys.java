package jamel.models.modelJEE.banks;

import jamel.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicBank}.
 */
class BasicBankKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicBankKeys}.
	 */
	private static BasicBankKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	static BasicBankKeys getInstance() {
		if (instance == null) {
			instance = new BasicBankKeys();
		}
		return instance;
	}

	@SuppressWarnings("javadoc")
	final public int assets = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int equities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inflation = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int installments = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int interests = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int liabilities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int longTermDebt = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int nominalRate = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int overdueDebt = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int realRate = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int shortTermDebt = this.getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private BasicBankKeys() {
		init(this.getClass().getFields());
	}

}
