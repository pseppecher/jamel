package jamel.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.io.File;
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
import org.jfree.chart.renderer.xy.VectorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.VectorSeries;
import org.jfree.data.xy.VectorSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import jamel.Jamel;
import jamel.data.DynamicSeries;
import jamel.data.ExpressionFactory;
import jamel.util.JamelObject;
import jamel.util.Parameters;

/**
 * The chart manager.
 */
public class ChartManager extends JamelObject {

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
	 * A thin stroke.
	 */
	private static final Stroke thinStroke = new BasicStroke(0.5f);

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

		//axis.setAutoRangeIncludesZero(false);
		
		if (axisDescription != null && !axisDescription.getAttribute("label").equals("")) {
			axis.setLabel(axisDescription.getAttribute("label"));
		}

		if (axisDescription != null && !axisDescription.getAttribute("integerUnit").isEmpty()) {
			axis.setIntegerUnit(Boolean.parseBoolean(axisDescription.getAttribute("integerUnit")));
		}

		// axis.setTickLabelFont(tickLabelFont);
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
		// plot.getRangeAxis().setLabelFont(axisLabelFont);
		// plot.getDomainAxis().setLabelFont(axisLabelFont);
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		return plot;
	}

	/**
	 * The list of the series to update.
	 */
	final private List<DynamicSeries> dynamicSeries = new LinkedList<>();

	/**
	 * The expression factory.
	 */
	final private ExpressionFactory expressionFactory;

	/**
	 * The parent Gui.
	 */
	final private Gui gui;

	/**
	 * The list of the panels (charts and html).
	 */
	final private List<Component> panels = new LinkedList<>();

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
		this.gui = gui;
		this.expressionFactory = expressionFactory;
	}

	/**
	 * Creates and returns a new XY chart.
	 * 
	 * @param params
	 *            the description of the XY chart to create.
	 * @return a new XY chart.
	 */
	private JamelChart getNewXYChart(final Parameters params) {
		assert params != null;
		final String name = params.getAttribute("title");
		final String defaultConditions;
		if (params.get("if") != null) {
			defaultConditions = params.get("if").getCompactText();
		} else {
			defaultConditions = null;
		}
		final List<Parameters> seriesList = params.getAll("series");

		final XYSeriesCollection xyDataset = new XYSeriesCollection();
		final XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer();

		VectorSeriesCollection vectorDataset = null;
		VectorRenderer vectorRenderer = null;

		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		final LegendItemCollection defaultLegendItemCollection = new LegendItemCollection();
		int countXYSeries = 0;
		// int countVectorSeries = 0;
		for (final Parameters seriesElement : seriesList) {

			final String conditions = (seriesElement.get("if") == null) ? defaultConditions
					: seriesElement.get("if").getCompactText();

			// TODO vérifier la présence de x et y, générer une erreur sinon.
			final String x = seriesElement.get("x").getCompactText();
			final String y = seriesElement.get("y").getCompactText();

			if (seriesElement.hasAttribute("vector") && seriesElement.getAttribute("vector").equals("true")) {

				final DynamicSeries newSeries;

				// Vector series

				if (vectorDataset == null) {
					vectorDataset = new VectorSeriesCollection();
					vectorRenderer = new VectorRenderer();
				}

				final String deltaX = seriesElement.get("deltaX").getCompactText();
				final String deltaY = seriesElement.get("deltaY").getCompactText();

				newSeries = expressionFactory.getVectorSeries(x, y, deltaX, deltaY, conditions);

				this.dynamicSeries.add(newSeries);
				vectorDataset.addSeries((VectorSeries) newSeries);
				// countVectorSeries++;

			} else {

				final DynamicSeries newSeries;
				if (seriesElement.hasAttribute("scatter") && seriesElement.getAttribute("scatter").equals("true")) {

					// Scatter series (permet de visualiser chaque agent d'un
					// secteur)

					final String sector = seriesElement.getAttribute("sector");
					final String selection = seriesElement.hasAttribute("select") ? seriesElement.getAttribute("select")
							: null;
					newSeries = this.getSector(sector).getScatterSeries(x, y,
							expressionFactory.parseConditions(conditions), selection);

				} else {

					// Line series

					newSeries = expressionFactory.getXYSeries(x, y, conditions);
				}
				if (newSeries != null) {
					
					this.dynamicSeries.add(newSeries);
					xyDataset.addSeries((XYSeries) newSeries);

					final boolean shapeFilled = true;
					Paint outlinePaint = Color.black;
					
					// Color

					final Paint seriesPaint;
					{
						final String color = seriesElement.getAttribute("color");
						if (color == "") {
							seriesPaint = drawingSupplier.getNextPaint();
						} else {
							seriesPaint = ColorParser.getColor(color);
						}
					}

					// Shapes ?

					final Shape shape; // = xyRenderer.getDefaultShape();
					final boolean shapeVisible;

					if (seriesElement.getAttribute("shapesVisible").equals("true")) {
						shape = drawingSupplier.getNextShape();
						xyRenderer.setSeriesShapesVisible(countXYSeries, true);
						xyRenderer.setSeriesShape(countXYSeries, shape);
						xyRenderer.setUseFillPaint(true);
						xyRenderer.setSeriesFillPaint(countXYSeries, seriesPaint);
						xyRenderer.setSeriesOutlineStroke(countXYSeries, thinStroke);
						xyRenderer.setSeriesStroke(countXYSeries, thinStroke);
						shapeVisible = true;
					} else {
						xyRenderer.setSeriesShapesVisible(countXYSeries, false);
						shape = xyRenderer.getDefaultShape();
						shapeVisible = false;
					}

					// Line color

					if (seriesElement.hasAttribute("lineColor")) {
						final Color lineColor = ColorParser.getColor(seriesElement.getAttribute("lineColor"));
						xyRenderer.setSeriesPaint(countXYSeries, lineColor);
						outlinePaint = lineColor;
					} else {
						xyRenderer.setSeriesPaint(countXYSeries, seriesPaint);
					}

					// Line visibility

					final boolean lineVisible = !seriesElement.getAttribute("linesVisible").equals("false");
					xyRenderer.setSeriesLinesVisible(countXYSeries, lineVisible);

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

					final LegendItem legendItem = new LegendItem(legendLabel, null, tooltip, null,
							shapeVisible, shape, shapeFilled, seriesPaint, true, outlinePaint, thinStroke, lineVisible, line,
							basicStroke, seriesPaint);

					defaultLegendItemCollection.add(legendItem);
				}

				countXYSeries++;

			}

		}

		final NumberAxis xAxis = createAxis(params.get("xAxis"), null);
		final NumberAxis yAxis = createAxis(params.get("yAxis"), null);

		final XYPlot plot;
		if (vectorDataset != null) {
			plot = createXYPlot(vectorDataset, xAxis, yAxis, vectorRenderer);
		} else {
			plot = createXYPlot(xyDataset, xAxis, yAxis, xyRenderer);
		}
		
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
			plot.setBackgroundPaint(ColorParser.getColor(background));
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
	 * Exports all the charts as pdf files.
	 * 
	 * @param event
	 *            the parameters of the export.
	 */
	public void exportCharts(Parameters event) {
		final File parent = this.getSimulation().getFile().getParentFile();
		final String exportDirectoryName;
		if (event.getAttribute("to").isEmpty()) {
			exportDirectoryName = "";
		} else {
			exportDirectoryName = event.getAttribute("to") + "/";
		}
		final Parameters chartDescription = event.get("format");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				private void ensureParentFileExist(final File file) {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
				}

				@Override
				public void run() {
					for (Component comp : panels) {
						final String tabName = comp.getParent().getParent().getName().replaceAll(" ", "_");
						if (comp instanceof JamelChartPanel) {
							final String filename = exportDirectoryName + tabName + "/"
									+ ((JamelChartPanel) comp).getChart().getTitle().getText().replaceAll(" ", "_")
									+ ".pdf";
							final File pdfFile = new File(parent, filename);
							ensureParentFileExist(pdfFile);
							((JamelChartPanel) comp).export(pdfFile, chartDescription);
						} else if (comp instanceof HtmlPanel) {
							final String filename = exportDirectoryName + tabName + "/" + "panel.html";
							// TODO: revoir le nommage de ce fichier
							final File htmlFile = new File(parent, filename);
							ensureParentFileExist(htmlFile);
							((HtmlPanel) comp).export(htmlFile);
						}
					}
				}

			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates and returns a new panel (a chart panel or a html panel).
	 * 
	 * @param params
	 *            the description of the panel to be created.
	 * @return the new panel.
	 */
	public Component getNewPanel(Parameters params) {
		final Component result;
		try {
			if (params.getName().equals("empty")) {
				result = new EmptyPanel();
			} else if (params.getName().equals("chart")) {
				Component chartPanel = null;
				try {
					chartPanel = new JamelChartPanel(this.getNewXYChart(params));
				} catch (final Exception e) {
					e.printStackTrace();
					chartPanel = HtmlPanel.getNewErrorPanel(e.getMessage());
				}
				if (chartPanel != null) {
					result = chartPanel;
				} else {
					result = new EmptyPanel();
					// TODO il vaudrait mieux un HtmlPanel avec un message
					// d'erreur.
				}
			} else if (params.getName().equals("html")) {
				result = HtmlPanel.getNewPanel(params.getElem(), this.gui, this.expressionFactory);
				// result = new HtmlPanel(params.getElem(), gui,
				// gui.expressionFactory);
			} else {
				throw new RuntimeException("Not yet implemented: " + params.getName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong", e);
		}
		this.panels.add(result);
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
					for (final DynamicSeries series : dynamicSeries) {
						series.update(true);
					}
					for (final Component panel : panels) {
						if (panel instanceof Updatable) {
							((Updatable) panel).update();
						}
					}
				}

			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

	}

}
