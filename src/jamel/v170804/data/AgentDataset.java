package jamel.v170804.data;

import jamel.util.Agent;

/**
 * Represents an agent dataset.
 */
public interface AgentDataset {

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
	 * @param key
	 *            the key for the data to be returned.
	 * @param t
	 *            the period of the data to be returned
	 * @return the value of the specified data.
	 */
	Double getData(final String key, final int t);

	/**
	 * Opens the dataset.
	 * 
	 * Must be called at the beginning of the period, before adding data.
	 */
	void open();

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

}