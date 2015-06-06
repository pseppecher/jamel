package jamelV3.basic.gui;

import jamelV3.basic.data.BasicDataManager;
import jamelV3.basic.util.InitializationException;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A basic chart manager. Used by the {@link BasicDataManager} to display dynamic charts in the GUI.
 */
public class BasicChartManager implements ChartManager {

	/** An empty panel.	 */
	private class EmptyPanel extends JPanel {{this.setBackground(emptyColor);}}

	/** A gray color for the empty panels. */
	private static final Color emptyColor = new Color(230,230,230);

	/** The list of the chart panels. */
	private final List<JamelChartPanel> chartPanels = new ArrayList<JamelChartPanel>(45);

	/** The list of the tabbed panes.*/
	private final JPanel[] tabbedPanes;

	/**
	 * Creates the chart manager.
	 * @param root  a XML element that contains the chart panel configuration.
	 * @param dataManager  the parent data manager.
	 * @throws InitializationException If something goes wrong.
	 */
public BasicChartManager(Element root, BasicDataManager dataManager) throws InitializationException {
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
					final JamelChartPanel chartPanel;
					if ("scatter".equals(chartElement.getAttribute("options"))) {
						final XYSeriesCollection data = dataManager.getScatterChartData(series);
						final XYZDataset xyzDataset = dataManager.getXYBlockData(chartElement);
						chartPanel = new JamelChartPanel(ChartGenerator.createScatterChart(chartElement,data,xyzDataset),true);
					}
					else {
						final XYSeriesCollection data = dataManager.getTimeChartData(series);
						chartPanel = new JamelChartPanel(ChartGenerator.createTimeChart(chartElement,data),false);						
					}
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

}

// ***
