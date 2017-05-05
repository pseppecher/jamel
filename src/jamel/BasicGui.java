package jamel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic graphical user interface.
 */
public class BasicGui extends JamelObject implements Gui {

	/**
	 * The background color for the tabed panels.
	 */
	private static final Color tabPanelBackgroundColor = new Color(0, 0, 0, 0);

	/**
	 * Marks the specified window as able to be full screened.
	 * 
	 * @param window
	 *            the window to be marked.
	 */
	private static void enableOSXFullscreen(Window window) {
		if ("Mac OS X".equals(System.getProperty("os.name"))) {
			if (window != null) {
				try {
					final Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
					util.getMethod("setWindowCanFullScreen", Window.class, Boolean.TYPE).invoke(util, window, true);
				} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates and returns a new panel (a chart panel or a html panel).
	 * 
	 * @param elem
	 *            the description of the panel to be created.
	 * @param gui
	 *            the parent Gui.
	 * @return the new panel.
	 */
	private static Component getNewPanel(final Element elem, final Gui gui) {
		Component result = null;
		try {
			if (elem.getNodeName().equals("empty")) {
				result = new EmptyPanel();
			} else if (elem.getNodeName().equals("chart")) {
				throw new RuntimeException("Not yet implemented");
				/*final JamelChartPanel chartPanel = ChartFactory.createChartPanel(elem, dataManager);
				chartPanels.put(chartPanel, tabbedPaneName);
				result = chartPanel;*/
			} else if (elem.getNodeName().equals("html")) {
				result = new HtmlPanel(elem, gui);
			} else {
				throw new RuntimeException("Not yet implemented: " + elem.getNodeName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Not yet implemented", e);
			/*e.printStackTrace();
			result = HtmlPanel
					.getErrorPanel("Error:<br />" + e.toString() + "<br />See jamel.log file for more details.");*/
		}
		return result;
	}

	/**
	 * The control panel.
	 */
	final private ControlPanel controlPanel;

	/**
	 * The tabbedPane.
	 */
	private final JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * The window.
	 */
	final private JFrame window = new JFrame();

	/**
	 * The list of the panels (charts and html).
	 */
	final private List<Component> panels = new LinkedList<>();

	/**
	 * Creates a new basic gui.
	 * 
	 * @param element
	 *            an XML element that specifies the gui to be created.
	 * @param parentFile
	 *            the file where the Gui was described.
	 * @param simulation
	 *            the parent simulation?
	 */
	public BasicGui(final Element element, final File parentFile, final Simulation simulation) {

		super(simulation);

		if (!element.getNodeName().equals("gui")) {
			throw new RuntimeException("Bad element: " + element.getNodeName());
		}

		this.controlPanel = new ControlPanel(simulation);

		{

			// Cette méthode devrait être commentée.

			final NodeList panelNodeList = element.getElementsByTagName("panel");
			for (int i = 0; i < panelNodeList.getLength(); i++) {
				if (panelNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
					throw new RuntimeException("This node should be a panel node.");
				}
				final Element panelElement = (Element) panelNodeList.item(i);
				if (!panelElement.getAttribute("visible").equals("false")) {
					final JPanel tabPanel = new JPanel();
					tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.X_AXIS));
					tabPanel.setBackground(tabPanelBackgroundColor);
					tabPanel.setName(panelElement.getAttribute("title"));
					final NodeList nodeList = panelElement.getChildNodes();
					JPanel col = new JPanel();
					col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
					tabPanel.add(col);
					for (int j = 0; j < nodeList.getLength(); j++) {
						final Node node = nodeList.item(j);
						if (node.getNodeName().equals("col")) {
							col = new JPanel();
							col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
							tabPanel.add(col);
						} else if (node.getNodeType() == Node.ELEMENT_NODE) {
							Component subPanel = null;
							try {
								subPanel = getNewPanel((Element) nodeList.item(j), this);
							} catch (Exception e) {
								throw new RuntimeException("Not yet implemented", e);
								/*e.printStackTrace();
								subPanel = HtmlPanel.getErrorPanel(
										"Error:<br />" + e.toString() + "<br />See jamel.log file for more details.");*/
							}
							if (subPanel != null) {
								this.panels.add(subPanel);
								col.add(subPanel);
							}
						}
					}
					this.tabbedPane.add(tabPanel);
				}
			}
		}

		// Setting the window.

		enableOSXFullscreen(this.window);
		this.window.setMinimumSize(new Dimension(400, 200));
		this.window.setPreferredSize(new Dimension(800, 400));
		this.window.pack();
		this.window.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.window.getContentPane().add(this.tabbedPane);
		this.window.getContentPane().add(this.controlPanel, "South");
		this.controlPanel.repaint();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);
		final String dateStr = simpleDateFormat.format(new Date());
		this.window.setTitle(simulation.getName() + " (" + dateStr + ")");
		this.window.setVisible(true);
		this.update();

	}

	@Override
	public File getFile() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void update() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BasicGui.this.controlPanel.update();
			}
		});

		for (final Component panel : this.panels) {
			if (panel instanceof Updatable) {
				((Updatable) panel).update();
			}
		}

	}

}
