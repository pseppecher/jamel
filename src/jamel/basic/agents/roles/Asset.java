package jamel.basic.agents.roles;

/**
 * Represents an asset.
 */
public interface Asset {
	
	/**
	 * Returns the net value (or capital) of the asset.
	 * @return the net value.
	 */
	long getCapital();

	/**
	 * Returns the name of this asset.
	 * @return the name.
	 */
	String getName();

}
