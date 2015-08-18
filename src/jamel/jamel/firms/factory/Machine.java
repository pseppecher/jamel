package jamel.jamel.firms.factory;

import jamel.jamel.widgets.LaborPower;

import java.util.List;

/**
 * Represents a machine.
 */
public interface Machine {

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
	public List<Materials> work(final LaborPower laborPower,
			Materials... inputs);

}

// ***
