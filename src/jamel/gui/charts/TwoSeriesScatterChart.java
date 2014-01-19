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
import java.awt.Paint;
import java.util.Date;
import java.util.TimeZone;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A scatter chart (type Phillips Curve and Beveridge Curve).
 */
@SuppressWarnings("serial")
public
class TwoSeriesScatterChart extends AbstractScatterChart {
	
	/** begin */
	private Date begin ;
	
	/** end */
	private Date end ;
	
	/** xTimeSeries */
	private final TimeSeries xTimeSeries ;
	
	/** yTimeSeries */
	private final TimeSeries yTimeSeries ;


	/**
	 * Creates a new scatter chart.
	 * @param title  the title.
	 * @param xTimeSeries  the x time series.
	 * @param xLabel  the label of the x axis.
	 * @param yTimeSeries  the y time series.
	 * @param yLabel  the label of the y axis.
	 * @param color  the paint.
	 */
	public TwoSeriesScatterChart(String title,TimeSeries xTimeSeries, String xLabel, TimeSeries yTimeSeries, String yLabel, Paint color) {
		super(title, null, xLabel, yLabel ) ;
		this.xTimeSeries = xTimeSeries ;	
		this.yTimeSeries = yTimeSeries ;	
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0,Color.GRAY);
		renderer.setSeriesFillPaint(0,color);
		renderer.setUseFillPaint(true);
		((XYPlot) getPlot()).setRenderer(renderer);
	}


	/**
	 * Sets the time range.
	 * 
	 * @param lower  the lower date limit.
	 * @param upper  the upper date limit.
	 */
	public void setTimeRange(Date lower, Date upper) {
		XYSeriesCollection dataset = (XYSeriesCollection) ((XYPlot) this.getPlot()).getDataset() ;	// nouvelle collection de s�ries
		if (((this.begin==null) | (this.end==null)) || ((!this.begin.equals(lower)) | (!this.end.equals(upper)))) {
			this.begin = lower ;
			this.end = upper ;
			int minIndex = 0 ;									// d�finit l'index des donn�es correspondant � la date de d�but
			try { minIndex = this.xTimeSeries.getIndex(RegularTimePeriod.createInstance(this.xTimeSeries.getTimePeriodClass(), lower, TimeZone.getDefault())); }
			catch (IllegalArgumentException i) {}
			if (minIndex<0) minIndex=0 ;															// rectifie �ventuellement le r�sultat
			int maxIndex = this.xTimeSeries.getItemCount()-1 ;
			try {	maxIndex =	this.xTimeSeries.getIndex(RegularTimePeriod.createInstance(this.xTimeSeries.getTimePeriodClass(), upper, TimeZone.getDefault())) ;}
			catch (IllegalArgumentException i) {}// d�finit l'index des donn�es correspondant � la date de fin
			if (maxIndex<0) maxIndex=this.xTimeSeries.getItemCount()-1 ;							// rectifie �ventuellement le r�sultat
			XYSeries newSeries = new XYSeries("XY Data",false);										// cr�e une nouvelle s�rie
			for (int index=minIndex; index<=maxIndex-1; index++) {
				RegularTimePeriod currentPeriod = this.xTimeSeries.getTimePeriod(index) ;			// r�cup�re la p�riode courante
				if (this.yTimeSeries.getValue(currentPeriod)!=null) newSeries.add(this.xTimeSeries.getValue(currentPeriod),this.yTimeSeries.getValue(currentPeriod)) ;
			}
			try { dataset.removeSeries(0) ; }														// efface la s�rie actuelle
			catch (IllegalArgumentException i) {}													// au cas o� il n'y aurait pas de s�ries � enlever (la premi�re fois)
			dataset.addSeries(newSeries) ;
		}
		
	}

}