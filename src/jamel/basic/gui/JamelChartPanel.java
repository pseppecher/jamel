package jamel.basic.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A convenient extension of ChartPanel.
 */
@SuppressWarnings("serial")
public abstract class JamelChartPanel extends ChartPanel {

	/** background */
	protected static final Color background = new Color(229,229,229);

	/** basicStroke */
	protected final static BasicStroke basicStroke = new BasicStroke();
	
	/** A transparent color. */
	protected static final Color colorTransparent = new Color(0,0,0,0);

	/** The font used for legend items. */
	protected static final Font legendItemFont = new Font("Monaco", Font.PLAIN, 10);

	/** The font used for tick labels. */
	protected static final Font tickLabelFont = new Font("Tahoma", Font.PLAIN, 10);

	/** The tick unit source. */
	protected static final TickUnitSource tickUnitSource = NumberAxis.createIntegerTickUnits();;

	/** The font used for chart titles. */
	protected static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/**
     * Creates and returns a new chart with the given title and plot.
     * 
     * @param title  the chart title (<code>null</code> permitted).
     * @param plot  controller of the visual representation of the data
     *              (<code>null</code> not permitted).
	 * @return  a new chart.
	 */
	protected static JFreeChart getNewChart(String title, XYPlot plot) {
		return new JFreeChart(title, titleFont, plot, true) {{
			this.setBackgroundPaint(colorTransparent);
			this.getLegend().setItemFont(legendItemFont);
		}};
	}

	/**
     * Creates and returns a new plot with the specified dataset, axes and renderer.
     * 
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param xAxis  the x axis (<code>null</code> permitted).
     * @param yAxis  the y axis (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
	 * @return a new plot.
	 */
	protected static XYPlot getNewXYPlot(XYDataset dataset, NumberAxis xAxis, NumberAxis yAxis, XYItemRenderer renderer) {
		return new XYPlot(dataset, xAxis, yAxis, renderer) {{
			this.setOrientation(PlotOrientation.VERTICAL);
			this.setDomainGridlinesVisible(false);
			this.setDomainMinorGridlinesVisible(false);
			this.setRangeGridlinesVisible(false);
			this.setRangeMinorGridlinesVisible(false);
			this.setRangeCrosshairVisible(true);
			this.setDomainCrosshairVisible(true);
			this.setBackgroundPaint(Color.WHITE);
		}};
	}
	
    /**
     * Constructs a number axis, using default values where necessary.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @return a number axis.
     */
	protected static NumberAxis getStandardAxis(String label) {
		return new NumberAxis(label){{
			this.setAutoRangeIncludesZero(false);
			this.setTickLabelFont(tickLabelFont);			
		}};		
	}

    /**
     * Constructs a number axis, for time axis usage.
     *
     * @param label  the axis label (<code>null</code> permitted).
     * @return a number axis.
     */
	protected static NumberAxis getTimeAxis(String label) {
		return new NumberAxis(label) {{
			this.setAutoRangeIncludesZero(true);
			this.setStandardTickUnits(tickUnitSource);	
			this.setTickLabelFont(tickLabelFont);
		}};
	}
	
	/**
	 * Creates a panel with its chart.
	 * @param title the title of the chart.
	 * @param yMin the lower bound for the axis.
	 * @param yMax the upper bound for the axis.
	 * @param data the dataset.
	 * @param colors the colors.
	 * @param legend the legend.
	 */
	public JamelChartPanel(String title, Double yMin, Double yMax, XYSeriesCollection data,
			Paint[] colors, String[] legend) {
		super(null);
		this.setChart(createChart(title, yMin, yMax, null, null, data, colors,legend));
		this.setBackground(background);
	}

	/**
	 * Creates a chart.
	 * @param title the title.
	 * @param yMin the lower bound for the axis.
	 * @param yMax the upper bound for the axis.
	 * @param xAxisLabel the xLabel.
	 * @param yAxisLabel the yLabel.
	 * @param dataset the data set.
	 * @param colors the series colors.
	 * @param legend the legend.
	 * @return the new chart.
	 */
	protected JFreeChart createChart(String title, Double yMin, Double yMax, String xAxisLabel, String yAxisLabel, final XYDataset dataset, final Paint[] colors,String[] legend) {
		final XYPlot plot = getNewXYPlot(dataset, getNewXAxis(xAxisLabel), getNewYAxis(yAxisLabel,yMin,yMax), getNewRenderer(dataset,colors));
		final String[] tooltips = new String[dataset.getSeriesCount()];
		for(int i=0; i<dataset.getSeriesCount();i++) {
			tooltips[i] = (String) dataset.getSeriesKey(i);
		}
		setLegend(plot,legend,tooltips,colors);
		return getNewChart(title,plot);	
	}

	/**
     * Creates and returns a new legend item.
     * @param label the label (<code>null</code> not permitted).
	 * @param tooltipText the tooltip text. 
	 * @param color the item color.
	 * @return a new legend item.
	 */
	protected abstract LegendItem getNewLegendItem(String label, String tooltipText, Paint color) ;

	/**
     * Creates and returns a new renderer.
	 * @param dataset  the dataset.
	 * @param colors  the series colors.
	 * @return a new renderer.
	 */
	protected abstract XYItemRenderer getNewRenderer(XYDataset dataset, Paint[] colors);
	
	/**
	 * Creates and returns a new X axis.
	 * @param label the axis label (<code>null</code> permitted).
	 * @return a new X axis.
	 */
	protected abstract NumberAxis getNewXAxis(String label);
	
	/**
	 * Creates and returns a new Y axis.
	 * @param label the axis label (<code>null</code> permitted).
	 * @param min the lower bound for the axis.
	 * @param max the upper bound for the axis.
	 * @return a new Y axis.
	 */
	protected NumberAxis getNewYAxis(String label, Double min, Double max) {
		NumberAxis axis = getStandardAxis(label);
		if (min!=null) {
			axis.setLowerBound(min);
		}
		if (max!=null) {
			axis.setUpperBound(max);
		}
		return axis;
	}

	/**
     * Sets the fixed legend items for the specified plot.
	 * @param plot  the plot to be modified.
	 * @param legend  the legend item labels.
	 * @param tooltip the tooltip texts.
	 * @param colors  the series colors.
	 */
	protected void setLegend(XYPlot plot, String[] legend, String[] tooltip, Paint[] colors) {
		if (legend!=null) {
			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
			int id=0;
			for (String label:legend) {
				final Paint color;
				if (colors!=null) {
					color=colors[id];
				}
				else {
					color=drawingSupplier.getNextPaint();
				}
				try {
					legendItemCollection.add(getNewLegendItem(label,tooltip[id],color));
				} catch (ArrayIndexOutOfBoundsException e) {}
				id++;
			}
			plot.setFixedLegendItems(legendItemCollection);		
		}		
	}

}

// ***
