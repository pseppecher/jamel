package jamel.models.m18.r01.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.util.Agent;

/**
 * An implementation of {@code Dataset} based on an array of {@code Double}.
 */
public class BasicPeriodDataset implements PeriodDataset {

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
	 * Creates a period dataset for the specified agent.
	 * 
	 * @param agent
	 *            the agent.
	 */
	public BasicPeriodDataset(final Agent agent) {
		this.agent = agent;
		this.period = this.agent.getSimulation().getPeriod();
		try {
			final Method getActionMethod = this.agent.getClass().getMethod("getDataKeys");
			this.keys = (DataKeys) getActionMethod.invoke(null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		this.data = new Double[keys.size()];
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
	public int getPeriod() {
		return this.period;
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
