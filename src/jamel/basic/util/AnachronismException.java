package jamel.basic.util;

/**
 * Thrown to indicate that a method has detected a time inconsistency.
 */
@SuppressWarnings("serial")
public class AnachronismException extends RuntimeException {

	/**
	 * Constructs an <code>AnachronismException</code> with no detail message.
	 */
	public AnachronismException() {
		super();
	}

	/**
	 * Constructs an <code>AnachronismException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public AnachronismException(String message) {
		super(message);
	}

}

// ***
