package jamel.gui;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;

/**
 * A convenient extension of JFreeChart. 
 */
public abstract class JamelChart extends JFreeChart implements Updatable {

	// 2016-05-01: classe créée pour faciliter l'ajout de markers.
	
	// 2017-05-05: implements Updatable

	/** A transparent color used for chart background. TODO devrait être une Jamel Color !*/
	private static final Color colorTransparent = new Color(0, 0, 0, 0);

	/** The font for displaying the legend items. */
	private static final Font legendItemFont = new Font("Monaco", Font.PLAIN, 10);

	/** The font for displaying the chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/**
	 * Creates a new chart based on the supplied plot.
	 * 
	 * @param title
	 *            the chart title (<code>null</code> permitted).
	 * 
	 * @param plot
	 *            the plot (null not permitted).
	 */
	public JamelChart(String title, Plot plot) {
		super(title, titleFont, plot, true);
		this.setBackgroundPaint(colorTransparent);
		this.getLegend().setItemFont(legendItemFont);
	}

	/**
	 * Adds a marker to the chart.
	 * 
	 * @param marker
	 *            the marker to be added (<code>null</code> not permitted).
	 */
	public abstract void addTimeMarker(ValueMarker marker);

}

// ***
