package jamel.jamel.banks;

import jamel.basic.util.Timer;

/**
 * An abstract loan.
 */
abstract class AbstractLoan implements Loan {

	/** The maturity date. */
	protected final int maturityDate;

	/**
	 * The period when the principal was borrowed.
	 */
	protected final int origin;

	/** The remaining principal. */
	protected long principal;

	/** The interest rate. */
	protected final double rate;

	/**
	 * Creates a new loan.
	 * 
	 * @param principal
	 *            the principal.
	 * @param rate
	 *            the normal rate of interest.
	 * @param normalTerm
	 *            the normal term.
	 * @param timer
	 *            the timer.
	 */
	public AbstractLoan(long principal, double rate, int normalTerm, Timer timer) {
		this.timer = timer;
		this.origin = timer.getPeriod().intValue();
		this.principal = principal;
		this.rate = rate;
		this.maturityDate = this.timer.getPeriod().intValue() + normalTerm;
	}

	@Override
	public void cancel() {
		cancel(this.principal);
	}

	@Override
	public int getMaturity() {
		return this.maturityDate;
	}

	@Override
	public long getPrincipal() {
		return this.principal;
	}

}

// ***
