package jamel.util;

/**
 * Thrown to indicate that a method should not to be called.
 */
public class NotUsedException extends RuntimeException {

	/**
	 * Constructs a {@code NotUsedException}.
	 */
	public NotUsedException() {
		super("This method should not be called.");
	}

}
