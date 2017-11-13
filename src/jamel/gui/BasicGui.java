package jamel.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jamel.Jamel;
import jamel.data.ExpressionFactory;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Simulation;

/**
 * A basic graphical user interface.
 */
public class BasicGui extends JamelObject implements Gui {

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
	 * The chart manager.
	 */
	final private ChartManager chartManager;

	/**
	 * The control panel.
	 */
	final private ControlPanel controlPanel;

	/**
	 * The file where the Gui was described.
	 */
	final private File sourceFile;

	/**
	 * The tabbedPane.
	 */
	private final JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * The window.
	 */
	final private JFrame window = new JFrame();

	/**
	 * Creates a new basic gui.
	 * 
	 * @param param
	 *            the description of the gui to be created.
	 * @param sourceFile
	 *            the file where the gui was described.
	 * @param simulation
	 *            the parent simulation.
	 * @param expressionFactory
	 *            the expression factory.
	 */
	public BasicGui(final Parameters param, final File sourceFile, final Simulation simulation,
			final ExpressionFactory expressionFactory) {

		super(simulation);

		this.chartManager = new ChartManager(this, expressionFactory);

		this.sourceFile = sourceFile;

		if (!param.getName().equals("gui")) {
			throw new RuntimeException("Bad element: " + param.getName());
		}

		this.controlPanel = new ControlPanel(simulation);

		{

			// Ce bloc devrait être commenté.

			final List<Parameters> panelList = param.getAll("panel");
			for (Parameters panelParam : panelList) {
				if (!panelParam.getAttribute("visible").equals("false")) {
					final JPanel tabPanel = new JPanel();
					tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.X_AXIS));
					tabPanel.setBackground(null);
					tabPanel.setName(panelParam.getAttribute("title"));
					final List<Parameters> nodeList = panelParam.getAll();
					JPanel col = new JPanel();
					col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
					tabPanel.add(col);
					for (Parameters node : nodeList) {
						if (node.getName().equals("col")) {
							col = new JPanel();
							col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
							tabPanel.add(col);
						} else {
							// 2017-11-12
							final Component subPanel = this.chartManager.getNewPanel(node);
							// final Component subPanel = getNewPanel(node,
							// this);
							// this.panels.add(subPanel);
							col.add(subPanel);
						}
					}
					this.tabbedPane.add(tabPanel);
					/*
					this.tabbedPane.setToolTipTextAt(this.tabbedPane.getTabCount() - 1,
							"<html>Some additional informations about the <i>" + tabPanel.getName()
									+ "</i> panel</html>");
					*/
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
		final String simulationTitle = simulation.getInfo("meta-title");
		final String windowTitle;
		if (simulationTitle != null) {
			windowTitle = simulationTitle + " (" + simulation.getName() + ")";
		} else {
			windowTitle = simulation.getName();
		}
		this.window.setTitle(windowTitle);
		this.window.setVisible(true);

	}

	@Override
	public void displayErrorMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(this.window, "<html>" + message + "<br>See the console for more details.</html>",
				title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void displayErrorMessage(final String title, final String message, final Exception e) {
		JOptionPane.showMessageDialog(this.window,
				"<html>Jamel said: " + message + "<br>" + "Cause: "
						+ (e.getMessage() == null ? e.getClass().getName() : e.getMessage()) + "<br>"
						+ "See the console for more details." + "</html>",
				title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void doEvent(Parameters event) {
		if (event.getName().equals("gui.exportCharts") || event.getAttribute("action").equals("exportCharts")) {
			this.chartManager.exportCharts(event);
		} else {
			Jamel.notYetImplemented("Unknown or not yet implemented event: '" + event.getName() + "', '"
					+ event.getAttribute("action") + "'");
		}
	}

	@Override
	public File getFile() {
		return this.sourceFile;
	}

	@Override
	public void refresh() {
		this.controlPanel.refresh();
		this.chartManager.refresh();
	}

}
