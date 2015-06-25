package jamel.basic.sector;

import jamel.basic.agent.AgentDataset;

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
	 * Returns the xyz data (an array with length 3, containing three arrays of equal length, the first containing the x-values, the second containing the y-values and the third containing the z-values).
	 * @param xKey  the x key.
	 * @param yKey  the y key.
	 * @param zKey  the z key.
	 * @return the xyz data.
	 */
	double[][] getXYZData(String xKey, String yKey, String zKey);

	/**
	 * Stores the specified agent data into this sector dataset.  
	 * @param data the data to be stored.
	 */
	void put(AgentDataset data);

}

// ***
