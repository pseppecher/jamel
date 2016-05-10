package jamel.basic.data;

import java.util.List;

import org.jfree.data.xy.XYDataItem;

/**
 * A database for the sector level.
 */
public interface SectorDataset {

	/**
	 * Returns the specified info about the specified agent.
	 * 
	 * @param agent
	 *            the name of the agent.
	 * @param key
	 *            the key of the info to be returned.
	 * @return the specified info about the specified agent.
	 */
	String getAgentInfo(String agent, String key);

	/**
	 * Returns the specified value for the specified agent.
	 * 
	 * @param dataKey
	 *            the name of field.
	 * @param agentName
	 *            the name of the agent.
	 * @return the value.
	 */
	Double getAgentValue(String dataKey, String agentName);

	/**
	 * Returns sector data at individual level.
	 * 
	 * @param dataKeys
	 *            the keys of the data to be returned.
	 * @param select
	 *            selection mode.
	 * @return a dataset of individual data.
	 */
	Object[][] getData(String[] dataKeys, String select);

	/**
	 * Returns an array of values for the specified key.
	 * 
	 * @param key
	 *            the key.
	 * @param select
	 *            a String that describes the elements to be selected (
	 *            <code>null</code> not permitted). If select is empty, all
	 *            elements are selected.
	 * @return an array of values for the specified key.
	 */
	Double[] getField(String key, String select);

	/**
	 * Returns the Gini index of the specified field in a selection of individual
	 * datasets.
	 * 
	 * @param data
	 *            the name of the field.
	 * @param select
	 *            specifies how to select the individual datasets.
	 * @return the Gini index of the specified field in a selection of individual
	 *         datasets.
	 */
	Double getGini(String data, String select);

	/**
	 * Returns the max of the specified field in a selection of individual
	 * datasets.
	 * 
	 * @param data
	 *            the name of the field.
	 * @param select
	 *            specifies how to select the individual datasets.
	 * @return the max of the specified field in a selection of individual
	 *         datasets.
	 */
	Double getMax(String data, String select);

	/**
	 * Returns the mean of the specified field in a selection of individual
	 * datasets.
	 * 
	 * @param data
	 *            the name of the field.
	 * @param select
	 *            specifies how to select the individual datasets.
	 * @return the mean of the specified field in a selection of individual
	 *         datasets.
	 */
	Double getMean(String data, String select);

	/**
	 * Returns the min of the specified field in a selection of individual
	 * datasets.
	 * 
	 * @param data
	 *            the name of the field.
	 * @param select
	 *            specifies how to select the individual datasets.
	 * @return the min of the specified field in a selection of individual
	 *         datasets.
	 */
	Double getMin(String data, String select);

	/**
	 * Returns a list of XYDataItem that contains the specified values for each
	 * agent selected.
	 * 
	 * @param xKey
	 *            the key for x values.
	 * @param yKey
	 *            the key for y values.
	 * @param select
	 *            the method to select the agents.
	 * @return a list of XYDataItem.
	 */
	List<XYDataItem> getScatter(String xKey, String yKey, String select);

	/**
	 * Returns the specified sectorial value.
	 * <p>
	 * Hendrik: The reason why I need this is that there are certain data that
	 * are sector-specific that can only be computed at the sector level (and
	 * not at the agent level). For example the calculation of GDP and real GDP.
	 * These require computations that are not simply the sum over the data of
	 * all agents in the sector (or something similar), but something more
	 * complicated as, for example, the credit market is excluded in those
	 * operations. Another example would be the number of firms that are created
	 * per time period.
	 * <p>
	 * 
	 * @param key
	 *            the key whose associated value is to be returned.
	 * @return the specified value.
	 */
	Double getSectorialValue(String key);

	/**
	 * Returns the sum of the specified field in a selection of individual
	 * datasets.
	 * 
	 * @param data
	 *            the name of the field.
	 * @param select
	 *            specifies how to select the individual datasets.
	 * @return the sum of the specified field in a selection of individual
	 *         datasets.
	 */
	Double getSum(String data, String select);

	/**
	 * Returns the xyz data (an array with length 3, containing three arrays of
	 * equal length, the first containing the x-values, the second containing
	 * the y-values and the third containing the z-values).
	 * 
	 * @param xKey
	 *            the x key.
	 * @param yKey
	 *            the y key.
	 * @param zKey
	 *            the z key.
	 * @return the xyz data.
	 */
	double[][] getXYZData(String xKey, String yKey, String zKey);

	/**
	 * Stores the specified agent data into this sector dataset.
	 * 
	 * @param data
	 *            the data to be stored.
	 */
	void putIndividualData(AgentDataset data);
	
	/**
	 * Stores the specified sector value into this dataset. Hendrik: The reason
	 * why I need this is that there are certain data that are sector-specific
	 * that can only be computed at the sector level (and not at the agent
	 * level). For example the calculation of GDP and real GDP. These require
	 * computations that are not simply the sum over the data of all agents in
	 * the sector (or something similar), but something more complicated as, for
	 * example, the credit market is excluded in those operations. Another
	 * example would be the number of firms that are created per time period.
	 * 
	 * @param key
	 *            the key for the value to be added.
	 * @param value
	 *            the value to be added.
	 */
	void putSectorialValue(String key, Number value);

}

// ***
