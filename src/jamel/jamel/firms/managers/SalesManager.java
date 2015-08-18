package jamel.jamel.firms.managers;

import jamel.basic.data.AgentDataset;
import jamel.jamel.widgets.Supply;

/**
 * Represents the sales manager of the firm.
 */
public interface SalesManager {

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
	 * Returns the gross profit of the period. 
	 * <p>
	 * "In accounting, gross profit or sales profit or 'credit sales' is the
	 * difference between revenue and the cost of making a product or providing
	 * a service, before deducting overhead, payroll, taxation, and interest
	 * payments (...) Gross profit = Net sales Ð Cost of goods sold"
	 * 
	 * (ref: <a
	 * href="https://en.wikipedia.org/wiki/Gross_profit">wikipedia.org</a>)
	 * 
	 * @return the gross profit of the period.
	 */
	double getGrossProfit();

	/**
	 * Returns the ratio <code>sales/supply</code>.
	 * @return the ratio <code>sales/supply</code>.
	 */
	Double getSalesRatio();

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