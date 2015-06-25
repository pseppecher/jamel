package jamel.jamel.sfc;

import java.awt.Component;

/**
 * The data validator.
 */
public interface DataValidator {

	/**
	 * Checks data consistency.
	 * @return <code>true</code> if the data are consistent, <code>false</code> otherwise.
	 */
	boolean checkConsistency();

	/**
	 * Returns the name of the data validator.
	 * @return  the name of the data validator.
	 */
	String getName();

	/**
	 * Returns a panel with the result of the tests.
	 * @return a panel with the result of the tests.
	 */
	Component getPanel();

}

// ***
