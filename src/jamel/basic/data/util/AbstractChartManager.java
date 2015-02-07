package jamel.basic.data.util;

import jamel.basic.data.util.xml.ChartDescription;
import jamel.basic.data.util.xml.SeriesDescription;
import jamel.basic.gui.JamelChartPanel;
import jamel.basic.gui.ScatterChartPanel;
import jamel.basic.gui.TimeChartPanel;
import jamel.util.Circuit;

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

	@SuppressWarnings("javadoc") private class EmptyPanel extends JPanel {{this.setBackground(new Color(230,230,230));}}

	/** The list of the panels.*/
	private final JPanel[] panels;

	/** The list of the timeChartPanel. */
	private final List<TimeChartPanel> timeChartPanelList = new ArrayList<TimeChartPanel>(45);

	/**
	 * Creates the chart manager.
	 * @param file a XML file that contains chart panel configuration.
	 * @throws ParserConfigurationException in the case of a serious configuration error. 
	 * @throws IOException in the case of failed or interrupted I/O operation.
	 * @throws SAXException in the case of a general SAX error or warning.
	 */
	public AbstractChartManager(File file) throws ParserConfigurationException, SAXException, IOException {

		final NodeList panelNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement().getElementsByTagName("panel");
		this.panels = new JPanel[panelNodeList.getLength()];

		for (int i = 0; i<panelNodeList.getLength(); i++) {

			// for each panel element

			if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new RuntimeException("This node should be a panel node.");					
			}
			final Element panelElement = (Element) panelNodeList.item(i);
			final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
			panels[i] = panel;
			panel.setBackground(new Color(0,0,0,0));
			panel.setName(panelElement.getAttribute("title"));
			final NodeList chartNodeList = panelElement.getElementsByTagName("chart");
			for (int j = 0; j<chartNodeList.getLength(); j++) {
				
				// for each chart element
				
				final Element chartElement = (Element) chartNodeList.item(j);
				if (chartElement.getAttribute("title").equals("Empty")) {
					panel.add(new EmptyPanel());								
				}
				else {
					// TODO quand on aura un peu de temps, revoir tout ca et se debarrasser des objets 'chartDescription' et 'seriesDescription'. 
					final ChartDescription chartDescription = new ChartDescription(chartElement.getAttribute("title"));
					final NodeList series = chartElement.getElementsByTagName("series");
					final int nbSeries = series.getLength();
					for (int k = 0; k<nbSeries; k++) {
						final Element serieXML = (Element) series.item(k);
						final SeriesDescription seriesDescription = new SeriesDescription(serieXML.getAttribute("value"),
								serieXML.getAttribute("color"),
								serieXML.getAttribute("label"));
						chartDescription.addSerie(seriesDescription);
					}
					chartDescription.setOptions(chartElement.getAttribute("options"));
					final NodeList yAxisList = chartElement.getElementsByTagName("yAxis");
					final int nbYAxis = yAxisList.getLength();
					for (int l = 0; l<nbYAxis; l++) {
						final Element yAxis = (Element) yAxisList.item(l);
						chartDescription.setYAxisMax(yAxis.getAttribute("max"));
						chartDescription.setYAxisMin(yAxis.getAttribute("min"));
					}
					final JamelChartPanel chartPanel;
					if ("scatter".equals(chartElement.getAttribute("options"))) {
						final XYSeriesCollection data = getScatterChartData(chartDescription.getSeries());
						chartPanel = new ScatterChartPanel(chartDescription,data);
					}
					else {
						final XYSeriesCollection data = getChartData(chartDescription.getSeries());
						final TimeChartPanel timeChartPanel = new TimeChartPanel(chartDescription,data);
						timeChartPanelList.add(timeChartPanel);
						chartPanel = timeChartPanel;
					}
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
	private XYSeriesCollection getChartData(List<SeriesDescription> list) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (SeriesDescription item:list){
			final XYSeries series = getSeries(item.getKey());
			if (series!=null) {
				try {
					data.addSeries(series);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("This dataset already contains a series with the key "+series.getKey());
				}
			}
			else {
				throw new RuntimeException(item.getKey()+" XYSeries not found.");
			}
		}
		return data;
	}

	/**
	 * Returns the data for a scatter chart.
	 * @param list the list of description of the series.
	 * @return the data for a scatter chart.
	 */
	private XYSeriesCollection getScatterChartData(List<SeriesDescription> list) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (SeriesDescription item:list){
			final String[] keys = item.getKey().split(",",2);
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
	public void addMarker(String label) {
		final ValueMarker marker = new ValueMarker(Circuit.getCurrentPeriod().intValue()) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setOutlinePaint(Color.WHITE);
		for (TimeChartPanel panel:this.timeChartPanelList) {
			panel.addMarker(marker);
		}
	}

	@Override
	public Component[] getChartPanelList() {
		return this.panels;
	}

}

// ***
