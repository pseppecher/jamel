package jamel.data;

import org.jfree.data.xy.XYDataItem;

import jamel.util.Agent;

/**
 * Represents an agent dataset.
 */
public interface AgentDataset {

	/**
	 * Returns the average value of the specified subset of data.
	 * 
	 * @param index
	 *            the index for the data to be averaged.
	 * @param laps
	 *            the number of data in the subset.
	 * 
	 * @return the average of the specified data.
	 */
	double average(int index, int laps);

	/**
	 * Closes the dataset.
	 */
	void close();

	/**
	 * Returns the owner of this dataset.
	 * 
	 * @return the owner of this dataset.
	 */
	Agent getAgent();

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param index
	 *            the index for the data to be returned.
	 * @param t
	 *            the period of the data to be returned
	 * @return the value of the specified data.
	 */
	Double getData(int index, int t);

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param key
	 *            the key for the data to be returned.
	 * @param t
	 *            the period of the data to be returned
	 * @return the value of the specified data.
	 */
	Double getData(final String key, final int t);

	/**
	 * Returns the index of the specified data.
	 * 
	 * @param key
	 *            the key of the data.
	 * @return the index of the data.
	 */
	int getDataIndex(String key);

	/**
	 * Returns the specified {@code XYDataItem} for the specified period.
	 * 
	 * @param x
	 *            the key for the x value.
	 * @param y
	 *            the key for the y value.
	 * @param t
	 *            the period.
	 * @return the specified {@code XYDataItem}.
	 */
	XYDataItem getXYDataItem(String x, String y, int t);

	/**
	 * Opens the dataset.
	 * 
	 * Must be called at the beginning of the period, before adding data.
	 */
	void open();

	/**
	 * Inserts the specified boolean value at the specified position in this
	 * agent dataset. If the agent dataset previously contained a value for the
	 * specified index, an exception is thrown.
	 * 
	 * @param index
	 *            index with which the specified value is to be associated.
	 * 
	 * @param b
	 *            boolean value to be associated with the specified key.
	 */
	void put(int index, boolean b);

	/**
	 * Inserts the specified value at the specified position in this agent
	 * dataset. If the agent dataset previously contained a value for the
	 * specified index, an exception is thrown.
	 * 
	 * @param index
	 *            index with which the specified value is to be associated.
	 * 
	 * @param value
	 *            value to be associated with the specified key.
	 */
	void put(int index, Number value);

	/**
	 * Returns the sum the specified subset of data.
	 * 
	 * @param index
	 *            the index for the data to be summed.
	 * @param laps
	 *            the number of data in the subset.
	 * 
	 * @return the sum of the specified data.
	 */
	double sum(int index, int laps);

}
