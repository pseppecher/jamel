package jamel.basic.gui;

import jamel.basic.util.JamelParameters.Param;
import jamel.util.Circuit;
import jamel.util.Sector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

/**
 *  The graphical user interface.
 */
public class GUI implements Sector {

	/**
	 * The Jamel window.
	 */
	@SuppressWarnings("serial")
	private final class JamelWindow extends JFrame {

		/** The control panel. */
		private final Component controlPanel;

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
			final JTabbedPane tabbedPane = new JTabbedPane() ;
			for(JPanel chartPanel: getChartPanelList(circuit,name)){
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

		/** The "chartPanel" keyword. */
		private static final String CHART_PANEL = "chartPanel";

		/** The "Circuit" keyword. */
		private static final String CIRCUIT = "Circuit";

		/** the "fileName" keyword. */
		private static final String FILE_NAME = "fileName";

		/** The title of the info tab. */
		private static final String INFO = "Info";

		/** The "panel" keyword. */
		private static final String PANEL = "panel";

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
	 * Returns the list of the chart panels.
	 * @param circuit the circuit.
	 * @param name the name of the GUI.
	 * @return an array of panels.
	 */
	private static JPanel[] getChartPanelList(Circuit circuit,String name) {
		final String value = circuit.getParameter(name,KEY.CHART_PANEL,"list");
		final JPanel[] result;
		if (value!=null){
			final String[] panelTitles = value.split(",");
			result = new JPanel[panelTitles.length];
			int index = 0;
			for (String panelTitle:panelTitles) {
				final JPanel panel = new JPanel(new GridLayout(3,3,10,10));
				panel.setBackground(new Color(0,0,0,0));
				panel.setName(panelTitle);
				final String[] chartList = circuit.getParameterArray(name,KEY.CHART_PANEL,panelTitle,"list");
				if (chartList!=null){
					for (String title:chartList) {
						panel.add(new GraphPanel(circuit, name+"."+KEY.CHART_PANEL+"."+panelTitle,title));							
					}
				}
				if (chartList.length<9) {
					for (int i=chartList.length; i<9; i++) {
						panel.add(emptyPanel());						
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
		final String[] panelTitles = circuit.getParameterArray(name,KEY.PANEL,"list");
		final Component[] result;
		if (panelTitles!=null){
			//final String[] panelTitles = value.split(",");
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
		// Does nothing (unused).
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
