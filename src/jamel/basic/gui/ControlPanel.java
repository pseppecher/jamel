package jamel.basic.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import jamel.basic.Circuit;
import jamel.basic.util.Timer;

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

	/**
	 * Creates a new control panel.
	 * 
	 * @param circuit
	 *            the macro-economic circuit.
	 */
	public ControlPanel(Circuit circuit) {
		this.circuit = circuit;
		this.pauseButton = this.getPauseButton();
		this.playButton = this.getPlayButton();
		final Timer timer = this.circuit.getTimer();
		this.timeCounter = new TimeCounter(0);
		timer.addListener(this.timeCounter);

		this.setLayout(new GridLayout(0, 3));
		final JPanel left = new JPanel();
		final JPanel central = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		central.add(playButton);
		central.add(pauseButton);
		central.add(timeCounter);
		this.add(left);
		this.add(central);
		this.add(messagePanel);
	}

	/**
	 * Creates and returns a new pause button.
	 * 
	 * @return a new pause button.
	 */
	private JButton getPauseButton() {
		return new JButton("Pause") {
			{
				this.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						circuit.setPause(true);
						ControlPanel.this.repaint();
					}
				});
				final URL url = cl.getResource("resources/suspend_co.gif");
				if (url != null) {
					this.setIcon(new ImageIcon(url));
					this.setText("");
				}
				this.setToolTipText("Pause " + circuit.getName());
				// this.setEnabled(false);
			}
		};
	}

	/**
	 * Creates and returns a new run button.
	 * 
	 * @return a new run button.
	 */
	private JButton getPlayButton() {
		return new JButton("Run") {
			{
				this.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						circuit.setPause(false);
						ControlPanel.this.repaint();
					}
				});
				final URL url = cl.getResource("resources/resume_co.gif");
				if (url != null) {
					this.setIcon(new ImageIcon(url));
					this.setText("");
				}
				this.setToolTipText("Run " + circuit.getName());
				this.setEnabled(false);
			}
		};
	}

	/**
	 * Updates the pause/run buttons.
	 */
	@Override
	public void repaint() {
		if (circuit != null) {
			final boolean b = circuit.isPaused();
			if (pauseButton != null) {
				pauseButton.setEnabled(!b);
				pauseButton.setFocusable(!b);
				pauseButton.setSelected(false);
				playButton.setEnabled(b);
				playButton.setFocusable(b);
				playButton.setSelected(false);
				if (b) {
					playButton.requestFocusInWindow();
				} else {
					pauseButton.requestFocusInWindow();
				}
				ControlPanel.this.setFocusable(false);
			}
		}
		super.repaint();
	}

}

// ***
