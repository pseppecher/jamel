package jamel.util;

/**
 * The interfaces for the sector components of the macro-economic circuit. 
 */
public interface Sector {

	/**
	 * Executes a phase of the circuit.
	 * @param phase the name of the phase to execute.
	 * @return <code>true</code> if the phase was correctly executed.
	 */
	boolean doPhase(String phase);

	/**
	 * Returns an object.
	 * @param message the message.
	 * @param args an array of objects.
	 * @return an object.
	 */
	Object forward(String message, Object ... args);

	/**
	 * Returns the name of the sector.
	 * @return the name of the sector.
	 */
	String getName();

	/**
	 * Receives notification that the simulation is paused.
	 */
	void pause();

}

// ***
