package jamel.jamel.util;

/**
 * Thrown to indicate that a method has detected an accounting inconsistency.
 */
public class ConsistencyException extends RuntimeException {

	/**
	 * Constructs an <code>ConsistencyException</code> with no detail message.
	 */
	public ConsistencyException() {
		super();
	}

	/**
	 * Constructs an <code>ConsistencyException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public ConsistencyException(String message) {
		super(message);
	}

}

// ***
