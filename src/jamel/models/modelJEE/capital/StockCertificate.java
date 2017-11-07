package jamel.models.modelJEE.capital;

import jamel.models.modelJEE.roles.Corporation;
import jamel.models.modelJEE.util.Asset;
import jamel.models.util.AccountHolder;
import jamel.models.util.Cheque;

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
	 * @param shareholder
	 *            the shareholder.
	 * 
	 * @return the cheque for the dividend.
	 */
	Cheque getDividend(AccountHolder shareholder);

	/**
	 * Returns the number of shares in this set.
	 * 
	 * @return the number of shares in this set.
	 */
	int getShares();

}

// ***
