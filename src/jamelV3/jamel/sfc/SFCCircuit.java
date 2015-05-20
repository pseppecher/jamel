package jamelV3.jamel.sfc;

import org.w3c.dom.Element;

import jamelV3.basic.BasicCircuit;
import jamelV3.basic.data.BasicDataManager;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

/**
 * A basic circuit with stock-flow consistent extensions.
 */
public class SFCCircuit extends BasicCircuit {

	@Override
	protected BasicDataManager getNewDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		return new SFCDataManager(settings, timer, path, name);
	}

	/**
	 * Creates a new basic circuit.
	 * @param circuitElem a XML element with the parameters for the new circuit.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public SFCCircuit(Element circuitElem, String path, String name) throws InitializationException {
		super(circuitElem, path, name);
	}

}

// ***
