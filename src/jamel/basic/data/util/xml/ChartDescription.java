package jamel.basic.data.util.xml;

import jamel.basic.gui.JamelColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.DefaultDrawingSupplier;


/**
 * The description of a chart. 
 */
public class ChartDescription {

	/** A basic stroke */
	private final static BasicStroke basicStroke = new BasicStroke();

	/** A shape line */
	private final static Shape line = new Line2D.Double(0, 0, 15, 0);

	/** A transparent gray paint. */
	private final static Paint lineColor = new Color(0.3f,0.3f,0.3f,0.5f);

	/** A square shape. */
	private final static Shape square = new Rectangle2D.Double(-3, -3, 6, 6);

	/** Chart name */
	final private String name;
	
	/** The options. */
	private String options = null;
	
	/** List of the series. */
	private List<SeriesDescription> seriesDescriptions = new ArrayList<SeriesDescription>();
	
	/** Max of the y Axis. */
	private Double yAxisMax = null;
	
	/** Min of the yAxis. */
	private Double yAxisMin = null;
	
	/**
	 * Creates a new chart description.
	 * @param name the name of the chart.
	 */
	public ChartDescription(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null not permitted");
		}
		this.name = name;
	}

	/**
	 * Adds a new series description.
	 * @param descritption the description of the series?
	 */
	public void addSerie(SeriesDescription descritption) {
		this.seriesDescriptions.add(descritption);
	}
	
	/**
	 * The list of the colors.
	 * @return the list of the colors.
	 */
	public Paint[] getColors() {
		final Paint[] result = new Paint[seriesDescriptions.size()];
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		for(int i=0; i<seriesDescriptions.size();i++) {
			if (seriesDescriptions.get(i).getColor()!=null) {
				try {
					result[i]=JamelColor.getColor(seriesDescriptions.get(i).getColor());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new RuntimeException("Color not found: "+seriesDescriptions.get(i).getColor());
				}
			}
			else {
				result[i]=drawingSupplier.getNextPaint();
			}			
		}
		return result ;
	}
	
	/**
	 * Returns a LegendItemCollection.
	 * @return a LegendItemCollection.
	 */
	public LegendItemCollection getLegendItemCollection() {
		final LegendItemCollection legendItemCollection = new LegendItemCollection();
		final DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		for(int i=0; i<seriesDescriptions.size();i++) {
			final String tooltip;
			final Paint color;
			if (seriesDescriptions.get(i).getColor()!=null) {
				color=JamelColor.getColor(seriesDescriptions.get(i).getColor());
			}
			else {
				color=drawingSupplier.getNextPaint();
			}
			final String label; 
			if (seriesDescriptions.get(i).getLabel()!=null) {
				label=seriesDescriptions.get(i).getLabel();
				tooltip = seriesDescriptions.get(i).getKey();
			}
			else {
				label=seriesDescriptions.get(i).getKey();
				tooltip = null;
			}
			final LegendItem legendItem;
			if ("scatter".equals(options)) {
				legendItem = new LegendItem(label, null, tooltip, null, true, square, true, color, true, lineColor, basicStroke, false, null, basicStroke, null);
			}
			else {
				legendItem = new LegendItem(label, null, tooltip, null, line, basicStroke, color);
			}
			try {
				legendItemCollection.add(legendItem);
			} catch (ArrayIndexOutOfBoundsException e) {}
		}
		return legendItemCollection;
	}

	/**
	 * Returns the name.
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the options.
	 * @return the options.
	 */
	public String getOptions() {
		return this.options;
	}

	/**
	 * Returns the list of the series descriptions.
	 * @return the list of the series descriptions.
	 */
	public List<SeriesDescription> getSeries() {
		return this.seriesDescriptions;
	}

	/**
	 * Returns the Y axis max.
	 * @return the Y axis max.
	 */
	public Double getYAxisMax() {
		return this.yAxisMax;
	}

	/**
	 * Returns the Y axis min.
	 * @return the Y axis min.
	 */
	public Double getYAxisMin() {
		return this.yAxisMin;
	}

	/**
	 * Sets the options.
	 * @param op the options to set.
	 */
	public void setOptions(String op) {
		this.options = op;
	}

	/**
	 * Sets the max.
	 * @param max the max to set.
	 */
	public void setYAxisMax(String max) {
		if (max.isEmpty()) {
			this.yAxisMax = null;			
		}
		else {
			this.yAxisMax = Double.parseDouble(max);
		}
	}

	/**
	 * Sets the min.
	 * @param min the min to set.
	 */
	public void setYAxisMin(String min) {
		if (min.isEmpty()) {
			this.yAxisMin = null;			
		}
		else {
			this.yAxisMin = Double.parseDouble(min);
		}
	}

}

// ***
