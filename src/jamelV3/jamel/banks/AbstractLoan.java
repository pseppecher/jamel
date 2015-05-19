package jamelV3.jamel.banks;

import jamelV3.basic.util.Period;
import jamelV3.basic.util.Timer;

/**
 * An abstract loan.
 */
public abstract class AbstractLoan implements Loan {

	/** The extended maturity date.*/
	protected final Period extendedDate;

	/** The period of the last payment of interest. */
	protected Period lastInterestPayment;

	/** The maturity date.*/
	protected final Period maturityDate;

	/** The penalty interest rate. */
	protected final double penaltyRate ;

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
	 * @param penaltyRate the penalty rate.
	 * @param normalTerm the normal term.
	 * @param extendedTerm the extended term.
	 * @param timer the timer.
	 */
	public AbstractLoan(long principal, double rate, double penaltyRate, int normalTerm, int extendedTerm, Timer timer) {
		this.timer=timer;
		this.principal = principal;
		this.rate = rate;
		this.penaltyRate = penaltyRate;
		this.maturityDate = this.timer.getPeriod().plus(normalTerm);
		this.extendedDate = this.timer.getPeriod().plus(extendedTerm);
	}

	@Override
	public long getPrincipal() {
		return this.principal;
	}
	
	@Override
	public boolean isDoubtfull() {
		return (timer.getPeriod().isAfter(this.maturityDate));
	}

}

// ***
