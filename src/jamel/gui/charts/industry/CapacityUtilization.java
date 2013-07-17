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

package jamel.gui.charts.industry;

import jamel.Circuit;
import jamel.gui.charts.JamelColor;
import jamel.gui.charts.TimeChart;
import jamel.util.data.Labels;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A <code>ChartPanel</code> that contains 
 * a time chart with the wages series.
 */
@SuppressWarnings("serial")
public class CapacityUtilization extends ChartPanel {
	
	/**
	 * Returns the chart.
	 * @return the chart.
	 */
	private static JFreeChart newChart() {
		TimeChart chart = new TimeChart("Capacity Utilization", "Percent",
				Circuit.getCircuit().getTimeSeries().get(Labels.annualCapacityUtilizationIntermediate),
				Circuit.getCircuit().getTimeSeries().get(Labels.annualCapacityUtilizationFinal)
				);
		chart.setColors( 0, JamelColor.intermediateColor, JamelColor.finalColor);
		chart.addLineLegendItem("Intermediate Sector", JamelColor.intermediateColor);
		chart.addLineLegendItem("Final Sector", JamelColor.finalColor);
		chart.getXYPlot().getRangeAxis().setRange(0,100);
		return chart;
	}
	
	/**
	 * Creates the <code>ChartPanel</code>.
	 */
	public CapacityUtilization() {
		super(newChart());
	}

}
