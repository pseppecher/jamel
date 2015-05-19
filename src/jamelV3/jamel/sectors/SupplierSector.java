package jamelV3.jamel.sectors;

import jamelV3.jamel.widgets.Supply;

/**
 * A sector that provides supplies.
 */
public interface SupplierSector {

	/**
	 * Returns a sample of supplies.
	 * @param size the size of the sample to be returned.
	 * @return an array of supplies.
	 */
	Supply[] getSupplies(int size);

}

// ***
