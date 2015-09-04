package jamel.jamel.firms.managers;

import jamel.basic.util.Timer;
import jamel.jamel.widgets.Supply;

/**
 * Represents the sales manager of the firm.
 */
public abstract class SalesManager extends AbstractManager implements Askable {
	
	/**
	 * Creates a new manager.
	 * 
	 * @param name
	 *            the name of the manger.
	 * @param timer
	 *            the timer.
	 */
	public SalesManager(String name, Timer timer) {
		super(name, timer);
	}

	/**
	 * Creates a new supply. Must be called at the end of the production phase.
	 */
	public abstract void createSupply();

	/**
	 * Returns the supply.
	 * 
	 * @return the supply.
	 */
	public abstract Supply getSupply();

}

// ***
