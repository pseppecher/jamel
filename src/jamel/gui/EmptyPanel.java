package jamel.gui;

import org.jfree.chart.ChartPanel;

/**
 * An empty panel.
 */
public class EmptyPanel extends ChartPanel {

	/**
	 * Creates an empty panel.
	 */
	public EmptyPanel() {
		super(null);
		this.setBackground(ColorParser.getColor("background"));
	}

}
