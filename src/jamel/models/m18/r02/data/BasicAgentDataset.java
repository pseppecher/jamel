package jamel.models.m18.r02.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import jamel.Jamel;
import jamel.data.DataKeys;
import jamel.util.Agent;
import jamel.util.Simulation;

/**
 * A basic implementation of {@code AgentDataset}.
 */
public class BasicAgentDataset implements AgentDataset {

	/**
	 * TODO 24 should be a parameter
	 */
	private static final int maxSize = 25;

	/**
	 * The owner agent.
	 */
	final private Agent agent;

	/**
	 * The data, in chronological order (most recent first).
	 */
	final private LinkedList<PeriodDataset> data = new LinkedList<>();

	/**
	 * The list of the keys of the data.
	 */
	final private DataKeys keys;

	/**
	 * The current period.
	 */
	private Integer period = null;

	/**
	 * The simulation.
	 */
	final private Simulation simulation;

	/**
	 * Creates a new dataset for this agent.
	 * 
	 * @param agent
	 *            the agent.
	 */
	public BasicAgentDataset(final Agent agent) {
		this.simulation = agent.getSimulation();
		this.agent = agent;
		try {
			final Method getActionMethod = this.agent.getClass().getMethod("getDataKeys");
			this.keys = (DataKeys) getActionMethod.invoke(null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the current period.
	 * 
	 * @return the current period.
	 */
	private int getPeriod() {
		return this.simulation.getPeriod();
	}

	@Override
	public double average(int dataKey, int laps) {
		if (laps < 0 || laps > maxSize) {
			throw new RuntimeException("Bad value: " + laps);
		}
		final double result;
		double sum = 0;
		int count = 0;
		for (final PeriodDataset dataset : this.data) {
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

	@Override
	public int getDataIndex(String key) {
		return this.keys.indexOf(key);
	}

	@Override
	public void put(PeriodDataset periodDataset) {
		if (this.period == null) {
			this.period = this.getPeriod();
		} else {
			this.period++;
			if (this.getPeriod() != this.period) {
				Jamel.println(this.getPeriod(), this.period);
				throw new RuntimeException("Inconsistency");
			}
		}
		if (periodDataset.getPeriod() != this.getPeriod()) {
			throw new RuntimeException("Inconsistency");
		}
		if (!this.data.isEmpty() && this.data.getFirst().getPeriod()!=this.period-1) {
			throw new RuntimeException("Inconsistency");
		}
		this.data.addFirst(periodDataset);
		if (this.data.size() > maxSize) {
			this.data.removeLast();
		}
	}

	@Override
	public double sum(int dataKey, int laps) {
		if (laps < 0 || laps > maxSize) {
			throw new RuntimeException("Bad value: " + laps);
		}
		double sum = 0;
		int count = 0;
		for (final PeriodDataset dataset : this.data) {
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

}
