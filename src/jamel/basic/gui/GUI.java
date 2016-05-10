package jamel.basic.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import jamel.Jamel;
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
	private final class JamelWindow extends JFrame implements KeyListener {

		/** The control panel. */
		private final Component controlPanel;

		/** The tabbedPane. */
		private final JTabbedPane tabbedPane = new JTabbedPane();

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
			if ("Mac OS X".equals(osName)) {
				enableOSXFullscreen(this);
			}
			this.setMinimumSize(new Dimension(400, 200));
			this.setPreferredSize(new Dimension(800, 400));
			this.pack();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

		@Override
		public void keyTyped(KeyEvent e) {
			Jamel.println("Hello");			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

	}

	/**
	 * Marks a window as able to be full screened.
	 * 
	 * @param window
	 *            the window to be marked.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void enableOSXFullscreen(Window window) {
		if (window != null) {
			try {
				Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
				Class params[] = new Class[] { Window.class, Boolean.TYPE };
				Method method = util.getMethod("setWindowCanFullScreen", params);
				method.invoke(util, window, true);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * The chart manager.
	 */
	private final ChartManager chartManager;

	/** The window. */
	private final JamelWindow window;

	/**
	 * The os name.
	 */
	private final String osName;

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
		this.osName = System.getProperty("os.name");
		this.window = new JamelWindow(name, new ControlPanel(circuit));
		final String fileName = settings.getAttribute("chartsConfigFile");
		if (!"".equals(fileName)) {
			final File file = new File(path + "/" + fileName);
			chartManager = new BasicChartManager(file, dataManager, timer);
			final Component[] panels = chartManager.getPanelList();
			if (panels == null) {
				throw new RuntimeException("Panels is null");
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (Component panel : panels) {
						if (panel != null) {
							window.addPanel(panel);
						}
					}
				}
			});
		} else {
			chartManager = null;
			throw new InitializationException("Missing attribute: chartsConfigFile");
		}
	}

	/**
	 * Add a marker to all time charts.
	 * 
	 * @param label
	 *            the label of the marker to add.
	 * @param period
	 *            the value of the marker (the current period).
	 */
	public void addMarker(String label, int period) {
		this.chartManager.addMarker(label, period);
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
