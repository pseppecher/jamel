package jamel.jamel.firms.managers;

import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;

/**
 * An abstract manager.
 */
public abstract class AbstractManager {

	/**
	 * The value of the current period.
	 */
	private Integer currentPeriod = null;

	/**
	 * The name of this manager.
	 */
	private final String name;

	/**
	 * The dataset of this manager.
	 */
	protected AgentDataset dataset = null;

	/**
	 * The timer.
	 */
	protected final Timer timer;

	/**
	 * Creates a new manager.
	 * 
	 * @param name
	 *            the name of the manger.
	 * @param timer
	 *            the timer.
	 */
	public AbstractManager(String name, Timer timer) {
		this.name = name;
		this.timer = timer;
	}

	/**
	 * Detects a possible inconsistency.
	 * <p>
	 * If an inconsistency is detected, an {@link Exception} is thrown.
	 */
	protected void checkConsistency() {
		this.timer.checkConsistency(this.currentPeriod);
	}

	/**
	 * Closes the manager.
	 * <p>
	 * Must be called at the end of the period.
	 */
	public abstract void close();

	/**
	 * Returns the dataset of this manager.
	 * 
	 * @return an {@link AgentDataset}.
	 */
	public AgentDataset getData() {
		return this.dataset;
	}

	/**
	 * Opens the manager.
	 * <p>
	 * Must be called at the beginning of the period.
	 */
	public void open() {
		if (this.currentPeriod == null) {
			this.currentPeriod = timer.getPeriod().intValue();
		} else {
			this.currentPeriod++;
			this.checkConsistency();
		}
		this.dataset = new BasicAgentDataset(this.name);
	}

}

// ***
