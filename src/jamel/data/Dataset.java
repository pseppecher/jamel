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
	 * @param key
	 *            the key for the data to be returned.
	 * @return the value of the specified data.
	 */
	public Double get(Object key);

	/**
	 * Associates the specified value with the specified key in this map. If the
	 * map previously contained a mapping for the key, the old value is
	 * replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            value to be associated with the specified key.
	 * @return the previous value associated with key, or null if there was no
	 *         mapping for key. (A null return can also indicate that the map
	 *         previously associated null with key.)
	 */
	public Double put(String key, Double value);

}
