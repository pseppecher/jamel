package jamel.basic.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.SwingUtilities;

/**
 * A basic implementation of the {@link JamelParameters} interface.
 * 
 * @author pascal
 *
 */
public class BasicParameters extends HashMap<String, Float> implements JamelParameters {

	/**
	 * The name of the sector.
	 */
	final private String sectorName;

	/**
	 * Creates a new basic set of parameters for the specified sector.
	 * 
	 * @param sectorName
	 *            the name of the sector.
	 */
	public BasicParameters(String sectorName) {
		this.sectorName = sectorName;
	}

	@Override
	public Float get(String key) {
		Float result = super.get(key);
		if (result == null) {

			// Si le paramètre est manquant on le demande à l'utilisateur.

			final InputParameterDialog input = new InputParameterDialog(sectorName, key);

			try {
				SwingUtilities.invokeAndWait(input);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			result = Float.parseFloat(input.getValue());
			super.put(key, result);

		}
		return result;
	}

	@Override
	public void put(String key, float value) {
		super.put(key, value);
	}

}

// ***
