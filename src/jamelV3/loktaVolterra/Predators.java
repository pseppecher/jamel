package jamelV3.loktaVolterra;

import jamelV3.basic.Circuit;
import jamelV3.basic.sector.AbstractPhase;
import jamelV3.basic.sector.AgentSet;
import jamelV3.basic.sector.BasicAgentSet;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.InitializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Prey sector.
 */
public class Predators implements Sector {
	
	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	@SuppressWarnings("javadoc")
	public static final String birthEnergy = "birthEnergy";

	@SuppressWarnings("javadoc")
	public static final String cost = "cost";

	@SuppressWarnings("javadoc")
	public static final String eatVolume = "eatVolume";

	@SuppressWarnings("javadoc")
	public static final String landHeight = "landHeight";

	@SuppressWarnings("javadoc")
	public static final String landWidth = "landWidth";

	@SuppressWarnings("javadoc")
	public static final String move = "move";

	@SuppressWarnings("javadoc")
	public static final String reproductionThreshold = "reproductionThreshold";

	/** The agents. */
	private final AgentSet<Predator> agents;

	/** The circuit. */
	private final Circuit circuit;

	/** The preys. */
	private Preys preys = null;
	
	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final Map<String,Float> params = new HashMap<String,Float>();

	/** The random. */
	private final Random random;

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	public Predators(String name, Circuit circuit) throws InitializationException {
		this.name=name;
		this.circuit=circuit;
		this.random=circuit.getRandom();
		this.agents=new BasicAgentSet<Predator>(random);
	}
	
	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Eats a prey.
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param eatVolume the volume of grass to eat.
	 * @return the energy.
	 */
	public double eatPrey(double x, double y, double eatVolume) {
		return this.preys.eat(x,y,eatVolume);
	}

	@Override
	public SectorDataset getDataset() {
		return this.agents.collectData();
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
		if (name.equals("move")) {
			result = new AbstractPhase(name, this){
				@Override
				public void run() {
					final float spontaneous = params.get("spontaneous");
					if(spontaneous<1) {
						if(spontaneous>random.nextFloat()) {
							final Predator predator = new Predator(Predators.this,random,1);
							agents.put(predator);							
						}
					}
					else {
						for(int i=0;i<spontaneous;i++) {
							final Predator predator = new Predator(Predators.this,random,1);
							agents.put(predator);								
						}
					}
					final List<Predator> list = agents.getList();
					for (Predator predator:list) {
						predator.move();
					}
				}				
			};			
		}
		return result;
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
		final Element preysSectorElement = (Element) refElement.getElementsByTagName("preySector").item(0);
		if (preysSectorElement==null) {
			throw new InitializationException("Dependencies: Element not found: preySector");
		}
		final String preysKey = preysSectorElement.getAttribute("value");
		if (preysKey=="") {
			throw new InitializationException("Attribute not found: value");
		}
		this.preys = (Preys) circuit.getSector(preysKey);

		// Looking for the land sector.
		final Element landSectorElement = (Element) refElement.getElementsByTagName("landSector").item(0);
		if (landSectorElement==null) {
			throw new InitializationException("Dependencies: Element not found: landSector");
		}
		final String grass = landSectorElement.getAttribute("value");
		if (grass=="") {
			throw new InitializationException("Attribute not found: value");
		}
		final LandSector landSector = (LandSector) circuit.getSector(grass);
		this.params.put(landHeight,landSector.getLandHeight().floatValue());
		this.params.put(landWidth,landSector.getLandWidth().floatValue());		
		
		// Looking for settings.
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
		for (int i=0;i<100;i++) {
			final Predator predator = new Predator(this, this.random,10);
			this.agents.put(predator);
		}
	}

	/**
	 * Adds the given predator to the sector.
	 * @param predator the predator to be added.
	 */
	public void newPredator(Predator predator) {
		this.agents.put(predator);
	}

	/**
	 * Removes the given predator from the sector.
	 * @param predator the predator to be removed.
	 */
	public void remove(Predator predator) {
		this.agents.remove(predator);
	}
}

// ***
