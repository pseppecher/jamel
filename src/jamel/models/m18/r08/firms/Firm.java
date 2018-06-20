package jamel.models.m18.r08.firms;

import jamel.models.m18.r08.roles.Employer;
import jamel.models.m18.r08.roles.Supplier;

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
	
}
