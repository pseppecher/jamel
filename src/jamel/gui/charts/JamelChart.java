/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.gui.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.Date;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * An abstract class for the charts.
 */
@SuppressWarnings("serial")
public abstract class JamelChart extends JFreeChart {

	/** basicStroke */
	private final static BasicStroke basicStroke = new BasicStroke();

	/** line */
	private final static Shape line = new Line2D.Double(0, 0, 15, 0);
	
	/** The standard chart theme. */
	private final static StandardChartTheme standardChartTheme = getNewChartTheme();

	/**
	 * Returns the standard chart theme.
	 * @return the standard chart theme.
	 */
	private static StandardChartTheme getNewChartTheme() {
		StandardChartTheme theme = new StandardChartTheme("Standard Chart Theme") ;
		float size = theme.getExtraLargeFont().getSize2D() ;
		Font titleFont = theme.getExtraLargeFont().deriveFont(size-4) ;
		theme.setExtraLargeFont(titleFont);
		Font axisFont = theme.getRegularFont();
		theme.setLargeFont(axisFont);
		theme.setChartBackgroundPaint(new Color(0,0,0,1));
		theme.setPlotBackgroundPaint(Color.WHITE) ;
		theme.setDomainGridlinePaint(Color.GRAY) ;
		theme.setRangeGridlinePaint(Color.GRAY) ;
		return theme;
	}

	/**
	 * Creates a new JamelChart.
	 * 
	 * @param title  the title.
	 * @param plot  the plot.
	 */
	public 	JamelChart(String title, Plot plot) { 
		super(title, plot) ;
		standardChartTheme.apply(this) ;
	}

	/**
	 * Returns the legend item collection.
	 * @return the legend item collection.
	 */
	private LegendItemCollection getFixedLegendItems() {
		LegendItemCollection legendItemCollection = this.getXYPlot().getFixedLegendItems();
		if (legendItemCollection==null) {
			legendItemCollection = new LegendItemCollection();
			this.getXYPlot().setFixedLegendItems(legendItemCollection);
		}
		return legendItemCollection;
	}

	/**
	 * Adds a line item to the legend of the chart.
	 * @param label  the label of the item.
	 * @param color  the color of the item.
	 */
	public void addLegendItem(String label, Color color){
		final LegendItemCollection gbcdg = getFixedLegendItems();
		gbcdg.add(new LegendItem(label, color));
	}

	/**
	 * Adds a line item to the legend of the chart.
	 * @param label  the label of the item.
	 * @param color  the color of the item.
	 */
	public void addLineLegendItem(String label, Color color){
		final LegendItemCollection gbcdg = getFixedLegendItems();
		gbcdg.add(new LegendItem(label, null, null, null, line, basicStroke, color));
	}

	/**
	 * Add a marker to the chart.
	 * 
	 * @param marker  the marker to add.
	 */
	public void addMarker(ValueMarker marker) {
		getXYPlot().addDomainMarker(marker);		
	}

	/**
	 * Sets the paints used for the series.
	 * 
	 * @param index  the renderer index.
	 * @param paint  the paints.
	 */
	public void setColors(int index, Paint... paint) {
		final XYItemRenderer renderer = ((XYPlot) getPlot()).getRenderer(index);
		for (int i = 0; i < paint.length; i++) {
			renderer.setSeriesPaint(i, paint[i]);
		}
	}

	/**
	 * Sets integer tick units on the domain axis.
	 */
	public void setIntegerTickUnitsOnDomainAxis() {
		((NumberAxis) ((XYPlot) this.getPlot()).getDomainAxis())
		.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	}

	/**
	 * Sets integer tick units on the range axis.
	 */
	public void setIntegerTickUnitsOnRangeAxis() {
		((NumberAxis) ((XYPlot) this.getPlot()).getRangeAxis())
		.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	}

	/**
	 * Sets the axis range.
	 * 
	 * @param lower  the lower axis limit.
     * @param upper  the upper axis limit.
	 */
	public void setRangeAxisRange(int lower, int upper) {
		this.getXYPlot().getRangeAxis().setRange(lower, upper);
	}
	
	/**
	 * Sets the time range.
	 * 
	 * @param lower  the lower date limit.
	 * @param upper  the upper date limit.
	 */
	public abstract void setTimeRange(Date lower, Date upper) ;

}