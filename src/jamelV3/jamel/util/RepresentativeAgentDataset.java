package jamelV3.jamel.util;

import jamelV3.basic.agent.AgentDataset;
import jamelV3.basic.sector.SectorDataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	public void exportHeadersTo(File outputFile) throws IOException {
		final FileWriter writer = new FileWriter(outputFile,true);
		for (String key:this.keySet()) {
			writer.write(key+",");
		}	
		writer.write(System.getProperty("line.separator"));
		writer.close();
	}

	@Override
	public void exportTo(File outputFile) throws IOException {
		final FileWriter writer = new FileWriter(outputFile,true);
		for (String key:this.keySet()) {
			writer.write(this.get(key)+",");
		}	
		writer.write(System.getProperty("line.separator"));
		writer.close();
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
		// Not used.
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

	@Override
	public Double put(String key, Double value) {
		// Not used.
		throw new RuntimeException("Not implemented");
	}

}

// ***
