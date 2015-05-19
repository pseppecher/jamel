package jamelV3.basic.gui;

import jamelV3.basic.util.InitializationException;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * An abstract chart manager.
 */
public abstract class AbstractChartManager implements ChartManager {

	@SuppressWarnings("javadoc") 
	private class EmptyPanel extends JPanel {{this.setBackground(new Color(230,230,230));}}

	/** The list of the ChartPanel. */
	private final List<JamelChartPanel> chartPanelList = new ArrayList<JamelChartPanel>(45);

	/** The list of the JPanels.*/
	private final JPanel[] panelList;

	/**
	 * Creates the chart manager.
	 * @param file a XML file that contains chart panel configuration.
	 * @throws InitializationException If something goes wrong.
	 */
	public AbstractChartManager(File file) throws InitializationException {
		NodeList panelNodeList=null;
		try {
			panelNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement().getElementsByTagName("panel");
		} catch (SAXException e) {
			throw new InitializationException("Error while creating the ChartManager.", e);
		} catch (IOException e) {
			throw new InitializationException("Error while creating the ChartManager.", e);
		} catch (ParserConfigurationException e) {
			throw new InitializationException("Error while creating the ChartManager.", e);
		}
		this.panelList = new JPanel[panelNodeList.getLength()];
		for (int i = 0; i<panelNodeList.getLength(); i++) {
			// for each panel element:
			if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new RuntimeException("This node should be a panel node.");					
			}
			final Element panelElement = (Element) panelNodeList.item(i);
			final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
			panelList[i] = panel;
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
					for (int k = 0; k<nbSeries; k++) {
						final Element serieXML = (Element) series.item(k);
						seriesList[k]=serieXML.getAttribute("value");
					}
					final JamelChartPanel chartPanel;
					final boolean isScatter = "scatter".equals(chartElement.getAttribute("options")); 
					final XYSeriesCollection data;
					if (isScatter) {
						data = getScatterChartData(seriesList);
					}
					else {
						data = getChartData(seriesList);
					}
					/*final JamelChartPanel chartPanel; // TODO CLEAN UP
					final boolean isScatter = "scatter".equals(chartElement.getAttribute("options")); 
					final XYSeriesCollection data;
					if (isScatter) {
						data = getScatterChartData(series);
					}
					else {
						try {
							data = getChartData(series);
						} catch (Exception e) {
							throw new InitializationException("Something goes wrong while parsing the description of the chart (panel "+panel.getName()+", chart "+j+")",e);
						}
					}*/
					if (data==null) {
						throw new NullPointerException("Data is null.");
					}
					chartPanel = new JamelChartPanel(ChartGenerator.createChart(chartElement,data),isScatter);
					chartPanelList.add(chartPanel);
					panel.add(chartPanel);
				}
			}
			if (chartNodeList.getLength()<9) {
				for (int j=chartNodeList.getLength(); j<9; j++) {
					panel.add(new EmptyPanel());						
				}
			}
		}
	}

	/**
	 * Returns the data for the specified chart.
	 * @param list an array of strings representing the name of the series.
	 * @return an XYSeriesCollection.
	 */
	private XYSeriesCollection getChartData(String[] list) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (String key:list){
			final XYSeries series = getSeries(key);
			if (series!=null) {
				try {
					data.addSeries(series);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("This dataset already contains a series with the key "+series.getKey());
				}
			}
			else {
				throw new RuntimeException(key+" XYSeries not found.");
			}
		}
		return data;
	}

	/**
	 * Returns the data for a scatter chart.
	 * @param list the list of description of the series.
	 * @return the data for a scatter chart.
	 */
	private XYSeriesCollection getScatterChartData(String[] list) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (String key:list){
			final String[] keys = key.split(",",2);
			final XYSeries series = getScatterSeries(keys[0].trim(),keys[1].trim());
			if (series!=null) {
				data.addSeries(series);
			}
			else {
				throw new RuntimeException(keys[0]+","+keys[1]+" XYSeries not found.");
			}
		}
		return data;
	}

	/**
	 * Returns the specified XYSeries.
	 * @param xKey the key for the X data.
	 * @param yKey the key for the Y date.
	 * @return a XYSeries.
	 */
	protected abstract XYSeries getScatterSeries(String xKey, String yKey);

	/**
	 * Returns the specified XYSeries.
	 * @param seriesKey the key for the Y data (X data will be time).
	 * @return a XYSeries.
	 */
	protected abstract XYSeries getSeries(String seriesKey);

	@Override
	public void addMarker(String label,int period) {
		final ValueMarker marker = new ValueMarker(period) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setOutlinePaint(Color.WHITE);
		for (JamelChartPanel panel:this.chartPanelList) {
			panel.addMarker(marker);
		}
	}

	@Override
	public Component[] getPanelList() {
		return this.panelList;
	}

}

// ***
