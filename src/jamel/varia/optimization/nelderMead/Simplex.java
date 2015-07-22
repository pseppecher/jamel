package jamel.varia.optimization.nelderMead;

import jamel.basic.Circuit;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;
import jamel.varia.optimization.artificialLandscapes.Landscape;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Nelder-Mead simplex sector.
 */
public class Simplex implements Sector {

	/** The <code>dependencies</code> element. */
	private static final String DEPENDENCIES = "dependencies";
	
	@SuppressWarnings("javadoc")
	public static final String birthEnergy = "birthEnergy";

	@SuppressWarnings("javadoc")
	public static final String cost = "cost";

	@SuppressWarnings("javadoc")
	public static final String eatVolume = "eatVolume";

	/** 
	 * The fitness comparator.<p>
	 * To compare vertices according to their fitness.
	 */
	public static final Comparator<Vertex>fitnessComparator = new Comparator<Vertex>() {
		@Override
		public int compare(Vertex pod1, Vertex pod2) {
			return (new Double(pod2.getFitness()).compareTo(pod1.getFitness()));
		}
	};

	@SuppressWarnings("javadoc")
	public static final String landHeight = "landHeight";

	@SuppressWarnings("javadoc")
	public static final String landWidth = "landWidth";

	@SuppressWarnings("javadoc")
	public static final String move = "move";

	@SuppressWarnings("javadoc")
	public static final String reproductionThreshold = "reproductionThreshold";

	/** The circuit. */
	private final Circuit circuit;

	/** The landscape. */
	private Landscape landscape = null;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private JamelParameters params = new BasicParameters();

	/** The agents. */
	private final AgentSet<Vertex> vertices;

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 * @throws InitializationException If something goes wrong.
	 */
	public Simplex(String name, Circuit circuit) throws InitializationException {
		this.name=name;
		this.circuit=circuit;
		this.vertices=new BasicAgentSet<Vertex>(null);
	}

	@Override
	public void doEvent(Element event) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public SectorDataset getDataset() {
		return this.vertices.collectData();
	}

	/**
	 * Return the fitness of the specified point.
	 * @param x  the x coordinate.
	 * @param y  the y coordinate.
	 * @return  the fitness of the specified point.
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
		if (name.equals("move")) {
			result = new AbstractPhase(name, this){
				/**
				 *  Implements the downhill simplex method.
				 * @see jamel.basic.sector.Phase#run()
				 */
				@Override
				public void run() {
					final List<Vertex> list = vertices.getList();
					Collections.sort(list, fitnessComparator);
					final Vertex first = list.get(0);
					final Vertex second = list.get(1);
					final Vertex third = list.get(2);
					final double firstFitness = first.getFitness(); 
					final double xG = second.getX() + (third.getX()-second.getX())/2;
					final double yG = second.getY() + (third.getY()-second.getY())/2;
					final double xV = xG-first.getX();
					final double yV = yG-first.getY();
					double xN = xG+xV;
					double yN = yG+yV;
					if (getFitness(xN, yN)<firstFitness) {
						double xNN = xG+2.*xV;
						double yNN = yG+2.*yV;
						if (getFitness(xNN, yNN)<getFitness(xN, yN)) {
							// expansion
							first.moveTo(xNN,yNN);							
						}
						else {
							// reflexion
							first.moveTo(xN,yN);							
						}						
					}
					else {
						xN = xG+xV/2;
						yN = yG+yV/2;				
						if (getFitness(xN, yN)<firstFitness) {
							// contraction
							first.moveTo(xN,yN);						
						}
						else {
							// shrink
							xN = (first.getX()+third.getX())/2;
							yN = (first.getY()+third.getY())/2;				
							first.moveTo(xN,yN);
							second.moveTo(xG,yG);
						}
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
		
		// Looking for the landscape sector.
		final Element landscapeElement = (Element) refElement.getElementsByTagName("landscape").item(0);
		if (landscapeElement==null) {
			throw new InitializationException("Element not found: landscape");
		}
		final String landscapeName = landscapeElement.getAttribute("value");
		if (landscapeName=="") {
			throw new InitializationException("Attribute not found: value");
		}
		this.landscape = (Landscape) circuit.getSector(landscapeName);
		
		// Looking for the vertices of the simplex. 
		final NodeList vertices = element.getElementsByTagName("vertex");
		for (int i=0;i<3;i++) {
			final Element elem = (Element) vertices.item(i);
			final float x = Float.parseFloat(elem.getAttribute("x"));
			final float y = Float.parseFloat(elem.getAttribute("y"));
			final Vertex vertex = new Vertex(this,x,y);
			this.vertices.put(vertex);
		}
		
	}

}

// ***
