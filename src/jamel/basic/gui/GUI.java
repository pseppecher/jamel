package jamel.basic.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

import jamel.basic.Circuit;
import jamel.basic.data.BasicDataManager;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

/**
 * The graphical user interface.
 */
public class GUI {

	/**
	 * The Jamel window.
	 */
	private final class JamelWindow extends JFrame {

		/** The control panel. */
		private final Component controlPanel;

		/** The tabbedPane. */
		private final JTabbedPane tabbedPane = new JTabbedPane();

		{
			this.setMinimumSize(new Dimension(400, 200));
			this.setPreferredSize(new Dimension(800, 400));
			this.pack();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		/**
		 * Creates a new window.
		 * 
		 * @param title
		 *            the title to be displayed in the frame's border.
		 * @param controlPanel
		 *            the control panel.
		 */
		private JamelWindow(String title, Component controlPanel) {
			super();
			this.getContentPane().add(tabbedPane);
			this.controlPanel = controlPanel;
			this.getContentPane().add(controlPanel, "South");
			controlPanel.repaint();
			this.setTitle(title);
			this.setVisible(true);
		}

		/**
		 * Adds a component to the tabbed pane.
		 * 
		 * @param component
		 *            the component to be added.
		 */
		private void addPanel(Component component) {
			this.tabbedPane.add(component);
		}

		/**
		 * Adds a component at the specified tab index.
		 * 
		 * @param component
		 *            the component to be added.
		 * @param index
		 *            the position to insert this new tab.
		 */
		private void addPanel(Component component, int index) {
			this.tabbedPane.add(component, index);
		}

		/**
		 * Gets the number of components in the tabbed panel.
		 * 
		 * @return the number of components in the tabbed panel.
		 */
		public int getTabCount() {
			return this.tabbedPane.getComponentCount();
		}

	}

	/**
	 * Initializes and returns a new chart manager.
	 * 
	 * @param dataManager
	 *            the parent data manager.
	 * @param timer2
	 *            the timer.
	 * @param settings
	 *            a XML element with the settings.
	 * @param path
	 *            the path of the scenario file.
	 * @return a new chart manager.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static ChartManager getNewChartManager(final BasicDataManager dataManager, Timer timer2, Element settings,
			String path) throws InitializationException {
		ChartManager chartManager = null;
		final String fileName = settings.getAttribute("chartsConfigFile");
		if (fileName != null) {
			final File file = new File(path + "/" + fileName);
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
				chartManager = new BasicChartManager(root, dataManager, timer2);
			} catch (Exception e) {
				throw new InitializationException(
						"Something goes wrong while parsing this file: " + file.getAbsolutePath(), e);
			}
		}
		return chartManager;
	}

	/**
	 * The chart manager.
	 */
	private final ChartManager chartManager;

	/** The window. */
	private final JamelWindow window;

	/**
	 * Creates a new graphical user interface.
	 * 
	 * @param name
	 *            the name of the interface.
	 * @param circuit
	 *            the circuit.
	 * @param path
	 *            the path to the scenario file.
	 * @param settings
	 *            the settings.
	 * @param timer
	 *            the timer.
	 * @param dataManager
	 *            the data manager.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	public GUI(String name, Circuit circuit, BasicDataManager dataManager, Timer timer, Element settings, String path)
			throws InitializationException {
		this.window = new JamelWindow(name, new ControlPanel(circuit));
		this.chartManager = getNewChartManager(dataManager, timer, settings, path);
		addPanel(chartManager.getPanelList());
	}

	/**
	 * Adds the specified components to the tabbed pane of the window.
	 * 
	 * @param panels
	 *            the components to be added.
	 */
	private void addPanel(final Component[] panels) {
		if (panels == null) {
			throw new RuntimeException("Panels is null");
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (Component panel : panels) {
					window.addPanel(panel);
				}
			}
		});
	}

	/**
	 * Add a marker to all time charts.
	 * @param label the label of the marker to add.
	 * @param period the value of the marker (the current period). 
	 */
	public void addMarker(String label, int period) {
		this.chartManager.addMarker(label, period);
	}

	/**
	 * Adds a component to the tabbed pane of the window.
	 * 
	 * @param panel
	 *            the panel to be added.
	 */
	public void addPanel(final Component panel) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.addPanel(panel);
			}
		});
	}

	/**
	 * Adds a component at the specified tab index.
	 * 
	 * @param panel
	 *            the component to be added.
	 * @param index
	 *            the position to insert this new tab.
	 */
	public void addPanel(final Component panel, final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.addPanel(panel, index);
			}
		});
	}

	/**
	 * Gets the number of components in the tabbed panel.
	 * 
	 * @return the number of components in the tabbed panel.
	 */
	public int getTabCount() {
		return this.window.getTabCount();
	}

	/**
	 * Repaints the control panel.
	 */
	public void repaintControls() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.controlPanel.repaint();
			}
		});
	}

	/**
	 * Repaints the components of the chart manager.
	 */
	public void update() {
		this.chartManager.update();
	}

}

// ***
