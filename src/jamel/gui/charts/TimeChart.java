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

import java.awt.Paint;
import java.util.Date;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition ;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


/**
 * A class for time charts.
 */
@SuppressWarnings("serial")
public class TimeChart extends JamelChart {

	/**
	 * Returns a new time plot.
	 * @param timeSeries  the time series.
	 * @param valueAxisLabel the value axis label.
	 * @return a new time plot.
	 */
	private static Plot newTimePlot(TimeSeries[] timeSeries, String valueAxisLabel) {
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.setXPosition(TimePeriodAnchor.MIDDLE);
		for (int i = 0; i < timeSeries.length; i++) {
			TimeSeries series = timeSeries[i];
			if (series==null)
				throw new RuntimeException();
			dataset.addSeries(series);
		}
		final XYPlot plot = new  XYPlot(dataset,new DateAxis(),new NumberAxis(valueAxisLabel),new XYLineAndShapeRenderer(true,false));
		((DateAxis) plot.getDomainAxis()).setAutoRange(false); 
		((DateAxis) plot.getDomainAxis()).setTickMarkPosition(DateTickMarkPosition.MIDDLE); 
		plot.setDomainGridlinesVisible(false);
		return plot ;
	}

	/**
	 * Creates a new time chart.
	 * @param title the title.
	 * @param valueAxisLabel the value axis label.
	 * @param timeSeries  the time series.
	 */
	public TimeChart(String title, String valueAxisLabel, TimeSeries... timeSeries) {
		super(title, newTimePlot(timeSeries,valueAxisLabel)) ;
	}

	/**
	 * Sets the time range.
	 * 
	 * @param lower  the lower date limit.
	 * @param upper  the upper date limit.
	 */
	public void setTimeRange(Date lower, Date upper) {
		setNotify(true) ;
		try { ((DateAxis) ((XYPlot)getPlot()).getDomainAxis()).setRange(lower, upper) ; }
		catch (ClassCastException e) {}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}		
		setNotify(false) ;
	}

	/**
	 * Sets a bar render.
	 * @param index  the index.
	 */
	public void setXYBarRender(int index) {
		final XYBarRenderer renderer = new XYBarRenderer(0.10);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setShadowVisible(false);
		((XYPlot)this.getPlot()).setRenderer(index, renderer);
	}

	/**
	 * Sets a difference renderer.
	 * 
	 * @param index  the index.
	 * @param positivePaint  the paint used to highlight positive differences.
	 * @param negativePaint  the paint used to highlight negative differences.
	 */
	public void setXYDifferenceRenderer(
			int index, 
			Paint positivePaint,
			Paint negativePaint
	) {
		XYDifferenceRenderer renderer = new XYDifferenceRenderer();
		renderer.setPositivePaint(positivePaint);
		renderer.setNegativePaint(negativePaint);
		XYPlot plot = (XYPlot) getPlot();
		plot.setRenderer(index, renderer);
	}
	
}