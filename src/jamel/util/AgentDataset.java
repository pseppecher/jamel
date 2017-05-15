package jamel.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The agent dataset.
 */
public class AgentDataset extends JamelObject {

	/**
	 * The owner agent.
	 */
	final private Agent agent;

	/**
	 * 
	 */
	private Map<String, Double> data = new HashMap<>();

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

	/**
	 * Returns the value of the specified data.
	 * 
	 * @param dataKey
	 *            the key for the data to be returned.
	 * @return the value of the specified data.
	 */
	public Double getData(String dataKey) {
		return this.data.get(dataKey);
	}

	/**
	 * Associates the specified value with the specified key in this dataset.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated.
	 * @param value
	 *            value to be associated with the specified key.
	 */
	public void put(String key, double value) {
		this.data.put(key, value);
	}

	public void clear() {
		this.data.clear();
	}

}
