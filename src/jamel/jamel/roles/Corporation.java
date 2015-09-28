package jamel.jamel.roles;

import java.util.List;

import jamel.jamel.capital.StockCertificate;
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

	/**
	 * Clears the ownership of this corporation and shares its capital.
	 * @param shares list of the shares of each new owner. 
	 * @return the stock certificates of the new owners.
	 */
	StockCertificate[] getNewShares(List<Integer> shares);

}

// ***
