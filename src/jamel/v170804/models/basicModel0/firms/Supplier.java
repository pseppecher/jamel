package jamel.v170804.models.basicModel0.firms;

import jamel.v170804.models.basicModel0.banks.AccountHolder;

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
