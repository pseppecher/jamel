package jamel.models.m18.r08.util;

import java.util.LinkedList;
import java.util.List;

import jamel.models.m18.r08.roles.Shareholder;
import jamel.util.Agent;

/**
 * A basic implementation of the ownership interface.
 */
public class BasicOwnership implements Ownership {

	/**
	 * Cancels and removes all of the equities in this ownership.
	 * The total value is set to zero.
	 */
	public void clear() {
		while (this.equities.size() > 0) {
			this.equities.removeFirst().cancel();
			this.equities.clear();
		}
		this.totalValue = 0;
	}

	/**
	 * A basic equity.
	 */
	private class BasicEquity extends AbstractEquity {

		/**
		 * The number of shares.
		 */
		private Long share = null;

		/**
		 * Creates an equity.
		 * 
		 * @param owner
		 *            the owner of the new equity.
		 * @param value
		 *            the value of the new equity.
		 */
		protected BasicEquity(final Shareholder owner, final long value) {
			super(owner, value);
		}

		@Override
		public String getCompanyName() {
			return BasicOwnership.this.company.getName();
		}

	}

	/**
	 * The total number of shares.
	 */
	final private static int k = 100000;

	/**
	 * The company.
	 */
	final private Agent company;

	/**
	 * The total value of the equities.
	 */
	private long totalValue = 0;

	/**
	 * The title of ownership of the firm.
	 */
	protected final LinkedList<BasicEquity> equities = new LinkedList<>();

	/**
	 * Creates an ownership for the specified company.
	 * 
	 * @param company
	 *            the owned company.
	 */
	public BasicOwnership(final Agent company) {
		this.company = company;
	}

	@Override
	public List<? extends Equity> getEquities() {
		return this.equities;
	}

	@Override
	public long getTotalValue() {
		return this.totalValue;
	}

	@Override
	public boolean isEmpty() {
		return this.equities.isEmpty();
	}

	@Override
	public Equity issue(final Shareholder shareholder, final long contribution) {
		// Updates the total value.
		this.totalValue += contribution;
		// Issue the new equity.
		final BasicEquity equity = new BasicEquity(shareholder, contribution);
		// Adds the new equity to the list.
		this.equities.add(equity);
		// Updates the shares of each equity.
		for (BasicEquity eq : equities) {
			eq.share = (long) (k * (((double) eq.getValue()) / this.totalValue));
		}
		// Returns the new equity.
		return equity;
	}

	@Override
	public long size() {
		return this.equities.size();
	}

	@Override
	public void updateValue(final long newValue) {
		this.totalValue = newValue;
		for (BasicEquity equity : equities) {
			equity.setValue((this.totalValue <= 0) ? 0 : (this.totalValue * equity.share) / k);
		}
	}

}
