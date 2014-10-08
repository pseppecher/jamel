package jamel.basic.agents.util;

import jamel.util.Circuit;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * The data of one single agent.
 * TODO : les données anciennes ne sont jamais utilisées ? pourquoi les enregistrer alors ?
 */
public class AgentData {
	
	/** The map */
	private final TreeMap<Integer,HashMap<String,Double>> map = new TreeMap<Integer,HashMap<String,Double>>();
	
	/**
	 * Returns the current value of the data associated with given key.
	 * @param key the key.
	 * @return the current value.
	 */
	public Double get(String key) {
		final int p = Circuit.getCurrentPeriod().getValue();
		if (!map.containsKey(p)) {
			throw new RuntimeException("No data for the current period.");
		}
		return map.get(p).get(key);
	}
	
	/**
	 * Associates the specified value with the specified key for the current period.
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 */
	public void put(String key, double value) {
		final int p = Circuit.getCurrentPeriod().getValue();
		if (!map.containsKey(p)) {
			throw new RuntimeException("No data for the current period.");
		}
		map.get(p).put(key, value);
	}
	
	// TODO Work in progress

}

// ***
