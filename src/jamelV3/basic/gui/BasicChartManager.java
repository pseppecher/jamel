package jamelV3.basic.gui;

import jamelV3.basic.data.MacroDataset;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A basic chart manager.
 */
public class BasicChartManager implements ChartManager {

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

	/** An empty panel.	 */
	private class EmptyPanel extends JPanel {{this.setBackground(emptyColor);}}

	/** A gray color for the empty panels. */
	private static final Color emptyColor = new Color(230,230,230);

	/** The list of the chart panels. */
	private final List<JamelChartPanel> chartPanels = new ArrayList<JamelChartPanel>(45);

	/** The macro dataset. */
	private final MacroDataset macroDataset;

	/** A collection of series. */
	private final Map<String,DynamicData> series = new LinkedHashMap<String,DynamicData>();

	/** The list of the tabbed panes.*/
	private final JPanel[] tabbedPanes;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates the chart manager.
	 * @param file a XML file that contains chart panel configuration.
	 * @param macroDataset the macroeconomic dataset.
	 * @param timer the timer. 
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicChartManager(File file, MacroDataset macroDataset, Timer timer) throws InitializationException {
		this.macroDataset = macroDataset;
		this.timer = timer;
		final Element root;
		try {
			root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
		} catch (Exception e) {
			throw new InitializationException("Something goes wrong while creating the ChartManager.", e);
		}
		if (!"charts".equals(root.getNodeName())) {
			throw new InitializationException("The root node of the scenario file must be named <charts>.");
		}
		final NodeList panelNodeList = root.getElementsByTagName("panel");
		this.tabbedPanes = new JPanel[panelNodeList.getLength()];
		for (int i = 0; i<panelNodeList.getLength(); i++) {
			// for each panel element:
			if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new RuntimeException("This node should be a panel node.");
			}
			final Element panelElement = (Element) panelNodeList.item(i);
			final int rows = Integer.parseInt(panelElement.getAttribute("rows"));
			final int cols = Integer.parseInt(panelElement.getAttribute("cols"));
			final JPanel panel = new JPanel(new GridLayout(rows,cols,10,10));
			tabbedPanes[i] = panel;
			panel.setBackground(new Color(0,0,0,0));
			panel.setName(panelElement.getAttribute("title"));
			final NodeList chartNodeList = panelElement.getElementsByTagName("chart");
			for (int j = 0; j<chartNodeList.getLength(); j++) {
				// for each chart element:				
				final Element chartElement = (Element) chartNodeList.item(j);
				if (chartElement.getAttribute("title").equals("Empty")) {
					panel.add(new EmptyPanel());
				}
				else {
					final NodeList series = chartElement.getElementsByTagName("series");
					final int nbSeries = series.getLength();
					final String[] seriesList = new String[nbSeries];
					final XYSeriesCollection data;
					final JamelChartPanel chartPanel;
					final boolean isScatter = "scatter".equals(chartElement.getAttribute("options"));
					if (isScatter) {
						data = new XYSeriesCollection();
						for (int k = 0; k<nbSeries; k++) {
							final Element seriesElem = (Element) series.item(k);
							data.addSeries(getScatterSeries(seriesElem));
						}						
						final XYZDataset xyzDataset = getXYBlockData(chartElement);
						chartPanel = new JamelChartPanel(ChartGenerator.createScatterChart(chartElement,data,xyzDataset),true);
					}
					else {
						for (int k = 0; k<nbSeries; k++) {
							final Element seriesElem = (Element) series.item(k);
							seriesList[k]=seriesElem.getAttribute("value");
						}
						data = getTimeChartData(seriesList);
						chartPanel = new JamelChartPanel(ChartGenerator.createTimeChart(chartElement,data),false);						
					}

					/*final boolean isScatter = "scatter".equals(chartElement.getAttribute("options")); 
					if (isScatter) {
						data = getScatterChartData(seriesList);
					}
					else {
						data = getTimeChartData(seriesList);
						chartPanel = new JamelChartPanel(ChartGenerator.createTimeChart(chartElement,data),false);
					}*/
					chartPanels.add(chartPanel);
					panel.add(chartPanel);
				}
			}
			final int nPanel = rows*cols;
			if (chartNodeList.getLength()<nPanel) {
				for (int j=chartNodeList.getLength(); j<nPanel; j++) {
					panel.add(new EmptyPanel());
				}
			}
		}
	}

	/**
	 * Returns the specified scatter series.
	 * @param description an XLM element that contains the description of the series.
	 * @return a scatter series.
	 */
	private DynamicXYSeries getScatterSeries(Element description) {
		final String sector=description.getAttribute("sector");
		final String xKey=description.getAttribute("x");
		final String yKey=description.getAttribute("y");
		final String select=description.getAttribute("select");
		final String seriesKey = sector+"."+xKey+"."+yKey+"."+select;
		final DynamicXYSeries scatterSeries;
		final DynamicXYSeries result1=(DynamicXYSeries) this.series.get(seriesKey);
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
			this.series.put(seriesKey, scatterSeries);
		}
		return scatterSeries;
	}

	/**
	 * Returns the data for the specified chart.
	 * @param list an array of strings representing the name of the series.
	 * @return an XYSeriesCollection.
	 */
	private XYSeriesCollection getTimeChartData(String[] list) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (final String key:list){
			final DynamicXYSeries timeSeries;
			final DynamicXYSeries xySeries=(DynamicXYSeries) this.series.get(key);
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
				this.series.put(key, timeSeries);
			}
			try {
				data.addSeries(timeSeries);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("This dataset already contains a series with the key "+timeSeries.getKey(),e);
			}
		}
		return data;
	}

	/**
	 * Returns a XYZ dataset.
	 * @param element a XML element that contains the description of the dataset.
	 * @return a XYZ dataset.
	 * @throws InitializationException If something goes wrong.
	 */
	private XYZDataset getXYBlockData(Element element) throws InitializationException {
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
			final BasicXYZDataset result1=(BasicXYZDataset) this.series.get(key);
			if (result1!=null) {
				dataset=result1;
			} 
			else {
				dataset = new BasicXYZDataset(key){
					@Override
					public void update() {
						this.data = macroDataset.getXYZData(sector,xKey,yKey,zKey);
					}
				};
				this.series.put(key, dataset);
			}
		}
		return dataset;
	}

	@Override
	public void addMarker(String label,int period) {
		final ValueMarker marker = new ValueMarker(period) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setOutlinePaint(Color.WHITE);
		for (JamelChartPanel panel:this.chartPanels) {
			panel.addMarker(marker);
		}
	}

	@Override
	public Component[] getPanelList() {
		return this.tabbedPanes;
	}

	@Override
	public void update() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					for (DynamicData series:series.values()) {
						series.update();
					}
				}
			}) ;
		} catch (Exception e) {
			throw new RuntimeException("Something goes wrong while updating the series.",e);
		}		
	}

}

// ***
