package jamel.models.modelJEE.roles;

import java.util.List;

import jamel.models.modelJEE.capital.StockCertificate;
import jamel.models.modelJEE.util.Asset;

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
	 * 
	 * @param shares
	 *            list of the shares of each new owner.
	 * @return the stock certificates of the new owners.
	 */
	StockCertificate[] getNewShares(List<Integer> shares);

	/**
	 * Returns the size of this firm (the number of machines).
	 * 
	 * @return the size of this firm (the number of machines).
	 */
	int getSize();

	/**
	 * Returns the total value of the assets of this firm.
	 * 
	 * @return the total value of the assets of this firm.
	 */
	long getValueOfAssets();

	/**
	 * Returns the total value of the liabilities of this firm.
	 * 
	 * @return the total value of the liabilities of this firm.
	 */
	long getValueOfLiabilities();
}

// ***
