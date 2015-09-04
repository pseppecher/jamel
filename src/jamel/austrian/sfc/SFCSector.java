package jamel.austrian.sfc;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import jamel.basic.Circuit;
import jamel.basic.sector.Sector;
import jamel.basic.util.InitializationException;

/**
 * Represents the sector of the households.
 */
public abstract class SFCSector extends SFCObject implements Sector {

	
	/** The parameters of the household sector. */
	protected final Map<String,Float> params = new HashMap<String,Float>();

	/** The type of agents that the sector contains. */
	protected String agentType;

	/** The number of agents contained. */
	protected int countAgents;
	
	
	/**
	 * Creates a new SFCSector.
	 * @param name the name of the sector.
	 */
	public SFCSector(String name, Circuit circuit) {
		super(name, circuit);
	}
	
	
	public void init(Element element) throws InitializationException {
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}

		// Initialization of the agent type:
		final String agentAttribute = element.getAttribute("agent");
		this.agentType =agentAttribute;

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}		
	}


	/**
	 * Returns a parameter of the sector.
	 * @return a parameter.
	 */
	public Float getParam(String key) {
		return this.params.get(key);
	}
	

}

// ***
