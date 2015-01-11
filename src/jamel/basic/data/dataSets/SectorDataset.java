package jamel.basic.data.dataSets;

import java.util.List;

import org.jfree.data.xy.XYDataItem;


/**
 * A dataset for the sector data.
 */
public interface SectorDataset {

	/**
	 * Returns the value.
	 * @param key the key whose associated value is to be returned.
	 * @return the value.
	 */
	Double get(String key);

	/**
	 * Stores the specified agent data into this sector dataset.  
	 * @param data the data to be stored.
	 */
	void put(AgentDataset data);

	/**
	 * Returns a list of XYDataItem that contains the specified values for each agent selected.
	 * @param method a string that contain the method to select the agents. 
	 * @param xKey the key for x values.
	 * @param yKey the key for y values.
	 * @return a list of XYDataItem.
	 */
	List<XYDataItem> getScatter(String method, String xKey, String yKey);

}

// ***
