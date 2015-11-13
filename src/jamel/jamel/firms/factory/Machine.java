package jamel.jamel.firms.factory;

import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.LaborPower;

import java.util.List;

/**
 * Represents a machine.
 */
public interface Machine extends Asset {

	/**
	 * Returns the productivity of this machine.
	 * 
	 * @return the productivity of this machine.
	 */
	public long getProductivity();

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

	/**
	 * Depreciates this machine.
	 * 
	 * @return the depreciation amount.
	 */
	public long depreciate();

	/**
	 * Removes this machine from service so as to convert it to scrap metal.
	 * 
	 * @return a heap of finished goods representing the scrap value of this
	 *         machine.
	 */
	public FinishedGoods scrap();

}

// ***
