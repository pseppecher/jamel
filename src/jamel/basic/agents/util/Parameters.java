package jamel.basic.agents.util;

import jamel.util.Circuit;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to store the parameters shared by the agents of one single sector.
 */
public class Parameters {

	/** The circuit. */
	private final Circuit circuit;

	/** A map to store the float values of the parameters. */
	private final Map<String,Float> map = new HashMap<String,Float>();

	/** The sector name. */
	final private String sectorName; 

	/**
	 * Creates a new set of parameters.
	 * @param sectorName the name of the sector.
	 * @param circuit the circuit.
	 */
	public Parameters(String sectorName,Circuit circuit) {
		this.sectorName = sectorName;
		this.circuit = circuit;
	}

	/**
	 * Returns the float value of the specified parameter.
	 * @param key the key of the parameter.
	 * @return a float.
	 */
	private Float getFloat(String key) {
		try {
			final String string = this.circuit.getParameter(sectorName,key);// TODO parametriser "Industry"
			final Float result;
			if (string!=null) {
				result = Float.parseFloat(string); 
			}
			else {
				result = null;
			}
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new RuntimeException("Parameter "+key+": a float was expected but I found: "+this.circuit.getParameter(key));
		}
	}

	/**
	 * Returns the float value of the specified parameter.
	 * @param key of the parameter the value of which is to be returned. 
	 * @return a float.
	 */
	public Float get(String key) {
		final Float value;
		if (this.map.containsKey(key)) {
			value = this.map.get(key);
		}
		else {
			value = getFloat(key);
			if (value!=null) {
				this.map.put(key, value);
			}
		}
		return value;
	}

	/**
	 * Updates the parameters.<p>
	 * Called when the firm is created.
	 * (Should be called after an exogenous event.)
	 */
	public void update() {
		for (String key: map.keySet()) {
			map.put(key, getFloat(key));
		}
	}

}