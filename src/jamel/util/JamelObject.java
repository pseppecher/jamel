package jamel.util;

import java.util.Random;

import jamel.Jamel;

/**
 * An abstract class that provides convenience methods.
 */
public abstract class JamelObject {

	/**
	 * A flag that indicates whether this object is open or not.
	 */
	private boolean open = false;

	/**
	 * The parent simulation.
	 */
	private final Simulation simulation;

	/**
	 * The current period.
	 */
	private Integer t = null;

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
	 * Checks if this object is open.
	 * If this object is closed, throws a new RuntimeException.
	 */
	protected void checkOpen() {
		if (!this.open) {
			throw new RuntimeException("Closed.");
		}
	}

	/**
	 * Closes the object.
	 * Must be called at the end of the period.
	 */
	protected void close() {
		this.open = false;
	}

	/**
	 * Returns {@code true} if the given period is the current period,
	 * {@code false} otherwise.
	 * 
	 * @param period
	 *            the period to be checked.
	 * @return Return {@code true} if the given period is the current period,
	 *         {@code false} otherwise.
	 */
	protected boolean isCurrent(Integer period) {
		return (t.equals(period));
	}

	/**
	 * Opens the object.
	 * Must be called at the beginning of the period.
	 */
	protected void open() {
		if (this.open) {
			Jamel.println();
			Jamel.println("***");
			Jamel.println("t",t);
			Jamel.println("this.simulation.getPeriod()",this.simulation.getPeriod());
			Jamel.println("***");
			throw new RuntimeException("Already open.");
		}
		if (t == null) {
			t = this.simulation.getPeriod();
		} else {
			t++;
		}
		if (t != this.getPeriod()) {
			throw new RuntimeException("Time inconsistency.");
		}
		this.open = true;
	}
	
	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	public Integer getPeriod() {
		return t;
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

	/**
	 * Returns <code>true</code> if this object is open, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if this object is open, <code>false</code>
	 *         otherwise.
	 */
	public boolean isOpen() {
		return this.open;
	}

}
