package jamel.util;

/**
 * Represents a phase of the circuit.
 */
public interface Phase {

	/**
	 * Returns the name of the phase.
	 * 
	 * @return a string.
	 */
	String getName();

	/**
	 * Returns the run time of this phase, ie, the cumulative time spent in this
	 * phase since the start of the simulation.
	 * 
	 * @return the run time of this phase.
	 */
	long getRuntime();

	/**
	 * Returns the sector linked to this phase.
	 * 
	 * @return a sector.
	 */
	Sector getSector();

	/**
	 * Runs the phase.
	 */
	void run();

}
