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
import java.awt.Paint;
import java.awt.Shape;
import java.util.Date;

import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYZDataset;


/**
 * A class for xyz instant chart.
 */
@SuppressWarnings("serial")
public class InstantXYZScatterChart extends AbstractScatterChart {

		
	/** The x label. */
	private final String xLabel;
	
	/** The y label. */
	private final String yLabel;
	
	/** The z label. */
	private final String zLabel;


	/**
	 * Initialize the renderer.
	 */
	private void initXYShapeRenderer() {
		XYShapeRenderer renderer = new XYShapeRenderer();
		renderer.setSeriesOutlinePaint(0, Color.LIGHT_GRAY);
		renderer.setDrawOutlines(true);
		Shape shape = new java.awt.geom.Ellipse2D.Double(0,0,7,7);
		renderer.setSeriesShape(0, shape);
		try {
			this.getXYPlot().setRenderer(1,renderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Sets the color scale.
	 * @param lowerBound the lower bound.
	 * @param upperBound the upper bound.
	 */
	private void setColorScale(double lowerBound, double upperBound) {
		if (upperBound==lowerBound) upperBound++;
		GrayPaintScale colorPaintScale = new GrayPaintScale(lowerBound, upperBound) {
			public Paint getPaint(double value) {
				double v = Math.max(value, this.getLowerBound());
				v = Math.min(v, this.getUpperBound());
				int g = 255-(int) ((v - this.getLowerBound())*2 / (this.getUpperBound() - this.getLowerBound()) * 255.0);
				if (g>=0) return new Color(255, 255-g, 0);
				return new Color(255+g, 255, 0);
			}			
		};
		XYShapeRenderer renderer = (XYShapeRenderer) this.getXYPlot().getRenderer(1);
		renderer.setPaintScale(colorPaintScale);
	}	

	/**
	 * Creates a new chart.
	 * @param title the title of the chart.
	 * @param xLabel the label of the x data;
	 * @param yLabel the label of the y data;
	 * @param zLabel the label of the z data;
	 */
	public InstantXYZScatterChart(
			String title, 
			String xLabel, 
			String yLabel,
			String zLabel
			) {
		super(title, null, xLabel, yLabel ) ;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.zLabel = zLabel;
		this.initXYShapeRenderer();
	}
	

	/**
	 * Update the chart.
	 * @param minDate the min date.
	 * @param maxDate the max date.
	 */
	public void setTimeRange(Date minDate, Date maxDate) {		
		double[][] data = Circuit.getCrossSectionSeries(xLabel,yLabel,zLabel) ;
		if (data==null) return;
		if (data[2].length==0) return;
		double min = 0;
		double max = data[2][0];
		for (int count=1; count<data[2].length; count ++) {
			if (data[2][count]>max) max=data[2][count];
		}
		setColorScale(min,max);
		DefaultXYZDataset dataset = new DefaultXYZDataset();
		dataset.addSeries("Data test", data );
		try {
			this.getXYPlot().setDataset(1,dataset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}