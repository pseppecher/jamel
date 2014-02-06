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

import java.awt.Color;
import java.util.Date;

import jamel.JamelObject;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;


/**
 * A class for histograms.
 */
@SuppressWarnings("serial")
public class Histogram extends JamelChart {

	/** The number of bars. */
	private static final int bins = 20;

	/**
	 * Returns a new plot.
	 * @param dataset  the dataset.
	 * @param hAxisLabel  the label for the x axis.
	 * @param vAxisLabel  the label for the y axis.
	 * @param color  the color of the chart.
	 * @return the new plot.
	 */
	static private Plot newPlot(HistogramDataset dataset, String hAxisLabel, String vAxisLabel, Color color) {
		NumberAxis xAxis = new NumberAxis(hAxisLabel);
        xAxis.setAutoRangeIncludesZero(false);
        ValueAxis yAxis = new NumberAxis(vAxisLabel);
        XYBarRenderer renderer = new XYBarRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setShadowVisible(false);
		renderer.setMargin(0.05);
		renderer.setSeriesPaint(0,color);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);		
        return plot ;
	}

	/** Title */
	private String baseTitle = "No Title" ;

	/** An access to the data. */
	private final HistogramDataAccess dataAccess ;

	/**
	 * Creates a new histogram.
	 * @param title  the title.
	 * @param dataAccess  the access to the data.
	 * @param hAxisLabel  the label for the x axis.
	 * @param vAxisLabel  the label for the y axis.
	 * @param color  the color of the chart.
	 */
	public Histogram(
			String title, 
			HistogramDataAccess dataAccess, 
			String hAxisLabel, String vAxisLabel, 
			Color color
	) {
		super(title, newPlot(dataAccess.getData(bins),hAxisLabel,vAxisLabel, color)) ;
		this.baseTitle = title;
		this.dataAccess = dataAccess;
		removeLegend();
	}


	/**
	 * Met � jour le graphique avec les derni�res donn�es disponibles.
	 * @see jamel.gui.charts.JamelChart#setTimeRange(java.util.Date, java.util.Date)
	 */
	public void setTimeRange(Date minDate, Date maxDate) {
		final HistogramDataset dataset = dataAccess.getData(bins);
		if (dataset != null) {
			setNotify(true);
			XYPlot plot=(XYPlot) getPlot();
			plot.setDataset(dataset);
			this.setTitle(this.baseTitle+" ("+JamelObject.getCurrentPeriod().toString()+")");
			setNotify(false);		
		}
	}

}