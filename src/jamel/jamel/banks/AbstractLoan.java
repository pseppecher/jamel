package jamel.jamel.banks;

import jamel.basic.util.Timer;

/**
 * An abstract loan.
 */
public abstract class AbstractLoan implements Loan {

	/** The maturity date.*/
	protected final int maturityDate;

	/** The remaining principal. */
	protected long principal ;

	/** The interest rate. */
	protected final double rate ;

	/** The timer. */
	protected final Timer timer;

	/**
	 * Creates a new loan.
	 * @param principal the principal.
	 * @param rate the normal rate of interest. 
	 * @param normalTerm the normal term.
	 * @param timer the timer.
	 */
	public AbstractLoan(long principal, double rate, int normalTerm, Timer timer) {
		this.timer=timer;
		this.principal = principal;
		this.rate = rate;
		this.maturityDate = this.timer.getPeriod().intValue()+normalTerm;
	}

	@Override
	public void cancel() {
		cancel(this.principal);
	}

	@Override
	public long getPrincipal() {
		return this.principal;
	}
	
	@Override
	public int getMaturity() {
		return this.maturityDate;
	}

}

// ***
