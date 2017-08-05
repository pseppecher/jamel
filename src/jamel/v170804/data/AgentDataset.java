package jamel.v170804.data;

import java.util.LinkedList;

import jamel.util.Agent;
import jamel.util.JamelObject;

/**
 * The agent dataset.
 */
public class AgentDataset extends JamelObject {

	/**
	 * TODO should be a parameter
	 */
	private static final int maxSize = 24;

	/**
	 * The owner agent.
	 */
	final private Agent agent;

	/**
	 * 
	 */
	final private LinkedList<Dataset> data = new LinkedList<>();

	/**
	 * Creates a new dataset for this agent.
	 * 
	 * @param agent
	 *            the agent.
	 */
	public AgentDataset(final Agent agent) {
		super(agent.getSimulation());
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
	public Agent getAgent() {
		return agent;
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
	public Double getData(final String key, final int t) {
		final int index = this.getPeriod()-t;
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
	 * Must be called at the beginning of the period, before adding data.
	 */
	@Override
	public void open() {
		this.data.addFirst(new BasicDataset());
		if (this.data.size() > maxSize) {
			this.data.removeLast().clear();
		}
		super.open();
	}

	/**
	 * Associates the specified value with the specified key in this dataset.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            value to be associated with the specified key.
	 */
	public void put(String key, Number value) {
		this.checkOpen();
		if (this.data.get(0).put(key, value.doubleValue()) != null) {
			throw new RuntimeException("Not null: " + key);
		}
	}

}
