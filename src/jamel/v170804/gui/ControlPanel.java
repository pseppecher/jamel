package jamel.v170804.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import jamel.util.Simulation;

/**
 * The control panel.
 */
public class ControlPanel extends JPanel {

	/** The pause button. */
	private final JButton pauseButton;

	/** The simulation. */
	private final Simulation simulation;

	/** The time counter. */
	private final JTextField timeCounter = new JTextField(5);

	/**
	 * The suspend icon.
	 */
	private final ImageIcon suspendIcon;

	/**
	 * The resume icon.
	 */
	private final ImageIcon resumeIcon;

	/**
	 * Creates a new control panel.
	 * 
	 * @param simulation
	 *            the simulation.
	 */
	public ControlPanel(Simulation simulation) {
		this.simulation = simulation;

		this.suspendIcon = getIcon("resources/suspend_co.gif");
		this.resumeIcon = getIcon("resources/resume_co.gif");

		this.pauseButton = this.getPauseButton();
		this.timeCounter.setHorizontalAlignment(SwingConstants.RIGHT);
		this.timeCounter.setEditable(false);
		this.timeCounter.setFocusable(false);
		this.timeCounter.setText("");
		this.add(pauseButton);
		this.add(timeCounter);
	}

	/**
	 * Creates and returns an ImageIcon from the specified resource.
	 * 
	 * @param name
	 *            the resource name.
	 * @return an ImageIcon from the specified URL.
	 */
	private static ImageIcon getIcon(String name) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final URL url = cl.getResource(name);
		final ImageIcon result;
		if (url != null) {
			result = new ImageIcon(url);
		} else {
			result = null;
		}
		return result;
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
						simulation.pause();
						ControlPanel.this.refresh();
					}
				});
				if (suspendIcon != null) {
					this.setIcon(suspendIcon);
					this.setText("");
					this.setToolTipText("Pause ");
				}
			}
		};
	}

	/**
	 * Updates this panel.
	 */
	public void refresh() {
		this.timeCounter.setText("" + this.simulation.getPeriod());
		if (simulation != null) {
			final boolean b = simulation.isPaused();
			if (pauseButton != null) {
				if (b) {
					if (resumeIcon != null) {
						pauseButton.setIcon(resumeIcon);
						pauseButton.setText("");
						pauseButton.setToolTipText("Resume");
					} else {
						pauseButton.setText("Resume");
					}
				} else {
					if (suspendIcon != null) {
						pauseButton.setIcon(suspendIcon);
						pauseButton.setText("");
						pauseButton.setToolTipText("Pause");
					} else {
						pauseButton.setText("Pause");
					}
				}
				// pauseButton.setEnabled(!b);
				// pauseButton.setIcon(myIcon);
				// playButton.setEnabled(b);
			}
		}
		this.repaint();
	}

}

// ***
