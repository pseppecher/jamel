package jamel.basic.gui;

import jamel.basic.util.InitializationException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A chart generator.
 */
public class ChartGenerator {

	/** A basic stroke used for legend items. */
	private static final BasicStroke basicStroke = new BasicStroke();

	/** A transparent color used for chart background. */
	private static final Color colorTransparent = new Color(0,0,0,0);

	/** The tick unit source. */
	private static final TickUnitSource IntegerTickUnits = NumberAxis.createIntegerTickUnits();

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

	/** The font used for chart titles. */
	private static final Font titleFont = new Font("Tahoma", Font.PLAIN, 14);

	/**
	 * Returns a list of {@link XYAnnotation}.
	 * @param element an XML element that contains the description of the annotations.
	 * @return a list of {@link XYAnnotation}.
	 * @throws InitializationException If something goes wrong.
	 */
	private static List<XYAnnotation> getAnnotations(Element element) throws InitializationException {
		final List<XYAnnotation> result;
		final NodeList list = element.getElementsByTagName("annotations");
		if (list.getLength()>0) {
			final Node item = list.item(0);
			if (item.getNodeType()!=Node.ELEMENT_NODE) {
				throw new InitializationException("Not an element.");
			}
			final Element annotations = (Element) item;
			final NodeList childNodes = annotations.getChildNodes();
			result=new ArrayList<XYAnnotation>();
			for (int i=0; i<childNodes.getLength(); i++) {
				final Node item2 = childNodes.item(i);
				if (item2.getNodeType()==Node.ELEMENT_NODE) {
					final Element element2 = (Element) item2;
					final String label = element2.getNodeName();
					final float x = Float.parseFloat(element2.getAttribute("x"));
					final float y = Float.parseFloat(element2.getAttribute("y"));
					final float angle = Float.parseFloat(element2.getAttribute("angle"));
					final XYAnnotation annotation = new XYPointerAnnotation(label, x, y, angle);
					result.add(annotation);
				}
			}
		}
		else {
			result=null;
		}
		return result;
	}

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
	 * Returns a new Y axis.
	 * @param elem an XML element with the description of the axis.
	 * @param source  the tick unit source.
	 * @return a new Y axis.
	 */
	private static NumberAxis getNewAxis(Element elem, TickUnitSource source) {
		final NumberAxis axis = new NumberAxis(null);
		axis.setAutoRangeIncludesZero(false);
		if (source!=null) {
			axis.setStandardTickUnits(source);
		}
		axis.setTickLabelFont(tickLabelFont);			
		if (elem!=null) {
			final Double max = parseDouble(elem.getAttribute("max"));
			if (max!=null) {
				axis.setUpperBound(max);				
			}
			final Double min = parseDouble(elem.getAttribute("min"));
			if (min!=null) {
				axis.setLowerBound(min);				
			}
		}
		return axis;
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
	 * Returns a new color {@link PaintScale}.
	 * @param elem  an XML element that contains the description of the Paintscale to be returned.
	 * @return a new color {@link PaintScale}.
	 * @throws InitializationException If something goes wrong.
	 */
	private static PaintScale getNewColorPaintScale(Element elem) throws InitializationException {
		PaintScale result = null;
		final NodeList list = elem.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			final Node truc = list.item(i);
			if (truc.getNodeName().equals("scale")) {
				final Element scaleElem = ((Element) truc); 
				final String type = scaleElem.getAttribute("type");
				if ("lookupScale".equals(type)) {
					final LookupPaintScale lookupPaintScale = new LookupPaintScale(1,3,Color.red);
					lookupPaintScale.add(1, Color.WHITE);
					lookupPaintScale.add(2, Color.BLACK);
					result = lookupPaintScale;
				}
				else if ("colorScale".equals(type)) {
					final String lowerColorString = scaleElem.getAttribute("lowerColor");
					final String upperColorString = scaleElem.getAttribute("upperColor");
					final Color upperColor;
					final Color lowerColor;
					if ("".equals(lowerColorString) || "".equals(upperColorString)) {
						// L'une des deux couleurs n'est pas dŽfinie.
						throw new IllegalArgumentException("A color is missing");
					}
					upperColor=JamelColor.getColor(upperColorString);
					lowerColor=JamelColor.getColor(lowerColorString);
					final double upperBound = Double.parseDouble(scaleElem.getAttribute("upperBound"));
					final double lowerBound = Double.parseDouble(scaleElem.getAttribute("lowerBound"));
					result = new ColorPaintScale(lowerBound,upperBound,lowerColor,upperColor);
				}
				else {
					throw new InitializationException("Unexpected PaintScale type: "+type);
				}				
				break;
			}
		}
		return result;
	}

	/**
	 * Returns a new {@link PaintScaleLegend} for the specified paint scale.
	 * @param scale  the paint scale.
	 * @return a new {@link PaintScaleLegend}.
	 */
	private static PaintScaleLegend getNewPaintScaleLegend(PaintScale scale) {
		final NumberAxis scaleAxis=new NumberAxis();
		scaleAxis.setRange(scale.getLowerBound(),scale.getUpperBound());
		final PaintScaleLegend psl=new PaintScaleLegend(scale,scaleAxis);
		psl.setMargin(new RectangleInsets(10,10,10,10));
		psl.setPosition(RectangleEdge.BOTTOM);
		psl.setAxisOffset(5.0);
		psl.setFrame(new BlockBorder(Color.GRAY));
		return psl;
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
	private static XYItemRenderer getNewTimeScatterChartRenderer(final XYDataset dataset, final Paint[] colors) {
		return new XYLineAndShapeRenderer(true, true){{
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
	 * Returns an array of Paint initialized to the colors represented by the specified strings.
	 * @param color an array of String representing the colors to be returned
	 * @return an array of Paint.
	 */
	private static Paint[] parseColors(String ... color) {
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
	 * Creates and returns a new scatter chart.
	 * @param description a XML element that contains the description of the chart.
	 * @param dataset the dataset.
	 * @param xyzDataset the xyzDataset.
	 * @return the new chart.
	 * @throws InitializationException If something goes wrong.
	 */
	public static JFreeChart createScatterChart(Element description, XYDataset dataset, XYZDataset xyzDataset) throws InitializationException {
		final String name=description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final String[] series = new String[nbSeries];
		final String[] color = new String[nbSeries];
		final String[] label = new String[nbSeries];
		for (int k = 0; k<nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			series[k] = serieXML.getAttribute("value");
			color[k] = serieXML.getAttribute("color");
			label[k] = serieXML.getAttribute("label");
		}
		final NumberAxis xAxis = getNewAxis((Element) description.getElementsByTagName("xAxis").item(0),null);
		final NumberAxis yAxis = getNewAxis((Element) description.getElementsByTagName("yAxis").item(0),null);
		final Paint[] colors = parseColors(color);

		final XYPlot plot = getNewXYPlot(dataset, xAxis, yAxis, getNewScatterChartRenderer(dataset,colors));

		final List<XYAnnotation> annotations = getAnnotations(description);
		if (annotations!=null) {
			for (XYAnnotation annotation:annotations) {
				plot.addAnnotation(annotation);			
			}
		}

		final PaintScaleLegend psl;
		if (xyzDataset!=null) {
			plot.setDataset(1, xyzDataset);
			final XYBlockRenderer renderer = new XYBlockRenderer();
			final NodeList xyBlockSeriesList = description.getElementsByTagName("xyBlockSeries");

			final Node node = xyBlockSeriesList.item(0);
			if (node.getNodeType()!=Node.ELEMENT_NODE) {
				throw new InitializationException();
			}
			final Element elem = (Element) node;
			final PaintScale scale = getNewColorPaintScale(elem);
			if (scale==null) {
				throw new InitializationException("The scale is null");
			}
			renderer.setPaintScale(scale);

			final String blockHeight =  elem.getAttribute("blockHeight");
			final String blockWidth =  elem.getAttribute("blockWidth");
			if (!"".equals(blockHeight)) {
				renderer.setBlockHeight(Double.parseDouble(blockHeight));				
			}
			if (!"".equals(blockWidth)) {
				renderer.setBlockWidth(Double.parseDouble(blockWidth));				
			}
			
			renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
			plot.setRenderer(1, renderer);

			final String showScale =  elem.getAttribute("showScale");
			if (!"false".equals(showScale)) {
				psl = getNewPaintScaleLegend(scale);
			}
			else {
				psl = null;				
			}
		}
		else {
			psl=null;
		}
		plot.setFixedLegendItems(getLegendItemCollection(series,colors,label,true));
		final String background=description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		final JFreeChart result = getNewChart(name,plot);
		if (psl!=null) {
			result.addSubtitle(psl);
		}
		return result;
	}

	/**
	 * Creates and returns a new time chart.
	 * @param description a XML element that contains the description of the chart.
	 * @param dataset the dataset.
	 * @return the new chart.
	 * @throws InitializationException If something goes wrong.
	 */
	public static JFreeChart createTimeChart(final Element description, final XYDataset dataset) throws InitializationException {
		final String name=description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final String[] series = new String[nbSeries];
		final String[] color = new String[nbSeries];
		final String[] label = new String[nbSeries];
		for (int k = 0; k<nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			series[k] = serieXML.getAttribute("value");
			color[k] = serieXML.getAttribute("color");
			label[k] = serieXML.getAttribute("label");
		}
		final NumberAxis xAxis = getNewAxis((Element) description.getElementsByTagName("xAxis").item(0),IntegerTickUnits);
		final NumberAxis yAxis = getNewAxis((Element) description.getElementsByTagName("yAxis").item(0),null);
		final Paint[] colors = parseColors(color);
		final XYPlot plot = getNewXYPlot(dataset, xAxis, yAxis, getNewTimeChartRenderer(dataset,colors));
		plot.setFixedLegendItems(getLegendItemCollection(series,colors,label,false));		
		return getNewChart(name,plot);	
	}

	/**
	 * Creates and returns a new time chart.
	 * @param description a XML element that contains the description of the chart.
	 * @param dataset the dataset.
	 * @return the new chart.
	 * @throws InitializationException If something goes wrong.
	 */
	public static JFreeChart createTimeScatterChart(Element description,
			XYSeriesCollection dataset) throws InitializationException {
		final String name=description.getAttribute("title");
		final NodeList seriesList = description.getElementsByTagName("series");
		final int nbSeries = seriesList.getLength();
		final String[] series = new String[nbSeries];
		final String[] color = new String[nbSeries]; // TODO: DELETE
		final String[] label = new String[nbSeries];
		for (int k = 0; k<nbSeries; k++) {
			final Element serieXML = (Element) seriesList.item(k);
			series[k] = serieXML.getAttribute("value");
			color[k] = serieXML.getAttribute("color");
			label[k] = serieXML.getAttribute("label");
		}
		final NumberAxis xAxis = getNewAxis((Element) description.getElementsByTagName("xAxis").item(0),null);
		final NumberAxis yAxis = getNewAxis((Element) description.getElementsByTagName("yAxis").item(0),null);
		final Paint[] colors = parseColors(color);

		final XYPlot plot = getNewXYPlot(dataset, xAxis, yAxis, getNewTimeScatterChartRenderer(dataset,colors));

		plot.setFixedLegendItems(getLegendItemCollection(series,colors,label,true));
		final String background=description.getAttribute("background");
		if (!"".equals(background)) {
			plot.setBackgroundPaint(JamelColor.getColor(background));
		}

		final JFreeChart result = getNewChart(name,plot);
		return result;
	}

}

// ***
