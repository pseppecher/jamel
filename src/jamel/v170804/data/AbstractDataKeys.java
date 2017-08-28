package jamel.v170804.data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract record of data keys.
 */
public class AbstractDataKeys implements DataKeys {

	/**
	 * A map that associates data keys with indexes.
	 */
	final private Map<String, Integer> keys = new HashMap<>();

	/**
	 * The next index.
	 */
	private int nextIndex = 0;

	/**
	 * Returns the next key.
	 * 
	 * @return the next key.
	 */
	protected int getNextIndex() {
		final int result = this.nextIndex;
		this.nextIndex++;
		return result;
	}

	/**
	 * Associates an index to each data key.
	 * 
	 * @param fields
	 *            the list of the fields, each field representing a key.
	 */
	protected void init(Field[] fields) {
		if (fields.length != nextIndex) {
			throw new RuntimeException("Inconsistency");
		}
		int index = 0;
		for (Field field : fields) {
			this.keys.put(field.getName(), index);
			index++;
		}
	}

	@Override
	final public int indexOf(String key) {
		if (!this.keys.containsKey(key)) {
			throw new RuntimeException("Unknown data key: " + key);
		}
		return this.keys.get(key);
	}

	@Override
	final public int size() {
		return this.keys.size();
	}

}
