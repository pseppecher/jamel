package jamel.models.m18.r08.data;

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
	private Integer lastRecordPeriod = null;

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
		final Double result;
		if (lastRecordPeriod == null) {
			result = null;
		} else {
			final int timeIndex = lastRecordPeriod - t;
			if (timeIndex < 0 || timeIndex >= maxSize) {
				throw new IllegalArgumentException("Bad lag: " + timeIndex);
			}
			if (timeIndex >= this.data.size()) {
				result = null;
			} else {
				final PeriodDataset periodDataset = this.data.get(timeIndex);
				if (periodDataset.getPeriod() != t) {
					Jamel.println("***");
					Jamel.println("t", t);
					Jamel.println("periodDataset.getPeriod()", periodDataset.getPeriod());
					throw new RuntimeException("Inconsistency");
				}
				result = periodDataset.get(index);
			}
		}
		return result;
	}

	@Override
	public int getDataIndex(String key) {
		return this.keys.indexOf(key);
	}

	@Override
	public void put(PeriodDataset periodDataset) {
		if (this.lastRecordPeriod == null) {
			this.lastRecordPeriod = this.simulation.getPeriod();
		} else {
			this.lastRecordPeriod++;
			if (this.simulation.getPeriod() != this.lastRecordPeriod) {
				Jamel.println(this.simulation.getPeriod(), this.lastRecordPeriod);
				throw new RuntimeException("Inconsistency");
			}
		}
		if (periodDataset.getPeriod() != this.simulation.getPeriod()) {
			throw new RuntimeException("Inconsistency");
		}
		if (!this.data.isEmpty() && this.data.getFirst().getPeriod() != this.lastRecordPeriod - 1) {
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
