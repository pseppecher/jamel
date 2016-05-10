package jamel.jamel.firms.factory;

import java.util.Map;

import jamel.basic.data.AgentDataset;
import jamel.jamel.firms.managers.Askable;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a factory.
 */
public interface Factory extends Askable {

	/**
	 * Cancels this factory.
	 */
	void cancel();

	/**
	 * Closes the factory.
	 */
	void close();

	/**
	 * Deletes the inventories and the machinery of this factory. The value of
	 * the factory will be zero.
	 */
	void delete();

	/**
	 * Expands the capacity of this factory by adding the specified machines to
	 * its machinery.
	 * 
	 * @param machines
	 *            the new machines to be added.
	 */
	void expandCapacity(Machine[] machines);

	/**
	 * Returns the capacity of the factory, i.e. its maximum number of workers,
	 * i.e its number of machines.
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
	 * Returns the current maximum capacity according to the level of input
	 * stocks.
	 * 
	 * @return the current maximum capacity.
	 */
	int getCurrentMaxCapacity();

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
	 * @return the inventory ratio.
	 */
	double getInventoryRatio();

	/**
	 * Returns the list of the inputs this factory needs.
	 * @return the list of the inputs this factory needs.
	 */
	Map<String, Long> getNeeds();

	/**
	 * Returns the average volume of production (finished goods) at the maximum
	 * utilization of the production capacity.
	 * 
	 * @return a volume.
	 */
	double getPotentialOutput();

	/**
	 * Returns the unit cost of the finished goods in the inventory. The unit
	 * cost is
	 * "The cost incurred by a company to produce, store and sell one unit of a particular product."
	 * 
	 * @see <a href="http://www.investopedia.com/terms/u/unitcost.asp">www.
	 *      investopedia.com/terms/u/unitcost.asp</a>
	 * @return the unit cost.
	 */
	Double getUnitCost();

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
	 * Provides this factory with the given input. 
	 * @param input the input to be provided.
	 */
	void putResources(Commodities input);

	/**
	 * Scraps the specified number of machines.
	 * 
	 * @param nMachine
	 *            the number of machines to be scrapped.
	 */
	void scrap(double nMachine);

	/**
	 * Sets the normal level of inventories.
	 * 
	 * @param normalLevel
	 *            the normal level, expressed as a number of period of
	 *            production at full capacity.
	 * 
	 */
	void setInventoryNormalLevel(float normalLevel);

	/**
	 * Returns the overhead cost by unit.
	 * 
	 * @return the overhead cost by unit.
	 */
	public double getOverheadCostByUnit();

}

// ***
