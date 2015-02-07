package jamel.basic.agents.banks.util;

import jamel.util.Circuit;
import jamel.util.Period;

@SuppressWarnings("javadoc")
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

	public AbstractLoan(long principal, double rate, double penaltyRate, int normalTerm, int extendedTerm) {
		this.principal = principal;
		this.rate = rate;
		this.penaltyRate = penaltyRate;
		this.maturityDate = Circuit.getCurrentPeriod().plus(normalTerm);
		this.extendedDate = Circuit.getCurrentPeriod().plus(extendedTerm);
	}

	@Override
	public long getPrincipal() {
		return this.principal;
	}
	
	@Override
	public boolean isDoubtfull() {
		return (Circuit.getCurrentPeriod().isAfter(this.maturityDate));
	}

}

// ***
