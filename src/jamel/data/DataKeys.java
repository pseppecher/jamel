package jamel.data;

/**
 * Represents a set of data keys mapped with related data indexes.
 */
public interface DataKeys {

	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * data key.
	 * 
	 * @param dataKey
	 *            key whose presence in this map is to be tested.
	 * 
	 * @return {@code true} if this map contains a mapping for the specified
	 *         data key.
	 */
	public boolean containsKey(String dataKey);

	/**
	 * Returns the key to witch the specified index is mapped.
	 * 
	 * @param index
	 *            the index whose associated key is to be returned
	 * @return the key to which the specified index is mapped
	 */
	public String getKey(int index);

	/**
	 * Returns the index to which the specified key is mapped,
	 * or {@code null} if this set contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated index is to be returned
	 * @return the index to which the specified key is mapped
	 */
	public int indexOf(String key);

	/**
	 * Returns the size of this set.
	 * 
	 * @return the size of this set
	 */
	public int size();

}
