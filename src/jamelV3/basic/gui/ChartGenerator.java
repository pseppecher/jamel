package jamelV3.basic.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * The description of a chart. 
 */
public class ChartGenerator {

	/** A basic stroke used for legend items. */
	private static final BasicStroke basicStroke = new BasicStroke();

	/** A transparent color used for chart background. */
	private static final Color colorTransparent = new Color(0,0,0,0);

	/** The font used for legend items. */
	private static final Font legendItemFont = new Font("Monaco", Font.PLAIN, 10);

	/** A shape line used by time charts.*/
	private static final Shape line = new Line2D.Double(0, 0, 15, 0);

	/** A transparent gray paint used by scatter charts. */
	private static final Paint lineColor = new Color(0.3f,0.3f,0.3f,0.5f);

	/** A square shape used by scatter charts. */
	private static final Shape square = new Rectangle2D.Double(-3, -3, 6, 6);

	/** The font used for tick labels. */
	private static final Font tickLabelFont = new Font("Tahoma", Font.PLAIN, 10);

	/** The tick unit source. */
	private static final TickUnitSource tickUnitSource = NumberAxis.createIntegerTickUnits();

	/** The font used for chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/**
	 * Returns a LegendItemCollection.
	 * @param series the series.
	 * @param colors the colors.
	 * @param label the legend labels.
	 * @param isScatter a flag that indicates if the chart is scatter chart.
	 * @return a LegendItemCollection.
	 */
	private static LegendItemCollection getLegendItemCollection(String[] series, Paint[] colors, String[] label, boolean isScatter) {
		final LegendItemCollection legendItemCollection = new LegendItemCollection();
		for(int i=0; i<label.length;i++) {
			final String tooltip;
			final String legendLabel; 
			if (label[i]!=null) {
				legendLabel=label[i];
				tooltip = series[i];
			}
			else {
				legendLabel=series[i];
				tooltip = null;
			}
			final LegendItem legendItem;
			if (isScatter) {
				legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true, colors[i], true, lineColor, basicStroke, false, null, basicStroke, null);
			}
			else {
				legendItem = new LegendItem(legendLabel, null, tooltip, null, line, basicStroke, colors[i]);
			}
			try {
				legendItemCollection.add(legendItem);
			} catch (ArrayIndexOutOfBoundsException e) {}
		}
		return legendItemCollection;
	}

	/**
	 * Creates and returns a new chart with the given title and plot.
	 * 
	 * @param title  the chart title (<code>null</code> permitted).
	 * @param plot  controller of the visual representation of the data
	 *              (<code>null</code> not permitted).
	 * @return  a new chart.
	 */
	private static JFreeChart getNewChart(String title, XYPlot plot) {
		return new JFreeChart(title, titleFont, plot, true) {{
			this.setBackgroundPaint(colorTransparent);
			this.getLegend().setItemFont(legendItemFont);
		}};
	}

	/**
	 * Creates and returns a new renderer.
	 * @param dataset  the dataset.
	 * @param colors  the series colors.
	 * @param isScatter a flag that indicates if the chart is a scatter chart or not.
	 * @return a new renderer.
	 */
	private static XYItemRenderer getNewRenderer(final XYDataset dataset, final Paint[] colors, boolean isScatter) {
		final XYItemRenderer result;
		if (isScatter) {
			result=getNewScatterChartRenderer(dataset,colors);			
		}
		else {
			result=getNewTimeChartRenderer(dataset,colors);
		}
		return result;
	}

	/**
	 * Creates and returns a new renderer.
	 * @param dataset  the dataset.
	 * @param colors  the series colors.
	 * @return a new renderer.
	 */
	private static XYItemRenderer getNewScatterChartRenderer(final XYDataset dataset, final Paint[] colors) {
		return new XYLineAndShapeRenderer(false, true){{
			final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				this.setSeriesShape(i, square);
				this.setSeriesPaint(i,lineColor);
				this.setSeriesFillPaint(i, drawingSupplier.getNextPaint());
			}				
			this.setUseFillPaint(true);
			if (colors!=null) {
				for (int i = 0; i < colors.length; i++) {
					this.setSeriesFillPaint(i, colors[i]);
				}
			}
		}};
	}

	/**
	 * Creates and returns a new renderer.
	 * @param dataset  the dataset.
	 * @param colors  the series colors.
	 * @return a new renderer.
	 */
	private static XYItemRenderer getNewTimeChartRenderer(final XYDataset dataset, final Paint[] colors) {
		return new XYLineAndShapeRenderer(true, false){{
			final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
			for (int i = 0; i < dataset.getSeriesCount(); i++) {
				this.setSeriesPaint(i, drawingSupplier.getNextPaint());
			}				
			if (colors!=null) {
				for (int i = 0; i < colors.length; i++) {
					this.setSeriesPaint(i, colors[i]);
				}
			}
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
	private static XYPlot getNewXYPlot(XYDataset dataset, NumberAxis xAxis, NumberAxis yAxis, XYItemRenderer renderer) {
		return new XYPlot(dataset, xAxis, yAxis, renderer) {{
			this.setOrientation(PlotOrientation.VERTICAL);
			this.setDomainGridlinesVisible(false);
			this.setDomainMinorGridlinesVisible(false);
			this.setRangeGridlinesVisible(false);
			this.setRangeMinorGridlinesVisible(false);
			this.setRangeCrosshairVisible(false);
			this.setDomainCrosshairVisible(false);
			this.setBackgroundPaint(Color.WHITE);
		}};
	}

	/**
	 * Creates and returns a new X axis.
	 * @param label the axis label (<code>null</code> permitted).
	 * @param min the lower bound for the axis.
	 * @param max the upper bound for the axis.
	 * @param isScatter a flag that indicates if the chart is a scatter or not.
	 * @return a new X axis.
	 */
	private static NumberAxis getNewXAxis(String label, Double min, Double max, boolean isScatter) {
		NumberAxis axis = getTimeAxis(label);
		if (isScatter) {
			axis = getStandardAxis(label);
		}
		if (min!=null) {
			axis.setLowerBound(min);
		}
		if (max!=null) {
			axis.setUpperBound(max);
		}
		return axis;
	}

	/**
	 * Creates and returns a new Y axis.
	 * @param label the axis label (<code>null</code> permitted).
	 * @param min the lower bound for the axis.
	 * @param max the upper bound for the axis.
	 * @return a new Y axis.
	 */
	private static NumberAxis getNewYAxis(String label, Double min, Double max) {
		final NumberAxis axis = getStandardAxis(label);
		if (min!=null) {
			axis.setLowerBound(min);
		}
		if (max!=null) {
			axis.setUpperBound(max);
		}
		return axis;
	}

	/**
	 * Constructs a number axis, using default values where necessary.
	 *
	 * @param label  the axis label (<code>null</code> permitted).
	 * @return a number axis.
	 */
	private static NumberAxis getStandardAxis(String label) {
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
	private static NumberAxis getTimeAxis(String label) {
		return new NumberAxis(label) {{
			this.setAutoRangeIncludesZero(true);
			this.setStandardTickUnits(tickUnitSource);	
			this.setTickLabelFont(tickLabelFont);
		}};
	}

	/**
	 * Returns an array of Paint initialized to the colors represented by the specified strings.
	 * @param color an array of String representing the colors to be returned
	 * @return an array of Paint.
	 */
	private static Paint[] ParseColors(String ... color) {
		final Paint[] result = new Paint[color.length];
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		for(int i=0; i<color.length; i++) {
			if (color[i]!="") {
				try {
					result[i]=JamelColor.getColor(color[i]);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("Color not found: "+color[i]);
				}
			}
			else {
				result[i]=drawingSupplier.getNextPaint();
			}			
		}
		return result ;
	}

	/**
	 * Returns a new double initialized to the value represented by the specified String. 
	 * @param s the string to be parsed.
	 * @return the double value represented by the string argument.
	 */
	private static Double parseDouble(String s) {
		final Double result;
		if (s.isEmpty()) {
			result = null;			
		}
		else {
			result = Double.parseDouble(s);
		}
		return result;
	}

	/**
	 * Creates and returns a new chart.
	 * @param description a XML element that contains the description of the chart.
	 * @param dataset the dataset.
	 * @return the new chart.
	 */
	public static JFreeChart createChart(Element description, XYDataset dataset) {
		final ChartGenerator chartGenerator = new ChartGenerator(description);
		return chartGenerator.createChart(dataset);
	}	

	/** The colors. */
	final private String[] color;

	/** The legend labels. */
	final private String[] label;

	/** Chart name */
	final private String name;

	/** The options. */
	final private String options;

	/** The series keys. */
	final private String[] series;

	/** Max of the x Axis. */
	private Double xAxisMax = null;

	/** Min of the x Axis. */
	private Double xAxisMin = null;

	/** Max of the y Axis. */
	private Double yAxisMax = null;

	/** Min of the yAxis. */
	private Double yAxisMin = null;

	/**
	 * Creates an new ChartGenerator.
	 * @param description of the chart from an XML document.
	 */
	private ChartGenerator(Element description) {
		this.name=description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		this.series = new String[nbSeries];
		this.color = new String[nbSeries];
		this.label = new String[nbSeries];
		for (int k = 0; k<nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			series[k] = serieXML.getAttribute("value");
			color[k] = serieXML.getAttribute("color");
			label[k] = serieXML.getAttribute("label");
		}
		this.options = description.getAttribute("options");
		final Element yAxis = (Element) description.getElementsByTagName("yAxis").item(0);
		if (yAxis!=null) {
			this.yAxisMax = parseDouble(yAxis.getAttribute("max"));
			this.yAxisMin = parseDouble(yAxis.getAttribute("min"));
		}
		final Element xAxis = (Element) description.getElementsByTagName("xAxis").item(0);
		if (xAxis!=null) {
			this.xAxisMax = parseDouble(xAxis.getAttribute("max"));
			this.xAxisMin = parseDouble(xAxis.getAttribute("min"));
		}
	}

	/**
	 * Returns a new chart.
	 * @param dataset the dataset.
	 * @return a new chart.
	 */
	private JFreeChart createChart(XYDataset dataset) {
		final boolean isScatter = this.options.equals("scatter");
		final Paint[] colors = ParseColors(this.color);
		final XYPlot plot = getNewXYPlot(dataset, getNewXAxis(null,xAxisMin,xAxisMax,isScatter), getNewYAxis(null,yAxisMin,yAxisMax), getNewRenderer(dataset,colors ,isScatter));
		plot.setFixedLegendItems(getLegendItemCollection(this.series,colors,this.label,isScatter));		
		return getNewChart(name,plot);	
	}

}

// ***
