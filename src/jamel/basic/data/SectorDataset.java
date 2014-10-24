package jamel.basic.data;


/**
 * The sector data.
 */
public interface SectorDataset {

	/**
	 * Returns the value for the specified key.
	 * @param key the key.
	 * @return the value.
	 * TODO: Javadoc: améliorer ce commentaire
	 */
	Double get(String key);

	/**
	 * Stores the specified agent data into this sector dataset.  
	 * @param data the data to be stored.
	 */
	void put(AgentDataset data);

}

// ***
