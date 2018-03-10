package jamel.models.m18.r01.util;

import java.util.HashMap;

import jamel.data.AgentDataset;

/**
 * Represents a set of moving averages.
 * 
 * 2017-11-09
 * 
 * TODO: si cet objet est réellement utile, le déplacer dans le package
 * models.util.
 */
public class MovingAverages {

	/** The data of the agent. */
	final private AgentDataset dataset;

	/**
	 * The list of time spans for each moving average.
	 */
	final private HashMap<Integer, Integer> timeSpans = new HashMap<>();

	/**
	 * The list of values of the moving averages.
	 */
	final private HashMap<Integer, Double> values = new HashMap<>();

	/**
	 * Creates a new set of moving averages.
	 * 
	 * @param dataset
	 *            the {@code AgentDataset}.
	 */
	public MovingAverages(AgentDataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * Updates the moving averages.
	 */
	public void update() {
		for (Integer dataIndex : this.values.keySet()) {
			this.values.put(dataIndex, this.dataset.average(dataIndex, timeSpans.get(dataIndex)));
		}
	}

	/**
	 * Adds a new average to this set.
	 * 
	 * @param key
	 *            the key of the data.
	 * @param timeSpan
	 *            the time span of the new average.
	 */
	public void newAverage(String key, int timeSpan) {
		final int dataIndex = this.dataset.getDataIndex(key);
		this.values.put(dataIndex, null);
		this.timeSpans.put(dataIndex, timeSpan);
	}

}
