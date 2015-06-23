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
	
	/**
	 * Creates a new basic circuit.
	 * @param elem a XML element with the parameters for the new circuit.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public SFCCircuit(Element elem, String path, String name) throws InitializationException {
		super(elem, path, name);
	}

	@Override
	protected BasicDataManager getNewDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		return new SFCDataManager(this, settings, timer, path, name);
	}

}

// ***
