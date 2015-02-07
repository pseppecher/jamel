package jamel.basic.agents.roles;

import jamel.basic.util.Cheque;

/**
 * A capital owner, or asset owner, is an object that can receives dividends.
 */
public interface Shareholder extends Agent {

	/**
	 * Adds the given asset to the possessions of this capital owner.
	 * @param asset the asset to be added.
	 */
	void addAsset(Asset asset);

	/**
	 * Receives dividend.
	 * @param cheque the dividend.
	 * @param asset the asset that pays the dividend.
	 */
	void receiveDividend(Cheque cheque,Asset asset);

	/**
	 * Receives notification of the bankruptcy of the specified asset. 
	 * @param asset the bankrupted asset.
	 */
	void removeAsset(Asset asset);

}

//***
