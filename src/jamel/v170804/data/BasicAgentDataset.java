package jamel.v170804.data;

import java.util.LinkedList;

import jamel.util.Agent;
import jamel.util.JamelObject;

/**
 * A basic implementation of {@code AgentDataset}.
 */
public class BasicAgentDataset extends JamelObject implements AgentDataset {

	/**
	 * TODO 24 should be a parameter
	 */
	private static final int maxSize = 24;

	/**
	 * The owner agent.
	 */
	final private Agent agent;

	/**
	 * The data, in chronological order (most recent first).
	 */
	final private LinkedList<Dataset> data = new LinkedList<>();

	/**
	 * The list of the keys of the data.
	 */
	final private DataKeys keys;

	/**
	 * Creates a new dataset for this agent.
	 * 
	 * @param agent
	 *            the agent.
	 * @param keys
	 *            the list of the keys of the data.
	 */
	public BasicAgentDataset(final Agent agent, final DataKeys keys) {
		super(agent.getSimulation());
		this.keys = keys;
		this.agent = agent;
	}

	@Override
	public void close() {
		super.close();
	}

	/**
	 * Returns the agent.
	 * 
	 * @return the agent.
	 */
	@Override
	public Agent getAgent() {
		return this.agent;
	}

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param key
	 *            the key for the data to be returned.
	 * @param t
	 *            the period of the data to be returned
	 * @return the value of the specified data.
	 */
	@Override
	public Double getData(final String key, final int t) {
		final int index = this.getPeriod() - t;
		if (index < 0 || index >= maxSize) {
			throw new IllegalArgumentException("Bad lag: " + index);
		}
		final Double result;
		if (index >= this.data.size()) {
			result = null;
		} else {
			result = this.data.get(index).get(key);
		}
		return result;
	}

	/**
	 * Opens the dataset.
	 * 
	 * Must be called at the beginning of the period, before adding data.
	 */
	@Override
	public void open() {
		this.data.addFirst(new BasicDataset(keys));
		if (this.data.size() > maxSize) {
			this.data.removeLast();
		}
		super.open();
	}

	@Override
	public void put(int index, Number value) {
		this.data.get(0).put(index, value);
	}

}
