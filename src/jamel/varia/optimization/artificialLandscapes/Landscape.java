package jamel.varia.optimization.artificialLandscapes;

import jamel.basic.Circuit;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.SectorDataset;
import jamel.basic.gui.XYZItem;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.JamelParameters;

import java.util.LinkedList;
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
	private double[][] map;

	/** The name of the sector. */
	private final String name;

	/** The parameters of the sector. */
	private final JamelParameters params = new BasicParameters();

	/**
	 * Creates a new sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public Landscape(String name, Circuit circuit) {
		this.name = name;
	}

	@Override
	public void doEvent(Element event) {
		// Not used.
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public SectorDataset getDataset() {
		return new SectorDataset() {

			@Override
			public String getAgentInfo(String agent, String key) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getAgentValue(String string1, String string2) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double[] getField(String string1, String string2) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getMax(String data, String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getMean(String data, String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getMin(String data, String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public List<XYDataItem> getScatter(String xKey, String yKey, String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getSectorialValue(String dataKey) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public Double getSum(String data, String select) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public double[][] getXYZData(String xKey, String yKey, String zKey) {
				final double[][] result;
				if ("x".equals(xKey) && "y".equals(yKey) && "z".equals(zKey)) {
					result = map;
				} else {
					result = null;
				}
				return result;
			}

			@Override
			public void putIndividualData(AgentDataset data) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

			@Override
			public void putSectorialValue(String string, Number n) {
				// Not used.
				throw new RuntimeException("Not yet implemented");
			}

		};
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the double value of the specified parameter.
	 * 
	 * @param key
	 *            the key of the parameter to be returned.
	 * @return the double value of the specified parameter.
	 */
	@Override
	public Float getParam(String key) {
		return this.params.get(key);
	}

	@Override
	public Phase getPhase(final String phaseName) {
		// Not used.
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Returns the z value.
	 * 
	 * @param x
	 *            the x value.
	 * @param y
	 *            the y value.
	 * @return the z value.
	 */
	public abstract double getZValue(double x, double y);

	@Override
	public void init(Element element) {
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
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
		final List<XYZItem> truc = new LinkedList<XYZItem>();
		for (float x = left; x < right; x += bWidth) {
			for (float y = bottom; y < top; y += bHeight) {
				truc.add(new XYZItem(x, y, getZValue(x, y)));
			}
		}
		this.map = new double[3][truc.size()];
		int i = 0;
		for (XYZItem item : truc) {
			this.map[0][i] = item.getXValue();
			this.map[1][i] = item.getYValue();
			this.map[2][i] = item.getZValue();
			i++;
		}
	}

}

// ***
