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

import java.util.Date;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * An abstract scatter chart.
 */
@SuppressWarnings("serial")
public abstract class AbstractScatterChart extends JamelChart {

	/**
	 * Returns a new plot.
	 * 
	 * @param xySeries  the series.
	 * @param xAxisLabel  the label of the horizontal axis.
	 * @param yAxisLabel  the label of the vertical axis.
	 * @return a new plot.
	 */
	static private Plot newPlot(XYSeries xySeries, String xAxisLabel, String yAxisLabel) {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		if (xySeries != null) dataset.addSeries(xySeries);
		final XYPlot plot = new XYPlot(dataset, new NumberAxis(xAxisLabel), new NumberAxis(yAxisLabel), new XYLineAndShapeRenderer(false, true));
		plot.setOrientation(PlotOrientation.VERTICAL);		
		return plot ;
	}

	/**
	 * Creates a new scatter chart.
	 * 
	 * @param title  the title.
	 * @param xySeries  the series.
	 * @param xAxisLabel  the labal for the horizontal axis.
	 * @param yAxisLabel  the label for the vertical axis 
	 */
	public AbstractScatterChart(String title, XYSeries xySeries, String xAxisLabel, String yAxisLabel) {
		super(title, newPlot(xySeries,xAxisLabel,yAxisLabel)) ;
		removeLegend() ;
	}

	/**
	 * Sets the time range.
	 * 
	 * @param lower  the lower date limit.
	 * @param upper  the upper date limit.
	 */
	public abstract void setTimeRange(Date lower, Date upper) ;

}