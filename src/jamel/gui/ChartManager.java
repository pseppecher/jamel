package jamel.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingUtilities;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
// import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import jamel.Jamel;
import jamel.data.Expression;
import jamel.data.ExpressionFactory;
import jamel.util.JamelObject;
import jamel.util.Parameters;

/**
 * The chart manager.
 * 
 * TODO / WORK IN PROGRESS
 */
public class ChartManager extends JamelObject {

	/**
	 * The font used for axis labels.
	 */
	private static final Font axisLabelFont = new Font("Tahoma", Font.PLAIN, 12);

	/**
	 * A basic stroke used for legend items.
	 */
	private static final BasicStroke basicStroke = new BasicStroke();
	// REMARQUE : Je ne comprends pas pourquoi c'est nécessaire.

	/**
	 * The shape of the line used by legends.
	 */
	private static final Shape line = new Line2D.Double(0, 0, 10, 0);

	/**
	 * The font used for tick labels.
	 */
	private static final Font tickLabelFont = new Font("Tahoma", Font.PLAIN, 10);

	/**
	 * Returns a new formatted string by adding spaces around math operators.
	 * 
	 * @param string
	 *            the string to be formatted.
	 * @return the formatted string.
	 */
	private static String addSpaces(String string) {
		return string.replaceAll(",", ", ").replaceAll("/", " / ").replaceAll("\\*", " * ").replaceAll("-", " - ")
				.replaceAll("\\+", " + ");
	}

	/**
	 * Creates and returns a new Y axis.
	 * 
	 * @param axisDescription
	 *            an XML element with the description of the axis.
	 * @param source
	 *            the tick unit source.
	 * @return a new Y axis.
	 */
	private static NumberAxis createAxis(Parameters axisDescription, TickUnitSource source) {
		final JamelAxis axis;
		axis = new JamelAxis(null);

		/*final NumberAxis axis;
		
		
		if (description != null && description.getAttribute("logarithmic").equals("true")) {
			axis = new LogarithmicAxis(null);
		} else {
			axis = new NumberAxis(null);
			axis.setAutoTickUnitSelection(true);
			if (source != null) {
				axis.setStandardTickUnits(source);
			}
		axis.setAutoRangeIncludesZero(false);
		}*/

		if (axisDescription != null && !axisDescription.getAttribute("label").equals("")) {
			axis.setLabel(axisDescription.getAttribute("label"));
		}

		if (axisDescription != null && !axisDescription.getAttribute("integerUnit").isEmpty()) {
			axis.setIntegerUnit(Boolean.parseBoolean(axisDescription.getAttribute("integerUnit")));
		}

		axis.setTickLabelFont(tickLabelFont);
		if (axisDescription != null) {
			final Double max = axisDescription.getDoubleAttribute("max");
			if (max != null) {
				axis.setUpperBound(max);
			}
			final Double min = axisDescription.getDoubleAttribute("min");
			if (min != null) {
				/*if (max == null && min == 0) {
					axis.setAutoRangeIncludesZero(true);
				} else*/ {
					axis.setLowerBound(min);
				}
			}
		}
		axis.setNumberFormatOverride(NumberFormat.getInstance(Locale.US));
		return axis;
	}

	/**
	 * Creates and returns a new plot with the specified dataset, axes and
	 * renderer.
	 * 
	 * @param dataset
	 *            the dataset (<code>null</code> permitted).
	 * @param xAxis
	 *            the x axis (<code>null</code> permitted).
	 * @param yAxis
	 *            the y axis (<code>null</code> permitted).
	 * @param renderer
	 *            the renderer (<code>null</code> permitted).
	 * @return a new plot.
	 */
	private static XYPlot createXYPlot(XYDataset dataset, NumberAxis xAxis, NumberAxis yAxis, XYItemRenderer renderer) {
		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setDomainGridlinesVisible(false);
		plot.setDomainMinorGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setRangeMinorGridlinesVisible(false);
		plot.setRangeCrosshairVisible(false);
		plot.setDomainCrosshairVisible(false);
		plot.setBackgroundPaint(Color.white);
		plot.getRangeAxis().setLabelFont(axisLabelFont);
		plot.getDomainAxis().setLabelFont(axisLabelFont);
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		return plot;
	}

	/**
	 * Returns the specified series.
	 * 
	 * @param x
	 *            the x data.
	 * @param y
	 *            the y data.
	 * @param conditions
	 *            the conditions.
	 * @param expressionFactory
	 *            the expression factory.
	 * @return the specified series.
	 */
	static private StandardDynamicXYSeries getSeries(String x, String y, String conditions,
			ExpressionFactory expressionFactory) {
		StandardDynamicXYSeries newSeries = null;
		try {
			final Expression xExp = expressionFactory.getExpression(x);
			final Expression yExp = expressionFactory.getExpression(y);
			if (conditions == null) {
				newSeries = new StandardDynamicXYSeries(xExp, yExp);
			} else {
				newSeries = new StandardDynamicXYSeries(xExp, yExp, parseConditions(conditions, expressionFactory));
			}
		} catch (final Exception e) {
			Jamel.println("Failure with this series: " + x, y, conditions);
			Jamel.errorMessage("Warning", "Something went wrong while creating a series.");
			e.printStackTrace();
		}
		return newSeries;
	}

	private static Expression[] parseConditions(String conditions, ExpressionFactory expressionFactory) {
		final String[] strings;
		final Expression[] result;
		if (conditions == null) {
			result = null;
		} else {
			strings = ExpressionFactory.split(conditions);
			result = new Expression[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = expressionFactory.getExpression(strings[i]);
			}
		}
		return result;
	}

	/**
	 * The list of the series to update.
	 */
	final private List<DynamicXYSeries> dynamicSeries = new LinkedList<>();

	/**
	 * The expression factory.
	 */
	final private ExpressionFactory expressionFactory;

	/**
	 * Creates a new chart manager.
	 * 
	 * @param gui
	 *            the parent gui.
	 * @param expressionFactory
	 *            the expression factory.
	 */
	public ChartManager(final Gui gui, final ExpressionFactory expressionFactory) {
		super(gui.getSimulation());
		this.expressionFactory = expressionFactory;
	}

	/**
	 * Creates and returns a new XY chart.
	 * 
	 * @param params
	 *            the description of the XY chart to create.
	 * @return a new XY chart.
	 */
	public JamelChart getNewXYChart(final Parameters params) {
		final String name = params.getAttribute("title");
		final String defaultConditions;
		if (params.get("if") != null) {
			defaultConditions = params.get("if").getCompactText();
		} else {
			defaultConditions = null;
		}
		final List<Parameters> seriesList = params.getAll("series");
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		int k = 0;
		for (final Parameters seriesElement : seriesList) {
			final String x = seriesElement.get("x").getCompactText();
			final String y = seriesElement.get("y").getCompactText();
			final String conditions;
			if (seriesElement.get("if") != null) {
				conditions = seriesElement.get("if").getCompactText();
			} else {
				conditions = defaultConditions;
			}
			final DynamicXYSeries newSeries;
			if (seriesElement.hasAttribute("scatter") && seriesElement.getAttribute("scatter").equals("true")) {
				final String sector = seriesElement.getAttribute("sector");
				final String selection = seriesElement.hasAttribute("select") ? seriesElement.getAttribute("select")
						: null;
				newSeries = this.getSector(sector).getScatterSeries(x, y,
						parseConditions(conditions, expressionFactory), selection);
			} else {
				newSeries = getSeries(x, y, conditions, expressionFactory);
			}
			if (newSeries != null) {
				this.dynamicSeries.add(newSeries);
				dataset.addSeries(newSeries);

				// Color

				final Paint seriesPaint;
				{
					final String color = seriesElement.getAttribute("color");
					if (color == "") {
						seriesPaint = drawingSupplier.getNextPaint();
					} else {
						seriesPaint = JamelColor.getColor(color);
					}
				}

				// Shapes ?

				final Shape shape = renderer.getBaseShape();
				// final Shape lineshape = renderer.getline();

				if (seriesElement.getAttribute("shapesVisible").equals("true")) {
					renderer.setSeriesShapesVisible(k, true);
					renderer.setUseFillPaint(true);
					renderer.setSeriesFillPaint(k, seriesPaint);
				} else {
					renderer.setSeriesShapesVisible(k, false);
				}

				// Line color

				if (seriesElement.hasAttribute("lineColor")) {
					final Color lineColor = JamelColor.getColor(seriesElement.getAttribute("lineColor"));
					renderer.setSeriesPaint(k, lineColor);
				} else {
					renderer.setSeriesPaint(k, seriesPaint);
				}

				// Line visibility

				renderer.setSeriesLinesVisible(k, (!seriesElement.getAttribute("linesVisible").equals("false")));

				// Preparing default legend:

				final String legendLabel;
				final String tooltip;
				if (seriesElement.getAttribute("label").equals("")) {
					legendLabel = ((XYSeries) newSeries).getDescription();
					tooltip = null;
				} else {
					legendLabel = seriesElement.getAttribute("label");
					tooltip = "<html>x = " + addSpaces(x) + "<br>y = " + addSpaces(y) + "</html>";
				}

				final LegendItem legendItem = new LegendItem(legendLabel, "description", tooltip, null, false, shape,
						true, seriesPaint, false, seriesPaint, basicStroke, true, line, basicStroke, seriesPaint);

				defaultLegendItemCollection.add(legendItem);
			}
			k++;
		}
		final NumberAxis xAxis = createAxis(params.get("xAxis"), null);
		final NumberAxis yAxis = createAxis(params.get("yAxis"), null);

		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		// TODO addZeroBaselines(plot, description);

		final Parameters legend = params.get("legend");
		if (legend != null) {
			final List<Parameters> items = legend.getAll("item");

			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			for (@SuppressWarnings("unused")
			final Parameters item : items) {
				// TODO work in progress
				// final Element item = (Element) items.item(i);
				// final String legendLabel = item.getAttribute("value");
				// final Paint paint =
				// JamelColor.getColor(item.getAttribute("color"));
				// final String tooltip = item.getAttribute("tooltip");
				/*TODO final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true,
						paint, true, lineColor, basicStroke, false, null, basicStroke, null);
				try {
					legendItemCollection.add(legendItem);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					// ???
				}*/
			}
			plot.setFixedLegendItems(legendItemCollection);
		} else {
			plot.setFixedLegendItems(defaultLegendItemCollection);
		}

		final String background = params.getAttribute("background");
		if (!background.isEmpty()) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		// TODO addMarkers(plot, description.getElementsByTagName("marker"));

		final JamelChart result = new JamelChart(name, plot, getSimulation()) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				Jamel.notYetImplemented();
				// Does nothing. TODO implement
			}

		};

		final Parameters info = params.get("info");
		if (info != null) {
			final TextTitle title = result.getTitle();
			title.setToolTipText(info.getText());
		}
		return result;
	}

	/**
	 * Refreshes each registred dynamic series.
	 */
	public void refresh() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					for (final DynamicXYSeries series : dynamicSeries) {
						series.update(true);
					}
				}

			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
