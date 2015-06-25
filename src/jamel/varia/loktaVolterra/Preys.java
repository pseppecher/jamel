package jamel.varia.loktaVolterra;

import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.sector.SectorDataset;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;

import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Prey sector.
 */
public class Preys implements Sector {

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
	private final AgentSet<Prey> agents;

	/** The circuit. */
	private final Circuit circuit;

	/** The grass. */
	private LandSector landSector = null;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final JamelParameters params = new BasicParameters();

	/** The random. */
	private final Random random;

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	public Preys(String name, Circuit circuit) throws InitializationException {
		this.name=name;
		this.circuit=circuit;
		this.random=circuit.getRandom();
		this.agents=new BasicAgentSet<Prey>(random);
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Eats a prey at the given location.
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param eatVolume the volume of meat to eat.
	 * @return the energy.
	 */
	public double eat(double x, double y, double eatVolume) {
		double result=0;
		for(Prey prey:agents.getList()) {
			final double pX = prey.getX();
			final double pY = prey.getY();
			boolean xProx = false;
			boolean yProx = false;
			if (Math.abs(pX-x)<1 || Math.abs(pX-(x+params.get(landWidth)))<1) {
				xProx = true;
			}
			if (Math.abs(pY-y)<1 || Math.abs(pY-(y+params.get(landHeight)))<1) {
				yProx = true;
			}
			if (xProx && yProx) {
				result = prey.eat(eatVolume);
				break;
			}
		}
		return result;
	}

	/**
	 * Eats the grass.
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param eatVolume the volume of grass to eat.
	 * @return the energy.
	 */
	public double eatGrass(double x, double y, double eatVolume) {
		return this.landSector.eat(x,y,eatVolume);
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
					final List<Prey> list = agents.getList();
					for (Prey prey:list) {
						prey.move();
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
		
		// Looking for the land sector.
		final Element landSectorElement = (Element) refElement.getElementsByTagName("landSector").item(0);
		if (landSectorElement==null) {
			throw new InitializationException("Element not found: landSector");
		}
		final String grass = landSectorElement.getAttribute("value");
		if (grass=="") {
			throw new InitializationException("Attribute not found: value");
		}
		this.landSector = (LandSector) circuit.getSector(grass);
		this.params.put(landHeight,this.landSector.getLandHeight().floatValue());
		this.params.put(landWidth,this.landSector.getLandWidth().floatValue());
		
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
		for (int i=0;i<10;i++) {
			Prey prey = new Prey(this, this.random);
			this.agents.put(prey);
		}
	}

	/**
	 * Adds the given prey to the sector.
	 * @param prey the prey to be added.
	 */
	public void newPrey(Prey prey) {
		this.agents.put(prey);
	}

	/**
	 * Removes the given from the sector.
	 * @param prey the prey to be removed.
	 */
	public void remove(Prey prey) {
		this.agents.remove(prey);
	}
}

// ***
