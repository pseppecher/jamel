package jamel.basic.gui;

import jamel.util.Circuit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 
 * A graph panel. Extends ChartPanel.
 */
@SuppressWarnings("serial")
public class GraphPanel extends ChartPanel {

	/**
	 * Enumerates the colors.
	 */
	private static enum JamelColor {

		/** black */
		black(Color.black),

		/** blue */
		blue(Color.blue),

		/** cyan */
		cyan(Color.cyan),

		/** gray */
		gray(Color.gray),

		/** green */
		green(Color.green),

		/** magenta */
		magenta(Color.magenta),

		/** orange */
		orange(Color.orange),

		/** red */
		red(Color.red),

		/** white */
		white(Color.white),

		/** yellow */
		yellow(Color.yellow);

		/**
		 * Returns the specified color.
		 * @param name the name of the color to return.
		 * @return a color.
		 */
		public static Color get(String name) {
			return valueOf(name).color;
		}

		/** The color. */
		private final Color color;

		/**
		 * Creates a color.
		 * @param color the color to create.
		 */
		private JamelColor(Color color){
			this.color=color;
		}

		/**
		 * Returns the color.
		 * @return the color.
		 */
		@SuppressWarnings("unused")
		private Color get() {
			return this.color;
		}

	}

	/**
	 * A static class to stores String constants.
	 */
	private static final class KEY {

		/** The "getData" message. */
		private static final String GET_SERIES = "getSeries";

	}

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
	 * Returns the array of paints for the specified chart.
	 * @param circuit the circuit.
	 * @param key the key of the chart.
	 * @param chartTitle the title of the chart.
	 * @return an array of paints.
	 */
	private static Paint[] getChartColors(Circuit circuit,String key, String chartTitle) {
		final String param2 = circuit.getParameter(key,chartTitle,"colors");
		final Paint[] colors;
		if (param2!=null){
			final String[] colorKeys = param2.split(",");
			colors = new Paint[colorKeys.length];
			for (int count = 0; count<colors.length ; count++){
				colors[count] = JamelColor.get(colorKeys[count]);
			}
		}
		else {
			colors = null;
		}
		return colors;
	}

	/**
	 * Returns the data for the specified chart.
	 * @param circuit the circuit.
	 * @param dataKeys an array of strings representing the name of the series.
	 * @return an XYSeriesCollection.
	 */
	private static XYSeriesCollection getChartData(Circuit circuit, String[] dataKeys) {
		final XYSeriesCollection data = new XYSeriesCollection();
		for (String key:dataKeys){
			final XYSeries series = (XYSeries) circuit.forward(KEY.GET_SERIES,"XYSeries",key);
			if (series!=null) {
				data.addSeries(series);
			}
			else {
				throw new RuntimeException(key+" XYSeries not found.");
			}
		}
		return data;
	}

	/**
	 * Creates a panel with its chart.
	 * @param circuit the circuit.
	 * @param key the key of the panel. 
	 * @param title the title of the chart.
	 */
	public GraphPanel(Circuit circuit, String key, String title) {
		super(null);
		this.setBackground(new Color(229,229,229));
		final XYSeriesCollection data;
		final String xLabel;
		final String yLabel;
		final String[] dataKeys = circuit.getParameterArray(key,title,"series");
		if (dataKeys.length>0){
			if ("scatter".equals(circuit.getParameter(key,title,"type"))) {
				xLabel = dataKeys[0];
				yLabel = dataKeys[1];
				final String[] dataKeys2 = {xLabel+"&&"+yLabel};
				data = getChartData(circuit, dataKeys2);							
			}
			else {
				xLabel = null;
				yLabel = null;
				data = getChartData(circuit, dataKeys);							
			}
		}
		else {
			throw new RuntimeException("No series found for the chart <"+key+"."+title+">");
		}			
		this.setChart(createChart(title, xLabel, yLabel, data, getChartColors(circuit, key, title)));
	}

}

// ***
