package jamelV3.basic.sector;


/**
 * Represents a phase of the circuit.
 */
public interface Phase {

	/**
	 * Returns the name of the phase.
	 * @return a string.
	 */
	String getName();

	/**
	 * Returns the sector linked to this phase.
	 * @return a sector.
	 */
	Sector getSector();

	/**
	 * Runs the phase.
	 */
	void run();

}

// ***
