package jamel.basic.data.dataSets;

import java.util.List;
import java.util.Set;

import org.jfree.data.xy.XYDataItem;

/**
 * A dataset that can be used when one single agent represents one sector. 
 */
public class RepresentativeAgentDataset implements AgentDataset, SectorDataset{
	
	/** The AgentDataset. */
	final private AgentDataset data;
	
	/**
	 * Creates a RepresentativeAgentDataset from an AgentDataset.
	 * @param data the AgentDataset of the representative agent.
	 */
	public RepresentativeAgentDataset(AgentDataset data) {
		this.data=data;
	}
	
	@Override
	public Double get(String key) {
		return this.data.get(key);
	}

	@Override
	public String getName() {
		return this.data.getName();
	}

	@Override
	public List<XYDataItem> getScatter(String method, String xKey, String yKey) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Set<String> keySet() {
		return this.data.keySet();
	}

	@Override
	public void put(AgentDataset agentData) {
		// Not used.
		throw new RuntimeException("Not implemented");
	}

}

// ***
