/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel.gui.charts;

import jamel.util.data.Labels;
import jamel.util.data.TimeseriesCollection;

import java.awt.Color;

import org.jfree.chart.ChartColor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;

/**
 * A factory for creating new {@link JFreeChart} instances.
 */
@SuppressWarnings("javadoc")
public class ChartFactory {

    /** averageColor */
	public final static Color AVERAGE_COLOR = ChartColor.LIGHT_GREEN;

	/** LIGHT_TRANSPARENT_BLUE */
	public static final Color LIGHT_TRANSPARENT_BLUE = new Color(0.5f,0.5f,1,0.7f) ;

	/** LIGHT_TRANSPARENT_GREEN */
	public static final Color LIGHT_TRANSPARENT_GREEN = new Color(0.5f,1,0.5f,0.7f) ;
	
	/** LIGHT_TRANSPARENT_RED */
	public static final Color LIGHT_TRANSPARENT_RED = new Color(1,0.5f,0.5f,0.7f) ;
	
	/** finalColor */
	public final static Color SECTOR1COLOR = ChartColor.LIGHT_BLUE;

	/** intermediateColor */
	public final static Color SECTOR2COLOR = ChartColor.LIGHT_RED;
	
	/** ULTRA_LIGHT_BLUE */
	public static final Color ULTRA_LIGHT_BLUE = new Color(0xDD, 0xDD, 0xFF);
	
	/** ULTRA_LIGHT_GREEN */
	public static final Color ULTRA_LIGHT_GREEN = new Color(0xDD, 0xFF, 0xDD);

	/** ULTRA_LIGHT_RED */
	public static final Color ULTRA_LIGHT_RED = new Color(0xFF, 0xDD, 0xDD);
	
	/** ULTRA_TRANSPARENT_BLUE */
	public static final Color ULTRA_TRANSPARENT_BLUE = new Color(0.9f,0.9f,1,0.7f) ;
	
	/** ULTRA_TRANSPARENT_GREEN */
	public static final Color ULTRA_TRANSPARENT_GREEN = new Color(0.9f,1,0.9f,0.7f) ;

	/** ULTRA_TRANSPARENT_RED */
	public static final Color ULTRA_TRANSPARENT_RED = new Color(1,0.9f,0.9f,0.7f) ;
	
	/** VERY_LIGHT_RED */
	public static final Color VERY_LIGHT_RED = ChartColor.VERY_LIGHT_RED ;
	
	/** VERY_TRANSPARENT_BLUE */
	public static final Color VERY_TRANSPARENT_BLUE = new Color(0.7f,0.7f,1,0.7f) ;

	/** VERY_TRANSPARENT_GREEN */
	public static final Color VERY_TRANSPARENT_GREEN = new Color(0.7f,1,0.7f,0.7f) ;

	/** VERY_TRANSPARENT_RED */
	public static final Color VERY_TRANSPARENT_RED = new Color(1,0.7f,0.7f,0.7f) ;

	public static JFreeChart getBankDividend(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Bank Dividend", 
				"Value",
				data.get(Labels.BANK_DIVIDEND)
				);
		chart.setColors(0, ChartColor.LIGHT_BLUE);
		return chart;
	}

	public static JFreeChart getBankRatios(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Bank Ratios", 
				"Percent",
				data.get(Labels.CAPITAL_ADEQUACY_RATIO),
				data.get(Labels.DOUBTFUL_DEBTS_RATIO)
				);
		chart.setXYDifferenceRenderer(0, ULTRA_TRANSPARENT_GREEN, ULTRA_TRANSPARENT_RED);
		chart.setColors(0, ChartColor.LIGHT_GREEN, ChartColor.LIGHT_RED);
		chart.getXYPlot().getRangeAxis().setRange(0,70);
		return chart;
	}

	public static JFreeChart getBankruptcies(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Bankruptcies", 
				"Firms",
				data.get(Labels.annualBankruptciesTotal)
				);
		final XYBarRenderer renderer = new XYBarRenderer(0.10);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setShadowVisible(false);
		((XYPlot) chart.getPlot()).setRenderer(0, renderer);
		chart.setIntegerTickUnitsOnRangeAxis();
		chart.setColors(0, Color.LIGHT_GRAY);
		chart.addLegendItem("Bankruptcies", Color.LIGHT_GRAY);
		return chart;
	}

	public static JFreeChart getBankruptcyRate(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Bankruptcy Rates", "Percent",
				data.get(Labels.annualBankruptcyRateIntermediate),
				data.get(Labels.annualBankruptcyRateFinal)
				);
		chart.setColors( 0,SECTOR2COLOR,SECTOR1COLOR,AVERAGE_COLOR);
		chart.addLineLegendItem("Sector 1", SECTOR2COLOR);
		chart.addLineLegendItem("Sector 2", SECTOR1COLOR);
		chart.getXYPlot().getRangeAxis().setRange(0,10);
		return chart;
	}

	static public JFreeChart getBeveridgeCurve(TimeseriesCollection data){
		final TwoSeriesScatterChart chart = new TwoSeriesScatterChart(
				"Beveridge Curve", 
				data.get(Labels.ANNUAL_UNEMPLOYMENT_RATE),
				"Unemployment (%)",
				data.get(Labels.vacanciesRateAnnual),
				"Vacancies (%)",
				LIGHT_TRANSPARENT_RED
				);
		chart.getXYPlot().getRangeAxis().setRange(4,12);
		chart.getXYPlot().getDomainAxis().setRange(0,30);
		chart.setIntegerTickUnitsOnDomainAxis();
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	public static JFreeChart getCapacityUtilization(
			TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Capacity Utilization", "Percent",
				data.get(Labels.annualCapacityUtilizationIntermediate),
				data.get(Labels.annualCapacityUtilizationFinal)
				);
		chart.setColors( 0, SECTOR2COLOR, SECTOR1COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		chart.getXYPlot().getRangeAxis().setRange(0,100);
		return chart;
	}

	public static JFreeChart getDebt(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Credits", 
				"Value",
				data.get(Labels.DEBT)
				);
		chart.setColors(0, ChartColor.LIGHT_GREEN);
		return chart;
	}

	public static JFreeChart getDistribution(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Income Distribution", "Percent",
				data.get(Labels.ANNUAL_PROFIT_SHARE), 
				data.get(Labels.ANNUAL_WAGE_SHARE)
				);
		chart.setRangeAxisRange(0, 100);
		chart.setColors(0,ChartColor.DARK_YELLOW,ChartColor.VERY_LIGHT_BLUE) ;
		return chart;
	}

	static public JFreeChart getFinalGoodsMarket(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Final Good Market", 
				"Volume",
				data.get(Labels.annualSalesFinalVolume),
				data.get(Labels.annualProductionFinalVolume)
				);
		chart.addLineLegendItem("Production",ChartColor.LIGHT_RED);
		chart.addLineLegendItem("Consumption",ChartColor.LIGHT_BLUE);
		return chart;
	}
	public static JFreeChart getFirms(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Firms", "Firms",
				data.get(Labels.firmsIntermediate),
				data.get(Labels.firmsFinal)
				);
		chart.setColors(0, SECTOR2COLOR, SECTOR1COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		return chart;
	}
	
	public static JFreeChart getFLeverage(TimeseriesCollection data) {
		TimeChart chart = new TimeChart(
				"Firms Leverage",
				"Value",
				data.get(Labels.DEBT),
				data.get(Labels.DEBT_TARGET)
				);
		chart.setColors(0, Color.red,ULTRA_LIGHT_RED);
		chart.addLineLegendItem("Leverage", Color.red);
		chart.addLineLegendItem("Target", Color.red);
		return chart;
	}
	
	public static JFreeChart getFPessimism(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Firms Pessimism", "Percent",
				data.get(Labels.fPessimism)
				);
		chart.setColors(0, Color.red);
		chart.addLineLegendItem("Pessimists", Color.red);
		chart.setRangeAxisRange(0, 100);
		return chart;
	}

	public static JFreeChart getFSelfFinancing(TimeseriesCollection data) {
		TimeChart chart = new TimeChart(
				"Firms Self-financing",
				"Percent",
				data.get(Labels.selfFinancingRatio),
				data.get(Labels.fPessimism)
				);
		chart.setColors(0, Color.red,ULTRA_LIGHT_RED);
		chart.addLineLegendItem("Self-financing ratio", Color.red);
		chart.setRangeAxisRange(0, 50);
		return chart;
	}

	public static JFreeChart getGPessimism(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Pessimism", "Percent",
				data.get(Labels.fPessimism),
				data.get(Labels.hPessimism)
				);
		chart.setColors(0, Color.red,Color.blue);
		chart.addLineLegendItem("Firms", Color.red);
		chart.addLineLegendItem("Households", Color.blue);
		chart.setRangeAxisRange(0, 100);
		return chart;
	}

	public static JFreeChart getHHoarding(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Households hoarding", "Percent",
				data.get(Labels.H_HOARD2INCOME_RATIO),
				data.get(Labels.hPessimism)
				);
		chart.setColors(0, Color.blue,ULTRA_LIGHT_BLUE);
		chart.addLineLegendItem("Hoarding to Income Ratio", Color.blue);
		chart.setRangeAxisRange(0, 50);
		return chart;
	}

	public static JFreeChart getHouseholdsLiquidSaving(TimeseriesCollection data) {
		TimeChart chart = new TimeChart(
				"Households Deposits",
				"Percent",
				data.get(Labels.HOUSEHOLDS_DEPOSITS),
				data.get(Labels.H_SAVING_TARGET)
				);
		chart.setColors(0,Color.red,Color.blue);
		chart.addLineLegendItem("Deposits", Color.red);
		chart.addLineLegendItem("Target", Color.blue);
		chart.setRangeAxisRange(0, 50);
		return chart;
	}

	public static JFreeChart getHPessimism(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Households Pessimism", "Percent",
				data.get(Labels.hPessimism),
				data.get(Labels.ANNUAL_UNEMPLOYMENT_RATE)
				);
		chart.setColors(0, Color.blue, Color.orange);
		chart.addLineLegendItem("Pessimists", Color.blue);
		chart.addLineLegendItem("Unemployed", Color.orange);
		chart.setRangeAxisRange(0, 100);
		return chart;
	}

	public static JFreeChart getInflationUnemployment(
			TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Inflation & Unemployment", "Percent",
				data.get(Labels.ANNUAL_INFLATION_RATE),
				data.get(Labels.ANNUAL_UNEMPLOYMENT_RATE)
				);
		return chart;
	}

	public static JFreeChart getInventoryLevel(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Inventories", "Months of Production",
				data.get(Labels.inventoryIntermediateLevel),
				data.get(Labels.inventoryFinalLevel)
				) ;
		chart.setColors( 0,SECTOR2COLOR,SECTOR1COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		return chart;
	}

	public static JFreeChart getInventoryVolumeFinal(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart("Inventories", "Volume",
				data.get(Labels.inventoryFinalVolume)				
				);
		chart.setXYBarRender(0);
		chart.setColors( 0, ChartColor.VERY_LIGHT_GREEN);
		chart.addLegendItem("Final goods", ChartColor.VERY_LIGHT_GREEN);
		return chart;
	}

	static public JFreeChart getJobSeekers(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Job Seekers", 
				"Workers",
				data.get(Labels.UNEMPLOYED)
				);
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	static public JFreeChart getJobVacancies(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Job Vacancies", 
				"Jobs",
				data.get(Labels.VACANCIES)
				);
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	static public JFreeChart getLabourMarket(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Labour Market", 
				"Workers, Jobs",
				data.get(Labels.UNEMPLOYED),
				data.get(Labels.JOB_OFFERS)				
				);
		chart.setXYDifferenceRenderer(0, ULTRA_TRANSPARENT_RED, ULTRA_TRANSPARENT_BLUE);
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	public static JFreeChart getLiabilitiesAndBankCapital(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Liabilities and bank capital", 
				"Value",
				data.get(Labels.FIRMS_DEPOSITS),
				data.get(Labels.HOUSEHOLDS_DEPOSITS),
				data.get(Labels.BANK_CAPITAL)
				);
		chart.setColors(0, ChartColor.LIGHT_RED, ChartColor.LIGHT_BLUE, ChartColor.LIGHT_GREEN);
		chart.addLineLegendItem("Firms deposits",ChartColor.LIGHT_RED);
		chart.addLineLegendItem("Households deposits",ChartColor.LIGHT_BLUE);
		chart.addLineLegendItem("Bank capital",ChartColor.LIGHT_GREEN);
		return chart;
	}

	public static JFreeChart getMarkup(TimeseriesCollection gata) {
		TimeChart chart = new TimeChart("Markups", "Percent",
				gata.get(Labels.markupIntermediateAnnual),
				gata.get(Labels.markupFinalAnnual)
				) ;
		chart.setColors( 0,SECTOR2COLOR,SECTOR1COLOR,AVERAGE_COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		return chart;
	}

	public static JFreeChart getMoney(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Money Supply", 
				"Value",
				data.get(Labels.DEPOSITS)
				);
		chart.setColors(0, ChartColor.LIGHT_GREEN);
		return chart;
	}

	public static JFreeChart getMoneyVelocity(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Velocity of Money", 
				"Frequency",
				data.get(Labels.MONEY_VELOCITY)
				);
		chart.setColors(0, ChartColor.LIGHT_GREEN);
		return chart;
	}

	public static JFreeChart getPhillipsCurve(TimeseriesCollection data) {
		TwoSeriesScatterChart chart = new TwoSeriesScatterChart(
				"Phillips Curve",
				data.get(Labels.ANNUAL_UNEMPLOYMENT_RATE),
				"Unemployment (%)",
				data.get(Labels.ANNUAL_INFLATION_RATE),
				"Inflation (%)",
				LIGHT_TRANSPARENT_RED
				);
		chart.getXYPlot().getRangeAxis().setRange(-10,20);
		chart.getXYPlot().getDomainAxis().setRange(0,30);
		chart.setIntegerTickUnitsOnDomainAxis();
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	public static JFreeChart getPrecautionarySavings(TimeseriesCollection data) {
		TimeChart chart = new TimeChart(
				"Precautionary Behaviors",
				"Percent",
				data.get(Labels.selfFinancingRatio),
				data.get(Labels.H_HOARD2INCOME_RATIO)
				);
		chart.setColors(0,Color.red,Color.blue);
		chart.addLineLegendItem("Firms Self-financing", Color.red);
		chart.addLineLegendItem("Households Hoardings", Color.blue);
		chart.setRangeAxisRange(0, 50);
		return chart;
	}

	static public JFreeChart getPrices(TimeseriesCollection data) {
		final TimeChart chart = new TimeChart(
				"Final Good Prices", 
				"Value",
				data.get(Labels.PRICE_FINAL_MIN), 
				data.get(Labels.PRICE_FINAL_MAX), 
				data.get(Labels.PRICE_FINAL_MEDIAN)
				);
		chart.setXYDifferenceRenderer(0, ULTRA_TRANSPARENT_RED, ULTRA_TRANSPARENT_RED);
		chart.setColors( 0, VERY_LIGHT_RED, VERY_LIGHT_RED, Color.RED);
		chart.addLineLegendItem("Median Price", Color.RED);
		chart.addLegendItem("Min-Max Prices", VERY_LIGHT_RED);
		return chart;
	}

	public static JFreeChart getProfits(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Profit ratios", "Volume",
				data.get(Labels.realProfitIntermediate),
				data.get(Labels.realProfitFinal)
				);
		chart.setColors( 0,SECTOR2COLOR,SECTOR1COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		return chart;
	}

	public static JFreeChart getRelativePrices(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Relative Prices", "",
				data.get(Labels.relativePrices)				
				);
		chart.setColors( 0,AVERAGE_COLOR);
		chart.addLineLegendItem("Final Price to Intermediate Price Ratio", AVERAGE_COLOR);
		return chart;
	}

	static public JFreeChart getUnemployment(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Unemployment & Vacancies", 
				"Workers, Jobs",
				data.get(Labels.UNEMPLOYED),
				data.get(Labels.VACANCIES)
				);
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	static public JFreeChart getUnemploymentDuration(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Unemployment Duration", 
				"Months",
				data.get(Labels.UNEMPLOYMENT_DURATION)
				);
		return chart;
	}

	static public JFreeChart getUnemploymentRate(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Unemployment Rate", 
				"Percent",
				data.get(Labels.ANNUAL_UNEMPLOYMENT_RATE)
				);
		chart.setColors( 0, ChartColor.LIGHT_BLUE);
		return chart;
	}

	static public JFreeChart getVacancyRate(TimeseriesCollection data){
		final TimeChart chart = new TimeChart(
				"Vacancies rate", 
				"Percent",
				data.get(Labels.vacanciesRateAnnual)
				);
		chart.setIntegerTickUnitsOnRangeAxis();
		return chart;
	}

	public static JFreeChart getVacancyRates(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Vacancy Rates", "Percent",
				data.get(Labels.vacancyRateIntermediate),
				data.get(Labels.vacancyRateFinal)
				) ;
		chart.setColors( 0,SECTOR2COLOR,SECTOR1COLOR);
		chart.addLineLegendItem("Intermediate Sector", SECTOR2COLOR);
		chart.addLineLegendItem("Final Sector", SECTOR1COLOR);
		chart.getXYPlot().getRangeAxis().setRange(0,100);
		return chart;
	}

	public static JFreeChart getWages(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Wages", "Value",
				data.get(Labels.MAX_REGULAR_WAGE),
				data.get(Labels.MIN_REGULAR_WAGE),
				data.get(Labels.MEDIAN_WAGE)
				);
		chart.setXYDifferenceRenderer(0, ULTRA_TRANSPARENT_BLUE, ULTRA_TRANSPARENT_BLUE );
		chart.setColors(0, ChartColor.VERY_LIGHT_BLUE, ChartColor.VERY_LIGHT_BLUE, ChartColor.LIGHT_BLUE, ChartColor.LIGHT_RED, ChartColor.LIGHT_RED);
		chart.addLineLegendItem("Median Wage", ChartColor.LIGHT_BLUE);
		chart.addLegendItem("Min-Max Wages", ULTRA_LIGHT_BLUE);
		return chart;
	}

	public static JFreeChart getWorkers(TimeseriesCollection data) {
		TimeChart chart = new TimeChart("Workers", "Workers",
				data.get(Labels.workersInSector1),
				data.get(Labels.workersInSector2)
				);
		chart.setColors(0, SECTOR2COLOR, SECTOR1COLOR);
		chart.addLineLegendItem("Sector 1", SECTOR2COLOR);
		chart.addLineLegendItem("Sector 2", SECTOR1COLOR);
		return chart;
	}

}























