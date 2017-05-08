package jamel.jamel.banks;

import jamel.util.JamelObject;

/**
 * A basic loans.
 */
/**
 * @author pascal
 *
 */
public class BasicLoan extends JamelObject implements Loan {

	/**
	 * If the loan is amortizing.
	 */
	private boolean amortizing;

	/**
	 * The maturity date of this loan.
	 */
	private int maturityDate;

	/**
	 * The principal of this loan.
	 */
	final private Amount principal = new Amount();

	/**
	 * The interest rate.
	 */
	private double rate;

	/**
	 * Creates a new basic loan.
	 * 
	 * @param account
	 *            the borrower account.
	 * @param amount
	 *            the amount of the loan.
	 * @param rate
	 *            the interest rate (monthly).
	 * @param maturityDate
	 *            the maturity.
	 * @param amortizing
	 *            if the loan is amortizing.
	 */
	public BasicLoan(final BasicAccount account, final long amount, final double rate, final int maturityDate,
			final boolean amortizing) {
		super(account.getSimulation());
		this.principal.plus(amount);
		this.rate = rate;
		this.maturityDate = maturityDate;
		this.amortizing = amortizing;

	}

	@Override
	public void cancel(long amount) {
		// TODO rename as "payBack" ?
		// TODO control period ?
		this.principal.minus(amount);
	}

	@Override
	public long getInstallment() {
		final long result;
		final long term = this.maturityDate - this.getPeriod();
		if (term == 0) {
			result = this.principal.getAmount();
		} else if (this.amortizing) {
			result = this.principal.getAmount() / (term + 1);
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public long getInterest() {
		return (long) (this.principal.getAmount() * this.rate);
	}

	@Override
	public int getMaturity() {
		return this.maturityDate;
	}

	@Override
	public long getPrincipal() {
		return this.principal.getAmount();
	}

}
