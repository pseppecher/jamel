package jamel.basic.data.util;

import jamel.basic.gui.JamelChartPanel;
import jamel.basic.gui.JamelColor;
import jamel.basic.gui.ScatterChartPanel;
import jamel.basic.gui.TimeChartPanel;
import jamel.util.Circuit;
import jamel.util.FileParser;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Paint;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;


/**
 * An abstract chart manager.
 */
public abstract class AbstractChartManager implements ChartManager {

	/** The list of the panels.*/
	private final JPanel[] panels;

	/** The list of the timeChartPanel. */
	private final List<TimeChartPanel> timeChartPanelList = new ArrayList<TimeChartPanel>(45);

	/**
	 * Creates the chart manager.
	 * @param fileName the name of config file.
	 * @throws FileNotFoundException if the config file is not found.
	 */
	public AbstractChartManager(String fileName) throws FileNotFoundException {
		final Map<String,String> chartDescription = FileParser.parseMap(fileName);
		final String value = chartDescription.get("panels");
		if (value!=null) {
			@SuppressWarnings("serial") class EmptyPanel extends JPanel {{this.setBackground(new Color(230,230,230));}}
			final String[] panelTitles = FileParser.toArray(value);
			panels = new JPanel[panelTitles.length];
			int index = 0;
			for (String panelTitle:panelTitles) {
				final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
				panel.setBackground(new Color(0,0,0,0));
				panel.setName(panelTitle);
				final String chartList = chartDescription.get(panelTitle+".list");
				if (chartList!=null){
					final String[] titles = FileParser.toArray(chartList);
					for (String title:titles) {
						if (title.equals("Empty")) {
							panel.add(new EmptyPanel());								
						}
						else {
							final String series = chartDescription.get(panelTitle+"."+title+".series");
							if (series!=null) {
								final String option = chartDescription.get(panelTitle+"."+title+".option");
								final String legend = chartDescription.get(panelTitle+"."+title+".legend");
								final String colors = chartDescription.get(panelTitle+"."+title+".colors");
								final Paint[] paints;
								final String[] legendItems;
								if (colors!=null) {
									paints = JamelColor.getColors(FileParser.toArray(colors));
								}
								else {
									paints = null;
								}
								if (legend!=null) {
									legendItems = FileParser.toArray(legend);
								}
								else {
									legendItems = null;
								}
								final JamelChartPanel chartPanel;
								if ("scatter".equals(option)) {
									final XYSeriesCollection data = getScatterChartData(FileParser.toArray(series));
									chartPanel = new ScatterChartPanel(title,data,paints,legendItems);
								}
								else {
									final String yAxisMin = chartDescription.get(panelTitle+"."+title+".yAxis.min");
									final String yAxisMax = chartDescription.get(panelTitle+"."+title+".yAxis.max");
									final Double yMin;
									if (yAxisMin!=null) {
										yMin = Double.parseDouble(yAxisMin);
									}
									else {
										yMin = null;
									}
									final Double yMax;
									if (yAxisMax!=null) {
										yMax = Double.parseDouble(yAxisMax);
									}
									else {
										yMax = null;
									}
									final XYSeriesCollection data = getChartData(FileParser.toArray(series));
									final TimeChartPanel timeChartPanel = new TimeChartPanel(title,yMin,yMax,data,paints,legendItems);
									timeChartPanelList.add(timeChartPanel);
									chartPanel = timeChartPanel;
								}
								panel.add(chartPanel);
							}
							else {
								// The case where the series description was not found.
							}
						}
					}
					if (titles.length<9) {
						for (int i=titles.length; i<9; i++) {
							panel.add(new EmptyPanel());						
						}
					}
				}
				panels[index] = panel;
				index++;
			}
		}
		else {
			this.panels = new JPanel[0]; // The panel list is empty.
		}

	}

	/**
	 * Returns the data for the specified chart.
	 * @param dataKeys an array of strings representing the name of the series.
	 * @return an XYSeriesCollection.
	 */
	private XYSeriesCollection getChartData(String[] dataKeys) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (String key:dataKeys){
			final XYSeries series = getSeries(key);//this.dataManager.getSeries(key);
			if (series!=null) {
				data.addSeries(series);
			}
			else {
				throw new RuntimeException(key+" XYSeries not found.");
			}
		}
		return data;
	}

	/**
	 * Returns the data for a scatter chart.
	 * @param dataKeys the list of the data.
	 * @return the data for a scatter chart.
	 */
	private XYSeriesCollection getScatterChartData(String[] dataKeys) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (int i = 0; i<dataKeys.length; i+=2){
			final XYSeries series = getScatterSeries(dataKeys[i],dataKeys[i+1]);
			if (series!=null) {
				data.addSeries(series);
			}
			else {
				throw new RuntimeException(dataKeys[i]+","+dataKeys[i+1]+" XYSeries not found.");
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
		final ValueMarker marker = new ValueMarker(Circuit.getCurrentPeriod().getValue()) ;
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
