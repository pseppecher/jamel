package jamel.v170804.models.basicModel4.firms;

import java.util.List;

import jamel.v170804.models.basicModel4.interfaces.Goods;
import jamel.v170804.models.basicModel4.interfaces.LaborContract;

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
	 * Returns the average volume of production at full capacity utilization.
	 * 
	 * @return the average volume of production at full capacity utilization.
	 */
	double getProductionAtFullCapacity();

	/**
	 * Returns the average productivity of the factory.
	 * 
	 * @return the average productivity of the factory.
	 */
	double getProductivity();

	/**
	 * Returns the value of this factory (= tangible assets).
	 * 
	 * @return the value of this factory.
	 */
	long getValue();

	/**
	 * Returns the amount of the wage bill, ie, the total amount paid in wages
	 * during the current period.
	 * 
	 * @return the amount of the wage bill.
	 */
	Long getWageBill();

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
