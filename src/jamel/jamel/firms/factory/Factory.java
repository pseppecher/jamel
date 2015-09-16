package jamel.jamel.firms.factory;

import jamel.basic.data.AgentDataset;
import jamel.jamel.firms.managers.Askable;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a factory.
 */
public interface Factory extends Askable {

	/**
	 * Definitively closes the factory.
	 */
	void bankrupt();

	/**
	 * Closes the factory.
	 */
	void close();

	/**
	 * Expands the capacity of this factory by adding the specified machines to
	 * its machinery.
	 * 
	 * @param machines
	 *            the new machines to be added.
	 */
	void expandCapacity(Machine[] machines);

	/**
	 * Returns the capacity of the factory, i.e. its maximum number of workers.
	 * 
	 * @return the capacity.
	 */
	int getCapacity();

	/**
	 * Subtracts and returns the specified volume of commodities from the
	 * inventories.
	 * 
	 * @param demand
	 *            the volume of commodities.
	 * @return the demanded commodities.
	 */
	Commodities getCommodities(long demand);

	/**
	 * Returns the dataset of the factory.
	 * 
	 * @return the dataset of the factory.
	 */
	AgentDataset getData();

	/**
	 * Returns the volume of finished goods.
	 * 
	 * @return the volume of finished goods.
	 */
	long getFinishedGoodsVolume();

	/**
	 * Returns the value of inventory losses for the current period.
	 * 
	 * @return the value of inventory losses for the current period.
	 */
	long getInventoryLosses();

	/**
	 * Returns the inventory ratio.
	 * <p>
	 * If inventoryRatio upper than 1 : the volume of finished goods exceeds the
	 * normal volume,<br>
	 * If inventoryRatio = 1 : the volume of finished goods meets the normal
	 * volume,<br>
	 * If inventoryRatio lower than 1 : the volume of finished goods is under
	 * the normal volume.
	 * 
	 * @param normalLevel
	 *            the normal level, expressed as a number of period of
	 *            production at full capacity.
	 * 
	 * @return the inventory ratio.
	 */
	double getInventoryRatio(float normalLevel);

	/**
	 * Returns the average volume of production (finished goods) at the maximum
	 * utilization of the production capacity.
	 * 
	 * @return a volume.
	 */
	double getMaxUtilAverageProduction();

	/**
	 * Returns the unit cost of the finished goods in the inventory. The unit
	 * cost is
	 * "The cost incurred by a company to produce, store and sell one unit of a particular product."
	 * 
	 * @see <a href="http://www.investopedia.com/terms/u/unitcost.asp">www.
	 *      investopedia.com/terms/u/unitcost.asp</a>
	 * @return the unit cost.
	 */
	double getUnitCost();

	/**
	 * Returns the total value of finished and unfinished goods present in the
	 * inventories. (= "the raw materials, work-in-process goods and completely
	 * finished goods that are considered to be the portion of a business's
	 * assets that are ready or will be ready for sale")
	 * 
	 * @see <a href="http://www.investopedia.com/articles/04/031004.asp">
	 *      investopedia.com</a>
	 * @return the total value.
	 */
	long getValue();

	/**
	 * Opens the factory.
	 */
	void open();

	/**
	 * Produces new goods by the expense of the given units of labor power.
	 * 
	 * @param laborPowers
	 *            the labor powers.
	 */
	void process(LaborPower... laborPowers);

	/**
	 * Scraps the machines under the specified productivity threshold.
	 * 
	 * @param threshold
	 *            productivity threshold under which the machines are to be
	 *            scraped.
	 */
	void scrap(double threshold);

}

// ***
