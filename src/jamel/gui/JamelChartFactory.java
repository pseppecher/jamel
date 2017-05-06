package jamel.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jamel.Simulation;
import jamel.data.Expression;

/**
 * A collection of utility methods for creating charts.
 * 
 * TODO / WORK IN PROGRESS
 */
public class JamelChartFactory {

	/**
	 * The font used for axis labels.
	 */
	private static final Font axisLabelFont = new Font("Tahoma", Font.PLAIN, 12);

	/**
	 * The font used for tick labels.
	 */
	private static final Font tickLabelFont = new Font("Tahoma", Font.PLAIN, 10);

	/**
	 * Creates and returns a new Y axis.
	 * 
	 * @param description
	 *            an XML element with the description of the axis.
	 * @param source
	 *            the tick unit source.
	 * @return a new Y axis.
	 */
	private static NumberAxis createAxis(Element description, TickUnitSource source) {
		final NumberAxis axis;
		if (description != null && description.getAttribute("logarithmic").equals("true")) {
			axis = new LogarithmicAxis(null);
		} else {
			axis = new NumberAxis(null);
			if (source != null) {
				axis.setStandardTickUnits(source);
			}
		}

		axis.setAutoRangeIncludesZero(false);

		if (description != null && !description.getAttribute("label").equals("")) {
			axis.setLabel(description.getAttribute("label"));
		}

		axis.setTickLabelFont(tickLabelFont);
		if (description != null) {
			final Double max = parseDouble(description.getAttribute("max"));
			if (max != null) {
				axis.setUpperBound(max);
			}
			final Double min = parseDouble(description.getAttribute("min"));
			if (min != null) {
				if (max == null && min == 0) {
					axis.setAutoRangeIncludesZero(true);
				} else {
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
		return new XYPlot(dataset, xAxis, yAxis, renderer) {
			{
				this.setOrientation(PlotOrientation.VERTICAL);
				this.setDomainGridlinesVisible(false);
				this.setDomainMinorGridlinesVisible(false);
				this.setRangeGridlinesVisible(false);
				this.setRangeMinorGridlinesVisible(false);
				this.setRangeCrosshairVisible(false);
				this.setDomainCrosshairVisible(false);
				this.setBackgroundPaint(Color.WHITE);
				this.getRangeAxis().setLabelFont(axisLabelFont);
				this.getDomainAxis().setLabelFont(axisLabelFont);
			}
		};
	}

	/**
	 * Creates and returns a new XY chart.
	 * 
	 * @param description
	 *            the description of the XY chart to create.
	 * @param simulation
	 *            the parent simulation.
	 * @return a new XY chart.
	 */
	private static JamelChart getNewXYChart(final Element description, final Simulation simulation) {
		final String name = description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		for (int k = 0; k < nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);

			final String x = getTagText("x", serieXML);
			final String y = getTagText("y", serieXML);

			// final String x = serieXML.getAttribute("x");
			// final String y = serieXML.getAttribute("y");
			final Integer start = null;// parseIntegerAttribute(serieXML,
										// "start");
			final Integer end = null;// parseIntegerAttribute(serieXML, "end");
			// TODO uses start, end, mod
			final Expression xExp = simulation.getExpression(x);
			final Expression yExp = simulation.getExpression(y);
			dataset.addSeries(new DynamicXYSeries(xExp, yExp));
			final String color = serieXML.getAttribute("color");
			renderer.setSeriesShape(k, null);
			final String shapesVisibles = serieXML.getAttribute("shapesVisibles");
			if (shapesVisibles.equals("false")) {
				renderer.setSeriesShapesVisible(k, false);
			}
			// renderer.setSeriesPaint(k, lineColor);
			final Paint paint;
			if (color.equals("")) {
				paint = drawingSupplier.getNextPaint();
			} else {
				paint = JamelColor.getColor(color);
			}
			renderer.setUseFillPaint(true);
			renderer.setSeriesFillPaint(k, paint);
			renderer.setSeriesPaint(k, paint);

			// Preparing default legend:
			final String legendLabel;
			final String tooltip;
			final String seriesKey = "x=" + x + ", y=" + y;
			if (serieXML.getAttribute("label").equals("")) {
				legendLabel = seriesKey;
				tooltip = null;
			} else {
				legendLabel = serieXML.getAttribute("label");
				tooltip = seriesKey;
			}
			/* TODO IMPLEMENT final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true, paint,
					true, lineColor, basicStroke, false, null, basicStroke, null);
			defaultLegendItemCollection.add(legendItem);*/

		}
		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0), null);
		final NumberAxis yAxis = createAxis((Element) description.getElementsByTagName("yAxis").item(0), null);

		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		// TODO addZeroBaselines(plot, description);

		final NodeList legends = description.getElementsByTagName("legend");
		if (legends.getLength() > 0) {
			Element legend = (Element) legends.item(0);
			final NodeList items = legend.getElementsByTagName("item");

			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			for (int i = 0; i < items.getLength(); i++) {
				final Element item = (Element) items.item(i);
				final String legendLabel = item.getAttribute("value");
				final Paint paint = JamelColor.getColor(item.getAttribute("color"));
				final String tooltip = item.getAttribute("tooltip");
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

		final String background = description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		// TODO addMarkers(plot, description.getElementsByTagName("marker"));

		final JamelChart result = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				// Does nothing. TODO implement
			}

			@Override
			public void update() {
				final List<?> list = dataset.getSeries();
				for (final Object series : list) {
					((DynamicXYSeries) series).update();
				}
			}

		};

		return result;
	}

	private static String getTagText(final String tagName, final Element element) {
		final String result;
		final Node node = element.getElementsByTagName(tagName).item(0);
		if (node == null) {
			result = null;
		} else {
			result = node.getTextContent();
		}
		return result;
	}

	/**
	 * Returns a new double initialized to the value represented by the
	 * specified String.
	 * 
	 * @param s
	 *            the string to be parsed.
	 * @return the double value represented by the string argument.
	 */
	private static Double parseDouble(String s) {
		final Double result;
		if (s.isEmpty()) {
			result = null;
		} else {
			result = Double.parseDouble(s);
		}
		return result;
	}

	/**
	 * Creates and returns a new chart panel.
	 * 
	 * @param elem
	 *            the description of the chart panel to create.
	 * @param simulation
	 *            the parent simulation.
	 * @return a new chart panel.
	 */
	public static JamelChartPanel createChartPanel(final Element elem, final Simulation simulation) {

		// final String type = elem.getAttribute("type");
		final JamelChart chart = getNewXYChart(elem, simulation);
		/*if ("".equals(type)) {
			throw new InitializationException("Chart " + elem.getAttribute("title") + ": Chart type is missing.");
		} else if ("scatter chart".equals(type)) {
			chart = createScatterChart(elem, dataManager);
		} else if ("time scatter chart".equals(type)) {
			chart = createTimeScatterChart(elem, dataManager);
		} else if ("time chart".equals(type)) {
			chart = createTimeChart(elem, dataManager);
		} else if ("histogram".equals(type)) {
			chart = createHistogram(elem, dataManager);
		} else {
			throw new InitializationException("Unexpected chart type: " + type);
		}*/
		// chart.getLegend().setItemFont(axisLabelFont);
		// TODO l'ajustement des fonts n'a pas sa place ici

		final JamelChartPanel chartPanel = new JamelChartPanel(chart);
		return chartPanel;
	}

}
