package jamel.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jamel.Jamel;
import jamel.util.Parameters;
import jamel.util.Simulation;

/**
 * The control panel.
 */
public class ControlPanel extends JPanel {

	/** The pause button. */
	private final JButton pauseButton;

	/** The export button. */
	private JButton exportButton = null;

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
	 * The export icon.
	 */
	private ImageIcon exportIcon = null;

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
		if (this.simulation.eventMethodImplemented()) {
			try {
				this.exportIcon = getIcon("resources/save_edit.gif");
				this.exportButton = this.getExportButton(getExportParameters());
				this.add(exportButton);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the export parameters.
	 * 
	 * @return the export parameters.
	 * @throws ParserConfigurationException
	 *             if something goes wrong.
	 * @throws SAXException
	 *             if something goes wrong.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	private static Parameters getExportParameters() throws ParserConfigurationException, SAXException, IOException {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final URL url = cl.getResource("resources/exportToPDF.xml");
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final Document doc = db.parse(url.openStream());
		final Element elem = doc.getDocumentElement();
		return new Parameters(elem);
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
			Jamel.println("Control Panel: Missing icon: " + name);
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
	 * Creates and returns a new export button.
	 * 
	 * @param parameters
	 *            the parameters of the export.
	 * 
	 * @return a new export button.
	 */
	private JButton getExportButton(Parameters parameters) {
		return new JButton("Export") {
			{
				this.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						simulation.event(parameters);
					}
				});
				if (exportIcon != null) {
					this.setIcon(exportIcon);
					this.setText("");
					this.setToolTipText("Export panels");
				}
			}
		};
	}

	/**
	 * Updates this panel.
	 */
	private void doRefresh() {
		this.timeCounter.setText("" + this.simulation.getPeriod());
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
			if (this.exportButton != null) {
				this.exportButton.setEnabled(b);
			}
		}
		this.repaint();
	}

	/**
	 * Updates this panel.
	 */
	public void refresh() {
		if (SwingUtilities.isEventDispatchThread()) {
			doRefresh();
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					doRefresh();
				}

			});
		}
	}

}
