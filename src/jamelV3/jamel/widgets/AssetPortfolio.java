package jamelV3.jamel.widgets;

/**
 * The collection of assets that the agent owns.
 */
public interface AssetPortfolio {

	/**
	 * Adds an asset to the list.
	 * @param asset the asset to be added.
	 */
	void add(Asset asset);

	/**
	 * Returns <code>true</code> if the given asset is in these possessions, <code>false</code> otherwise.
	 * @param asset the asset.
	 * @return a boolean.
	 */
	boolean contains(Asset asset);

	/**
	 * Returns the net value of the possessions (the sum of the capital of each asset owned).
	 * @return the net value of the possessions.
	 */
	long getNetValue();

	/**
	 * Removes the specified asset.
	 * @param asset the asset to be removed.
	 */
	void remove(Asset asset);

}

// ***
