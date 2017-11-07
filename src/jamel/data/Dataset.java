package jamel.data;

/**
 * Represents a dataset.
 */
public interface Dataset {

	/**
	 * Removes all of the data from this dataset. The dataset will be empty
	 * after this call returns.
	 */
	public void clear();

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
