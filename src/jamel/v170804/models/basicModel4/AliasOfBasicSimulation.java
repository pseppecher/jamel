package jamel.v170804.models.basicModel4;

import java.io.File;

import jamel.util.Parameters;
import jamel.v170804.util.BasicSimulation;

/**
 * An alias of {@code BasicSimulation}.
 */
public class AliasOfBasicSimulation extends BasicSimulation {

	/**
	 * Creates an new simulation.
	 * 
	 * @param scenario
	 *            the parameters of the simulation.
	 * @param file
	 *            The file that contains the description of the simulation.
	 */
	public AliasOfBasicSimulation(final Parameters scenario, final File file) {
		super(scenario, file);
	}

}
