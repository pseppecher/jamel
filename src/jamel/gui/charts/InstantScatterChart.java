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

import jamel.Circuit;

import java.awt.Color;
import java.util.Date;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A scatter chart to represent the dispersion of agents at a given time. 
 */
@SuppressWarnings("serial")
public class InstantScatterChart extends AbstractScatterChart {

	/** The label of the x series. */
	private final String xLabel;
	
	/** The label of the y series. */
	private final String yLabel;

	/**
	 * Creates a new instant chart.
	 * 
	 * @param title  the title.
	 * @param xLabel  the label of the x series.
	 * @param yLabel  the label of the y series.
	 */
	public InstantScatterChart(String title, String xLabel, String yLabel) {
		super(title, null, xLabel, yLabel) ;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseLinesVisible(false);
		renderer.setSeriesPaint(0,new Color(0.3f,0.3f,0.3f,0.5f));
		renderer.setSeriesFillPaint(0,new Color(0.3f,0.3f,1f,0.5f));
		renderer.setUseFillPaint(true);
		((XYPlot) getPlot()).setRenderer(renderer);
	}

	/**
	 * Sets the time range.
	 * 
	 * @param lower  the lower date limit (not used).
	 * @param upper  the upper date limit (not used).
	 */
	public void setTimeRange(Date lower, Date upper) {		
		XYSeriesCollection dataset = (XYSeriesCollection) ((XYPlot) this.getPlot()).getDataset() ;
		XYSeries newSeries = Circuit.getCrossSectionSeries(xLabel,yLabel) ;
		if (newSeries == null) return;
		try { dataset.removeSeries(0) ; }														
		catch (IllegalArgumentException i) {}
		dataset.addSeries(newSeries) ;
	}

}