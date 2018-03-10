package jamel.models.m18.r01.households;

import jamel.data.AbstractDataKeys;

/**
 * Data keys for the {@code BasicWorker}.
 */
public class BasicWorkerKeys extends AbstractDataKeys {

	/**
	 * An instance of {@code BasicWorkerKeys}.
	 */
	private static BasicWorkerKeys instance;

	/**
	 * Returns an instance of {@code BasicWorkerKeys}.
	 * 
	 * @return an instance of {@code BasicWorkerKeys}.
	 */
	public static BasicWorkerKeys getInstance() {
		if (instance == null) {
			instance = new BasicWorkerKeys();
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
	final public int employed = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int employmentDuration = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int hiring = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int money = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int reservationWage = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int unempDuration = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int unemployed = getNextIndex();

	@SuppressWarnings("javadoc")
	final public int wage = getNextIndex();

	/**
	 * Creates a new set of data keys.
	 */
	private BasicWorkerKeys() {
		this.init(this.getClass().getFields());
	}

}
