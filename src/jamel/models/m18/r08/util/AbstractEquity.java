package jamel.models.m18.r08.util;

import jamel.models.m18.r08.roles.Shareholder;

/*
 * 2018-04-11: jamel.models.m18.r07.util.AbstractEquity
 * Pour une meilleure gestion du capital des firmes, avec l'objectif de
 * permettre une recapitalisation.
 * 
 */

/**
 * An abstract equity.
 */
abstract public class AbstractEquity implements Equity {

	/**
	 * If this equity is canceled.
	 */
	private boolean canceled = false;

	/**
	 * The owner of this equity.
	 */
	private final Shareholder owner;

	/**
	 * The value of the equity.
	 */
	private long value;

	/**
	 * Creates a new abstract equity.
	 * 
	 * @param owner
	 *            the owner of the equity.
	 * @param value
	 *            the initial value of the equity.
	 */
	protected AbstractEquity(final Shareholder owner, final long value) {
		this.owner = owner;
		this.value = value;
	}

	/**
	 * Cancels this equity.
	 */
	protected void cancel() {
		this.canceled = true;
	}

	/**
	 * Sets the value of this equity.
	 * 
	 * @param value
	 *            the new value.
	 */
	protected void setValue(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("Negative value: " + value);
		}
		this.value = value;
	}

	@Override
	final public Shareholder getOwner() {
		return this.owner;
	}

	@Override
	final public long getValue() {
		if (canceled) {
			throw new RuntimeException("Canceled");
		}
		return this.value;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

}
