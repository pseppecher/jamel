package jamel.models.m18.r08.roles;

import jamel.models.m18.r08.util.AccountHolder;
import jamel.models.m18.r08.util.Supply;

/**
 * Represents a supplier.
 */
public interface Supplier extends AccountHolder {

	/**
	 * Returns the supply.
	 * 
	 * @return the supply.
	 */
	Supply getSupply();

}
