package jamel.util;

import java.util.Random;


/**
 * Defines the interface for the macro-economic circuit.
 */
public abstract class Circuit {

	/** The random. */
	private static Random random;
	
	/** The timer. */
	private static Timer timer;
	
	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public static Period getCurrentPeriod() {
		return timer.getPeriod();
	}
	
	/**
	 * Returns the random.
	 * @return the random.
	 */
	public static Random getRandom() {
		return random;
	}

	/**
	 * Creates a new circuit.
	 * @param timer the timer.
	 * @param random the random.
	 */
	public Circuit(Timer timer, Random random) {
		Circuit.timer = timer;
		Circuit.random = random;
	}

	/**
	 * Changes the current period to the next.
	 */
	protected void nextPeriod() {
		timer.next();
	}

	/**
	 * Returns an object.
	 * @param message a string.
	 * @param args an array of objects.
	 * @return an object.
	 */
	public abstract Object forward(String message,Object ... args);
	
	/**
	 * Returns the parameter to which the specified key is mapped, or null if the circuit contains no mapping for the key.
	 * @param keys strings that will be concatenated using dots to form the key whose associated parameter is to be returned.
	 * @return the parameter to which the specified key is mapped, or null if the circuit contains no mapping for the key.
	 */
	public abstract String getParameter(String... keys);

	/**
	 * Splits and returns the parameter to which the specified key is mapped.
	 * @param keys strings that will be concatenated using dots to form the key whose associated parameter is to be returned.
	 * @return the array of strings computed by splitting the parameter around matches of the given regular expression
	 */
	public abstract String[] getParameterArray(String... keys);

	/**
	 * Returns an array containing all the keys starting with the specified prefix.
	 * @param prefix the prefix.
	 * @return an array of strings.
	 */
	public abstract String[] getStartingWith(String prefix);

	/**
	 * Returns <code>true</code> if the circuit is paused, <code>false</code> otherwise. 
	 * @return a boolean.
	 */
	public abstract boolean isPaused();

	/**
	 * Runs the simulation.
	 */
	public abstract void run();

}

// ***
