package jamel.jamel.firms.factory;

import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

import java.util.List;
import java.util.Map;

/**
 * Represents a machine.
 */
public interface Machine extends Asset {

	/**
	 * Adds an input resource.
	 * 
	 * @param input
	 *            the input resource.
	 */
	public void addResource(Commodities input);

	/**
	 * Depreciates this machine.
	 * 
	 * @return the depreciation amount.
	 */
	public long depreciate();

	/**
	 * Returns the productivity of this machine.
	 * 
	 * @return the productivity of this machine.
	 */
	public long getProductivity();

	/**
	 * Returns the list of the resources this machine needs.
	 * 
	 * @return the list of the resources this machine needs.
	 */
	public String[] getResources();

	/**
	 * Returns the technical coefficients of this machine.
	 * 
	 * @return the technical coefficients of this machine.
	 */
	public Map<String, Float> getTechnicalCoefficients();

	/**
	 * Returns the type of production.
	 * 
	 * @return the type of production.
	 */
	public String getTypeOfProduction();

	/**
	 * Returns the cost of one unit of product of this machine given the
	 * specified costs.
	 * 
	 * @param costs
	 *            the input costs.
	 * @return the unit product cost.
	 */
	public Double getUnitProductCost(Map<String, Double> costs);

	/**
	 * Removes this machine from service so as to convert it to scrap metal.
	 * 
	 * @return a heap of finished goods representing the scrap value of this
	 *         machine.
	 */
	public FinishedGoods scrap();

	/**
	 * Works the machine.
	 * 
	 * @param laborPower
	 *            the labor power.
	 * @param inputs
	 *            the inputs.
	 * @return the outputs.
	 */
	public List<Materials> work(final LaborPower laborPower, Materials... inputs);

}

// ***
