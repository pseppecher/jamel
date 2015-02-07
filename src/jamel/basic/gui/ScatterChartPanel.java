package jamel.basic.gui;

import jamel.basic.data.util.xml.ChartDescription;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A ChartPanel that contains a scatter plot.
 */
@SuppressWarnings("serial")
public class ScatterChartPanel extends JamelChartPanel {

	/** A transparent gray. */
	private static final Paint lineColor = new Color(0.3f,0.3f,0.3f,0.5f);

	/** A square shape. */
	private final static Shape square = new Rectangle2D.Double(-3, -3, 6, 6);

	/**
	 * Creates a panel with its chart.
	 * @param chartDescription the description of the chart.
	 * @param data the dataset.
	 */
	public ScatterChartPanel(ChartDescription chartDescription, XYSeriesCollection data) {
		super(chartDescription,data);
	}

	@Override
	protected LegendItem getNewLegendItem(String label, String toolTipText, Paint color) {
		return new LegendItem
				(label, null,
						toolTipText, null,
						true, square,
						true, color,
						true, lineColor,
						basicStroke,
						false, null,
						basicStroke, null);
	}
	
	@Override
	protected XYItemRenderer getNewRenderer(final XYDataset dataset, final Paint[] colors) {
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
	
	@Override
	protected NumberAxis getNewXAxis(String label) {
		return getStandardAxis(label);
	}

}

// ***
