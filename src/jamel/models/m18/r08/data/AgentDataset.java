package jamel.models.m18.r08.data;

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
	 * Returns the index of the specified data.
	 * 
	 * @param key
	 *            the key of the data.
	 * @return the index of the data.
	 */
	int getDataIndex(String key);

	/**
	 * Adds a new period dataset to this agent dataset.
	 * 
	 * @param periodDataset
	 *            the period dataset to be added.
	 */
	void put(PeriodDataset periodDataset);

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
