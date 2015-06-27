package jamel.jamel.firms.managers;

import jamel.basic.agent.AgentDataset;

/**
 * The pricing manager.
 */
public interface PricingManager {

	/**
	 * Closes the manager.<p>
	 * Updates internal variables at the end of the period. 
	 */
	void close();

	/**
	 * Returns the dataset of the manager.
	 * @return the dataset of the manager.
	 */
	AgentDataset getData();

	/**
	 * Returns the price.
	 * @return the price.
	 */
	Double getPrice();
	
	/**
	 * Opens the manager.
	 */
	void open();

	/**
	 * Updates the price.
	 */
	void updatePrice();

}