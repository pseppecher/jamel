package jamel.jamel.aggregates;

import jamel.jamel.widgets.Supply;

/**
 * A sector that provides supplies.
 */
public interface Suppliers {

	/**
	 * Returns a sample of supplies.
	 * @param size the size of the sample to be returned.
	 * @return an array of supplies.
	 */
	Supply[] getSupplies(int size);

}

// ***
