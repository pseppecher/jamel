package jamel.v170804.util;

/**
 * A utility class for checking parameters.
 * Inspired by the class ParamChecks in JFreeChart.
 */
public class ArgChecks {

    /**
     * Throws an <code>IllegalArgumentException</code> if the supplied 
     * <code>param</code> is <code>null</code>.
     *
     * @param param  the parameter to check (<code>null</code> permitted).
     * @param name  the name of the parameter (to use in the exception message
     *     if <code>param</code> is <code>null</code>).
     *
     * @throws IllegalArgumentException  if <code>param</code> is 
     *     <code>null</code>.
     *
     */
    public static void nullNotPermitted(Object param, String name) {
        if (param == null) {
            throw new IllegalArgumentException("Null '" + name + "' argument.");
        }
    }
    
}
