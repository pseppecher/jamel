package jamel.basic.data.dataSets;

import java.util.HashMap;

/**
 * A basic dataset implementing the AgentDataset interface.
 */
public abstract class BasicAgentDataset extends HashMap<String,Double> implements AgentDataset {
		
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	
	/** The name of the agent. */
	final private String name;
	
	/**
	 * Creates a new BasicAgentDataset.
	 * @param name the name of the agent.
	 */
	public BasicAgentDataset(String name) {
		super();
		this.name = name;
	}

	@Override
	public Double get(String key) {
		return super.get(key);
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Updates the data.
	 */
	public abstract void update();

}

// ***
