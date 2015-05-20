package jamelV3.jamel.sfc;

import java.awt.Component;

/**
 * The data validator.
 */
public interface DataValidator {

	/**
	 * Checks data consistency.
	 * @return <code>true</code> if the data are consistent, <code>false</code> otherwise.
	 */
	boolean CheckConsistency();

	/**
	 * Returns a panel with the result of the tests.
	 * @return a panel with the result of the tests.
	 */
	Component getPanel();

}

// ***
