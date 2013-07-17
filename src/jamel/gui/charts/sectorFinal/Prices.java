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

package jamel.gui.charts.sectorFinal;

import jamel.Circuit;
import jamel.gui.charts.JamelColor;
import jamel.gui.charts.TimeChart;
import jamel.util.data.Labels;

import java.awt.Color;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A <code>ChartPanel</code> that contains a time chart with the prices of the final sector.
 */
@SuppressWarnings("serial")
public class Prices extends ChartPanel {
	
	/**
	 * Returns the chart.
	 * @return the chart.
	 */
	private static JFreeChart newChart() {
		final TimeChart chart = new TimeChart("Final Good Prices", "Value",
				Circuit.getCircuit().getTimeSeries().get(Labels.PRICE_FINAL_MIN), 
				Circuit.getCircuit().getTimeSeries().get(Labels.PRICE_FINAL_MAX), 
				Circuit.getCircuit().getTimeSeries().get(Labels.PRICE_FINAL_MEDIAN)
				);
		chart.setXYDifferenceRenderer(0, JamelColor.ULTRA_TRANSPARENT_RED, JamelColor.ULTRA_TRANSPARENT_RED);
		chart.setColors( 0, JamelColor.VERY_LIGHT_RED, JamelColor.VERY_LIGHT_RED, Color.RED);
		chart.addLineLegendItem("Median Price", Color.RED);
		chart.addLegendItem("Min-Max Prices", JamelColor.VERY_LIGHT_RED);
		return chart;
	}
	
	/**
	 * Creates the <code>ChartPanel</code>.
	 */
	public Prices() {
		super(newChart());
	}

}
