package jamel.jamel.firms.managers;

import jamel.basic.util.Timer;

/**
 * The pricing manager.
 */
public abstract class PricingManager extends AbstractManager{

	/**
	 * Creates a new manager.
	 * 
	 * @param name
	 *            the name of the manger.
	 * @param timer
	 *            the timer.
	 */
	public PricingManager(String name, Timer timer) {
		super(name, timer);
	}

	/**
	 * Returns the price.
	 * @return the price.
	 */
	public abstract Double getPrice();
	
	/**
	 * Updates the price.
	 */
	public abstract void updatePrice();

	/**
	 * Sets the price.
	 * @param price the price to set.
	 */
	public abstract void setPrice(double price);

}

// ***
