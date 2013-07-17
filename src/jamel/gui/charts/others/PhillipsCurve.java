package jamel.gui.charts.others;

import jamel.Circuit;
import jamel.gui.charts.TwoSeriesScatterChart;
import jamel.util.data.Labels;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A <code>ChartPanel</code> that contains the Phillips curve.
 */
@SuppressWarnings("serial")
public class PhillipsCurve extends ChartPanel {
	
	/**
	 * Returns the chart.
	 * @return the chart.
	 */
	private static JFreeChart newChart() {
		TwoSeriesScatterChart chart = new TwoSeriesScatterChart(
				"Phillips Curve",
				Circuit.getCircuit().getTimeSeries().get(Labels.ANNUAL_UNEMPLOYMENT_RATE),
				"Unemployment (%)",
				Circuit.getCircuit().getTimeSeries().get(Labels.ANNUAL_INFLATION_RATE),
				"Inflation (%)");
		chart.getXYPlot().getRangeAxis().setRange(-5,10);
		chart.getXYPlot().getDomainAxis().setRange(0,30);
		chart.setIntegerTickUnitsOnDomainAxis();
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}
	
	/**
	 * Creates the <code>ChartPanel</code>.
	 */
	public PhillipsCurve() {
		super(newChart());
	}

}
