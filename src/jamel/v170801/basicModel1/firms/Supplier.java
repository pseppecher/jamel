package jamel.v170801.basicModel1.firms;

import jamel.v170801.basicModel1.banks.AccountHolder;

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
