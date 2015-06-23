package jamelV3.jamel.firms.managers;

/**
 * The pricing manager.
 */
public interface PricingManager {

	/**
	 * Returns the price.
	 * @return the price.
	 */
	Double getPrice();

	/**
	 * Updates the price.
	 */
	void updatePrice();

	/**
	 * Closes the manager.<p>
	 * Updates internal variables at the end of the period. 
	 */
	void close();

}