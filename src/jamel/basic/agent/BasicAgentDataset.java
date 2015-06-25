package jamel.basic.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

/**
 * A basic dataset implementing the {@link AgentDataset} interface.
 */
public class BasicAgentDataset extends TreeMap<String,Double> implements AgentDataset {
		
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
	
	@Override
	public String getName() {
		return name;
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
	public void exportHeadersTo(File outputFile) throws IOException {
		final FileWriter writer = new FileWriter(outputFile,true);
		for (String key:this.keySet()) {
			writer.write(key+",");
		}	
		writer.write(System.getProperty("line.separator"));
		writer.close();
	}

}

// ***
