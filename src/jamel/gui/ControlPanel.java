package jamel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The control panel.
 */
public class ControlPanel extends JPanel {

	/** The context class loader. */
	private final ClassLoader cl = Thread.currentThread().getContextClassLoader();

	/** The pause button. */
	private final JButton pauseButton;

	/** The play button. */
	private final JButton playButton;

	/** The simulation. */
	private final Simulation simulation;

	/** The time counter. */
	private final JTextField timeCounter = new JTextField(5);

	/**
	 * Creates a new control panel.
	 * 
	 * @param simulation
	 *            the simulation.
	 */
	public ControlPanel(Simulation simulation) {
		this.simulation = simulation;
		this.pauseButton = this.getPauseButton();
		this.playButton = this.getPlayButton();
		this.timeCounter.setHorizontalAlignment(SwingConstants.RIGHT);
		this.timeCounter.setEditable(false);
		this.timeCounter.setText("");
		this.setLayout(new GridLayout(0, 3));
		final JPanel left = new JPanel();
		final JPanel central = new JPanel();
		central.add(playButton);
		central.add(pauseButton);
		central.add(timeCounter);
		this.add(left);
		this.add(central);
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
						simulation.setPause(true);
						playButton.requestFocusInWindow();
						// ControlPanel.this.repaint();
					}
				});
				final URL url = cl.getResource("resources/suspend_co.gif");
				if (url != null) {
					this.setIcon(new ImageIcon(url));
					this.setText("");
				}
				this.setToolTipText("Pause " + simulation.getName());
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
						simulation.setPause(false);
						pauseButton.requestFocusInWindow();
						ControlPanel.this.update();
						// ControlPanel.this.repaint();
					}
				});
				final URL url = cl.getResource("resources/resume_co.gif");
				if (url != null) {
					this.setIcon(new ImageIcon(url));
					this.setText("");
				}
				this.setToolTipText("Run " + simulation.getName());
				this.setEnabled(false);
			}
		};
	}

	/**
	 * Updates this panel.
	 */
	public void update() {
		this.timeCounter.setText("" + this.simulation.getPeriod());
		if (simulation != null) {
			final boolean b = simulation.isPaused();
			if (pauseButton != null) {
				pauseButton.setEnabled(!b);
				playButton.setEnabled(b);
			}
		}
		this.repaint();
	}

}

// ***
