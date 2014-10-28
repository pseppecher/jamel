package jamel.basic.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.xy.XYDataItem;

import jamel.Simulator;

/**
 * A basic implementation of the SectorDataSet interface.
 */
public class BasicSectorDataSet implements SectorDataset {

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
				if (values==null) {
					final String error = "Field not found: "+keys[1];
					Simulator.showErrorDialog(error);
					throw new RuntimeException(error);
				}
				for(final double value:values) {
					result+=value;
				}			
			}
		}

		// Returns the max, min, or mean value.
		else if (keys[0].equals("max")||keys[0].equals("min")||keys[0].equals("mean")) {
			if (size!=0) {
				final Double[] values = this.fields.get(keys[1]);
				if (values==null) {
					final String error = "Field not found: "+keys[1];
					Simulator.showErrorDialog(error);
					throw new RuntimeException(error);
				}
				if (size==1) {
					result=values[0];
				}
				else {
					if (keys[0].equals("max")) {
						try {
							result = Collections.max(Arrays.asList(values));
						} 
						catch (NullPointerException e) {}
					}
					else if (keys[0].equals("min")) {
						try {
							result = Collections.min(Arrays.asList(values));
						} 
						catch (NullPointerException e) {}
					}
					else if (keys[0].equals("mean")) {
						try {
							result=0d;
							for(final double value:values) {
								result+=value;
							}			
							result = result/size;
						} 
						catch (NullPointerException e) {}
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
					final String error = "Field not found: "+keys2[1];
					Simulator.showErrorDialog(error);
					throw new RuntimeException(error);
				}
				result=values[agents.get(keys2[0])];
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

		if (method.equals("all")) {
			final Double[] xValues = this.fields.get(xKey);
			final Double[] yValues = this.fields.get(yKey);
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

		else {
			final String[] instruction = method.split("\\.",2);
			if (instruction.length==2 && instruction[0].equals("agent")) {
				if (agents.containsKey(instruction[1])) {
					result = new ArrayList<XYDataItem>(1);
					final XYDataItem item = new XYDataItem(getValue(instruction[1],xKey), getValue(instruction[1],yKey));
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
