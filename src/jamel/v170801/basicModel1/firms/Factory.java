package jamel.v170801.basicModel1.firms;

import java.util.List;

/**
 * Represents a factory.
 */

interface Factory {

	/**
	 * Closes the factory at the end of the period.
	 */
	void close();

	/**
	 * Returns the capacity of this factory.
	 * 
	 * @return the capacity of this factory.
	 */
	int getCapacity();

	/**
	 * Returns the inventories of this factory.
	 * 
	 * @return the inventories of this factory.
	 */
	Goods getInventories();

	/**
	 * Returns the value of this factory (= tangible assets).
	 * 
	 * @return the value of this factory.
	 */
	long getValue();

	/**
	 * Opens the factory at the beginning of the period.
	 */
	void open();

	/**
	 * Produces.
	 * 
	 * @param workforce
	 *            the list of the labor contracts.
	 */
	void production(final List<? extends LaborContract> workforce);

}
