/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher and contributors.
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
 */

package jamel.util.data;

import java.util.LinkedList;

/**
 * A dataset for the macroeconomic data of the current year.
 */
public class YearDataset extends GlobalDataset{

	/** The average price on the goods market last year. */
	private Double lastFinalPrice;

	/** bankruptcies */
	public double bankruptcies;

	/** bankruptciesFinal */
	public int bankruptciesFinal;

	/** capacityUtilization */
	//public double capacityUtilization;

	/** bankruptciesIntermediate */
	public int bankruptciesIntermediate;

	/** bankruptcyRateAverage */
	public double bankruptcyRateAverage;

	/** bankruptcyRateFinal */
	public double bankruptcyRateFinal;

	/** bankruptcyRateIntermediate */
	public double bankruptcyRateIntermediate;

	/** capacityUtilizationTotal */
	public double capacityUtilization;

	/** capacityUtilizationFinal */
	public double capacityUtilizationFinal;

	/** capacityUtilizationIntermediate */
	public double capacityUtilizationIntermediate;

	/** A string that represents the date at the format yyyy-mm. */
	public String date;

	/** productionVolume */
	//public double productionVolume;

	/** productivity */
	//public double productivity;

	/** debt */
	public long debt;

	/** salesValue */
	//public double salesValue;

	/** salesVolume */
	//public double salesVolume;

	/** machinery */
	//public double machinery;

	/** effectiveMarkup */
	//public double effectiveMarkup;

	/** deposits */
	public long deposits;

	/** dividends */
	public long dividends;

	/** doubtfulDebt */
	public long doubtfulDebt;

	/** firmsFinal */
	public int firmsFinal;

	/** firmsIntermediate */
	public int firmsIntermediate;

	/** workforce */
	//public double workforce;

	/** firmsTotal */
	public int firmsTotal;

	/** The gross profit. */
	public long grossProfit;

	/** households */
	public double households;

	/** income */
	public double income;

	/** inflation */
	public double inflation;

	/** final inventoryVolume */
	public double inventoryFinalVolumeDecember;

	/** annualInventoryIntermediateVolume */
	public double inventoryIntermediateVolumeDecember;

	/** machineryFinal */
	public long machineryFinal;

	/** machineryIntermediate */
	public long machineryIntermediate;

	/** machineryTotal */
	public long machineryTotal;

	/** markupAverage */
	public double markupAverage;

	/** markupFinal */
	public double markupFinal;

	/** markupIntermediate */
	public double markupIntermediate;

	/** moneyVelocity */
	public double moneyVelocity;

	/** The number of the period. */
	public int period;

	/** productionFinalVolume */
	public long productionFinalVolume;

	/** productionIntermediateVolume */
	public long productionIntermediateVolume;

	/** productivityFinal */
	public long productivityFinal;

	/** productivityIntermediate */
	public long productivityIntermediate;

	/** profitShare */
	public double profitShare;

	/** salesFinalValue */
	public long salesFinalValue;

	/** wageBill */
	//public double wageBill;

	/** salesFinalVolume */
	public long salesFinalVolume;

	/** vacancies */
	//public double vacancies;

	/** salesIntermediateValue */
	public long salesIntermediateValue;

	/** salesIntermediateVolume */
	public long salesIntermediateVolume;

	/** savingsRate */
	public double savingsRate;

	/** unemployed */
	public double unemployed;

	/** unemploymentRate */
	public double unemploymentRate;

	/** vacanciesRate */
	public double vacanciesRate;

	/** vacanciesTotal */
	public long vacanciesTotal;

	/** wageBillTotal */
	public long wageBill;

	/** wageBillFinal */
	public long wageBillFinal;

	/** wageBillIntermediate */
	public long wageBillIntermediate;

	/** wageShare */
	public double wageShare;

	/** anticipatedWorkforce */
	public double workforceAnticipatedTotal;

	/** workforceFinal */
	public long workforceFinal;

	/** workforceIntermediate */
	public long workforceIntermediate;

	/** workforceTotal */
	public long workforceTotal;

	/** employed */
	public long employed;

	/** Valeur total des biens finis et non finis en stock. */
	public long inventoryTotalValue;

	/** Dépôts des firmes. */
	public long fDeposits;

	/** Dépôts des ménages. */
	public long hDeposits;

	/**
	 * Creates a new dataset.
	 * @param yearDataset the previous dataset.
	 */
	public YearDataset(YearDataset yearDataset) {
		if (yearDataset!=null)
			this.lastFinalPrice = yearDataset.lastFinalPrice;
	}

	/**
	 * Updates the data set.
	 * @param dataList - a list that contains the datasets of each month of the year.
	 */
	public void update(LinkedList<PeriodDataset> dataList) {
		
		this.period = getCurrentPeriod().getValue();
		this.date = getCurrentPeriod().toString();
		
		for(PeriodDataset periodData: dataList) {
			
			this.debt += periodData.debtS1+periodData.debtS2;
			this.doubtfulDebt += periodData.doubtDebtS1+periodData.doubtDebtS2;
			this.deposits += periodData.DEPOSITS;
			this.fDeposits += periodData.fDeposits;
			this.hDeposits += periodData.hDeposits;
			
			this.grossProfit += periodData.grossProfit;

			this.dividends += periodData.DIVIDENDS;
			this.households += periodData.HOUSEHOLDS;
			this.unemployed += periodData.unemployed;
			this.employed += periodData.EMPLOYED;

			this.bankruptciesFinal += periodData.bankruptS2;
			this.bankruptciesIntermediate += periodData.bankruptS1;

			this.firmsFinal += periodData.firmsS2;
			this.firmsIntermediate += periodData.firmsS1;

			this.machineryFinal += periodData.machineryS2;
			this.machineryIntermediate += periodData.machineryS1;

			this.productionFinalVolume += periodData.prodVolS2;
			this.productionIntermediateVolume += periodData.prodVolS1;

			this.salesFinalValue += periodData.salesPriceValS2;
			this.salesIntermediateValue += periodData.salesPriceValS1;

			this.salesFinalVolume += periodData.salesVolS2;
			this.salesIntermediateVolume += periodData.salesVolS1;

			this.vacanciesTotal += periodData.vacanciesTotal;

			this.wageBillFinal += periodData.wageBillS2;
			this.wageBillIntermediate += periodData.wageBillS1;

			this.workforceAnticipatedTotal += periodData.workforceAnticipatedTotal;

			this.workforceFinal += periodData.workforceS2;
			this.workforceIntermediate += periodData.workforceS1;
			
			this.inventoryTotalValue += periodData.invUnfVal+periodData.invFinVal;

		}

		this.bankruptcies = this.bankruptciesFinal + this.bankruptciesIntermediate;

		this.firmsTotal = this.firmsFinal + this.firmsIntermediate;

		this.machineryTotal = this.machineryFinal + this.machineryIntermediate;

		this.workforceTotal = this.workforceFinal + this.workforceIntermediate;

		this.wageBill = this.wageBillFinal + this.wageBillIntermediate;

		this.income = this.wageBill+this.grossProfit;

		this.inventoryFinalVolumeDecember = dataList.getLast().invVolS2;
		this.inventoryIntermediateVolumeDecember = dataList.getLast().invVolS1;
		
		this.bankruptcyRateFinal = 1200.*bankruptciesFinal/this.firmsFinal;
		this.bankruptcyRateIntermediate = 1200.*bankruptciesIntermediate/this.firmsIntermediate;
		this.bankruptcyRateAverage = 1200.*bankruptcies/this.firmsTotal;

		this.moneyVelocity = this.income/dataList.getLast().DEPOSITS;		
		this.savingsRate = 100.*dataList.getLast().hDeposits/this.income;
		this.wageShare = 100.*this.wageBill/this.income;
		this.profitShare = 100.*this.grossProfit/this.income;
		this.unemploymentRate = 100.*this.unemployed/(this.unemployed+this.employed);		
		this.vacanciesRate = 100.*this.vacanciesTotal/ this.workforceAnticipatedTotal;

		this.capacityUtilizationFinal = 100.*this.workforceFinal/this.machineryFinal;
		this.capacityUtilizationIntermediate = 100.*this.workforceIntermediate/this.machineryIntermediate;
		this.capacityUtilization = 100.*this.workforceTotal/this.machineryTotal;

		if (this.workforceFinal!=0)
			this.productivityFinal = this.productionFinalVolume/this.workforceFinal;
		if (this.workforceIntermediate!=0)
			this.productivityIntermediate = this.productionIntermediateVolume/this.workforceIntermediate;

		final double value=this.salesFinalValue;
		final double volume=this.salesFinalVolume;
		if (volume>0) {
			final double newPrice=value/volume;
			if (lastFinalPrice!=null) {
				final double inflation=100.*(newPrice-lastFinalPrice)/lastFinalPrice;
				this.inflation = inflation;
				}
			lastFinalPrice=newPrice;
		}

		if (this.wageBillIntermediate!=0)
			this.markupIntermediate = 100.*this.salesIntermediateValue /this.wageBillIntermediate - 100.;
		if (this.wageBillFinal!=0)
			this.markupFinal = 100.*this.salesFinalValue /(this.wageBillFinal+this.salesIntermediateValue) - 100.;
		if (this.wageBill!=0)
			this.markupAverage = 100.*(this.salesFinalValue+this.salesIntermediateValue) /(this.wageBillIntermediate+this.wageBillFinal+this.salesIntermediateValue) - 100.;

	}

}
