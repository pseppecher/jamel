package jamel.basic.gui;

import jamel.basic.util.JamelParameters.Param;
import jamel.util.Circuit;
import jamel.util.FileParser;
import jamel.util.Sector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

/**
 *  The graphical user interface.
 */
public class GUI implements Sector {

	/**
	 * Enumerates the colors.
	 */
	private static enum JamelColor {

		/** black */
		black(Color.black),

		/** blue */
		blue(Color.blue),

		/** cyan */
		cyan(Color.cyan),

		/** gray */
		gray(Color.gray),

		/** green */
		green(Color.green),

		/** magenta */
		magenta(Color.magenta),

		/** orange */
		orange(Color.orange),

		/** red */
		red(Color.red),

		/** white */
		white(Color.white),

		/** yellow */
		yellow(Color.yellow);

		/**
		 * Returns the specified color.
		 * @param name the name of the color to return.
		 * @return a color.
		 */
		private static Color get(String name) {
			return valueOf(name).color;
		}

		/**
		 * Returns the array of paints.
		 * @param colorKeys the keys of the paints to return.
		 * @return an array of paints.
		 */
		private static Paint[] getColors(String... colorKeys) {
			final Paint[] colors;
			if (colorKeys.length>0){
				colors = new Paint[colorKeys.length];
				for (int count = 0; count<colors.length ; count++){
					colors[count] = JamelColor.get(colorKeys[count]);
				}
			}
			else {
				colors = null;
			}
			return colors;
		}

		/** The color. */
		private final Color color;

		/**
		 * Creates a color.
		 * @param color the color to create.
		 */
		private JamelColor(Color color){
			this.color=color;
		}

		/**
		 * Returns the color.
		 * @return the color.
		 */
		@SuppressWarnings("unused")
		private Color get() {
			return this.color;
		}

	}

	/**
	 * The Jamel window.
	 */
	@SuppressWarnings("serial")
	private final class JamelWindow extends JFrame {
		
		/** A map thaht contains the description of the chart panels. */
		private final Map<String, String> chartDescription;

		/** The control panel. */
		private final Component controlPanel;
		
		/** The list of the timeChartPanel. */
		private final List<TimeChartPanel> timeChartPanelList = new ArrayList<TimeChartPanel>(45);

		{
			this.setMinimumSize(new Dimension(400,200));
			this.setPreferredSize(new Dimension(800,400));
			this.pack();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		}

		/**
		 * Creates a new window.
		 */
		private JamelWindow() {
			super();
			this.chartDescription = getChartDescription();
			final JTabbedPane tabbedPane = new JTabbedPane() ;
			for(JPanel chartPanel: getChartPanelList()){
				tabbedPane.add(chartPanel);
			};
			for(Component component: getOtherPanelList(circuit,name)){
				tabbedPane.add(component);
			};
			tabbedPane.add(getParametersPanel(circuit));
			tabbedPane.add(getInfoPanel());
			tabbedPane.setSelectedIndex(0);
			this.getContentPane().add(tabbedPane);
			this.controlPanel = getControlPanel(GUI.this.circuit);
			this.getContentPane().add(controlPanel,"South");
			controlPanel.repaint();
			this.setTitle(GUI.this.circuit.getParameter(KEY.CIRCUIT,KEY.FILE_NAME));			
			this.setVisible(true);
		}

		/**
		 * Add a marker to all time charts.
		 * @param label the label of the marker to add.
		 */
		private void addMarker(String label) {
			final ValueMarker marker = new ValueMarker(Circuit.getCurrentPeriod().getValue()) ;
			marker.setLabel(label);
			marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			marker.setOutlinePaint(Color.WHITE);
			for (TimeChartPanel panel:this.timeChartPanelList) {
				panel.addMarker(marker);
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
				final XYSeries series = (XYSeries) circuit.forward(KEY.GET_SERIES,"XYSeries",key);
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
		 * Returns a map that contains the description of the chart panels. 
		 * @return a map that contains the description of the chart panels.
		 */
		private Map<String,String> getChartDescription() {
			final String fileName = circuit.getParameter(name,"chart panels description");
			final Map<String,String> map;
			try {
				map = FileParser.parse(fileName);
				return map;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				new RuntimeException("Chart panels description not found.");
			}
			return null;
		}

		/**
		 * Returns the list of the chart panels.
		 * @return an array of panels.
		 */
		private JPanel[] getChartPanelList() {
			final String value = chartDescription.get("panels");
			final JPanel[] result;
			if (value!=null){
				final String[] panelTitles = FileParser.toArray(value);
				result = new JPanel[panelTitles.length];
				int index = 0;
				for (String panelTitle:panelTitles) {
					final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
					panel.setBackground(new Color(0,0,0,0));
					panel.setName(panelTitle);
					final String chartList = chartDescription.get(panelTitle+".list");
					if (chartList!=null){
						final String[] titles = FileParser.toArray(chartList);
						for (String title:titles) {
							final String truc = chartDescription.get(panelTitle+"."+title+".series");
							if (truc==null) {
								throw new RuntimeException(panelTitle+"."+title+".series: not found.");
							}
							final XYSeriesCollection data = getChartData(FileParser.toArray(truc));
							final String colors = chartDescription.get(panelTitle+"."+title+".colors");
							final Paint[] paints;
							if (colors!=null) {
								paints = JamelColor.getColors(FileParser.toArray(colors));
							}
							else {
								paints = null;
							}
							final TimeChartPanel newPanel = new TimeChartPanel(title,data,paints);
							panel.add(newPanel);
							timeChartPanelList.add(newPanel);
						}
						if (titles.length<9) {
							for (int i=titles.length; i<9; i++) {
								panel.add(emptyPanel());						
							}
						}
					}
					result[index] = panel;
					index++;
				}
			}
			else {
				result=new JPanel[0];
			}
			return result;
		}

		/**
		 * Updates the control panel.
		 */
		private void pause() {
			this.controlPanel.repaint();
		}

	}

	/**
	 * A static class to stores String constants.
	 */
	private static final class KEY {

		/** The "Circuit" keyword. */
		private static final String CIRCUIT = "Circuit";

		/** the "fileName" keyword. */
		private static final String FILE_NAME = "fileName";

		/** The "getData" message. */
		private static final String GET_SERIES = "getSeries";

		/** The title of the info tab. */
		private static final String INFO = "Info";

		/** The "panel" keyword. */
		private static final String OTHER_PANELS = "otherPanels";

		/** The title of the parameters tab. */
		@SuppressWarnings("unused")
		private static final String PARAMETERS = "Parameters";

		/** The "pause" message. */
		private static final String PAUSE = "pause";

		/** The "unpause" message. */
		private static final String UNPAUSE = "unpause";

	}

	/**
	 * Returns an empty panel.
	 * @return an empty panel.
	 */
	private static Component emptyPanel() {
		return new JPanel(){
			private static final long serialVersionUID = 1L;
			{
				this.setBackground(new Color(230,230,230));
			}
		};
	}

	/**
	 * Returns the control panel.
	 * @param circuit the circuit.
	 * @return a component.
	 */
	@SuppressWarnings("serial")
	private static Component getControlPanel(final Circuit circuit) {
		return new JPanel() {

			/** The pause button. */
			private final JButton pauseButton = new JButton("Pause") {{
				this.setToolTipText("Pause Simulation") ;
				this.setEnabled(false);			
			}};

			/** The play button. */
			private final JButton playButton = new JButton("Run") {{
				this.setToolTipText("Pause Simulation") ;
				this.setEnabled(false);
			}};

			{
				this.pauseButton.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) {
						circuit.forward(KEY.PAUSE);
						repaint();
					} 
				}) ;
				this.playButton.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						circuit.forward(KEY.UNPAUSE);
						repaint();
					} 
				}) ;
				this.playButton.setToolTipText("Run Simulation") ;
				this.playButton.setEnabled(false);
				this.add(pauseButton);
				this.add(playButton);
			}

			/**
			 * Updates the pause/run buttons.
			 */
			public void repaint() {
				if (circuit!=null) {
					final boolean b = circuit.isPaused();
					if (pauseButton!=null) {
						pauseButton.setEnabled(!b) ;
						pauseButton.setSelected(b) ;
						playButton.setEnabled(b) ;
						playButton.setSelected(!b) ;
					}
				}
				super.repaint();
			}

		};
	}

	/**
	 * Returns a component containing some informations about Jamel.
	 * @return a component.
	 */
	private static Component getInfoPanel() {
		final Component jEditorPane = new JEditorPane() {
			private static final long serialVersionUID = 1L;
			{
				final String infoString = readFile("info.html");
				this.setContentType("text/html");
				this.setText("<center>"+infoString+"</center>");
				this.setEditable(false);
				this.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
							try {
								java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
							} catch (Exception ex) {
								ex.printStackTrace();
							}	        
					}
				});
			}};
			final JScrollPane pane = new JScrollPane(jEditorPane);
			pane.setName(KEY.INFO);
			return pane ;
	}

	/**
	 * Returns an array with the panels to add to the window.
	 * @param circuit the circuit.
	 * @param name the name of the GUI.
	 * @return an array of component.
	 */
	private static Component[] getOtherPanelList(Circuit circuit,String name) {
		final String[] panelTitles = circuit.getParameterArray(name,KEY.OTHER_PANELS);
		final Component[] result;
		if (panelTitles!=null){
			result = new Component[panelTitles.length];
			int index = 0;
			for (String panelTitle:panelTitles) {
				final Component panel = (Component) circuit.forward(panelTitle);
				result[index] = panel;
				index++;
			}
		}
		else {
			result=new Component[0];
		}
		return result;
	}

	/**
	 * Returns the parameters panel.
	 * @param circuit the circuit.
	 * @return a component.
	 */
	@SuppressWarnings("serial")
	private static Component getParametersPanel(Circuit circuit) {
		final JTree jTree = (JTree) circuit.forward("getJTreeViewOfParameters");
		return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT){{
			this.setBorder(null);
			this.setDividerSize(5);
			final JTextArea fieldDetailsTextArea = new JTextArea() {{
				this.setEditable(false);
				this.setBackground(Color.WHITE);
			}};
			final JPanel rightPanel = new JPanel() {{
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				this.add(fieldDetailsTextArea);
				this.setBackground(Color.WHITE);
			}};
			final JPanel leftPanel = new JPanel() {{
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				jTree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
						if (node != null) {
							final Param nodeInfo = (Param) node.getUserObject();
							fieldDetailsTextArea.setText(nodeInfo.getValue());
						}
					}
				});
				this.add(jTree);
				this.setBackground(Color.white);
			}};
			this.setName("Parameters");
			this.setResizeWeight(0.3);
			this.setLeftComponent(new JScrollPane(leftPanel));
			this.setRightComponent(new JScrollPane(rightPanel));
		}};
	}

	/**
	 * Reads the specified file in resource and returns its contain as a String.
	 * @param filename  the name of the resource file.
	 * @return a String.
	 */
	private static String readFile(String filename) {
		BufferedReader br=new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/"+filename)));
		String line;		
		String string="";
		final String rc = System.getProperty("line.separator");
		try {
			while ((line=br.readLine())!=null){
				string+=line+rc;
			}
			br.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

	/** The circuit. */
	private final Circuit circuit;

	/** The name of the GUI. */
	private final String name;

	/** The window. */
	private final JamelWindow window;

	/**
	 * Creates a new graphical user interface.
	 * @param name the name of the interface.
	 * @param circuit the circuit.
	 */
	public GUI(String name, Circuit circuit) {
		this.name = name;
		this.circuit = circuit;
		this.window = new JamelWindow();
	}

	@Override
	public boolean doPhase(String phase) {
		throw new RuntimeException("The GUI has no phase.");
	}

	@Override
	public Object forward(String message, Object ... args) {
		if (message.equals("marker")) {
			this.window.addMarker((String) args[0]);
		}
		return null;
	}

	/**
	 * Returns the name of the GUI.
	 * @return the name of the GUI.
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public void pause() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				window.pause();		
			}
		});			
	}

}

// ***
