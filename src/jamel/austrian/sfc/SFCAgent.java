package jamel.austrian.sfc;

import jamel.austrian.roles.AccountHolder;
import jamel.basic.Circuit;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;

/**
 * Represents the sector of the households.
 */
public abstract class SFCAgent extends SFCObject implements AccountHolder{
	
	
	/** The sector to which the agent belongs. */
	protected SFCSector sector;
		
	/** The data set. */
	protected AgentDataset data;
	
	
	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public SFCAgent(String aName, Circuit aCircuit, SFCSector aSector) {
		super(aName, aCircuit);
		sector = aSector;
		data = new BasicAgentDataset(aName);
	}
	
	
	/**
	 * Returns the data set of the agent.
	 */
	@Override
	public AgentDataset getData() {
		return this.data;
	}
	
	
	/**
	 * Returns a single data item as specified by the key.
	 *  @param key the key.
	 */
	public double getData(String key) {
		if (!data.containsKey(key)) return Double.NaN;
		return this.data.get(key);
	}
	
}
