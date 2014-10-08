package jamel.basic.agents.firms.util;

import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.Commodities;

/**
 * Represents a factory.
 */
public interface Factory {

	/**
	 * Definitively closes the factory.
	 */
	void bankrupt();

	/**
	 * Returns the capacity of the factory, ie its maximum number of workers. 
	 * @return the capacity.
	 */
	float getCapacity();

	/**
	 * Subtracts and returns the specified volume of commodities from the inventories.
	 * @param demand the volume of commodities.
	 * @return the demanded commodities.
	 */
	Commodities getCommodities(long demand);

	/**
	 * Returns the value of finished goods.
	 * @return the value of finished goods.
	 */
	long getFinishedGoodsValue();

	/**
	 * Returns the volume of finished goods.
	 * @return the volume of finished goods.
	 */
	long getFinishedGoodsVolume();
	
	/**
	 * Returns the value of inventory losses for the current period. 
	 * @return the value of inventory losses for the current period.
	 */
	double getInventoryLosses();

	/**
	 * Returns the average volume of production (finished goods) at the maximum utilization of the production capacity. 
	 * @return a volume.
	 */
	double getMaxUtilAverageProduction();

	/**
	 * Return the value of finished goods produced by the last production process.
	 * @return the value of product.
	 */
	long getProductionValue();

	/**
	 * Return the volume of finished goods produced by the last production process.
	 * @return the volume of product.
	 */
	long getProductionVolume();

	/**
	 * Returns the unit cost of the finished goods in the inventory.
	 * @return the unit cost.
	 */
	double getUnitCost();

	/**
	 * Returns the total value of the finished and unfinished goods in the inventories.
	 * @return the total value.
	 */
	long getValue();

	/**
	 * Returns the number of labor powers put in the last production process.
	 * @return an int.
	 */
	int getWorkforce();

	/**
	 * Produces new goods by the expense of the given units of labor power.
	 * @param laborPowers the labor powers.
	 */
	void process(LaborPower... laborPowers);

	/**
	 * Sets the productivity of the factory.
	 * @param productivity the productivity to set.
	 */
	void setProductivity(float productivity);

}

// ***
