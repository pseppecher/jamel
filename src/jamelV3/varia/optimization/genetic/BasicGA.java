package jamelV3.varia.optimization.genetic;

import jamelV3.basic.Circuit;
import jamelV3.basic.sector.AbstractPhase;
import jamelV3.basic.sector.AgentSet;
import jamelV3.basic.sector.BasicAgentSet;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.BasicParameters;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.JamelParameters;
import jamelV3.varia.optimization.artificialLandscapes.Landscape;

import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A basic genetic algorithm.
 */
public class BasicGA implements Sector {

	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The circuit. */
	private final Circuit circuit;

	/** The landscape. */
	private Landscape landscape = null;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final JamelParameters params = new BasicParameters();

	/** The agents. */
	private final AgentSet<Individual> population;

	/** The random. */
	private final Random random;

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicGA(String name, Circuit circuit) throws InitializationException {
		this.name=name;
		this.circuit=circuit;
		this.random=circuit.getRandom();
		this.population=new BasicAgentSet<Individual>(random);
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public SectorDataset getDataset() {
		return this.population.collectData();
	}

	/**
	 * Returns the fitness of the specified solution.
	 * @param x  the x coordinate of the solution.
	 * @param y  the y coordinate of the solution.
	 * @return the fitness of the specified solution.
	 */
	public double getFitness(double x, double y) {
		return this.landscape.getZValue(x, y);
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the double value of the specified parameter.
	 * @param key the key of the parameter to be returned.
	 * @return the double value of the specified parameter.
	 */
	public Float getParam(String key) {
		return this.params.get(key);
	}

	@Override
	public Phase getPhase(final String name) {
		Phase result = null;
		if (name.equals("tournament")) {
			result = new AbstractPhase(name, this){
				@Override
				public void run() {
					final List<Individual> list = population.getList();
					for (Individual indiv:list) {
						indiv.tournament();
					}
				}				
			};			
		}
		else if (name.equals("adaptation")) {
			result = new AbstractPhase(name, this){
				@Override
				public void run() {
					final List<Individual> list = population.getList();
					for (Individual indiv:list) {
						indiv.adapt();
					}
				}				
			};			
		}
		return result;
	}

	/**
	 * Returns a list of agents selected at random.
	 * @param size  the size of the list to be returned.
	 * @return a list of agents selected at random.
	 */
	public List<Individual> getTournament(int size) {
		return this.population.getSimpleRandomSample(size);
	}

	@Override
	public void init(Element element) throws InitializationException {
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}
		
		// Looking for dependencies.
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement==null) {
			throw new InitializationException("Element not found: "+DEPENDENCIES);
		}
		
		// Looking for the landscape sector.
		final Element landscapeElement = (Element) refElement.getElementsByTagName("landscape").item(0);
		if (landscapeElement==null) {
			throw new InitializationException("Element not found: landscape");
		}
		final String lanscapeKey = landscapeElement.getAttribute("value");
		if (lanscapeKey=="") {
			throw new InitializationException("Attribute not found: value");
		}
		this.landscape = (Landscape) circuit.getSector(lanscapeKey);
		
		// Looking for the settings. 
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
		
		final String type = element.getAttribute("agentType");
		if ("".equals(type)) {
			throw new InitializationException("Missing attribute (agentType).");			
		}
		for (int i=0;i<100;i++) {
			Individual indiv;
			try {
				indiv = (Individual) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(BasicGA.class,Random.class).newInstance(this,this.random);
			} catch (Exception e) {
				throw new InitializationException("Something went wrong while creating new individuals.",e);
			}
			this.population.put(indiv);
		}
	}

}

// ***
