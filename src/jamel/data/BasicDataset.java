package jamel.data;

import jamel.Jamel;
import jamel.util.Agent;

/**
 * An implementation of {@code Dataset} based on an array of {@code Double}.
 */
public class BasicDataset implements Dataset {

	/**
	 * The agent.
	 */
	final private Agent agent;

	/**
	 * The data.
	 */
	final private Double[] data;

	/**
	 * The keys.
	 */
	final private DataKeys keys;

	/**
	 * The period.
	 */
	final private int period;

	/**
	 * Creates a dataset of the specified agent.
	 * 
	 * @param agent
	 *            the agent.
	 * @param period
	 *            the period
	 * @param keys
	 *            the keys of the dataset to create.
	 */
	public BasicDataset(final Agent agent, final int period, final DataKeys keys) {
		this.agent = agent;
		this.period = period;
		this.keys = keys;
		this.data = new Double[keys.size()];
	}

	@Override
	public void clear() {
		for (int i = 0; i < data.length; i++) {
			this.data[i] = null;
		}
	}

	@Override
	public Double get(int index) {
		return this.data[index];
	}

	@Override
	public Double get(String key) {
		return this.data[keys.indexOf(key)];
	}

	@Override
	public void put(final int index, final Number value) {
		if (index < 0 || index >= this.data.length) {
			throw new IllegalArgumentException("Index out of range: " + index);
		}
		if (this.data[index] != null) {
			Jamel.println(this.agent.getName(), this.period, index, keys.getKey(index), this.data[index], value);
			throw new RuntimeException("Already in the database: " + keys.getKey(index));
		}
		this.data[index] = (value != null && Double.isFinite(value.doubleValue())) ? value.doubleValue() : null;
	}

}
