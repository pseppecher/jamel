package jamel.jamel.roles;

import jamel.jamel.widgets.Asset;

/**
 * Represents a corporation (a business entity like a firm or a bank).
 */
public interface Corporation extends Asset {

	@Override
	Long getBookValue();

	/**
	 * Returns the name of this corporation.
	 * 
	 * @return the name of this corporation.
	 */
	String getName();

}

// ***
