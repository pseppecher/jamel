package jamel.basic.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jamel.basic.data.BasicDataManager;
import jamel.basic.data.ExpressionFactory;
import jamel.basic.util.InitializationException;

/**
 * A collection of utility methods for creating charts.
 * 
 * @author pascal
 */
public class ChartFactory {
	
	// 2016-05-01: introduction de JamelChart
	// pour une meilleure utilisation des markers.

	/** A basic stroke used for legend items. */
	private static final BasicStroke basicStroke = new BasicStroke();

	/** The tick unit source. */
	private static final TickUnitSource IntegerTickUnits = NumberAxis.createIntegerTickUnits();

	/** A shape line used by time charts. */
	private static final Shape line = new Line2D.Double(0, 0, 15, 0);

	/** A transparent gray paint used by scatter charts. */
	private static final Paint lineColor = new Color(0.3f, 0.3f, 0.3f, 0.5f);

	/** A square shape used by scatter charts. */
	private static final Shape square = new Rectangle2D.Double(-3, -3, 6, 6);

	/** The font used for tick labels. */
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
		return axis;
	}

	/**
	 * Creates and returns a new combined time chart. A combined chart is a
	 * chart composed of multiple subplots.
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart to
	 *            create.
	 * @param dataManager
	 *            the data manager.
	 * @return a new combined chart.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static JamelChart createCombinedTimeChart(Element description, BasicDataManager dataManager)
			throws InitializationException {
		final JamelChart chart;
		final String name = description.getAttribute("title");

		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0),
				IntegerTickUnits);
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(xAxis);

		final NodeList childList = description.getChildNodes();
		final List<XYSeries> mainSeries = new ArrayList<XYSeries>();
		final List<Paint> mainColors = new ArrayList<Paint>();

		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();

		for (int j = 0; j < childList.getLength(); j++) {
			if (childList.item(j).getNodeName().equals("series")) {
				final Element serieXML = (Element) childList.item(j);
				final String seriesKey = serieXML.getAttribute("value");
				if (!seriesKey.equals("")) {
					final XYSeries timeSeries = dataManager.getTimeSeries(seriesKey, serieXML.getAttribute("mod"));
					if (timeSeries != null) {
						mainSeries.add(timeSeries);
						final Paint paint = JamelColor.getColor(serieXML.getAttribute("color"));
						mainColors.add(paint);
						final String label = serieXML.getAttribute("label");
						final String legendLabel;
						final String tooltip;
						if (!label.equals("")) {
							legendLabel = label;
							tooltip = ExpressionFactory.format(seriesKey);
						} else {
							legendLabel = seriesKey;
							tooltip = "";
						}
						final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, line,
								basicStroke, paint);
						defaultLegendItemCollection.add(legendItem);
					}
				}
			}
		}

		final NodeList subPlots = description.getElementsByTagName("subplot");
		final int nSubPlots = subPlots.getLength();
		for (int i = 0; i < nSubPlots; i++) {
			final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
			final XYSeriesCollection dataset = new XYSeriesCollection();

			int countSeries = 0;
			for (int l = 0; l < mainSeries.size(); l++) {
				final XYSeries timeSeries = mainSeries.get(l);
				dataset.addSeries(timeSeries);
				final Paint paint = mainColors.get(l);
				renderer.setSeriesPaint(countSeries, paint);
				countSeries++;
			}

			final Element subplotElem = (Element) subPlots.item(i);
			final NodeList seriesList = subplotElem.getElementsByTagName("series");
			final int nbSeries = seriesList.getLength();
			for (int k = 0; k < nbSeries; k++) {
				final Element serieXML = (Element) seriesList.item(k);
				final String seriesKey = serieXML.getAttribute("value");
				if (!seriesKey.equals("")) {
					final XYSeries timeSeries = dataManager.getTimeSeries(seriesKey, serieXML.getAttribute("mod"));
					if (timeSeries != null) {
						dataset.addSeries(timeSeries);
						final Paint paint = JamelColor.getColor(serieXML.getAttribute("color"));
						renderer.setSeriesPaint(countSeries, paint);
						countSeries++;
						final String label = serieXML.getAttribute("label");
						final String legendLabel;
						final String tooltip;
						if (!label.equals("")) {
							legendLabel = label;
							tooltip = seriesKey;
						} else {
							legendLabel = seriesKey;
							tooltip = "";
						}
						final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, line,
								basicStroke, paint);
						defaultLegendItemCollection.add(legendItem);
					}
				}
			}
			final NumberAxis yAxis = createAxis((Element) subplotElem.getElementsByTagName("yAxis").item(0), null);
			final XYPlot subplot = createXYPlot(dataset, xAxis, yAxis, renderer);
			if (subplotElem.getAttribute("xZeroBaselineVisible").equals("true")) {
				subplot.setDomainZeroBaselineVisible(true);
			}
			if (subplotElem.getAttribute("yZeroBaselineVisible").equals("true")) {
				subplot.setRangeZeroBaselineVisible(true);
			}

			plot.add(subplot);
		}

		chart = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				@SuppressWarnings("unchecked")
				final List<XYPlot> subplots = plot.getSubplots();
				for (final XYPlot subplot : subplots) {
					subplot.addDomainMarker(marker);
				}
			}
		};

		final NodeList legends = description.getElementsByTagName("legend");
		if (legends.getLength() > 0) {
			final Element legend = (Element) legends.item(0);
			final NodeList items = legend.getElementsByTagName("item");
			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			for (int i = 0; i < items.getLength(); i++) {
				final Element item = (Element) items.item(i);
				final String tooltip = item.getAttribute("tooltip");
				final String legendLabel = item.getAttribute("label");
				final String color = item.getAttribute("color");
				final Paint paint = JamelColor.getColor(color);
				final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, line, basicStroke,
						paint);
				try {
					legendItemCollection.add(legendItem);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					// ???
				}
			}
			plot.setFixedLegendItems(legendItemCollection);
		} else {
			plot.setFixedLegendItems(defaultLegendItemCollection);
		}
		return chart;
	}

	/**
	 * Creates and returns a new histogram.
	 * <p>
	 * <b>Work in progress. TODO: color, legend,</b>
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart to
	 *            create.
	 * @param dataManager
	 *            the data manager.
	 * @return a new histogram.
	 */
	private static JamelChart createHistogram(Element description, BasicDataManager dataManager) {
		final String name = description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final XYItemRenderer renderer = new XYBarRenderer();
		// renderer.setUseFillPaint(true);
		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0), null);
		final NumberAxis yAxis = createAxis((Element) description.getElementsByTagName("yAxis").item(0), null);

		final Element serieXML = (Element) seriesList.item(0);

		final String sector = serieXML.getAttribute("sector");
		final String x = serieXML.getAttribute("value");
		// TODO: tester si x est empty.
		final String select = serieXML.getAttribute("select");
		final String bins = serieXML.getAttribute("bins");
		final String color = serieXML.getAttribute("color");
		final Paint paint = JamelColor.getColor(color);
		// TODO: color
		renderer.setSeriesPaint(0, paint);
		// renderer.setSeriesPaint(k, lineColor);
		// renderer.setSeriesShape(k, square);
		String t = serieXML.getAttribute("t");
		if (t.equals("")) {
			t = "t";
		}
		final DynamicHistogramDataset dataset = dataManager.getHistogramDataset(sector, x, t, select, bins);
		final String toolTipText = "Not yet implemented."; // FIXME
		if (select.equals("")) {
			// toolTipText = "x=" + x + ", y=" + y +
			// " for each element in "+sector+", "+t;// FIXME
		} else {
			// toolTipText = "x=" + x + ", y=" + y +
			// " for each element in "+sector+", "+t+ " where "+select;// FIXME
		}
		final String label = serieXML.getAttribute("label");
		final LegendItem legendItem = new LegendItem(label, null, toolTipText, null, true, square, true, paint, true,
				lineColor, basicStroke, false, null, basicStroke, null);

		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		defaultLegendItemCollection.add(legendItem);

		final String background = description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		plot.setFixedLegendItems(new LegendItemCollection()); // Pas de legende

		final JamelChart result = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				// Does nothing.
			}
		};

		/*
		 * final NodeList legends = description.getElementsByTagName("legend");
		 * if (legends.getLength()>0) { Element legend = (Element)
		 * legends.item(0); final NodeList items =
		 * legend.getElementsByTagName("item");
		 * 
		 * final LegendItemCollection legendItemCollection = new
		 * LegendItemCollection(); for (int i=0; i<items.getLength(); i++) {
		 * final Element item = (Element) items.item(i); final String tooltip =
		 * item.getAttribute("series"); final String legendLabel =
		 * item.getAttribute("value"); final Paint
		 * paint=JamelColor.getColor(item.getAttribute("color")); final
		 * LegendItem legendItem = new LegendItem(legendLabel, null, tooltip,
		 * null, true, square, true, paint, true, lineColor, basicStroke, false,
		 * null, basicStroke, null); try { legendItemCollection.add(legendItem);
		 * } catch (ArrayIndexOutOfBoundsException e) { e.printStackTrace(); //
		 * ??? } } plot.setFixedLegendItems(legendItemCollection); } else {
		 * plot.setFixedLegendItems(defaultLegendItemCollection); }
		 */
		return result;
	}

	/**
	 * Creates and returns a new scatter chart.
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart to
	 *            create.
	 * @param dataManager
	 *            the data manager.
	 * @return a new scatter chart.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static JamelChart createScatterChart(Element description, BasicDataManager dataManager)
			throws InitializationException {
		final String name = description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
		renderer.setUseFillPaint(true);
		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0), null);
		final NumberAxis yAxis = createAxis((Element) description.getElementsByTagName("yAxis").item(0), null);

		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		PaintScaleLegend psl = null;

		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		for (int k = 0; k < nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			final String xyBlockSeries = serieXML.getAttribute("xyBlockSeries");
			if (xyBlockSeries.equals("true")) {
				final XYZDataset xyzDataset;
				final String sector = serieXML.getAttribute("sector");
				final String x = serieXML.getAttribute("x");
				final String y = serieXML.getAttribute("y");
				final String z = serieXML.getAttribute("z");
				xyzDataset = dataManager.getXYBlockData(sector, x, y, z);
				plot.setDataset(1, xyzDataset);
				final XYBlockRenderer blockRenderer = new XYBlockRenderer();
				final PaintScale scale = getNewColorPaintScale(serieXML);
				if (scale == null) {
					throw new InitializationException("The paint scale is null");
				}
				blockRenderer.setPaintScale(scale);
				final String blockHeight = serieXML.getAttribute("blockHeight");
				final String blockWidth = serieXML.getAttribute("blockWidth");
				if (!"".equals(blockHeight)) {
					blockRenderer.setBlockHeight(Double.parseDouble(blockHeight));
				}
				if (!"".equals(blockWidth)) {
					blockRenderer.setBlockWidth(Double.parseDouble(blockWidth));
				}

				blockRenderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
				plot.setRenderer(1, blockRenderer);

				final String showScale = serieXML.getAttribute("showScale");
				if (!"false".equals(showScale)) {
					final NumberAxis scaleAxis = new NumberAxis();
					scaleAxis.setRange(scale.getLowerBound(), scale.getUpperBound());
					psl = new PaintScaleLegend(scale, scaleAxis);
					psl.setMargin(new RectangleInsets(10, 10, 10, 10));
					psl.setPosition(RectangleEdge.BOTTOM);
					psl.setAxisOffset(5.0);
					psl.setFrame(new BlockBorder(Color.GRAY));
				} else {
					psl = null;
				}
			}

			else {
				final String sector = serieXML.getAttribute("sector");
				final String x = serieXML.getAttribute("x");
				final String y = serieXML.getAttribute("y");
				final String select = serieXML.getAttribute("select");
				final String color = serieXML.getAttribute("color");
				final Paint paint = JamelColor.getColor(color);
				renderer.setSeriesFillPaint(k, paint);
				renderer.setSeriesPaint(k, lineColor);
				renderer.setSeriesShape(k, square);
				String t = serieXML.getAttribute("t");
				if (t.equals("")) {
					t = "t";
				}
				final XYSeries scatterSeries = dataManager.getScatterSeries(sector, x, y, t, select);
				dataset.addSeries(scatterSeries);
				final String toolTipText;
				if (select.equals("")) {
					toolTipText = "x=" + x + ", y=" + y + " for each element in " + sector + ", " + t;
				} else {
					toolTipText = "x=" + x + ", y=" + y + " for each element in " + sector + ", " + t + " where "
							+ select;
				}
				final String label = serieXML.getAttribute("label");
				final LegendItem legendItem = new LegendItem(label, null, toolTipText, null, true, square, true, paint,
						true, lineColor, basicStroke, false, null, basicStroke, null);
				defaultLegendItemCollection.add(legendItem);
			}
		}

		// Annotations:
		final List<XYAnnotation> annotations = getAnnotations(description);
		if (annotations != null) {
			for (XYAnnotation annotation : annotations) {
				plot.addAnnotation(annotation);
			}
		}

		final String background = description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		final JamelChart result = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				// Does nothing.
			}
		};
		if (psl != null) {
			result.addSubtitle(psl);
		}

		final NodeList legends = description.getElementsByTagName("legend");
		if (legends.getLength() > 0) {
			Element legend = (Element) legends.item(0);
			final NodeList items = legend.getElementsByTagName("item");

			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			for (int i = 0; i < items.getLength(); i++) {
				final Element item = (Element) items.item(i);
				final String tooltip = item.getAttribute("series");
				final String legendLabel = item.getAttribute("label");
				final Paint paint = JamelColor.getColor(item.getAttribute("color"));
				final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true,
						paint, true, lineColor, basicStroke, false, null, basicStroke, null);
				try {
					legendItemCollection.add(legendItem);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					// ???
				}
			}
			plot.setFixedLegendItems(legendItemCollection);
		} else {
			plot.setFixedLegendItems(defaultLegendItemCollection);
		}
		return result;
	}

	/**
	 * Creates and returns a new standard time chart.
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart to
	 *            create.
	 * @param dataManager
	 *            the data manager.
	 * @return a new standard time chart.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	private static JamelChart createStandardTimeChart(final Element description, final BasicDataManager dataManager)
			throws InitializationException {

		final JamelChart chart;
		final String name = description.getAttribute("title");
		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0),
				IntegerTickUnits);
		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();

		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		for (int k = 0; k < nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			final String seriesKey = serieXML.getAttribute("value");
			if (!seriesKey.equals("")) {
				final XYSeries timeSeries = dataManager.getTimeSeries(seriesKey, serieXML.getAttribute("mod"));
				if (timeSeries != null) {
					dataset.addSeries(timeSeries);
				}
			}

			// Series color:
			final Paint paint;
			if (!serieXML.getAttribute("color").equals("")) {
				try {
					paint = JamelColor.getColor(serieXML.getAttribute("color"));
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Color not found: " + serieXML.getAttribute("color"), e);
				}
			} else {
				paint = drawingSupplier.getNextPaint();
			}
			renderer.setSeriesPaint(k, paint);

			// Preparing default legend:
			final String legendLabel;
			final String tooltip;
			if (serieXML.getAttribute("label").equals("")) {
				legendLabel = seriesKey;
				tooltip = null;
			} else {
				legendLabel = serieXML.getAttribute("label");
				tooltip = ExpressionFactory.format(seriesKey);
			}
			final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, line, basicStroke, paint);
			defaultLegendItemCollection.add(legendItem);
		}

		final NumberAxis yAxis = createAxis((Element) description.getElementsByTagName("yAxis").item(0), null);
		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		if (description.getAttribute("xZeroBaselineVisible").equals("true")) {
			plot.setDomainZeroBaselineVisible(true);
		}
		if (description.getAttribute("yZeroBaselineVisible").equals("true")) {
			plot.setRangeZeroBaselineVisible(true);
		}

		chart = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				this.getXYPlot().addDomainMarker(marker);
			}
		};
		
		final NodeList legends = description.getElementsByTagName("legend");
		if (legends.getLength() > 0) {
			Element legend = (Element) legends.item(0);
			final NodeList items = legend.getElementsByTagName("item");

			final LegendItemCollection legendItemCollection = new LegendItemCollection();
			for (int i = 0; i < items.getLength(); i++) {
				final Element item = (Element) items.item(i);
				final String tooltip = item.getAttribute("series");
				final String legendLabel = item.getAttribute("label");
				final Paint paint = JamelColor.getColor(item.getAttribute("color"));
				final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, line, basicStroke,
						paint);
				try {
					legendItemCollection.add(legendItem);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					// ???
				}
			}
			plot.setFixedLegendItems(legendItemCollection);
		} else {
			plot.setFixedLegendItems(defaultLegendItemCollection);
		}
		return chart;
	}

	/**
	 * Creates and returns a new time chart.
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart.
	 * @param dataManager
	 *            the data manager.
	 * @return the new chart.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static JamelChart createTimeChart(final Element description, BasicDataManager dataManager)
			throws InitializationException {
		final JamelChart chart;
		final NodeList subPlots = description.getElementsByTagName("subplot");
		if (subPlots.getLength() > 0) {
			chart = createCombinedTimeChart(description, dataManager);
		} else {
			chart = createStandardTimeChart(description, dataManager);
		}
		return chart;
	}

	/**
	 * Creates and returns a new time scatter chart.
	 * 
	 * @param description
	 *            an XML element that contains the description of the chart to
	 *            create.
	 * @param dataManager
	 *            the data manager.
	 * @return a new time scatter chart.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	private static JamelChart createTimeScatterChart(Element description, BasicDataManager dataManager)
			throws InitializationException {
		final String name = description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		for (int k = 0; k < nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			final String x = serieXML.getAttribute("x");
			final String y = serieXML.getAttribute("y");
			final XYSeries xySeries = dataManager.getTimeScatterSeries(x, y, serieXML.getAttribute("mod"));
			dataset.addSeries(xySeries);
			final String color = serieXML.getAttribute("color");
			renderer.setSeriesShape(k, null);
			final String shapesVisibles = serieXML.getAttribute("shapesVisibles");
			if (shapesVisibles.equals("false")) {
				renderer.setSeriesShapesVisible(k, false);
			}
			renderer.setSeriesPaint(k, lineColor);
			final Paint paint;
			if (color.equals("")) {
				paint = drawingSupplier.getNextPaint();
			} else {
				paint = JamelColor.getColor(color);
			}
			renderer.setUseFillPaint(true);
			renderer.setSeriesFillPaint(k, paint);

			// Preparing default legend:
			final String legendLabel;
			final String tooltip;
			final String seriesKey = "x=" + x + ", y=" + y;
			if (serieXML.getAttribute("label").equals("")) {
				legendLabel = seriesKey;
				tooltip = null;
			} else {
				legendLabel = serieXML.getAttribute("label");
				tooltip = ExpressionFactory.format(seriesKey);
			}
			final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true, paint,
					true, lineColor, basicStroke, false, null, basicStroke, null);
			defaultLegendItemCollection.add(legendItem);

		}
		final NumberAxis xAxis = createAxis((Element) description.getElementsByTagName("xAxis").item(0), null);
		final NumberAxis yAxis = createAxis((Element) description.getElementsByTagName("yAxis").item(0), null);

		final XYPlot plot = createXYPlot(dataset, xAxis, yAxis, renderer);

		// final Element truc = (Element)
		// description.getElementsByTagName("yAxis").item(0);
		// if (truc )

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
				final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null, true, square, true,
						paint, true, lineColor, basicStroke, false, null, basicStroke, null);
				try {
					legendItemCollection.add(legendItem);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					// ???
				}
			}
			plot.setFixedLegendItems(legendItemCollection);
		} else {
			plot.setFixedLegendItems(defaultLegendItemCollection);
		}

		final String background = description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		final JamelChart result = new JamelChart(name, plot) {
			@Override
			public void addTimeMarker(ValueMarker marker) {
				// Does nothing.
			}
		};
		
		return result;
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
			}
		};
	}

	/**
	 * Returns a list of {@link XYAnnotation}.
	 * 
	 * @param description
	 *            an XML element that contains the description of the
	 *            annotations.
	 * @return a list of {@link XYAnnotation}.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static List<XYAnnotation> getAnnotations(Element description) throws InitializationException {
		final List<XYAnnotation> result;
		final NodeList list = description.getElementsByTagName("annotations");
		if (list.getLength() > 0) {
			final Node item = list.item(0);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				throw new InitializationException("Not an element.");
			}
			final Element annotations = (Element) item;
			final NodeList childNodes = annotations.getChildNodes();
			result = new ArrayList<XYAnnotation>();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node item2 = childNodes.item(i);
				if (item2.getNodeType() == Node.ELEMENT_NODE) {
					final Element element2 = (Element) item2;
					final String label = element2.getNodeName();
					final float x = Float.parseFloat(element2.getAttribute("x"));
					final float y = Float.parseFloat(element2.getAttribute("y"));
					final float angle = Float.parseFloat(element2.getAttribute("angle"));
					final XYAnnotation annotation = new XYPointerAnnotation(label, x, y, angle);
					result.add(annotation);
				}
			}
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Returns a new color {@link PaintScale}.
	 * 
	 * @param elem
	 *            an XML element that contains the description of the
	 *            {@link PaintScale} to be returned.
	 * @return a new color {@link PaintScale}.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	private static PaintScale getNewColorPaintScale(Element elem) throws InitializationException {
		PaintScale result = null;
		final NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			final Node truc = list.item(i);
			if (truc.getNodeName().equals("scale")) {
				final Element scaleElem = ((Element) truc);
				final String type = scaleElem.getAttribute("type");
				if ("lookupScale".equals(type)) {
					final LookupPaintScale lookupPaintScale = new LookupPaintScale(1, 3, Color.red);
					lookupPaintScale.add(1, Color.WHITE);
					lookupPaintScale.add(2, Color.BLACK);
					result = lookupPaintScale;
				} else if ("colorScale".equals(type)) {
					final String lowerColorString = scaleElem.getAttribute("lowerColor");
					final String upperColorString = scaleElem.getAttribute("upperColor");
					final Color upperColor;
					final Color lowerColor;
					if ("".equals(lowerColorString) || "".equals(upperColorString)) {
						// L'une des deux couleurs n'est pas d�finie.
						throw new IllegalArgumentException("A color is missing");
					}
					upperColor = JamelColor.getColor(upperColorString);
					lowerColor = JamelColor.getColor(lowerColorString);
					final double upperBound = Double.parseDouble(scaleElem.getAttribute("upperBound"));
					final double lowerBound = Double.parseDouble(scaleElem.getAttribute("lowerBound"));
					result = new ColorPaintScale(lowerBound, upperBound, lowerColor, upperColor);
				} else {
					throw new InitializationException("Unexpected PaintScale type: " + type);
				}
				break;
			}
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
	 * @param dataManager
	 *            the data manager.
	 * @return a new chart panel.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	public static JamelChartPanel createChartPanel(Element elem, BasicDataManager dataManager)
			throws InitializationException {
		final JamelChartPanel chartPanel;
		final String type = elem.getAttribute("type");
		if ("".equals(type)) {
			throw new InitializationException("Chart " + elem.getAttribute("title") + ": Chart type is missing.");
		} else if ("scatter chart".equals(type)) {
			chartPanel = new JamelChartPanel(createScatterChart(elem, dataManager));
		} else if ("time scatter chart".equals(type)) {
			chartPanel = new JamelChartPanel(createTimeScatterChart(elem, dataManager));
		} else if ("time chart".equals(type)) {
			chartPanel = new JamelChartPanel(createTimeChart(elem, dataManager));
		} else if ("histogram".equals(type)) {
			chartPanel = new JamelChartPanel(createHistogram(elem, dataManager));
		} else {
			throw new InitializationException("Unexpected chart type: " + type);
		}
		return chartPanel;
	}

}

// ***
