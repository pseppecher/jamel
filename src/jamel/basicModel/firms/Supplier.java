package jamel.basicModel.firms;

import jamel.basicModel.banks.AccountHolder;

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
