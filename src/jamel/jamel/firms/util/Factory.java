package jamel.jamel.firms.util;

import jamel.basic.agent.AgentDataset;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a factory.
 */
public interface Factory {

	/**
	 * Definitively closes the factory.
	 */
	void bankrupt();

	/**
	 * Closes the factory.
	 */
	void close();

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
	 * Returns the dataset of the factory.
	 * @return the dataset of the factory.
	 */
	AgentDataset getData();

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
	 * Returns the value of goods in process.
	 * @return the value of goods in process.
	 */
	double getGoodsInProcessValue();

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
	 * Returns the unit cost of the finished goods in the inventory.
	 * The unit cost is "The cost incurred by a company to produce, store and sell one unit of a particular product."
	 * @see <a href="http://www.investopedia.com/terms/u/unitcost.asp">www.investopedia.com/terms/u/unitcost.asp</a>
	 * @return the unit cost.
	 */
	double getUnitCost();

	/**
	 * Returns the total value of finished and unfinished goods present in the inventories.
	 * (= "the raw materials, work-in-process goods and completely finished goods that are considered 
	 * to be the portion of a business's assets that are ready or will be ready for sale")
	 * @see <a href="http://www.investopedia.com/articles/04/031004.asp">investopedia.com</a>
	 * @return the total value.
	 */
	long getValue();

	/**
	 * Receives an investment process. A new machine is then created.
	 * @param investmentProcess the 
	 */
	void investment(InvestmentProcess investmentProcess);

	/**
	 * Opens the factory.
	 */
	void open();

	/**
	 * Produces new goods by the expense of the given units of labor power.
	 * @param laborPowers the labor powers.
	 */
	void process(LaborPower... laborPowers);

	/**
	 * Scraps the machines under the specified productivity threshold.
	 * @param threshold productivity threshold under which the machines are to be scraped.
	 */
	void scrap(double threshold);

}

// ***
