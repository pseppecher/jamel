package jamel.jamel.roles;

import jamel.basic.agent.Agent;
import jamel.jamel.firms.capital.StockCertificate;
import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.Cheque;

/**
 * A capital owner, or asset owner, is an agent that can receives dividends.
 */
public interface Shareholder extends Agent {

	/**
	 * Adds the given asset to the possessions of this capital owner.
	 * 
	 * @param asset
	 *            the asset to be added.
	 */
	void addAsset(Asset asset);

	/**
	 * Receives notification of the bankruptcy of the specified asset.
	 * 
	 * @param asset
	 *            the bankrupted asset.
	 */
	void removeAsset(Asset asset);

	/**
	 * Takes the dividends.
	 */
	void takeDividends();

	/**
	 * Returns the financial capacity of this shareholder (i.e. the amount of
	 * cash available for buying assets).
	 * 
	 * @return the financial capacity of this shareholder.
	 */
	long getFinancialCapacity();

	/**
	 * Buys the specified amount of shares at the specified price.
	 * @param shares a {@link StockCertificate} that encapsulates the shares to be bought.
	 * @param price the price of the shares.
	 * @return a cheque. 
	 */
	Cheque buy(StockCertificate shares, long price);

}

// ***
