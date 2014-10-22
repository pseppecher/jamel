package jamel.basic.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A graph panel. Extends ChartPanel.
 */
@SuppressWarnings("serial")
public class TimeChartPanel extends ChartPanel {

	/** A transparent color. */
	private static final Color colorTransparent = new Color(0,0,0,0);

	/** The font used for legend items. */
	private static final Font legendItemFont = new Font("Monaco", Font.PLAIN, 10);

	/** The font used for tick labels. */
	private static final Font tickLabelFont = new Font("Tahoma", Font.PLAIN, 10);

	/** The tick unit source. */
	private static final TickUnitSource tickUnitSource = NumberAxis.createIntegerTickUnits();

	/** The font used for chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);;

	/**
	 * Creates a chart.
	 * @param title the title.
	 * @param xAxisLabel the xLabel.
	 * @param yAxisLabel the yLabel.
	 * @param dataset the data	.
	 * @param colors the series colors.
	 * @return the new chart.
	 */
	private static JFreeChart createChart(String title, String xAxisLabel, String yAxisLabel, XYDataset dataset, final Paint[] colors) {
		final NumberAxis xAxis = new NumberAxis(xAxisLabel) {{
			this.setAutoRangeIncludesZero(true);
			this.setStandardTickUnits(tickUnitSource);	
			this.setTickLabelFont(tickLabelFont);
		}};
		final NumberAxis yAxis = new NumberAxis(yAxisLabel){{
			this.setAutoRangeIncludesZero(false);
			this.setTickLabelFont(tickLabelFont);			
		}};
		final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false){{
			if (colors!=null) {
				for (int i = 0; i < colors.length; i++) {
					this.setSeriesPaint(i, colors[i]);
				}
			}		    			
		}};
		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer) {{
			this.setOrientation(PlotOrientation.VERTICAL);
			this.setDomainGridlinesVisible(false);
			this.setDomainMinorGridlinesVisible(false);
			this.setRangeGridlinesVisible(false);
			this.setRangeMinorGridlinesVisible(false);
			this.setRangeCrosshairVisible(true);
			this.setDomainCrosshairVisible(true);
			this.setBackgroundPaint(Color.WHITE);
		}};
		final JFreeChart chart = new JFreeChart(title, titleFont, plot, true) {{
			this.setBackgroundPaint(colorTransparent);
			this.getLegend().setItemFont(legendItemFont);
		}};
		return chart;
	}

	/**
	 * Creates a panel with its chart.
	 * @param title the title of the chart.
	 * @param data the dataset.
	 * @param colors the colors.
	 */
	public TimeChartPanel(String title, XYSeriesCollection data, Paint[] colors) {
		super(null);
		this.setBackground(new Color(229,229,229));
		this.setChart(createChart(title, null, null, data, colors));
	}

	/**
	 * Adds a marker to the chart.
	 * @param marker the marker to be added.
	 */
	public void addMarker(ValueMarker marker) {
		this.getChart().getXYPlot().addDomainMarker(marker);
	}

}

// ***
