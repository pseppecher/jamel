package jamelV3.basic.data;

import jamelV3.basic.gui.BasicChartManager;
import jamelV3.basic.gui.BasicXYZDataset;
import jamelV3.basic.gui.ChartManager;
import jamelV3.basic.gui.DynamicData;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

import java.awt.Component;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic data manager with a ChartManager and a GUI.
 */
public class BasicDataManager {

	/**
	 * A dynamic XYSeries.
	 */
	private abstract class DynamicXYSeries extends XYSeries implements DynamicData {

		/**
		 * Constructs a new empty series, with the auto-sort flag set as <code>false</code>, and duplicate values allowed.
		 * @param key the series key (<code>null</code> not permitted).
		 */
		public DynamicXYSeries(String key) {
			super(key,false);
		}

	}

	/**
	 * Initializes and returns a new chart manager.
	 * @param dataManager the parent data manager.
	 * @param settings a XML element with the settings.
	 * @param path the path of the scenario file.
	 * @return a new chart manager.
	 * @throws InitializationException If something goes wrong.
	 */
	private static ChartManager getNewChartManager(final BasicDataManager dataManager, Element settings, String path) throws InitializationException {
		ChartManager chartManager = null;
		final String fileName = settings.getAttribute("chartsConfigFile");
		if (fileName != null) {
			final File file = new File(path+"/"+fileName);
			final Element root;
			try {
				root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while creating the ChartManager.", e);
			}
			if (!"charts".equals(root.getNodeName())) {
				throw new InitializationException("The root node of the scenario file must be named <charts>.");
			}			
			try {
				chartManager = new BasicChartManager(root,dataManager);
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while parsing this file: "+file.getAbsolutePath(),e);
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/** Register of the dynamic data used by the GUI. */
	private final Map<String,DynamicData> dynamicData = new LinkedHashMap<String,DynamicData>();

	/** The chart manager. */
	protected final ChartManager chartManager;

	/** The macro dataset. */
	protected final MacroDataset macroDataset;

	/** The timer. */
	final protected Timer timer;

	/**
	 * Creates a new basic data manager.
	 * @param settings a XML element with the settings.
	 * @param timer the timer.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		if (timer==null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer = timer;
		this.chartManager = getNewChartManager(this, settings, path);
		this.macroDataset = getNewMacroDataset();
	}

	/**
	 * Creates and returns a new {@link MacroDataset}. 
	 * @return a new {@link MacroDataset}.
	 */
	protected MacroDataset getNewMacroDataset() {
		return new BasicMacroDataset();
	}

	/**
	 * Updates all the registered series.
	 */
	protected void updateSeries() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					for (DynamicData series:dynamicData.values()) {
						series.update();
					}
				}
			}) ;
		} catch (Exception e) {
			throw new RuntimeException("Something goes wrong while updating the series.",e);
		}		
	}

	/**
	 * Add a marker to all time charts.
	 * @param label the label of the marker.
	 */
	public void addMarker(String label) {
		this.chartManager.addMarker(label,timer.getPeriod().intValue());
	}

	/**
	 * Returns the list of panel of the chart manager.
	 * @return the list of panel of the chart manager.
	 */
	public Component[] getPanelList() {
		return this.chartManager.getPanelList();
	}

	/**
	 * Returns the specified scatter series.
	 * @param series a node list that contains the description of the series.
	 * @return a scatter series.
	 */
	public XYSeriesCollection getScatterChartData(NodeList series) {
		final XYSeriesCollection data = new XYSeriesCollection();
		final int nbSeries = series.getLength();
		for (int k = 0; k<nbSeries; k++) {
			final Element description = (Element) series.item(k);
			final String sector=description.getAttribute("sector");
			final String xKey=description.getAttribute("x");
			final String yKey=description.getAttribute("y");
			final String select=description.getAttribute("select");
			final String seriesKey = sector+"."+xKey+"."+yKey+"."+select;
			final DynamicXYSeries scatterSeries;
			final DynamicXYSeries result1=(DynamicXYSeries) this.dynamicData.get(seriesKey);
			if (result1!=null) {
				scatterSeries=result1;
			}
			else {
				scatterSeries = new DynamicXYSeries(seriesKey){
					@SuppressWarnings("unchecked")
					@Override
					public void update() {
						final List<XYDataItem> newData = macroDataset.getScatterData(sector,xKey,yKey,select);
						this.data.clear();
						if (newData!=null) {
							this.data.addAll(newData);
						}
						this.fireSeriesChanged();
					}
				};
				this.dynamicData.put(seriesKey, scatterSeries);
			}
			data.addSeries(scatterSeries);
		}
		return data;
	}

	/**
	 * Returns the data for the specified chart.
	 * @param series an array of strings representing the name of the series.
	 * @return an XYSeriesCollection.
	 */
	public XYSeriesCollection getTimeChartData(NodeList series) {
		final XYSeriesCollection result = new XYSeriesCollection();
		final int nbSeries = series.getLength();
		for (int k = 0; k<nbSeries; k++) {
			final Element seriesElem = (Element) series.item(k);
			final String key=seriesElem.getAttribute("value");
			final DynamicXYSeries timeSeries;
			final DynamicXYSeries xySeries=(DynamicXYSeries) this.dynamicData.get(key);
			if (xySeries!=null) {
				timeSeries=xySeries;
			} 
			else {
				timeSeries = new DynamicXYSeries(key) {
					@Override
					public void update() {
						final Double value = macroDataset.get(key);
						final int period = timer.getPeriod().intValue();
						if (value!=null) {
							this.add(period, value);
						}
					}
				};
				this.dynamicData.put(key, timeSeries);
			}
			try {
				result.addSeries(timeSeries);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("This dataset already contains a series with the key "+timeSeries.getKey(),e);
			}
		}
		return result;
	}

	/**
	 * Returns a XYZ dataset.
	 * @param element a XML element that contains the description of the dataset.
	 * @return a XYZ dataset.
	 * @throws InitializationException If something goes wrong.
	 */
	public XYZDataset getXYBlockData(Element element) throws InitializationException {
		final BasicXYZDataset dataset;
		final Node xyzSeriesNode = element.getElementsByTagName("xyBlockSeries").item(0);
		if (xyzSeriesNode==null) {
			dataset=null;
		}
		else {
			if (Node.ELEMENT_NODE!=xyzSeriesNode.getNodeType()) {
				throw new InitializationException("This node should be an element.");
			}
			final Element xyzSeriesElement = (Element) xyzSeriesNode;
			final String sector = xyzSeriesElement.getAttribute("sector");
			final String xKey = xyzSeriesElement.getAttribute("x");
			final String yKey = xyzSeriesElement.getAttribute("y");
			final String zKey = xyzSeriesElement.getAttribute("z");
			final String key = sector+".surf."+xKey+"."+yKey+"."+zKey;
			final BasicXYZDataset result1=(BasicXYZDataset) this.dynamicData.get(key);
			if (result1!=null) {
				dataset=result1;
			} 
			else {
				dataset = new BasicXYZDataset(){
					@Override
					public void update() {
						this.addSeries(0, macroDataset.getXYZData(sector,xKey,yKey,zKey));
					}
				};
				this.dynamicData.put(key, dataset);
			}
		}
		return dataset;
	}
	
	/**
	 * Puts the specified sector dataset into the macroeconomic dataset.
	 * @param sectorName the name of the sector.
	 * @param sectorDataset the dataset.
	 */
	public void putData(String sectorName, SectorDataset sectorDataset) {
		// TODO vŽrifier que les donnŽes ne sont pas dŽjˆ prŽsentes ?
		this.macroDataset.putData(sectorName, sectorDataset);
	}

	/**
	 * Updates series and clears the macro dataset.
	 */
	public void update() {
		this.updateSeries();
		this.macroDataset.clear();
	}
	

}

// ***
