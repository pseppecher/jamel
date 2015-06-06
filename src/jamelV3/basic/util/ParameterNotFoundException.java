package jamelV3.basic.util;

/**
 * Signals that an attempt to get the parameter denoted by a specified name has failed.
 */
public class ParameterNotFoundException extends RuntimeException {

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 * @param message  the detail message.
	 */
	public ParameterNotFoundException(String message) {
		super("\""+message+"\".");
	}

}

// ***
