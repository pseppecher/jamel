package jamelV3.loktaVolterra;

import java.util.HashMap;

import jamelV3.basic.Circuit;
import jamelV3.basic.sector.BasicSectorDataSet;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Prey sector.
 */
public class Grass implements Sector, LandSector {
	
	/**
	 * An abstract phase for this sector.
	 */
	abstract private class AbstractPhase implements Phase {
		
		/** The name of the phase. */
		final private String name;
		
		/**
		 * Creates a new phase.
		 * @param name the phase name.
		 */
		private AbstractPhase(String name) {
			this.name=name;
		}

		@Override
		public String getName() {
			return this.name;
		}
		
		@Override
		public Sector getSector() {
			return Grass.this;
		}

	}

	@SuppressWarnings("javadoc")
	private class Squares {
		
		final private LandSquare[][] squares = new LandSquare[width][height]; 
	
		public SectorDataset collectData() {
			final SectorDataset sectorDataset = new BasicSectorDataSet(width*height);
			for (int x=0;x<width;x++) {
				for (int y=0;y<height;y++) {
					sectorDataset.put(squares[x][y].getData());						
				}
			}
			return sectorDataset;	
		}

		public LandSquare get(int x, int y) {
			return this.squares[x][y];
		}

		public void init() {
			for (int x=0;x<width;x++) {
				for (int y=0;y<height;y++) {
					LandSquare landSquare = new LandSquare(Grass.this,x,y);
					this.squares[x][y]=landSquare;				
				}
			}				
		}
		
	}

	@SuppressWarnings("javadoc")
	public static final String growth = "growth";

	@SuppressWarnings("javadoc")
	public static final String lim = "lim";

	/** The height of the grass. */
	private int height;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final HashMap<String,Double> params = new HashMap<String,Double>();

	/** The squares. */
	private Squares squares = null;

	/** The width of the grass. */
	private int width;
	
	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public Grass(String name, Circuit circuit) {
		this.name=name;
	}

	@Override
	public void doEvent(Element event) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public double eat(double x, double y, double volume) {
		return squares.get((int)x, (int)y).consume(volume);
	}

	@Override
	public SectorDataset getDataset() {
		return this.squares.collectData();
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
	public Double getParam(String key) {
		return this.params.get(key);
	}

	@Override
	public Phase getPhase(final String name) {
		Phase result = null;
		if (name.equals("grow")) {
			result = new AbstractPhase(name){
				@Override
				public void run() {
					for (int x=0;x<width;x++) {
						for (int y=0;y<height;y++) {
							squares.get(x,y).grow();						
						}
					}
				}				
			};			
		}
		return result;
	}


	@Override
	public void init(Element element) {
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.params.put(attr.getName(), Double.parseDouble(attr.getValue()));
			}
		}
		this.width=this.params.get("width").intValue();
		this.height=this.params.get("height").intValue();
		this.squares=new Squares();
		this.squares.init();
	}

}

// ***
