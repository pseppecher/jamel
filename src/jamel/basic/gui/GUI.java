package jamel.basic.gui;

import jamel.util.Circuit;
import jamel.util.Sector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

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

		/** The tabbedPane. */
		private final JTabbedPane tabbedPane = new JTabbedPane() ;

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
			this.getContentPane().add(tabbedPane);
			this.controlPanel = getControlPanel(GUI.this.circuit);
			this.getContentPane().add(controlPanel,"South");
			controlPanel.repaint();
			this.setTitle(GUI.this.circuit.getParameter(KEY.CIRCUIT,KEY.FILE_NAME));			
			this.setVisible(true);
		}

		/**
		 * Adds a component to the tabbed pane.
		 * @param component the component to be added.
		 */
		private void addPanel(Component component) {
			this.tabbedPane.add(component);
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

		/** The "pause" message. */
		private static final String PAUSE = "pause";

		/** The "unpause" message. */
		private static final String UNPAUSE = "unpause";

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
					@Override
					public void actionPerformed(ActionEvent e) {
						circuit.forward(KEY.PAUSE);
						repaint();
					} 
				}) ;
				this.playButton.addActionListener(new ActionListener() { 
					@Override
					public void actionPerformed(ActionEvent e) { 
						circuit.forward(KEY.UNPAUSE);
						repaint();
					} 
				}) ;
				this.playButton.setToolTipText("Run Simulation") ;
				this.playButton.setEnabled(false);
				this.add(pauseButton);
				this.add(playButton);
				this.add(Circuit.getTimeCounter());
			}

			/**
			 * Updates the pause/run buttons.
			 */
			@Override
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
		throw new IllegalArgumentException("Unknown phase <"+phase+">");
	}

	@Override
	public Object forward(String message, final Object ... args) {
		
		if (message==null) {
			throw new IllegalArgumentException("The request is null");
		}

		if ("addPanel".equals(message)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.addPanel((Component) args[0]);
				}
			});			
		}
		
		else {
			throw new RuntimeException("Unknown request: "+message);
		}
		
		return null;
	}

	/**
	 * Returns the name of the GUI.
	 * @return the name of the GUI.
	 */
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void pause() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.pause();		
			}
		});			
	}

}

// ***
