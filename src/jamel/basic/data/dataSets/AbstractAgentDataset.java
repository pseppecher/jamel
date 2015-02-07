package jamel.basic.data.dataSets;

import java.util.HashMap;

/**
 * An abstract dataset implementing the {@link AgentDataset} interface.
 */
@SuppressWarnings("serial")
public abstract class AbstractAgentDataset extends HashMap<String,Double> implements AgentDataset {
		
	/** The name of the agent. */
	final private String name;
	
	/**
	 * Creates a new BasicAgentDataset.
	 * @param name the name of the agent.
	 */
	public AbstractAgentDataset(String name) {
		super();
		this.name = name;
	}

	@Override
	public Double get(String key) {
		return super.get(key);
	}
	
	@Override
	public String getName() {
		return name;
	}

}

// ***
