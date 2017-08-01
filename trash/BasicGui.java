package jamel.gui;

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
	 * @param elem
	 *            the description of the panel to be created.
	 * @param gui
	 *            the parent Gui.
	 * @return the new panel.
	 */
	private static Component getNewPanel(final Parameters elem, final Gui gui) {
		final Component result;
		try {
			if (elem.getName().equals("empty")) {
				result = new EmptyPanel();
			} else if (elem.getName().equals("chart")) {
				JamelChartPanel chartPanel = null;
				try {
					chartPanel = JamelChartFactory.createChartPanel(elem.getElem(), gui.getSimulation());
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
			} else if (elem.getName().equals("html")) {
				result = new HtmlPanel(elem.getElem(), gui);
			} else {
				throw new RuntimeException("Not yet implemented: " + elem.getName());
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
	 * The period of the latest refresh.
	 */
	private Integer latestRefresh = null;

	/**
	 * The list of the panels (charts and html).
	 */
	final private List<Component> panels = new LinkedList<>();

	/**
	 * The laps between to refreshs (in ms).
	 */
	final private int refresh;

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
	 * @param parentFile
	 *            the file where the Gui was described.
	 * @param simulation
	 *            the parent simulation.
	 */
	public BasicGui(final Parameters param, final File parentFile, final Simulation simulation) {

		super(simulation);
		
		if (!param.getName().equals("gui")) {
			throw new RuntimeException("Bad element: " + param.getName());
		}

		this.refresh = param.getIntAttribute("refresh");

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
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);
		final String dateStr = simpleDateFormat.format(new Date());
		this.window.setTitle(simulation.getName() + " (" + dateStr + ")");
		this.window.setVisible(true);
		// this.update();

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
				@Override
				public void run() {
					for (Component comp : panels) {
						if (comp instanceof JamelChartPanel) {
							final String tabName = comp.getParent().getParent().getName().replaceAll(" ", "_");
							final String filename = exportDirectoryName + tabName + "/"
									+ ((JamelChartPanel) comp).getChart().getTitle().getText().replaceAll(" ", "_")
									+ ".pdf";
							final File pdfFile = new File(parent, filename);
							if (!pdfFile.getParentFile().exists()) {
								pdfFile.getParentFile().mkdirs();
							}
							((JamelChartPanel) comp).writeAsPDF(pdfFile, chartDescription);
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
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BasicGui.this.controlPanel.refresh();
			}
		});
		for (final Component panel : this.panels) {
			if (panel instanceof Updatable) {
				((Updatable) panel).update();
			}
		}
		this.latestRefresh=this.getPeriod();
	}

	@Override
	public void open() {
		super.open();
	}

	@Override
	public void close() {
		final Integer present = this.getPeriod();
		if (present != null && (this.latestRefresh== null || present - this.latestRefresh >= this.refresh || this.getSimulation().isPaused())) {
			this.refresh();
		}
		super.close();
	}

}
