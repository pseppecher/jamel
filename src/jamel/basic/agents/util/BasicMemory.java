package jamel.basic.agents.util;

import jamel.util.Circuit;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A basic implementation of the <code>Memory</code> interface.
 */
public class BasicMemory implements Memory{
	
	/** The data memorized. */
	final private TreeMap<Integer,HashMap<String,Double>> data = new TreeMap<Integer,HashMap<String,Double>>();
	
	/** The maximum number of period in memory. */
	final private int lim;
	
	/**
	 * Creates an new basic memory.
	 * @param lim the maximum number of period in memory.
	 */
	public BasicMemory(int lim) {
		this.lim=lim;
	}

	/**
	 * Adds a new empty record in memory for the current period.
	 */
	private void newPeriod() {
		final int now = Circuit.getCurrentPeriod().intValue();
		if (data.containsKey(now)) {
			throw new RuntimeException("This period is already in memory.");
		}
		data.put(now, new HashMap<String,Double>());
		if (data.size()>lim) {
			data.pollFirstEntry();
		}
	}
	
	@Override
	public void add(String key, double value) {
		if (!this.containsKey(key)) {
			put(key,value);
		}
		else {
			final int now = Circuit.getCurrentPeriod().intValue(); 
			value += data.get(now).get(key);
			data.get(now).put(key,value);
		}
	}

	@Override
	public boolean checkConsistency(String key1, String key2) {
		boolean result = true;
		for (Map<String,Double> map: data.values()) {
			if ((map.containsKey(key1)&&!map.containsKey(key2))||(!map.containsKey(key1)&&map.containsKey(key2))) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	@Override
	public boolean containsKey(String key) {
		final boolean result;
		final int now = Circuit.getCurrentPeriod().intValue(); 
		if (!data.containsKey(now)) {
			result = false;
		}
		else if (!data.get(now).containsKey(key)) {
			result = false;
		}
		else {
			result = true;
		}
		return result;
	}

	@Override
	public Double get(String key) {
		final int currentPeriod = Circuit.getCurrentPeriod().intValue();
		final Double result;
		if (!data.containsKey(currentPeriod)) {
			result = null;
		}
		else {
			result = data.get(currentPeriod).get(key);
		}
		return result;
	}

	@Override
	public Double getMean(String key, int start,int lim) {
		final Double result;
		if (!data.containsKey(start)) {
			result = null;
		}
		else {
			double sum = this.data.get(start).get(key);
			int n = 1;
			for(int t = start-1; t>start-lim; t--) {
				if (data.containsKey(t)) {
					sum += this.data.get(t).get(key);
					n++;					
				}
				else {
					break;
				}
			}
			result = sum/n;
		}
		return result;
	}

	@Override
	public Double getSum(String key, int start,int lim) {
		final Double result;
		if (!data.containsKey(start)) {
			result = null;
		}
		else {
			double sum = this.data.get(start).get(key);
			for(int t = start-1; t>start-lim; t--) {
				if (data.containsKey(t)) {
					sum += this.data.get(t).get(key);
				}
				else {
					break;
				}
			}
			result = sum;
		}
		return result;
	}

	@Override
	public void put(String key, double value) {
		final int now = Circuit.getCurrentPeriod().intValue(); 
		if (!data.containsKey(now)) {
			newPeriod();
		}
		if (data.get(now).containsKey(key)) {
			throw new RuntimeException("Data exists: "+key);
		}
		data.get(now).put(key,value);
	}

}

// ***
