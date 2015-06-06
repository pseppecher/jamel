package jamelV3.basic.util;

import java.util.HashMap;

/**
 * A basic implementation of the {@link JamelParameters} interface.
 */
public class BasicParameters extends HashMap<String, Float> implements
		JamelParameters {

	@Override
	public Float get(String key) {
		final Float result = super.get(key);
		if (result==null) {
			throw new ParameterNotFoundException(key);
		}
		return result;
	}

	@Override
	public void put(String key, float value) {
		super.put(key, value);
	}

}

// ***