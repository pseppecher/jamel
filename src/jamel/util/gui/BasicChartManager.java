package jamel.basic.gui;

import jamel.basic.data.BasicDataManager;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.TextAnchor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic chart manager. Used to display dynamic charts in the {@link GUI}.
 */
public class BasicChartManager implements ChartManager {

	/** An empty panel. */
	private class EmptyPanel

			extends ChartPanel {
		@SuppressWarnings("javadoc")
		public EmptyPanel() {
			super(null);
			this.setBackground(JamelColor.getColor("background"));
		}
	}

	/** The font used for chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/** The list of the chart panels. */
	private final List<JamelChartPanel> chartPanels = new ArrayList<JamelChartPanel>(45);

	/** The data manager. */
	private final BasicDataManager dataManager;

	/** The list of the tabbed panes. */
	private final JPanel[] tabbedPanes;

	/**
	 * The list of objects to be updated.
	 */
	private List<Updatable> updatables = new LinkedList<Updatable>();

	/**
	 * The XML file that contains the panels configuration.
	 */
	private final File file;

	/**
	 * Creates the chart manager.
	 * 
	 * @param file
	 *            an XML file that contains the panels configuration.
	 * @param dataManager
	 *            the parent data manager.
	 * @param timer
	 *            the timer.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	public BasicChartManager(File file, BasicDataManager dataManager, Timer timer) throws InitializationException {
		this.file = file;
		final Element root;
		try {
			root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
		} catch (Exception e) {
			throw new InitializationException("Something goes wrong while creating the ChartManager.", e);
		}
		if (!"charts".equals(root.getNodeName())) {
			throw new InitializationException("The root node of the file must be named <charts>.");
		}
		final NodeList panelNodeList = root.getElementsByTagName("panel");
		this.dataManager = dataManager;
		this.tabbedPanes = new JPanel[panelNodeList.getLength()];
		for (int i = 0; i < panelNodeList.getLength(); i++) {
			// for each panel element:
			if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
				throw new RuntimeException("This node should be a panel node.");
			}
			final Element panelElement = (Element) panelNodeList.item(i);
			if (!panelElement.getAttribute("visible").equals("false")) {
				final JPanel tabPanel = new JPanel();
				tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.X_AXIS));
				tabbedPanes[i] = tabPanel;
				tabPanel.setBackground(new Color(0, 0, 0, 0));
				tabPanel.setName(panelElement.getAttribute("title"));
				final NodeList nodeList = panelElement.getChildNodes();

				// 2016-03-28: changement de la syntaxe
				// Par défaut, les graphiques sont organisés en colonne.
				// La balise 'col' indique un saut de colonne.

				final List<JPanel> subPanelList = getSubPanels(nodeList, timer);

				for (JPanel subPanel : subPanelList) {
					tabPanel.add(subPanel);
				}
			}
		}
	}

	/**
	 * Creates and returns a new {@link JPanel} according to the specified
	 * description.
	 * 
	 * @param node
	 *            an XML node that contains the description of the panel to
	 *            create.
	 * @param timer
	 *            a timer.
	 * @return a new JPanel {@link JPanel}
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private Component getPanel(Node node, Timer timer) throws InitializationException {
		Component result = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			final Element elem = (Element) node;
			try {
				if (elem.getNodeName().equals("chart")) {
					final NodeList series = elem.getElementsByTagName("series");
					if (series.getLength() == 0) {
						// The chart is empty.
						result = new EmptyPanel();
					} else {
						final JamelChartPanel chartPanel = ChartFactory.createChartPanel(elem, this.dataManager);
						chartPanels.add(chartPanel);
						result = chartPanel;
					}
				} else if (elem.getNodeName().equals("html")) {
					final HtmlPanel panel = HtmlPanel.getNewHtmlPanel(elem, this.dataManager.getMacroDatabase(), file);
					this.updatables.add(panel);
					result = panel;
				} else if (elem.getNodeName().equals("validation")) {
					final ValidationPanel panel = new ValidationPanel(elem, this.dataManager.getMacroDatabase(), timer);
					this.updatables.add(panel);
					result = panel;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result = HtmlPanel
						.getErrorPanel("Error:<br />" + e.toString() + "<br />See jamel.log file for more details.");
			}
			if (result == null) {
				final JPanel jPanel = new JPanel();
				if (elem.getNodeName().equals("col")) {
					final BoxLayout layout = new BoxLayout(jPanel, BoxLayout.Y_AXIS);
					jPanel.setLayout(layout);
					final String title = elem.getAttribute("title");
					if (!title.equals("")) {
						final JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
						titleLabel.setBackground(JamelColor.getColor("background"));
						titleLabel.setOpaque(true);
						titleLabel.setMaximumSize(new Dimension(1000, 25));
						titleLabel.setMinimumSize(new Dimension(10, 25));
						titleLabel.setPreferredSize(new Dimension(1000, 25));
						titleLabel.setFont(titleFont);
						titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
						jPanel.add(titleLabel);
					}
				} else if (elem.getNodeName().equals("line")) {
					jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
				} else {
					throw new InitializationException("Unexpected node name: " + elem.getNodeName());
				}
				final NodeList nodeList = elem.getChildNodes();
				if (nodeList.getLength() > 0) {
					for (int j = 0; j < nodeList.getLength(); j++) {
						final Component subPanel = getPanel(nodeList.item(j), timer);
						if (subPanel != null) {
							jPanel.add(subPanel);
						}
					}
				} else {
					jPanel.add(new EmptyPanel());
				}
				result = jPanel;
			}
		}
		return result;
	}

	/**
	 * Creates and returns a list of sub-panels. Each sub-panel represents a
	 * column of the main panel. In a sub-panel, elements are added vertically.
	 * 
	 * @param nodeList
	 *            the XML description of the sub-panel to be created.
	 * @param timer
	 *            the timer.
	 * @return a list of sub-panels.
	 */
	private List<JPanel> getSubPanels(NodeList nodeList, Timer timer) {

		// 2016-03-28: changement de la syntaxe
		// Construit une liste de panels, chacun représentant une colonne.
		// A l'intérieur de la colonne, les panneaux sont empilés les uns en
		// dessous des autres.
		// La balise 'col' indique un saut de colonne.

		final List<JPanel> result = new LinkedList<JPanel>();

		JPanel col = new JPanel();
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		result.add(col);

		for (int j = 0; j < nodeList.getLength(); j++) {
			final Node node = nodeList.item(j);
			if (node.getNodeName().equals("col")) {
				col = new JPanel();
				col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
				result.add(col);
			} else {
				Component subPanel = null;
				try {
					subPanel = getPanel(nodeList.item(j), timer);
				} catch (Exception e) {
					e.printStackTrace();
					subPanel = HtmlPanel.getErrorPanel(
							"Error:<br />" + e.toString() + "<br />See jamel.log file for more details.");
				}
				if (subPanel != null) {
					col.add(subPanel);
				}
			}
		}
		return result;
	}

	@Override
	public void addMarker(String label, int period) {
		// FIXME: doesn't work with subplot.
		final ValueMarker marker = new ValueMarker(period);
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		for (JamelChartPanel panel : this.chartPanels) {
			panel.addMarker(marker);
		}
	}

	@Override
	public Component[] getPanelList() {
		return this.tabbedPanes;
	}

	@Override
	public void update() {
		for (Updatable updatable : this.updatables) {
			updatable.update();
		}
	}

}

// ***
