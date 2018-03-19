package jamel.models.m18.r02.banks;

import jamel.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicBank}.
 */
class BankKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicBankKeys}.
	 */
	private static BankKeys instance;

	/**
	 * Returns an instance of {@code BasicShareholderKeys}.
	 * 
	 * @return an instance of {@code BasicShareholderKeys}.
	 */
	static BankKeys getInstance() {
		if (instance == null) {
			instance = new BankKeys();
		}
		return instance;
	}

	@SuppressWarnings("javadoc")
	final public int assets = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int count = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int debtCancellationCount = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int debtCancellationValue = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int dividends = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int equities = this.getNextIndex();

	@SuppressWarnings("javadoc")
	final public int inflation = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int installments = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int interests = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int interestsNormal = this.getNextIndex();

	@SuppressWarnings("javadoc")
	public final int interestsOverdue = this.getNextIndex();

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
	private BankKeys() {
		init(this.getClass().getFields());
	}

}
