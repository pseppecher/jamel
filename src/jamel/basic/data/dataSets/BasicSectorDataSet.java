package jamel.basic.data.dataSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYDataItem;

import jamel.Simulator;

/**
 * A basic implementation of the SectorDataSet interface.
 * Last modification: 23-11-2014.
 */
public class BasicSectorDataSet implements SectorDataset {

	/**
	 * Returns the maximum value of the given collection.
	 * @param values the collection whose maximum value is to be determined.
	 * @return the maximum value of the given collection.
	 * @since 23-11-2014.
	 */
	private static Double getMax(Double[] values) {
		Double max = null;
		for(Double value: values) {
			if (value!=null && !Double.isNaN(value)) {
				if (max==null || max<value) {
					max = value;
				}
			}
		}
		return max;
	}

	/**
	 * Returns the mean of the specified values.
	 * @param values the values.
	 * @return the mean.
	 */
	private static Double getMean(Double[] values) {
		final Double result;
		double sum = 0;
		int count = 0;
		for(Double value: values) {
			if (value!=null && !Double.isNaN(value)) {
				sum+=value;
				count++;
			}
		}
		if (count!=0) {
			result = sum/count;
		}
		else {
			result=null;
		}
		return result;
	}


	/**
	 * Returns the minimum value of the given collection.
	 * @param values the collection whose minimum value is to be determined.
	 * @return the minimum value of the given collection.
	 * @since 23-11-2014.
	 */
	private static Double getMin(Double[] values) {
		Double min = null;
		for(Double value: values) {
			if (value!=null && !Double.isNaN(value)) {
				if (min==null || min>value) {
					min = value;
				}
			}
		}
		return min;
	}

	/** A map that associated each agent with its index. */
	private final HashMap<String,Integer> agents = new HashMap<String,Integer>();

	/** The fields of the data set. */
	private HashMap<String, Double[]> fields;

	/** The index. */
	private int index = 0;

	/** The size of each field (the number of agents). */
	private final int size;

	/**
	 * Creates a new data set.
	 * @param size the number of agents to record.
	 */
	public BasicSectorDataSet(int size) {
		this.size = size;
		this.fields = new HashMap<String,Double[]>();
	}

	/**
	 * Returns the value of the specified field for the specified agent.
	 * @param name the name of the agent.
	 * @param key the key of the field.
	 * @return the value.
	 */
	private Double getValue(String name, String key) {
		final Double result;
		final Double[] values = this.fields.get(key);
		final Integer agentID = agents.get(name);
		if (values != null && agentID != null) {
			result = values[agents.get(name)];
		}
		else {
			result = null;
		}
		return result;
	}

	@Override
	public Double get(String key) {
		Double result = null;
		final String[] keys = key.split("\\.",2);

		// Returns the sum of each individual data.
		if (keys[0].equals("sum")) {
			result = 0d;
			if (size!=0) {
				final Double[] values = this.fields.get(keys[1]);
				if (values!=null) {
					for(final double value:values) {
						if (!Double.isNaN(value)) {
							result+=value;
						}
					}
				}
				else {
					result = null;
				}
			}
		}

		// Returns the max, min, or mean value.
		else if (keys[0].equals("max")||keys[0].equals("min")||keys[0].equals("mean")) {
			if (size!=0) {
				final Double[] values = this.fields.get(keys[1]);
				if (values==null) {
					result = null;
				}
				else if (size==1) {
					result=values[0];
				}
				else {
					// modified: 23-11-2014 
					if (keys[0].equals("max")) {
						result = getMax(values);
					}
					else if (keys[0].equals("min")) {
						result = getMin(values);
					}
					else if (keys[0].equals("mean")) {
						result = getMean(values);
					}
				}
			}
		}


		// Returns the value of one single agent.
		else if (keys[0].equals("agent")) {
			final String[] keys2 = keys[1].split("\\.",2);
			if (agents.containsKey(keys2[0])) {
				final Double[] values = this.fields.get(keys2[1]);
				if (values==null) {
					result = null;
				}
				else {
					result=values[agents.get(keys2[0])];
				}
			}
		}

		// Unexpected command.
		else {
			final String error = "Unexpected command: "+keys[0];
			Simulator.showErrorDialog(error);
			throw new RuntimeException(error);
		}

		return result;
	}

	@Override
	public List<XYDataItem> getScatter(String method, String xKey, String yKey) {
		final List<XYDataItem> result;

		if ("all".equals(method)) {
			final Double[] yValues = this.fields.get(yKey);
			final Double[] xValues = this.fields.get(xKey);
			if (xValues!=null && yValues!=null) {
				result = new ArrayList<XYDataItem>(xValues.length);
				if (xValues.length!=yValues.length) {
					throw new RuntimeException("xValue[] and yValue[] must have the same lenght.");
				}
				for (int index = 0; index<xValues.length;index++) {
					final XYDataItem item = new XYDataItem(xValues[index], yValues[index]);
					result.add(item);
				}
			}
			else {
				result=null;
			}
		}

		else if ("agent".equals(method)) {
			final String[] wordX = xKey.split("\\.",2); 
			final String[] wordY = yKey.split("\\.",2);
			if (!wordX[0].equals(wordY[0])) {
				throw new RuntimeException("Data Inconstistency: "+xKey+" is not consistent with "+yKey);
			}
			if (agents.containsKey(wordX[0])) {
				result = new ArrayList<XYDataItem>(1);
				final XYDataItem item = new XYDataItem(getValue(wordX[0],wordX[1]), getValue(wordX[0],wordY[1]));
				result.add(item);
			}

			else {
				result=null;
			}
		}

		else {
			result=null;
			throw new IllegalArgumentException("Unknown method: "+method);
		}

		return result;
	}

	@Override
	public void put(AgentDataset agentDataset) {
		final String agentName = agentDataset.getName();
		if (this.agents.containsKey(agentName)) {
			final String message = "Data for this agent are already stored: "+agentName;
			Simulator.showErrorDialog(message);
			throw new RuntimeException(message);
		}
		this.agents.put(agentName, index);
		if (this.index == 0) {
			for (String key:agentDataset.keySet()) {
				final Double[] array = new Double[this.size];
				array[0]=agentDataset.get(key);
				this.fields.put(key, array);
			}
		}
		else {
			for (String key:agentDataset.keySet()) {
				this.fields.get(key)[index]=agentDataset.get(key);
			}
		}
		index++;
	}

}

// ***
