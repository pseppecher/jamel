/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2011, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/>. 
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

import java.util.LinkedList;

/**
 * A dataset for the macroeconomic data of the current year.
 */
public class YearDataset {

	/** The average price on the goods market last year. */
	private Double lastFinalPrice;

	/** anticipatedWorkforce */
	public double workforceAnticipatedTotal;

	/** bankruptciesFinal */
	public int bankruptciesFinal;

	/** capacityUtilization */
	//public double capacityUtilization;

	/** bankruptciesIntermediate */
	public int bankruptciesIntermediate;

	/** bankruptcies */
	public double bankruptciesTotal;

	/** bankruptcyRateAverage */
	public double bankruptcyRateAverage;

	/** bankruptcyRateFinal */
	public double bankruptcyRateFinal;

	/** bankruptcyRateIntermediate */
	public double bankruptcyRateIntermediate;

	/** capacityUtilizationFinal */
	public double capacityUtilizationFinal;

	/** capacityUtilizationIntermediate */
	public double capacityUtilizationIntermediate;

	/** capacityUtilizationTotal */
	public double capacityUtilizationTotal;

	/** productionVolume */
	//public double productionVolume;

	/** productivity */
	//public double productivity;

	/** dividends */
	public double dividends;

	/** salesValue */
	//public double salesValue;

	/** salesVolume */
	//public double salesVolume;

	/** machinery */
	//public double machinery;

	/** effectiveMarkup */
	//public double effectiveMarkup;

	/** firmsFinal */
	public int firmsFinal;

	/** firmsIntermediate */
	public int firmsIntermediate;

	/** firmsTotal */
	public int firmsTotal;

	/** households */
	public double households;

	/** income */
	public double income;

	/** workforce */
	//public double workforce;

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

	/** salesFinalVolume */
	public long salesFinalVolume;

	/** salesIntermediateValue */
	public long salesIntermediateValue;

	/** salesIntermediateVolume */
	public long salesIntermediateVolume;

	/** savingsRate */
	public double savingsRate;

	/** unemployed */
	public double unemployed;

	/** wageBill */
	//public double wageBill;

	/** unemploymentRate */
	public double unemploymentRate;

	/** vacancies */
	//public double vacancies;

	/** vacanciesRate */
	public double vacanciesRate;

	/** wageBillFinal */
	public long wageBillFinal;

	/** wageBillIntermediate */
	public long wageBillIntermediate;

	/** wageBillTotal */
	public long wageBillTotal;

	/** wageShare */
	public double wageShare;

	/** workforceFinal */
	public long workforceFinal;

	/** workforceIntermediate */
	public long workforceIntermediate;

	/** workforceTotal */
	public long workforceTotal;

	/** vacanciesTotal */
	public long vacanciesTotal;

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
		for(PeriodDataset periodData: dataList) {

			this.dividends += periodData.DIVIDENDS;
			this.households += periodData.HOUSEHOLDS;
			this.unemployed += periodData.UNEMPLOYED;

			this.bankruptciesFinal += periodData.bankruptS2;
			this.bankruptciesIntermediate += periodData.bankruptS1;

			this.firmsFinal += periodData.firmsS2;
			this.firmsIntermediate += periodData.firmsS1;

			this.machineryFinal += periodData.machineryS2;
			this.machineryIntermediate += periodData.machineryS1;

			this.productionFinalVolume += periodData.prodVolS2;
			this.productionIntermediateVolume += periodData.prodVolS1;

			this.salesFinalValue += periodData.salesValPS2;
			this.salesIntermediateValue += periodData.salesValPS1;

			this.salesFinalVolume += periodData.salesVolS2;
			this.salesIntermediateVolume += periodData.salesVolS1;

			this.vacanciesTotal += periodData.vacanciesTotal;

			this.wageBillFinal += periodData.wageBillS2;
			this.wageBillIntermediate += periodData.wageBillS1;

			this.workforceAnticipatedTotal += periodData.workforceAnticipatedTotal;

			this.workforceFinal += periodData.workforceS2;
			this.workforceIntermediate += periodData.workforceS1;

		}

		this.bankruptciesTotal = this.bankruptciesFinal + this.bankruptciesIntermediate;

		this.firmsTotal = this.firmsFinal + this.firmsIntermediate;

		this.machineryTotal = this.machineryFinal + this.machineryIntermediate;

		this.workforceTotal = this.workforceFinal + this.workforceIntermediate;

		this.wageBillTotal = this.wageBillFinal + this.wageBillIntermediate;

		this.income = this.wageBillTotal+this.dividends;

		this.inventoryFinalVolumeDecember = dataList.getLast().invVolS2;
		this.inventoryIntermediateVolumeDecember = dataList.getLast().invVolS1;


		this.bankruptcyRateFinal = 1200.*bankruptciesFinal/this.firmsFinal;
		this.bankruptcyRateIntermediate = 1200.*bankruptciesIntermediate/this.firmsIntermediate;
		this.bankruptcyRateAverage = 1200.*bankruptciesTotal/this.firmsTotal;

		this.moneyVelocity = this.income/dataList.getLast().DEPOSITS;		
		this.savingsRate = 100.*dataList.getLast().HOUSEHOLDS_DEPOSITS/this.income;
		this.wageShare = 100.*this.wageBillTotal/this.income;
		this.profitShare = 100.*this.dividends/this.income;
		this.unemploymentRate = 100.*this.unemployed/this.households;		
		this.vacanciesRate = 100.*this.vacanciesTotal/ this.workforceAnticipatedTotal;

		this.capacityUtilizationFinal = 100.*this.workforceFinal/this.machineryFinal;
		this.capacityUtilizationIntermediate = 100.*this.workforceIntermediate/this.machineryIntermediate;
		this.capacityUtilizationTotal = 100.*this.workforceTotal/this.machineryTotal;

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
				this.inflation = inflation;}
			lastFinalPrice=newPrice;
		}

		if (this.wageBillIntermediate!=0)
			this.markupIntermediate = 100.*this.salesIntermediateValue /this.wageBillIntermediate - 100.;
		if (this.wageBillFinal!=0)
			this.markupFinal = 100.*this.salesFinalValue /(this.wageBillFinal+this.salesIntermediateValue) - 100.;
		if (this.wageBillTotal!=0)
			this.markupAverage = 100.*(this.salesFinalValue+this.salesIntermediateValue) /(this.wageBillIntermediate+this.wageBillFinal+this.salesIntermediateValue) - 100.;

	}

}
