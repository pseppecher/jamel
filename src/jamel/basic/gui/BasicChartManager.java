package jamel.basic.gui;

import jamel.basic.data.BasicDataManager;
import jamel.basic.util.InitializationException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A basic chart manager. Used by the {@link BasicDataManager} to display dynamic charts in the {@link GUI}.
 */
public class BasicChartManager implements ChartManager {

	/** An empty panel.	 */
	private class EmptyPanel extends ChartPanel {@SuppressWarnings("javadoc")
	public EmptyPanel() {
		super(null);
		this.setBackground(backgroundColor);	}}

	/** A gray color for the empty panels. */
	private static final Color backgroundColor = new Color(230,230,230);

	/** The font used for chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/** The list of the chart panels. */
	private final List<JamelChartPanel> chartPanels = new ArrayList<JamelChartPanel>(45);

	/** The data manager. */
	private final BasicDataManager dataManager;

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
		this.dataManager=dataManager;
		this.tabbedPanes = new JPanel[panelNodeList.getLength()];
		for (int i = 0; i<panelNodeList.getLength(); i++) {
			// for each panel element:
			if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new RuntimeException("This node should be a panel node.");
			}
			final Element panelElement = (Element) panelNodeList.item(i);
			final JPanel tabPanel = new JPanel();
			tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.X_AXIS));
			tabbedPanes[i] = tabPanel;
			tabPanel.setBackground(new Color(0,0,0,0));
			tabPanel.setName(panelElement.getAttribute("title"));
			final NodeList nodeList = panelElement.getChildNodes();
			for (int j = 0; j<nodeList.getLength(); j++) {
				final JPanel subPanel = getPanel(nodeList.item(j));
				if (subPanel!=null) {
					tabPanel.add(subPanel);					
				}
			}
		}
	}

	/**
	 * Creates and returns a new {@link JPanel} according to the specified description. 
	 * @param node an XML node that contains the description of the panel to create.
	 * @return a new JPanel {@link JPanel}
	 * @throws InitializationException If something goes wrong.
	 */
	private JPanel getPanel(Node node) throws InitializationException {
		final JPanel result;
		if (node.getNodeType()!=Node.ELEMENT_NODE) {
			result=null;
		}
		else {
			final Element elem = (Element) node;
			if (elem.getNodeName().equals("chart")) {
				// The panel is a chart panel.
				final NodeList series = elem.getElementsByTagName("series");
				if (series.getLength()==0) {
					// The chart is empty.
					result = new EmptyPanel();
				}
				else {
					final JamelChartPanel chartPanel = ChartFactory.createChartPanel(elem,this.dataManager);
					chartPanels.add(chartPanel);
					result = chartPanel;
				}
			}
			else {
				// The panel is not a chart panel.
				result = new JPanel();
				if (elem.getNodeName().equals("col")) {
					final BoxLayout layout = new BoxLayout(result, BoxLayout.Y_AXIS);
					result.setLayout(layout);
					final String title = elem.getAttribute("title");
					if (!title.equals("")) {
						final JLabel titleLabel = new JLabel(title,SwingConstants.CENTER);
						titleLabel.setBackground(backgroundColor);
						titleLabel.setOpaque(true);
						titleLabel.setMaximumSize(new Dimension(1000, 25));
						titleLabel.setMinimumSize(new Dimension(10, 25));
						titleLabel.setPreferredSize(new Dimension(1000, 25));
						titleLabel.setFont(titleFont);
						titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
						result.add(titleLabel);
					}
				}
				else if (elem.getNodeName().equals("line")) {
					result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
				}
				else {
					throw new InitializationException("Unexpected node name: "+elem.getNodeName());
				}
				final NodeList nodeList = elem.getChildNodes();
				if (nodeList.getLength()>0) {
					for (int j = 0; j<nodeList.getLength(); j++) {
						final JPanel subPanel = getPanel(nodeList.item(j));
						if (subPanel!=null) {
							result.add(subPanel);
						}
					}
				}
				else {
					result.add(new EmptyPanel());
				}
			}
		}
		return result;
	}

	@Override
	public void addMarker(String label,int period) {
		final ValueMarker marker = new ValueMarker(period) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
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
