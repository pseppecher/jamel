package jamel.util;

/**
 * A utility class for checking agruments.
 * Inspired by the class ParamChecks in JFreeChart.
 */
public class ArgChecks {

	/**
	 * Throws an <code>IllegalArgumentException</code> if the supplied
	 * <code>number</code> is is less than or equal to 0.
	 *
	 * @param number
	 *            the parameter to check (<code>null</code> not permitted).
	 * @param name
	 *            the name of the parameter (to use in the exception message if
	 *            <code>number</code> is less than or equal to 0.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>number</code> is less than or equal to 0.
	 */
	public static void negativeOr0NotPermitted(Number number, String name) {
		if (number.doubleValue() <= 0) {
			throw new IllegalArgumentException("Negative or zero '" + name + "' argument.");
		}
	}

	/**
	 * Throws an <code>IllegalArgumentException</code> if the supplied
	 * <code>param</code> is <code>null</code>.
	 *
	 * @param param
	 *            the parameter to check (<code>null</code> permitted).
	 * @param name
	 *            the name of the parameter (to use in the exception message
	 *            if <code>param</code> is <code>null</code>).
	 *
	 * @throws IllegalArgumentException
	 *             if <code>param</code> is
	 *             <code>null</code>.
	 */
	public static void nullNotPermitted(Object param, String name) {
		if (param == null) {
			throw new IllegalArgumentException("Null '" + name + "' argument.");
		}
	}

}
