package jamel.basic.util;

/**
 * Signals that an initialization exception of some sort has occurred. 
 * This class is the general class of exceptions produced by failed or interrupted initialization operations. 
 */
public class InitializationException extends Exception {

	/**
	 * Constructs an <code>InitializationException</code> with no detail message.
	 */
	public InitializationException() {
		super();
	}

	/**
	 * Constructs an <code>InitializationException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public InitializationException(String message) {
		super(message);
	}

	/**
	 * Constructs an <code>InitializationException</code> with the specified detail message and the specified cause.
	 * @param message the detail message.
	 * @param cause the cause.
	 */
	public InitializationException(String message, Throwable cause) {
		super(message);
		this.initCause(cause);
	}

}

// ***
