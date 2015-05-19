package jamelV3.jamel.firms.managers;

/**
 * The production manager.
 */
public interface ProductionManager {

	/**
	 * Returns the capacity utilization targeted.
	 * @return a float in [0,1].
	 */
	float getTarget();

	/**
	 * Updates the target of capacity utilization.
	 */
	void updateCapacityUtilizationTarget();

}