package jamel.basic.gui;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.ValueMarker;

/**
 * A convenient extension of ChartPanel.
 * 
 * @author pascal
 */
public class JamelChartPanel extends ChartPanel {

	// 2016-05-01: simplification
	// La différence entre les time chart et les autres est évacuée
	// L'ajout des marker est délégué aux impléméntations de JamelChart

	/** background */
	private static final Color background = JamelColor.getColor("background");

	/**
	 * Constructs a panel that displays the specified chart.
	 * 
	 * @param chart
	 *            the chart to be displayed.
	 */
	public JamelChartPanel(JamelChart chart) {
		super(chart);
		this.setBackground(background);
	}

	/**
	 * Adds a marker to the chart.
	 * 
	 * @param marker
	 *            the marker to be added (<code>null</code> not permitted).
	 */
	public void addMarker(ValueMarker marker) {
		// 2016-05-01
		((JamelChart) this.getChart()).addTimeMarker(marker);
	}

}

// ***
