package jamelV3.basic.util;

/**
 * The interface for Jamel parameters.
 */
public interface JamelParameters {

	/**
	 * Returns the <code>Float</code> value of the parameter to which the specified key is mapped.
	 * @param key  the key whose associated parameter is to be returned.
	 * @return the <code>Float</code> value of the parameter to which the specified key is mapped.
	 */
	Float get(String key);

	/**
	 * Associates the specified value with the specified key in this map. 
	 * If the map previously contained a mapping for the key, the old value should be replaced.
	 * @param key  key with which the specified value is to be associated.
	 * @param value  value to be associated with the specified key.
	 */
	void put(String key, float value);

}

// ***
