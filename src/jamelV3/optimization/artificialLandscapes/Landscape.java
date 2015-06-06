package jamelV3.optimization.artificialLandscapes;

import jamelV3.basic.Circuit;
import jamelV3.basic.agent.AgentDataset;
import jamelV3.basic.gui.XYZItem;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.BasicParameters;
import jamelV3.basic.util.JamelParameters;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataItem;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * An artificial landscape useful to evaluate optimization algorithms.
 */
public abstract class Landscape implements Sector {
	
	/** The map. */
	private List<XYZItem> map;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final JamelParameters params = new BasicParameters();

	/**
	 * Creates a new sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public Landscape(String name, Circuit circuit) {
		this.name=name;
	}

	@Override
	public void doEvent(Element event) {
		// Not used.
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public SectorDataset getDataset() {
		return new SectorDataset(){

			@Override
			public Double get(String key) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public List<XYDataItem> getScatter(String xKey, String yKey,
					String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public List<XYZItem> getXYZData(String xKey, String yKey, String zKey) {
				final List<XYZItem> result;
				if ("x".equals(xKey) && "y".equals(yKey) && "z".equals(zKey)) {
					result=map;
				}
				else {
					result = null;
				}
				return result;
			}

			@Override
			public void put(AgentDataset data) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}};
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
		// Not used.
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Returns the z value.
	 * @param x the x value.
	 * @param y the y value.
	 * @return the z value.
	 */
	public abstract double getZValue(double x, double y);

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
		final float top = this.params.get("top");
		final float left = this.params.get("left");
		final float bottom = this.params.get("bottom");
		final float right = this.params.get("right");
		final float bWidth = this.params.get("blockWidth");
		final float bHeight = this.params.get("blockHeight");		
		final ArrayList<XYZItem> truc = new ArrayList<XYZItem>();
		for (float x=left;x<right;x+=bWidth) {
			for (float y=bottom;y<top;y+=bHeight) {
				truc.add(new XYZItem(x,y,getZValue(x,y)));
			}
		}
		this.map=truc;
	}

}

// ***
