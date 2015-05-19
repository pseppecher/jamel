package jamelV3.jamel.roles;

import jamelV3.jamel.widgets.Asset;

/**
 * Represents a corporation (a business entity like a firm or a bank).
 */
public interface Corporation extends Asset {
	
	@Override
	long getBookValue();

}

//***
