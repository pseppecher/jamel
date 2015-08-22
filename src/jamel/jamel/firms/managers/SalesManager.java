package jamel.jamel.firms.managers;

import jamel.basic.data.AgentDataset;
import jamel.jamel.widgets.Supply;

/**
 * Represents the sales manager of the firm.
 */
public interface SalesManager extends Askable {
	
	/**
	 * Closes the manager at the end of the period.
	 */
	void close();

	/**
	 * Creates a new supply. Must be called at the end of the production phase.
	 */
	void createSupply();

	/**
	 * Returns the metrics of the manager.
	 * 
	 * @return the metrics of the manager.
	 */
	AgentDataset getData();

	/**
	 * Returns the supply.
	 * 
	 * @return the supply.
	 */
	Supply getSupply();

	/**
	 * Opens the manager at the beginning of the period.
	 */
	void open();

}

// ***