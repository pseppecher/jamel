package jamel.util;

/**
 * Thrown to indicate that a method is not yet implemented.
 */
public class NotYetImplementedException extends RuntimeException {

	/**
	 * Constructs a new not-yet-implemented exception.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 */
	public NotYetImplementedException() {
		super();
	}

	/**
	 * Constructs a new not-yet-implemented exception with the specified detail
	 * message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 *
	 * @param message
	 *            the detail message. The detail message is saved for
	 *            later retrieval by the {@link #getMessage()} method.
	 */
	public NotYetImplementedException(String message) {
		super(message);
	}

}
