package jamel.basic.data.dataSets;

import java.util.Set;

/**
 * An interface for the data of one individual agent.
 */
public interface AgentDataset {

	/**
	 * Returns the value to which the specified key is mapped, 
	 * or <code>null</code> if this dataset contains no mapping for the key. 
	 * @param key the key whose associated value is to be returned.
	 * @return the value to which the specified key is mapped, 
	 * or <code>null</code> if this dataset contains no mapping for the key.
	 */
	public Double get(String key);
	
	/**
	 * Returns the name of the agent.
	 * @return the name of the agent.
	 */
	public String getName();

	/**
	 * Returns a <code>Set</code> view of the keys contained in this dataset.
	 * @return a <code>Set</code> view of the keys contained in this dataset.
	 */
	public Set<String> keySet();
	
}

// ***
