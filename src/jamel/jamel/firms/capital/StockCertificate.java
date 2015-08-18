package jamel.jamel.firms.capital;

import jamel.jamel.roles.Corporation;
import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.Cheque;

/**
 * A set of ownership shares of a {@link Corporation}.
 */
public interface StockCertificate extends Asset {

	/**
	 * Returns the corporation associated to this equity.
	 * 
	 * @return the corporation associated to this equity.
	 */
	Corporation getCorporation();

	/**
	 * Returns the dividend due. Called by the shareholder.
	 * 
	 * @return the cheque for the dividend.
	 */
	Cheque getDividend();

	/**
	 * Returns the number of shares in this set.
	 * 
	 * @return the number of shares in this set.
	 */
	int getShares();

}

// ***
