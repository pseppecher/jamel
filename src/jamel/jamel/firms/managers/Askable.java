package jamel.jamel.firms.managers;

/**
 * That can be asked.
 */
public interface Askable {

	/**
	 * Returns the specified element, or <code>null</code> if the specified
	 * element is not available.
	 * 
	 * @param key
	 *            the key of the element to be returned.
	 * @return an object.
	 */
	Object askFor(String key);

}

// ***
