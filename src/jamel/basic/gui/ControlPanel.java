package jamel.basic.gui;

import jamel.basic.Circuit;
import jamel.basic.util.Timer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * The control panel.
 */
public class ControlPanel extends JPanel {

	/** The macro-economic circuit. */
	private final Circuit circuit;

	/** The context class loader. */
	private final ClassLoader cl = Thread.currentThread().getContextClassLoader();

	/** The message panel. */
	private final JPanel messagePanel = new JPanel();

	/** The pause button. */
	private final JButton pauseButton;


	/** The play button. */
	private final JButton playButton;


	/** The time counter. */
	private final TimeCounter timeCounter;

	/** The warning icon. */
	private final Icon warningIcon;

	/**
	 * Creates a new control panel.
	 * @param circuit the macro-economic circuit.
	 */
	public ControlPanel(Circuit circuit) {
		this.circuit = circuit;
		this.pauseButton = this.getPauseButton();
		this.playButton = this.getPlayButton();
		final Timer timer = this.circuit.getTimer();
		this.timeCounter = new TimeCounter(0);
		timer.addListener(this.timeCounter);

		this.setLayout(new GridLayout(0,3));
		final JPanel left = new JPanel();
		final JPanel central = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		central.add(playButton);
		central.add(pauseButton);
		central.add(timeCounter);
		final URL warningImage = cl.getResource("resources/warning.gif");
		if (warningImage!=null) {
			warningIcon = new ImageIcon(warningImage);
		}
		else {
			warningIcon = null;
		}
		this.add(left);
		this.add(central);
		this.add(messagePanel);
	}

	/**
	 * Creates and returns a new pause button.
	 * @return a new pause button.
	 */
	private JButton getPauseButton() {
		return new JButton("Pause") {{
			this.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) {
					circuit.pause(true);
					playButton.setFocusable(true);
					playButton.requestFocusInWindow();
					setFocusable(false);
					ControlPanel.this.repaint();
				}
			}) ;
			final URL url = cl.getResource("resources/suspend_co.gif");
			if (url!=null) {
				this.setIcon(new ImageIcon(url));
				this.setText("");
			}
			this.setToolTipText("Pause "+circuit.getName()) ;
			this.setEnabled(false);			
		}};		
	}

	/**
	 * Creates and returns a new pause button.
	 * @return a new pause button.
	 */
	private JButton getPlayButton() {
		return new JButton("Run") {{
			this.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(ActionEvent e) { 
					circuit.pause(false);
					pauseButton.setFocusable(true);
					pauseButton.requestFocusInWindow();
					setFocusable(false);
					ControlPanel.this.repaint();
				} 
			}) ;
			final URL url = cl.getResource("resources/resume_co.gif");
			if (url!=null) {
				this.setIcon(new ImageIcon(url));
				this.setText("");
			}
			this.setToolTipText("Run "+circuit.getName()) ;
			this.setEnabled(false);
		}};
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
				pauseButton.setSelected(false) ;
				playButton.setEnabled(b) ;
				playButton.setSelected(false) ;
			}			
		}
		super.repaint();
	}

	/**
	 * Displays a warning message.
	 * @param message the message to display.
	 * @param toolTipText the string to display in a tool tip.
	 */
	public void warning(String message, String toolTipText) {
		final JLabel label;
		if (warningIcon!=null) {
			label = new JLabel(message, warningIcon,SwingConstants.CENTER); 
		}
		else {
			label = new JLabel("Warning",SwingConstants.CENTER); 					
		}
		label.setToolTipText(toolTipText);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				messagePanel.removeAll();
				messagePanel.add(label,BorderLayout.WEST);
				messagePanel.validate();
			}
		});			

	}

}

// ***
