package jamel.v170804.data;

/**
 * Represents a set of data keys.
 */
public interface DataKeys {

	/**
	 * Returns the index to which the specified key is mapped,
	 * or {@code null} if this set contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated index is to be returned
	 * @return the index to which the specified key is mapped, or {@code null}
	 *         if this map contains no mapping for the key
	 */
	public int indexOf(String key);

	/**
	 * Returns the size of this set.
	 * 
	 * @return the size of this set
	 */
	public int size();

}
