package jamel.v170801.basicModel0.firms;

import jamel.v170801.basicModel0.banks.AccountHolder;

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
