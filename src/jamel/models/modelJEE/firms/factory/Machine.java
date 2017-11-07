package jamel.models.modelJEE.firms.factory;

import java.util.List;
import java.util.Map;

import jamel.models.modelJEE.util.Asset;
import jamel.models.util.JobContract;
import jamel.models.util.Worker;

/**
 * Represents a machine.
 */
public interface Machine extends Asset {

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
	 * Returns the cost of one unit of product of this machine given the
	 * specified costs.
	 * 
	 * @param costs
	 *            the input costs.
	 * @return the unit product cost.
	 */
	public Double getUnitProductCost(Map<String, Double> costs);

	/**
	 * Works the machine.
	 * 
	 * @param jobContract
	 *            the jobContract.
	 * @param inputs
	 *            the inputs.
	 * @return the outputs.
	 */
	public List<Materials> work(final JobContract jobContract, Materials... inputs);

}
