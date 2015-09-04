package jamel.jamel.firms.managers;

import jamel.basic.util.Timer;

/**
 * The production manager.
 */
public abstract class ProductionManager extends AbstractManager {

	/**
	 * Creates a new manager.
	 * 
	 * @param name
	 *            the name of the manger.
	 * @param timer
	 *            the timer.
	 */
	public ProductionManager(String name, Timer timer) {
		super(name, timer);
	}

	/**
	 * Returns the capacity utilization targeted.
	 * @return a float in [0,1].
	 */
	public abstract float getTarget();

	/**
	 * Updates the target of capacity utilization.
	 */
	public abstract void updateCapacityUtilizationTarget();

}

// ***
