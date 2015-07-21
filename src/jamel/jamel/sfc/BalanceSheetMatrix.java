package jamel.jamel.sfc;

import java.awt.Component;

/**
 * The balance sheet matrix of the economy.
 */
public interface BalanceSheetMatrix {

	/**
	 * Returns a panel containing a representation of the balance sheet matrix.
	 * @return a panel containing a representation of the balance sheet matrix.
	 */
	Component getPanel();

	/**
	 * Updates the representation of the balance sheet matrix.
	 */
	void update();

}

// ***
