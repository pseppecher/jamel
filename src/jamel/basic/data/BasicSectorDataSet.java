package jamel.basic.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import jamel.Simulator;

/**
 * A basic implementation of the SectorDataSet interface.
 */
public class BasicSectorDataSet implements SectorDataset {

	/** The fields of the data set. */
	private HashMap<String, Double[]> fields;

	/** The index. */
	private int index = 0;

	/** The size of each field (the number of agents). */
	private final int size;

	/** A map that associated each agent with its index. */
	private final HashMap<String,Integer> agents = new HashMap<String,Integer>();

	/**
	 * Creates a new data set.
	 * @param size the number of agents to record.
	 */
	public BasicSectorDataSet(int size) {
		this.size = size;
		this.fields = new HashMap<String,Double[]>();
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
						result = Collections.max(Arrays.asList(values));
					}
					else if (keys[0].equals("min")) {
						result = Collections.min(Arrays.asList(values));
					}
					else if (keys[0].equals("mean")) {
						result=0d;
						for(final double value:values) {
							result+=value;
						}			
						result = result/size;
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
	public void put(AgentDataset agentDataset) {
		// TODO the index must be linked with the name of the agent, if we want to extract the data of an agent known by his name
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
