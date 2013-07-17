package jamel.gui.charts.labor;

import jamel.Circuit;
import jamel.gui.charts.TwoSeriesScatterChart;
import jamel.util.data.Labels;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A <code>ChartPanel</code> that contains 
 * a time chart with the wages series.
 */
@SuppressWarnings("serial")
public class BeveridgeCurve extends ChartPanel {
	
	/**
	 * Returns the chart.
	 * @return the chart.
	 */
	private static JFreeChart newChart() {
		TwoSeriesScatterChart chart = new TwoSeriesScatterChart(
				"Beveridge Curve", 
				Circuit.getCircuit().getTimeSeries().get(Labels.ANNUAL_UNEMPLOYMENT_RATE),
				"Unemployment (%)",
				Circuit.getCircuit().getTimeSeries().get(Labels.vacanciesRateAnnual),
				"Vacancies (%)"
		);
		chart.getXYPlot().getRangeAxis().setRange(0,15);
		chart.getXYPlot().getDomainAxis().setRange(0,30);
		chart.setIntegerTickUnitsOnDomainAxis();
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}
	
	/**
	 * Creates the <code>ChartPanel</code>.
	 */
	public BeveridgeCurve() {
		super(newChart());
	}

}
