package jamel.data;

import java.util.LinkedList;

import org.jfree.data.xy.XYDataItem;

import jamel.Jamel;
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
	 * 
	 */
	private boolean open = false;

	/**
	 * The current period.
	 */
	private Integer period = null;

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
		if (!this.open) {
			throw new RuntimeException("Should be closed");
		}
		this.open = false;
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

	@Override
	public double getAverage(int dataKey, int laps) {
		if (laps < 0 || laps > maxSize) {
			throw new RuntimeException("Bad value: " + laps);
		}
		final double result;
		double sum = 0;
		int count = 0;
		for (final Dataset dataset : this.data) {
			final Double value = dataset.get(dataKey);
			if (value != null) {
				sum += value;
				count++;
			}
			if (count == laps) {
				break;
			}
		}
		if (count > 0) {
			result = sum / count;
		} else {
			result = 0;
		}
		return result;
	}

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param index
	 *            the index for the data to be returned.
	 * @param t
	 *            the period of the data to be returned
	 * @return the value of the specified data.
	 */
	@Override
	public Double getData(final int index, final int t) {
		final int timeIndex = this.getPeriod() - t;
		if (timeIndex < 0 || timeIndex >= maxSize) {
			throw new IllegalArgumentException("Bad lag: " + timeIndex);
		}
		final Double result;
		if (timeIndex >= this.data.size()) {
			result = null;
		} else {
			result = this.data.get(timeIndex).get(index);
		}
		return result;
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
		if (this.open) {
			throw new RuntimeException("Should be closed");
		}
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

	@Override
	public double getSum(int dataKey, int laps) {
		if (laps < 0 || laps > maxSize) {
			throw new RuntimeException("Bad value: " + laps);
		}
		double sum = 0;
		int count = 0;
		for (final Dataset dataset : this.data) {
			final Double value = dataset.get(dataKey);
			if (value != null) {
				sum += value;
				count++;
			}
			if (count == laps) {
				break;
			}
		}
		return sum;
	}

	@Override
	public XYDataItem getXYDataItem(String x, String y, int t) {
		final XYDataItem result;
		final int index = this.getPeriod() - t;
		if (index < 0 || index >= maxSize) {
			throw new IllegalArgumentException("Bad lag: " + index);
		}
		if (index >= this.data.size()) {
			result = null;
		} else {
			final Double xValue = this.data.get(index).get(x);
			if (xValue != null) {
				final Double yValue = this.data.get(index).get(y);
				result = new XYDataItem(xValue, yValue);
			} else {
				result = null;
			}
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
		if (this.open) {
			throw new RuntimeException("Already open");
		}
		this.open = true;
		if (this.period == null) {
			this.period = this.getPeriod();
		} else {
			this.period++;
			if ( this.getPeriod() != this.period) {
				Jamel.println(this.getPeriod(), this.period);
				throw new RuntimeException("Inconsistency");
			}
		}
		this.data.addFirst(new BasicDataset(agent, this.getPeriod(), keys));
		if (this.data.size() > maxSize) {
			this.data.removeLast();
		}
	}

	@Override
	public void put(int index, Number value) {
		if (!this.open) {
			throw new RuntimeException("Should be open");
			// Une fois fermé, plus aucune donnée ne doit être ajoutée.
		}
		this.data.get(0).put(index, value);
	}

}
