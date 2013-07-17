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

package jamel.gui.charts.money;

import jamel.Circuit;
import jamel.gui.charts.JamelColor;
import jamel.gui.charts.TimeChart;
import jamel.util.data.Labels;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A <code>ChartPanel</code> that contains a time chart 
 * with the main ratios of the bank sector.
 */
@SuppressWarnings("serial")
public class BankRatios extends ChartPanel {
	
	/**
	 * Returns the chart.
	 * @return the chart.
	 */
	private static JFreeChart newChart() {
		final TimeChart chart = new TimeChart(
				"Bank Ratios", "Percent",
				Circuit.getCircuit().getTimeSeries().get(Labels.CAPITAL_ADEQUACY_RATIO),
				Circuit.getCircuit().getTimeSeries().get(Labels.DOUBTFUL_DEBTS_RATIO)
				);
		chart.setXYDifferenceRenderer(0, JamelColor.ULTRA_TRANSPARENT_GREEN, JamelColor.ULTRA_TRANSPARENT_RED);
		chart.setColors(0, ChartColor.LIGHT_GREEN, ChartColor.LIGHT_RED);
		chart.getXYPlot().getRangeAxis().setRange(0,70);
		return chart;
	}
	
	/**
	 * Creates the <code>ChartPanel</code>.
	 */
	public BankRatios() {
		super(newChart());
	}

}
