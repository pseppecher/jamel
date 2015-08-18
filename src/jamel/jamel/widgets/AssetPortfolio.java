package jamel.jamel.widgets;

import java.util.List;

/**
 * The collection of assets that the agent owns.
 */
public interface AssetPortfolio {

	/**
	 * Adds an asset to the list.
	 * 
	 * @param asset
	 *            the asset to be added.
	 */
	void add(Asset asset);

	/**
	 * Returns <code>true</code> if the given asset is in these possessions,
	 * <code>false</code> otherwise.
	 * 
	 * @param asset
	 *            the asset.
	 * @return a boolean.
	 */
	boolean contains(Asset asset);

	/**
	 * Returns the list of the assets.
	 * 
	 * @return the list of the assets.
	 */
	List<Asset> getList();

	/**
	 * Returns the net value of the possessions (the sum of the book value of
	 * each asset owned).
	 * 
	 * @return the net value of the possessions.
	 */
	long getNetValue();

	/**
	 * Removes the specified asset.
	 * 
	 * @param asset
	 *            the asset to be removed.
	 */
	void remove(Asset asset);

}

// ***
