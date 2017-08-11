package jamel.util;

/**
 * Represents a phase of period
 */
public interface Phase {

	/**
	 * Returns the name of this phase.
	 * 
	 * @return the name of this phase.
	 */
	String getName();

	/**
	 * Returns the runtime (in milliseconds) of this phase since the start of
	 * the simulation.
	 * 
	 * For performance purposes.
	 * 
	 * @return the runtime of this phase.
	 */
	long getRuntime();

	/**
	 * Returns the sector.
	 * 
	 * @return the sector.
	 */
	Sector getSector();

	/**
	 * Executes this phase.
	 */
	void run();

}
