package jamel.varia.loktaVolterra;

import jamel.basic.Circuit;
import jamel.basic.data.BasicSectorDataset;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.JamelParameters;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The grass sector.
 */
public class Grass implements Sector, LandSector {

	@SuppressWarnings("javadoc")
	public static final String growth = "growth"; 

	@SuppressWarnings("javadoc")
	public static final String lim = "lim";

	/** The height of the grass. */
	private Integer height=null;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final JamelParameters params = new BasicParameters();

	/** The squares of land. */
	private LandSquare[][] squares = null;

	/** The width of the grass. */
	private Integer width=null;

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
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public double eat(double x, double y, double volume) {
		return this.squares[(int) x][(int) y].consume(volume);
	}

	@Override
	public SectorDataset getDataset() {
		final SectorDataset sectorDataset = new BasicSectorDataset();
		for (int x=0;x<width;x++) {
			for (int y=0;y<height;y++) {
				sectorDataset.putIndividualData(squares[x][y].getData());						
			}
		}
		return sectorDataset;	
	}

	@Override
	public Integer getLandHeight() {
		return this.height;
	}

	@Override
	public Integer getLandWidth() {
		return this.width;
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
		if (name.equals("grow")) {
			result = new AbstractPhase(name, this){
				@Override
				public void run() {
					for (int x=0;x<width;x++) {
						for (int y=0;y<height;y++) {
							squares[x][y].grow();						
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
				this.params.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
		this.width=this.params.get("width").intValue();
		this.height=this.params.get("height").intValue();
		this.squares = new LandSquare[width][height];
		for (int x=0;x<width;x++) {
			for (int y=0;y<height;y++) {
				LandSquare landSquare = new LandSquare(Grass.this,x,y);
				this.squares[x][y]=landSquare;				
			}
		}				
	}

}

// ***
