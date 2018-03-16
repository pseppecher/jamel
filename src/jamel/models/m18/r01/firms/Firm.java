package jamel.models.m18.r01.firms;

import jamel.models.m18.r01.markets.BasicLaborMarket;
import jamel.models.util.Employer;
import jamel.models.util.Supplier;

/**
 * Represents a firm.
 */
public interface Firm extends Employer, Supplier {

	/**
	 * The investment phase.
	 * 
	 * 2018-03-10
	 * Called by the investment good market.
	 * 
	 */
	void invest();

	/**
	 * Sets the labor market.
	 * 
	 * @param laborMarket
	 *            the labor market.
	 */
	void setLaborMarket(BasicLaborMarket laborMarket);

}
