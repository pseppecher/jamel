package jamelV3.basic.sector;

import jamelV3.basic.agent.AgentDataset;

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
	 * Returns a list of XYDataItem that contains the specified values for each agent selected.
	 * @param xKey  the key for x values.
	 * @param yKey  the key for y values.
	 * @param select  the method to select the agents. 
	 * @return a list of XYDataItem.
	 */
	List<XYDataItem> getScatter(String xKey, String yKey, String select);

	/**
	 * Returns an array of xyz items.
	 * Each element is an array of length 3 that contains the x, y, z values.
	 * @param xKey  the x key.
	 * @param yKey  the y key.
	 * @param zKey  the z key.
	 * @return an array of xyz items.
	 */
	double[][] getXYZData(String xKey, String yKey, String zKey);

	/**
	 * Stores the specified agent data into this sector dataset.  
	 * @param data the data to be stored.
	 */
	void put(AgentDataset data);

}

// ***
