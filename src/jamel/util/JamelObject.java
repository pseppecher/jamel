package jamel.util;

import java.util.Random;

/**
 * An abstract class that provides convenience methods.
 */
public abstract class JamelObject {

	/**
	 * The parent simulation.
	 */
	private final Simulation simulation;

	/**
	 * Creates a JamelObject.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public JamelObject(Simulation simulation) {
		this.simulation = simulation;
	}

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	public int getPeriod() {
		return this.simulation.getPeriod();
	}

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	public Random getRandom() {
		return this.simulation.getRandom();
	}

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	public Simulation getSimulation() {
		return this.simulation;
	}

}
