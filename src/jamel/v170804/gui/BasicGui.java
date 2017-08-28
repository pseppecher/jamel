package jamel.v170804.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import jamel.Jamel;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Simulation;

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
	 * @param params
	 *            the description of the panel to be created.
	 * @param gui
	 *            the parent Gui.
	 * @return the new panel.
	 */
	private static Component getNewPanel(final Parameters params, final Gui gui) {
		final Component result;
		try {
			if (params.getName().equals("empty")) {
				result = new EmptyPanel();
			} else if (params.getName().equals("chart")) {
				JamelChartPanel chartPanel = null;
				try {
					chartPanel = JamelChartFactory.createChartPanel(params, gui.getSimulation());
				} catch (final Exception e) {
					e.printStackTrace();
				}
				if (chartPanel != null) {
					result = chartPanel;
				} else {
					result = new EmptyPanel();
					// TODO il vaudrait mieux un HtmlPanel avec un message
					// d'erreur.
				}
			} else if (params.getName().equals("html")) {
				result = new HtmlPanel(params.getElem(), gui);
			} else {
				throw new RuntimeException("Not yet implemented: " + params.getName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Not yet implemented", e);
		}
		return result;
	}

	/**
	 * The control panel.
	 */
	final private ControlPanel controlPanel;

	/**
	 * The list of the panels (charts and html).
	 */
	final private List<Component> panels = new LinkedList<>();

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
	 *            the file where the Gui was described.
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicGui(final Parameters param, final File sourceFile, final Simulation simulation) {

		super(simulation);

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
					tabPanel.setBackground(tabPanelBackgroundColor);
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
							final Component subPanel = getNewPanel(node, this);
							this.panels.add(subPanel);
							col.add(subPanel);
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

	/**
	 * Exports all the charts as pdf files.
	 * 
	 * @param event
	 *            the parameters of the export.
	 */
	private void exportCharts(final Parameters event) {
		final File parent = this.getSimulation().getFile().getParentFile();
		final String exportDirectoryName;
		if (event.getAttribute("to").isEmpty()) {
			exportDirectoryName = "";
		} else {
			exportDirectoryName = event.getAttribute("to") + "/";
		}
		final Parameters chartDescription = event.get("format");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				private void ensureParentFileExist(final File file) {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
				}

				@Override
				public void run() {
					for (Component comp : panels) {
						final String tabName = comp.getParent().getParent().getName().replaceAll(" ", "_");
						if (comp instanceof JamelChartPanel) {
							final String filename = exportDirectoryName + tabName + "/"
									+ ((JamelChartPanel) comp).getChart().getTitle().getText().replaceAll(" ", "_")
									+ ".pdf";
							final File pdfFile = new File(parent, filename);
							ensureParentFileExist(pdfFile);
							((JamelChartPanel) comp).export(pdfFile, chartDescription);
						} else if (comp instanceof HtmlPanel) {
							final String filename = exportDirectoryName + tabName + "/" + "panel.html";
							// TODO: revoir le nommage de ce fichier
							final File htmlFile = new File(parent, filename);
							ensureParentFileExist(htmlFile);
							((HtmlPanel) comp).export(htmlFile);
						}
					}
				}

			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void displayErrorMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(this.window, "<html>" + message + "<br>See the console for more details.</html>",
				title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void doEvent(Parameters event) {
		if (event.getName().equals("gui.exportCharts")) {
			this.exportCharts(event);
		} else {
			Jamel.notYetImplemented(event.getName());
		}
	}

	@Override
	public File getFile() {
		return this.sourceFile;
	}

	@Override
	public void open() {
		super.open();
	}

	@Override
	public void refresh() {
		controlPanel.refresh();
		if (!SwingUtilities.isEventDispatchThread()) {
			for (final Component panel : this.panels) {
				if (panel instanceof Updatable) {
					((Updatable) panel).update();
				}
			}
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						for (final Component panel : panels) {
							if (panel instanceof Updatable) {
								((Updatable) panel).update();
							}
						}
					}

				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
