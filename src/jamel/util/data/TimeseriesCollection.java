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

package jamel.util.data;

import jamel.JamelObject;

import java.util.HashMap;
import java.util.Map;

import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;

/**
 * A class for the times series.
 */
@SuppressWarnings("serial")
public class TimeseriesCollection  extends HashMap<String, TimeSeries>{

	/**
	 * Creates the series set.
	 */
	public TimeseriesCollection() {
		initialize();
	}

	/**
	 * Creates each time series.
	 */
	private void initialize() {

		this.newSeries(Labels.DEBT_TARGET);
		this.newSeries(Labels.H_SAVING_TARGET);
		this.newSeries(Labels.selfFinancingRatio);
		this.newSeries(Labels.H_HOARD2INCOME_RATIO);
		this.newSeries(Labels.fPessimism);

		this.newSeries(Labels.CONSUMPTION_VALUE);
		this.newSeries(Labels.CONSUMPTION_VOLUME);
		this.newSeries(Labels.DIVIDENDS);
		this.newSeries(Labels.EMPLOYED);
		this.newSeries(Labels.HOUSEHOLDS);
		this.newSeries(Labels.INCOME);
		this.newSeries(Labels.INVOLUNTARY_UNEMPLOYED);
		this.newSeries(Labels.MAX_REGULAR_WAGE);
		this.newSeries(Labels.MEDIAN_WAGE);
		this.newSeries(Labels.MIN_REGULAR_WAGE);
		this.newSeries(Labels.HOUSEHOLDS_DEPOSITS);
		this.newSeries(Labels.RESERVATION_WAGE);
		this.newSeries(Labels.UNEMPLOYED);
		this.newSeries(Labels.UNEMPLOYMENT_DURATION);
		this.newSeries(Labels.VOLUNTARY_UNEMPLOYED);
		this.newSeries(Labels.BANK_CAPITAL);
		this.newSeries(Labels.BANK_DIVIDEND);
		this.newSeries(Labels.DEPOSITS);
		this.newSeries(Labels.DOUBTFUL_DEBTS);
		this.newSeries(Labels.LOANS);
		this.newSeries(Labels.CAPITAL_ADEQUACY_RATIO);
		this.newSeries(Labels.DOUBTFUL_DEBTS_RATIO);
		this.newSeries(Labels.JOB_OFFERS);
		this.newSeries(Labels.FIRMS_DEPOSITS);
		this.newSeries(Labels.VACANCIES);
		this.newSeries(Labels.REAL_WAGE);
		this.newSeries(Labels.FORCED_SAVINGS_RATE);
		this.newSeries(Labels.INTERMEDIATE_NEEDS_VOLUME);
		this.newSeries(Labels.INTERMEDIATE_NEEDS_BUDGET);
		this.newSeries(Labels.INTERMEDIATE_CAP_UTIL);
		this.newSeries(Labels.FINAL_CAP_UTIL);
		this.newSeries(Labels.RAW_MAT_INV_RATE);

		this.newSeries(Labels.averageDividendFinal);
		this.newSeries(Labels.averageDividendIntermediate);
		
		this.newSeries(Labels.firmsFinal);
		this.newSeries(Labels.firmsIntermediate);
		
		this.newSeries(Labels.inventoryFinalLevel);
		this.newSeries(Labels.inventoryIntermediateLevel);
		
		this.newSeries(Labels.markupFinalMax);
		this.newSeries(Labels.markupFinalMedian);
		this.newSeries(Labels.markupFinalMin);
		
		this.newSeries(Labels.markupIntermediateMax);
		this.newSeries(Labels.markupIntermediateMedian);
		this.newSeries(Labels.markupIntermediateMin);
		
		this.newSeries(Labels.profitsRatio);
		this.newSeries(Labels.realProfitIntermediate);
		this.newSeries(Labels.realProfitFinal);

		this.newSeries(Labels.productionIntermediateVolume);
		this.newSeries(Labels.productionFinalVolume);

		this.newSeries(Labels.relativePrices);

		this.newSeries(Labels.salesFinalValue);
		this.newSeries(Labels.salesFinalVolume);
		
		this.newSeries(Labels.salesIntemediateValue);
		this.newSeries(Labels.salesIntermediateVolume);

		this.newSeries(Labels.utilizationFinalMax);
		this.newSeries(Labels.utilizationFinalMedian);
		this.newSeries(Labels.utilizationFinalMin);
		
		this.newSeries(Labels.utilizationIntermediateMax);
		this.newSeries(Labels.utilizationIntermediateMedian);
		this.newSeries(Labels.utilizationIntermediateMin);
		
		this.newSeries(Labels.vacancyRateAverage);
		this.newSeries(Labels.vacancyRateFinal);
		this.newSeries(Labels.vacancyRateIntermediate);

		this.newSeries(Labels.wageBillTotal);

		this.newSeries(Labels.workforceTotal);
		this.newSeries(Labels.workersInSector1);
		this.newSeries(Labels.workersInSector2);

		this.newSeries(Labels.PRICE_FINAL_MAX);
		this.newSeries(Labels.PRICE_FINAL_MEDIAN);
		this.newSeries(Labels.PRICE_FINAL_MIN);

		this.newSeries(Labels.PRICE_INTERMEDIATE_MAX);
		this.newSeries(Labels.PRICE_INTERMEDIATE_MEDIAN);
		this.newSeries(Labels.PRICE_INTERMEDIATE_MIN);

		this.newSeries(Labels.ANNUAL_INCOME);
		//this.newSeries(MacroLabels.PRODUCTIVITY);
		this.newSeries(Labels.vacanciesRateAnnual);
		this.newSeries(Labels.vacanciesAnnual);
		this.newSeries(Labels.vacanciesRateAnnual);
		this.newSeries(Labels.ANNUAL_SAVING_RATE);
		this.newSeries(Labels.ANNUAL_PROFIT_SHARE);
		this.newSeries(Labels.ANNUAL_WAGE_SHARE);
		this.newSeries(Labels.ANNUAL_UNEMPLOYMENT_RATE);
		this.newSeries(Labels.MONEY_VELOCITY);
		this.newSeries(Labels.ANNUAL_INFLATION_RATE);

		this.newSeries(Labels.markupAverageAnnual);
		this.newSeries(Labels.markupFinalAnnual);
		this.newSeries(Labels.markupIntermediateAnnual);

		this.newSeries(Labels.annualBankruptciesTotal);
		this.newSeries(Labels.annualBankruptcyRateAverage);
		this.newSeries(Labels.annualBankruptcyRateFinal);
		this.newSeries(Labels.annualBankruptcyRateIntermediate);

		this.newSeries(Labels.annualCapacityUtilizationFinal);
		this.newSeries(Labels.annualCapacityUtilizationIntermediate);
		this.newSeries(Labels.annualCapacityUtilizationTotal);

		this.newSeries(Labels.annualProductionFinalVolume);
		this.newSeries(Labels.annualProductionIntermediateVolume);

		this.newSeries(Labels.annualSalesFinalVolume);

		this.newSeries(Labels.annualWorkforceTotal);

		this.newSeries(Labels.inventoryFinalVolume);
		this.newSeries(Labels.inventoryIntermediateVolume);
		
		this.newSeries(Labels.hOptimism);
		this.newSeries(Labels.hPessimism);

		this.newSeries(Labels.DEBT);
	}

	/**
	 * Creates the month time series for the given label.
	 * @param label the label.
	 */
	private void newSeries(String label) {
		this.put(label,new TimeSeries(label));
	}

	/**
	 * 
	 */
	public void clear() {
		for (Map.Entry<String , TimeSeries> mapEntry : this.entrySet()){ 
			TimeSeries timeSeries = mapEntry.getValue();
			timeSeries.clear();
			timeSeries=null;
		}
		super.clear();
	}

	/**
	 * Returns the time series associated to the given key.
	 * @param key the key.
	 * @return the time series
	 */
	public TimeSeries get(String key) {
		TimeSeries timeSeries = super.get(key);
		if (timeSeries==null) 
			throw new NullPointerException("No timeSerie for this key: "+key);
		return timeSeries;
	}

	/**
	 * Updates each series.
	 * @param periodDataset - the dataset of the period.
	 */
	public void updateSeries(PeriodDataset periodDataset) {
		final Month month = JamelObject.getCurrentPeriod().getMonth();
		this.get(Labels.DEBT).add(month,periodDataset.debtS1+periodDataset.debtS2);
		this.get(Labels.selfFinancingRatio).add(month,periodDataset.sefFinancingRatio);
		this.get(Labels.CONSUMPTION_VALUE).add(month,periodDataset.consumptionVal);
		this.get(Labels.CONSUMPTION_VOLUME).add(month,periodDataset.consumptionVol);
		this.get(Labels.DIVIDENDS).add(month,periodDataset.DIVIDENDS);
		this.get(Labels.EMPLOYED).add(month,periodDataset.EMPLOYED);
		this.get(Labels.HOUSEHOLDS).add(month,periodDataset.HOUSEHOLDS);
		this.get(Labels.INCOME).add(month,periodDataset.INCOME);
		this.get(Labels.INVOLUNTARY_UNEMPLOYED).add(month,periodDataset.INVOLUNTARY_UNEMPLOYED);
		this.get(Labels.MAX_REGULAR_WAGE).add(month,periodDataset.MAX_REGULAR_WAGE);
		this.get(Labels.MEDIAN_WAGE).add(month,periodDataset.MEDIAN_WAGE);
		this.get(Labels.MIN_REGULAR_WAGE).add(month,periodDataset.MIN_REGULAR_WAGE);
		this.get(Labels.HOUSEHOLDS_DEPOSITS).add(month,periodDataset.hDeposits);
		this.get(Labels.UNEMPLOYED).add(month,periodDataset.unemployed);
		this.get(Labels.UNEMPLOYMENT_DURATION).add(month,periodDataset.unemploymentDuration);
		this.get(Labels.BANK_CAPITAL).add(month,periodDataset.capitalBank);
		this.get(Labels.BANK_DIVIDEND).add(month,periodDataset.BANK_DIVIDEND);
		this.get(Labels.DEPOSITS).add(month,periodDataset.DEPOSITS);
		this.get(Labels.DOUBTFUL_DEBTS).add(month,periodDataset.doubtDebt);
		this.get(Labels.LOANS).add(month,periodDataset.LOANS);
		this.get(Labels.CAPITAL_ADEQUACY_RATIO).add(month,periodDataset.CAPITAL_ADEQUACY_RATIO);
		this.get(Labels.DOUBTFUL_DEBTS_RATIO).add(month,periodDataset.DOUBTFUL_DEBTS_RATIO);
		this.get(Labels.JOB_OFFERS).add(month,periodDataset.jobOffers);
		this.get(Labels.FIRMS_DEPOSITS).add(month,periodDataset.fDeposits);
		this.get(Labels.VACANCIES).add(month,periodDataset.vacanciesTotal);
		this.get(Labels.REAL_WAGE).add(month,periodDataset.REAL_WAGE);
		this.get(Labels.FORCED_SAVINGS_RATE).add(month,periodDataset.FORCED_SAVINGS_RATE);
		this.get(Labels.INTERMEDIATE_NEEDS_VOLUME).add(month,periodDataset.intermediateNeedsVolume);
		this.get(Labels.INTERMEDIATE_NEEDS_BUDGET).add(month,periodDataset.intermediateNeedsBudget);
		this.get(Labels.INTERMEDIATE_CAP_UTIL).add(month,periodDataset.intermediateCapUtil);
		this.get(Labels.FINAL_CAP_UTIL).add(month,periodDataset.finalCapUtil);
		this.get(Labels.RAW_MAT_INV_RATE).add(month,periodDataset.rawMaterialsInventoriesRate);
		
		this.get(Labels.profitsRatio).add(month,periodDataset.profitsRatio);
		this.get(Labels.realProfitFinal).add(month,periodDataset.realProfitFinal);
		this.get(Labels.realProfitIntermediate).add(month,periodDataset.realProfitIntermediate);
		
		this.get(Labels.averageDividendFinal).add(month,periodDataset.averageDividendFinal);
		this.get(Labels.averageDividendIntermediate).add(month,periodDataset.averageDividendIntermediate);
		
		this.get(Labels.firmsFinal).add(month,periodDataset.firmsS2);
		this.get(Labels.firmsIntermediate).add(month,periodDataset.firmsS1);				
		
		this.get(Labels.markupFinalMax).add(month,periodDataset.markupFinalMax);
		this.get(Labels.markupFinalMedian).add(month,periodDataset.markupFinalMedian);
		this.get(Labels.markupFinalMin).add(month,periodDataset.markupFinalMin);

		this.get(Labels.markupIntermediateMax).add(month,periodDataset.markupIntermediateMax);
		this.get(Labels.markupIntermediateMedian).add(month,periodDataset.markupIntermediateMedian);
		this.get(Labels.markupIntermediateMin).add(month,periodDataset.markupIntermediateMin);

		this.get(Labels.inventoryFinalLevel).add(month,periodDataset.invS2Level);
		this.get(Labels.inventoryIntermediateLevel).add(month,periodDataset.inventoryS1Level);	

		this.get(Labels.productionIntermediateVolume).add(month,periodDataset.prodVolS1);
		this.get(Labels.productionFinalVolume).add(month,periodDataset.prodVolS2);

		this.get(Labels.relativePrices).add(month,periodDataset.relativePrices);

		this.get(Labels.salesFinalVolume).add(month,periodDataset.salesVolS2);
		this.get(Labels.salesFinalValue).add(month,periodDataset.salesPriceValS2);
		
		this.get(Labels.salesIntermediateVolume).add(month,periodDataset.salesVolS1);
		this.get(Labels.salesIntemediateValue).add(month,periodDataset.salesPriceValS1);

		this.get(Labels.utilizationFinalMax).add(month,periodDataset.utilizationFinalMax);
		this.get(Labels.utilizationFinalMedian).add(month,periodDataset.utilizationFinalMedian);
		this.get(Labels.utilizationFinalMin).add(month,periodDataset.utilizationFinalMin);

		this.get(Labels.utilizationIntermediateMax).add(month,periodDataset.utilizationIntermediateMax);
		this.get(Labels.utilizationIntermediateMedian).add(month,periodDataset.utilizationIntermediateMedian);
		this.get(Labels.utilizationIntermediateMin).add(month,periodDataset.utilizationIntermediateMin);

		this.get(Labels.vacancyRateAverage).add(month,periodDataset.vacancyRateAverage);
		this.get(Labels.vacancyRateFinal).add(month,periodDataset.vacancyRateFinal);
		this.get(Labels.vacancyRateIntermediate).add(month,periodDataset.vacancyRateIntermediate);

		this.get(Labels.wageBillTotal).add(month,periodDataset.wageBillS1+periodDataset.wageBillS2);

		this.get(Labels.workforceTotal).add(month,periodDataset.workforceTotal);
		
		this.get(Labels.workersInSector1).add(month,periodDataset.avWorkforceS1);
		this.get(Labels.workersInSector2).add(month,periodDataset.avWorkforceS2);

		this.get(Labels.PRICE_FINAL_MEDIAN).add(month,periodDataset.priceS2Med);
		this.get(Labels.PRICE_FINAL_MIN).add(month,periodDataset.priceS2Min);
		this.get(Labels.PRICE_FINAL_MAX).add(month,periodDataset.priceS2Max);

		this.get(Labels.PRICE_INTERMEDIATE_MEDIAN).add(month,periodDataset.priceS1Med);
		this.get(Labels.PRICE_INTERMEDIATE_MIN).add(month,periodDataset.priceS1Minimum);
		this.get(Labels.PRICE_INTERMEDIATE_MAX).add(month,periodDataset.priceS1Max);

		this.get(Labels.hPessimism).add(month,periodDataset.hPessimismRatio);
		
		this.get(Labels.fPessimism).add(month,periodDataset.fPessimismRatio);

		this.get(Labels.H_HOARD2INCOME_RATIO).add(month,periodDataset.hHoardingRatio);

		this.get(Labels.H_SAVING_TARGET).add(month,periodDataset.hSavingTarget);
		this.get(Labels.DEBT_TARGET).add(month,periodDataset.debtTarget);

	}

	/**
	 * Updates each series.
	 * @param yearDataset - the dataset of the year.
	 */
	public void updateSeries(YearDataset yearDataset) {
		final Year year = JamelObject.getCurrentPeriod().getYear();
		this.get(Labels.ANNUAL_INCOME).add(year,yearDataset.income);
		this.get(Labels.ANNUAL_SAVING_RATE).add(year,yearDataset.savingsRate);
		this.get(Labels.ANNUAL_PROFIT_SHARE).add(year,yearDataset.profitShare);
		this.get(Labels.ANNUAL_WAGE_SHARE).add(year,yearDataset.wageShare);
		this.get(Labels.ANNUAL_UNEMPLOYMENT_RATE).add(year,yearDataset.unemploymentRate);
		this.get(Labels.MONEY_VELOCITY).add(year,yearDataset.moneyVelocity);
		this.get(Labels.ANNUAL_INFLATION_RATE).add(year,yearDataset.inflation);

		this.get(Labels.annualBankruptciesTotal).add(year,yearDataset.bankruptcies);
		this.get(Labels.annualBankruptcyRateAverage).add(year,yearDataset.bankruptcyRateAverage);
		this.get(Labels.annualBankruptcyRateFinal).add(year,yearDataset.bankruptcyRateFinal);
		this.get(Labels.annualBankruptcyRateIntermediate).add(year,yearDataset.bankruptcyRateIntermediate);

		this.get(Labels.annualCapacityUtilizationFinal).add(year,yearDataset.capacityUtilizationFinal);
		this.get(Labels.annualCapacityUtilizationIntermediate).add(year,yearDataset.capacityUtilizationIntermediate);
		this.get(Labels.annualCapacityUtilizationTotal).add(year,yearDataset.capacityUtilization);
		
		this.get(Labels.annualWorkforceTotal).add(year,yearDataset.workforceTotal);

		this.get(Labels.annualProductionFinalVolume).add(year,yearDataset.productionFinalVolume);
		this.get(Labels.annualProductionIntermediateVolume).add(year,yearDataset.productionIntermediateVolume);

		this.get(Labels.annualSalesFinalVolume).add(year,yearDataset.salesFinalVolume);

		this.get(Labels.inventoryFinalVolume).add(year,yearDataset.inventoryFinalVolumeDecember);
		this.get(Labels.inventoryIntermediateVolume).add(year,yearDataset.inventoryIntermediateVolumeDecember);

		this.get(Labels.markupAverageAnnual).add(year,yearDataset.markupAverage);
		this.get(Labels.markupFinalAnnual).add(year,yearDataset.markupFinal);
		this.get(Labels.markupIntermediateAnnual).add(year,yearDataset.markupIntermediate);

		this.get(Labels.vacanciesRateAnnual).add(year,yearDataset.vacanciesRate);
		this.get(Labels.vacanciesAnnual).add(year,yearDataset.vacanciesTotal);
		
	
	}

}
