package jamel.models.m18.r08.data;

/**
 * Represents a dataset for one agent and one period.
 */
public interface PeriodDataset {

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param index
	 *            the index of the data to be returned.
	 * 
	 * @return the value of the specified data.
	 */
	public Double get(int index);

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param key
	 *            the key for the data to be returned.
	 * 
	 * @return the value of the specified data.
	 */
	public Double get(String key);

	/**
	 * Returns the period of the dataset.
	 * 
	 * @return the period of the dataset.
	 */
	public int getPeriod();

	/**
	 * Inserts the specified value at the specified position in this dataset. If
	 * the dataset previously contained a value for the specified index, an
	 * exception is thrown.
	 * 
	 * @param index
	 *            index with which the specified value is to be associated.
	 * 
	 * @param value
	 *            value to be associated with the specified key.
	 */
	public void put(int index, Number value);

}
