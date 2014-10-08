package jamel.basic.data;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A TreeMap to aggregate the data at the sector level.
 */
@SuppressWarnings("serial")
public class SectorData extends TreeMap<String, Double>{

	/**
	 * Adds the data of a single agent to the sector data.
	 * @param data the individual data to be added.
	 */
	public void addData(Map<String, Double> data) {
		for (Entry<String,Double> e:data.entrySet()){
			final String key = e.getKey();
			final Double value = e.getValue();
			if (!this.containsKey(key)) {
				this.put(key, value);
			}
			else {
				final Double result = this.get(key) + value;
				this.put(key, result);
			}
		}
	}

}
