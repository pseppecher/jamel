package jamel.v170801.gui;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;

import jamel.util.Simulation;

/**
 * A convenient extension of JFreeChart.
 */
public abstract class JamelChart extends JFreeChart {

	/**
	 * A transparent color used for chart background. TODO devrait être une
	 * Jamel Color !
	 */
	private static final Color colorTransparent = new Color(0, 0, 0, 0);

	/** The font for displaying the legend items. */
	private static final Font legendItemFont = new Font("Monaco", Font.PLAIN, 10);

	/** The font for displaying the chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/**
	 * The simulation.
	 */
	private Simulation simulation;

	/**
	 * Creates a new chart based on the supplied plot.
	 * 
	 * @param title
	 *            the chart title (<code>null</code> permitted).
	 * @param plot
	 *            the plot (null not permitted).
	 * @param simulation
	 *            the simulation.
	 */
	public JamelChart(String title, Plot plot, Simulation simulation) {
		super(title, titleFont, plot, true);
		this.simulation = simulation;
		this.setBackgroundPaint(colorTransparent);
		this.getLegend().setItemFont(legendItemFont);
		this.setNotify(this.simulation.isPaused());
	}

	/**
	 * Adds a marker to the chart.
	 * 
	 * @param marker
	 *            the marker to be added (<code>null</code> not permitted).
	 */
	public abstract void addTimeMarker(ValueMarker marker);

	/**
	 * Updates the chart.
	 */
	public void update() {
		this.setNotify(true);
		if (!this.simulation.isPaused()) {
			this.setNotify(false);
		}

	}

}

// ***
