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

import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;

import jamel.agents.firms.Firm;
import jamel.agents.firms.FirmDataset;
import jamel.agents.firms.ProductionType;
import jamel.agents.households.Household;
import jamel.agents.households.HouseholdDatasetInterface;
import jamel.spheres.monetarySphere.BankData;
import jamel.spheres.realSphere.IntermediateFactory;

/**
 * A dataset that aggregates the macroeconomic data of one period.
 */
public class PeriodDataset extends AbstractDataset {

	/** dividendsFinal */
	private long dividendsFinal;

	/** dividendsIntermediate */
	private long dividendsIntermediate;

	/** averageDividendFinal */
	public double averageDividendFinal;
	
	/** averageDividendIntermediate */
	public double averageDividendIntermediate;	

	/** avWorkforceS1 */
	public int avWorkforceS1;

	/** avWorkforceS2 */
	public int avWorkforceS2;

	/** BANK_DIVIDEND */
	public long BANK_DIVIDEND;

	/** BANKRUPTCIES */
	public long BANKRUPTCIES; // TODO utiliser cette donnée issue de la banque pour contrôler les données issues des entreprises.

	/** bankruptciesIntermediate */
	public int bankruptS1;

	/** bankruptciesFinal */
	public int bankruptS2;

	/** CAPITAL_ADEQUACY_RATIO */
	public double CAPITAL_ADEQUACY_RATIO;

	/** BANK_CAPITAL */
	public long capitalBank;

	/** capitalS1 */
	public long capitalS1;

	/** capitalS2 */
	public long capitalS2;

	/** CONSUMPTION_BUDGET */
	public long CONSUMPTION_BUDGET;

	/** CONSUMPTION_VALUE */
	public long CONSUMPTION_VALUE;

	/** CONSUMPTION_VOLUME */
	public long CONSUMPTION_VOLUME;

	/** date */
	public final String date;

	/** debtS1 */
	public long debtS1;

	/** debtS2 */
	public long debtS2;

	/** DEPOSITS */
	public long DEPOSITS;

	/** DIVIDENDS */
	public long DIVIDENDS;

	/** doubtDebtS1 */
	public long doubtDebtS1;

	/** doubtDebtS2 */
	public long doubtDebtS2;

	/** DOUBTFUL_DEBTS */
	public long DOUBTFUL_DEBTS;

	/** DOUBTFUL_DEBTS_RATIO */
	public double DOUBTFUL_DEBTS_RATIO;

	/** EMPLOYED */
	public long EMPLOYED;

	/** finalCapUtil */
	public double finalCapUtil;

	/** FIRMS_DEPOSITS */
	public long FIRMS_DEPOSITS;

	/** FIRMS_CAPITAL */
	//public long FIRMS_CAPITAL;

	/** firmsIntermediate */
	public int firmsS1;

	/** firmsFinal */
	public int firmsS2;

	/** FORCED_SAVINGS */
	public long FORCED_SAVINGS;

	/** FORCED_SAVINGS_RATE */
	public double FORCED_SAVINGS_RATE;

	/** The gross profit */
	public long grossProfit;

	/** grossProfitFinal */
	public long grossProfitFinal;

	/** grossProfitIntermediate */
	public long grossProfitIntermediate;

	/** HOUSEHOLDS */
	public long HOUSEHOLDS;

	/** HOUSEHOLDS_DEPOSITS */
	public long HOUSEHOLDS_DEPOSITS;

	/** INCOME */
	public long INCOME;

	/** intermediateCapUtil */
	public double intermediateCapUtil;

	/** The budget for the purchase of intermediate needs. */
	public long intermediateNeedsBudget;

	/** The volume of intermediate goods needed by the final goods sector. */
	public long intermediateNeedsVolume;

	/** INVENTORY_VALUE */
	public long INVENTORY_UNF_VALUE;

	/** INVENTORY_VALUE */
	public long INVENTORY_VALUE;

	/** inventoryFinalLevel */
	public double inventoryFinalLevel;

	/** inventoryIntermediateLevel */
	public double inventoryIntermediateLevel;

	/** INVOLUNTARY_UNEMPLOYED */
	public long INVOLUNTARY_UNEMPLOYED;

	/** INVENTORY value */
	public double invValS1;

	/** INVENTORY_VOLUME */
	public double invValS2;

	/** INVENTORY_VOLUME */
	public double invVolS1;

	/** INVENTORY_VOLUME */
	public double invVolS2;

	/** PRODUCTION_VOLUME */
	//public long PRODUCTION_VOLUME;

	/** JOB_OFFERS */
	public long JOB_OFFERS;

	/** LOANS */
	public long LOANS;

	/** SALES_VALUE */
	//public long SALES_VALUE;

	/** SALES_VOLUME */
	//public long SALES_VOLUME;

	/** intermediateMachinery */
	public long machineryS1;

	/** finalMachinery */
	public long machineryS2;

	/** The maximum regular markup target of the firms. */
	public float markupFinalMax;

	/** The median markup target of the firms. */
	public float markupFinalMedian;

	/** The minimum regular markup target of the firms. */
	public float markupFinalMin;

	/** The maximum regular markup target of the firms. */
	public float markupIntermediateMax;

	/** The median markup target of the firms. */
	public float markupIntermediateMedian;

	/** The minimum regular markup target of the firms. */
	public float markupIntermediateMin;

	/** The list of the markups target. */
	public final LinkedList<Float> markupListFinal = new LinkedList<Float>();

	/** The list of the markups target. */
	public final LinkedList<Float> markupListIntermediate = new LinkedList<Float>();

	/** MAX_REGULAR_WAGE */
	public long MAX_REGULAR_WAGE;

	/** MEASURABLE_RATE */
	public long MEASURABLE_RATE;

	/** MEDIAN_WAGE */
	public long MEDIAN_WAGE;

	/** MIN_REGULAR_WAGE */
	public long MIN_REGULAR_WAGE;

	/** Non Performing Loans (=the loans cancelled). */
	public long nPLoans;

	/** period */
	public final double period;

	/** MAXIMUM_REGULAR_PRICE */
	public long priceFinalMax;

	/** priceFinalMean */
	public double priceFinalMean;

	/** MEDIAN_PRICE */
	public long priceFinalMedian;

	/** MINIMUM_REGULAR_PRICE */
	public long priceFinalMinimum;

	/** priceIntermediateMax */
	public long priceIntermediateMax;

	/** priceIntermediateMean */
	public double priceIntermediateMean;

	/** priceIntermediateMedian */
	public long priceIntermediateMedian;

	/** The total value of the sales of intermediate goods. */
	//public long intermediateSalesValue;

	public long priceIntermediateMinimum;

	/** pricesList */
	public final LinkedList<Long> pricesFinalList = new LinkedList<Long>();

	/** pricesList */
	public final LinkedList<Long> pricesIntermediateList = new LinkedList<Long>();

	/** productionMaxFinalVolume */
	public int productionMaxFinalVolume;

	/** productionMaxIntermediateVolume */
	public int productionMaxIntermediateVolume;

	/** The value of interm. goods produced. */
	public long prodValS1;

	/** The value of final goods produced. */
	public long prodValS2;

	/** The volume of intermediate goods produced. */
	public long prodVolS1;

	/** The volume of final goods produced. */
	public long prodVolS2;

	/** profitsRatio */
	public double profitsRatio;

	/** randomSeed */
	public int randomSeed;

	/** rawMaterialEffectiveVolume */
	public long rawMaterialEffectiveVolume;

	/** rawMaterialNormalVolume */
	public long rawMaterialNormalVolume;

	/** rawMaterialsInventoriesRate */
	public double rawMaterialsInventoriesRate;

	/** REAL_WAGE */
	public double REAL_WAGE;

	/** realProfitFinal */
	public double realProfitFinal;

	/** realProfitIntermediate */
	public double realProfitIntermediate;

	/** relativePrices */
	public double relativePrices;

	/** RESERVATION_WAGE */
	public long RESERVATION_WAGE;

	/** Value of sales (at the cost value). */
	public long salesValCS1;

	/** Value of sales (at the cost value). */
	public long salesValCS2;

	/** Value of sales (at the price value). */
	public long salesValPS1;

	/** Value of sales (at the price value). */
	public long salesValPS2;

	/** salesFinalVolume */
	public long salesVolS1;

	/** salesFinalVolume */
	public long salesVolS2;

	/** UNEMPLOYED */
	public long UNEMPLOYED;

	/** UNEMPLOYMENT_DURATION */
	public double UNEMPLOYMENT_DURATION;

	/** unemploymentDurationList */
	public final LinkedList<Double> unemploymentDurationList = new LinkedList<Double>();

	/** unemploymentTotalDuration */
	public double unemploymentTotalDuration = 0;

	/** utilizationFinalMax */
	public float utilizationFinalMax;

	/** utilizationFinalMedian */
	public float utilizationFinalMedian;

	/** utilizationFinalMin */
	public float utilizationFinalMin;

	/** utilizationIntermediateMax */
	public float utilizationIntermediateMax;

	/** utilizationIntermediateMedian */
	public float utilizationIntermediateMedian;

	/** utilizationIntermediateMin */
	public float utilizationIntermediateMin;

	/** utilizationListFinal */
	public final LinkedList<Float> utilizationListFinal = new LinkedList<Float>();

	/** utilizationListIntermediate */
	public final LinkedList<Float> utilizationListIntermediate = new LinkedList<Float>();

	/** wageBillTotal */
	//public long wageBillTotal;

	/** vacanciesFinal */
	public long vacanciesFinal;

	/** vacanciesIntermediate */
	public long vacanciesIntermediate;

	/** vacanciesTotal */
	public long vacanciesTotal;

	/** vacanciesRateTotal */
	public double vacancyRateAverage;

	/** vacanciesRateFinal */
	public double vacancyRateFinal;

	/** vacanciesRateIntermediate */
	public double vacancyRateIntermediate;

	/** VOLUNTARY_UNEMPLOYED */
	public long VOLUNTARY_UNEMPLOYED;

	/** wageBillIntermediate */
	public long wageBillS1;

	/** wageBillFinal */
	public long wageBillS2;

	/** wagesList */
	public final LinkedList<Long> wagesList = new LinkedList<Long>();

	/** workforceAnticipatedFinal */
	public long workforceAnticipatedFinal;

	/** workforceAnticipatedIntermediate */
	public long workforceAnticipatedIntermediate;

	/** workforceAnticipatedTotal */
	public long workforceAnticipatedTotal;

	/** intermediateWorkforce */
	public long workforceS1;

	/** finalWorkforce */
	public long workforceS2;

	/** workforceTotal */
	public long workforceTotal;

	/**
	 */
	public PeriodDataset() {
		super();
		this.period = getCurrentPeriod().getValue();
		this.date = getCurrentPeriod().toString();
	}

	/**
	 * Adds the data of one firm.
	 * @param data - the data to add.
	 */
	private void addIndividualData(FirmDataset data) {
		this.JOB_OFFERS += data.jobOffers;
		this.FIRMS_DEPOSITS += data.deposit;
		this.INVENTORY_VALUE += data.invVal;
		this.INVENTORY_UNF_VALUE += data.invUnfVal;
		this.intermediateNeedsVolume += data.intermediateNeedsVolume;
		this.intermediateNeedsBudget += data.intermediateNeedsBudget;
		this.rawMaterialEffectiveVolume += data.rawMaterialEffectiveVolume;
		this.rawMaterialNormalVolume += data.rawMaterialNormalVolume;
		if (data.factory.equals(IntermediateFactory.class.getName())) {
			this.firmsS1++;
			this.capitalS1 += data.capital;
			this.debtS1 += data.debt;
			this.doubtDebtS1 += data.doubtDebt;
			if (data.bankrupt) this.bankruptS1++;
			this.dividendsIntermediate += data.dividend;
			this.grossProfitIntermediate += data.grossProfit; 
			this.invVolS1 += data.invVol;
			this.invValS1 += data.invVal;
			this.prodVolS1 += data.prodVol;
			this.prodValS1 += data.prodVal;
			this.productionMaxIntermediateVolume += data.maxProduction;
			this.salesValPS1 += data.salesPVal;
			this.salesValCS1 += data.salesCVal;
			this.salesVolS1 += data.salesVol;
			this.machineryS1 += data.machinery;
			this.vacanciesIntermediate += data.vacancies;
			this.wageBillS1 += data.wageBill;
			this.workforceS1 += data.workforce;
			this.workforceAnticipatedIntermediate += data.anticipatedWorkforce;
			this.pricesIntermediateList.add((long)data.price);// FIXME pourquoi long ?
			this.markupListIntermediate.add(data.markupTarget);
			this.utilizationListIntermediate.add(data.utilizationTarget);
		}
		else{
			this.firmsS2++;
			this.capitalS2 += data.capital;
			this.debtS2 += data.debt;
			this.doubtDebtS2 += data.doubtDebt;
			if (data.bankrupt) this.bankruptS2++;
			this.invVolS2 += data.invVol;
			this.invValS2 += data.invVal;
			this.dividendsFinal += data.dividend;
			this.grossProfitFinal += data.grossProfit; 
			this.prodVolS2 += data.prodVol;
			this.prodValS2 += data.prodVal;
			this.productionMaxFinalVolume += data.maxProduction;
			this.salesValPS2 += data.salesPVal;
			this.salesValCS2 += data.salesCVal;
			this.salesVolS2 += data.salesVol;
			this.machineryS2 += data.machinery;
			this.vacanciesFinal += data.vacancies;
			this.wageBillS2 += data.wageBill;
			this.workforceS2 += data.workforce;
			this.workforceAnticipatedFinal += data.anticipatedWorkforce;
			this.pricesFinalList.add((long)data.price);
			this.markupListFinal.add(data.markupTarget);		
			this.utilizationListFinal.add(data.utilizationTarget);
		}		
	}

	/**
	 * Adds the data of one household.
	 * @param data - the data to add.
	 */
	private void addIndividualData(HouseholdDatasetInterface data) {
		this.CONSUMPTION_BUDGET += data.getConsumptionBudget();
		this.FORCED_SAVINGS += data.getForcedSavings();
		this.CONSUMPTION_VALUE += data.getConsumptionValue();
		this.CONSUMPTION_VOLUME += data.getConsumptionVolume();
		this.DIVIDENDS += data.getDividend();
		this.INCOME += data.getIncome();
		this.HOUSEHOLDS_DEPOSITS += data.getDeposits();
		this.HOUSEHOLDS ++;
		if (data.getSector()==(ProductionType.finalProduction))
			this.avWorkforceS2++;
		else if (data.getSector()==(ProductionType.intermediateProduction))
			this.avWorkforceS1++;
		if (data.getEmploymentStatus()==Labels.STATUS_EMPLOYED) {
			this.EMPLOYED++;
			this.wagesList.add(data.getWage());
			if (data.getWage()==0)
				throw new RuntimeException("Wage is 0.");
		}
		else {
			if (data.getEmploymentStatus()==Labels.STATUS_INVOLUNTARY_UNEMPLOYED) {
				this.INVOLUNTARY_UNEMPLOYED++;
				this.UNEMPLOYED++;
				this.unemploymentTotalDuration += data.getUnemploymentDuration();
			}
			else {
				this.VOLUNTARY_UNEMPLOYED++;
				this.UNEMPLOYED++;
				this.unemploymentTotalDuration += data.getUnemploymentDuration();
			}
		}
	}

	/**
	 * Adds the data of the bank.
	 * @param data - the data to add.
	 */
	public void compileBankData(BankData data) {
		this.nPLoans= data.nPLoans;
		this.capitalBank = data.getCapital();
		this.BANK_DIVIDEND = data.getDividend();
		this.DEPOSITS = data.getDeposits();
		this.DOUBTFUL_DEBTS = data.getDoubtfulDebts();
		this.LOANS = data.getLoans();
		this.BANKRUPTCIES =  data.getBankruptcies();
		this.CAPITAL_ADEQUACY_RATIO = 100.*data.getCapital()/data.getLoans();
		this.DOUBTFUL_DEBTS_RATIO = 100.*data.getDoubtfulDebts()/data.getLoans();	
	}

	/**
	 * Adds the data of each firm.
	 * @param firmsList - the list of the firms.
	 */
	public void compileFirmsData(LinkedList<Firm> firmsList) {
		this.pricesFinalList.clear();
		this.markupListFinal.clear();
		this.markupListIntermediate.clear();
		for (Firm aFirm : firmsList) {
			final FirmDataset data = aFirm.getData();
			addIndividualData(data);
		}		
	}

	/**
	 * Adds the data of each households.
	 * @param householdsList - the list of the households.
	 */
	public void compileHouseholdsData(LinkedList<Household> householdsList) {
		this.wagesList.clear();
		this.unemploymentDurationList.clear();
		unemploymentTotalDuration=0;
		for (Household selectedHousehold : householdsList) {
			addIndividualData(selectedHousehold.getData());
		}
	}

	/**
	 * Updates the ratios that require data from different sectors.
	 */
	public void updateRatios() {

		//this.profitsRatio = ((double) (this.grossProfitFinal*this.machineryIntermediate))/(this.machineryFinal*this.grossProfitIntermediate);
		
		this.grossProfit = this.grossProfitFinal+this.grossProfitIntermediate;
		
		if (this.machineryS1>0)
			this.realProfitIntermediate = ((double)this.grossProfitIntermediate/this.machineryS1)*((double)this.CONSUMPTION_VOLUME/this.CONSUMPTION_VALUE);
		else 
			this.realProfitIntermediate = 0;
		this.realProfitFinal = ((double)this.grossProfitFinal/this.machineryS2)*((double)this.CONSUMPTION_VOLUME/this.CONSUMPTION_VALUE);


		if (this.firmsS2!=0) 
			this.averageDividendFinal = this.dividendsFinal/this.firmsS2;

		if (this.firmsS1!=0)
			this.averageDividendIntermediate = this.dividendsIntermediate/this.firmsS1;

		this.workforceAnticipatedTotal = this.workforceAnticipatedFinal+this.workforceAnticipatedIntermediate;

		this.vacanciesTotal = this.vacanciesFinal+this.vacanciesIntermediate;

		this.workforceTotal = this.workforceS2+this.workforceS1;
		//this.wageBillTotal = this.wageBillS2+this.wageBillS1;

		//if (this.productionMaxFinalVolume!=0) 
		this.inventoryFinalLevel = this.invVolS2/this.productionMaxFinalVolume;
		if (this.productionMaxIntermediateVolume!=0) 
			this.inventoryIntermediateLevel = this.invVolS1/this.productionMaxIntermediateVolume;
		else
			this.inventoryIntermediateLevel = 0;
		//System.out.println(this.inventoryIntermediateLevel);// DELETE

		this.UNEMPLOYMENT_DURATION = this.unemploymentTotalDuration/this.UNEMPLOYED;
		this.FORCED_SAVINGS_RATE = 100.*this.FORCED_SAVINGS/this.CONSUMPTION_BUDGET;
		if (wagesList.size()>0) {
			BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(wagesList);
			this.MAX_REGULAR_WAGE = item.getMaxRegularValue().longValue();
			this.MEDIAN_WAGE = item.getMedian().longValue();
			this.MIN_REGULAR_WAGE = item.getMinRegularValue().longValue();
		}
		if (pricesFinalList.size()>0) {
			BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(pricesFinalList);
			this.priceFinalMax = item.getMaxRegularValue().longValue();
			this.priceFinalMedian = item.getMedian().longValue();
			this.priceFinalMean = item.getMean().doubleValue();
			this.priceFinalMinimum = item.getMinRegularValue().longValue();
		}
		if (pricesIntermediateList.size()>0) {
			BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(pricesIntermediateList);
			this.priceIntermediateMax = item.getMaxRegularValue().longValue();
			this.priceIntermediateMedian = item.getMedian().longValue();
			this.priceIntermediateMean = item.getMean().doubleValue();
			this.priceIntermediateMinimum = item.getMinRegularValue().longValue();
		}

		//if (this.priceIntermediateMean!=0)
		this.relativePrices=this.priceFinalMean/this.priceIntermediateMean;

		if (markupListFinal.size()>0) {
			final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(markupListFinal);
			this.markupFinalMax = 100.f*item.getMaxRegularValue().floatValue();
			this.markupFinalMedian = 100.f*item.getMedian().floatValue();
			this.markupFinalMin = 100.f*item.getMinRegularValue().floatValue();
		}
		if (markupListIntermediate.size()>0) {
			final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(markupListIntermediate);
			this.markupIntermediateMax = 100.f*item.getMaxRegularValue().floatValue();
			this.markupIntermediateMedian = 100.f*item.getMedian().floatValue();
			this.markupIntermediateMin = 100.f*item.getMinRegularValue().floatValue();
		}

		if (utilizationListFinal.size()>0) {
			final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(utilizationListFinal);
			this.utilizationFinalMax = item.getMaxRegularValue().floatValue();
			this.utilizationFinalMedian = item.getMedian().floatValue();
			this.utilizationFinalMin = item.getMinRegularValue().floatValue();
		}
		if (utilizationListIntermediate.size()>0) {
			final BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(utilizationListIntermediate);
			this.utilizationIntermediateMax = item.getMaxRegularValue().floatValue();
			this.utilizationIntermediateMedian = item.getMedian().floatValue();
			this.utilizationIntermediateMin = item.getMinRegularValue().floatValue();
		}

		if ((this.CONSUMPTION_VALUE!=0)&(this.EMPLOYED!=0))
			this.REAL_WAGE = (double)((this.wageBillS1+this.wageBillS2)*this.CONSUMPTION_VOLUME)/(this.CONSUMPTION_VALUE*this.EMPLOYED);
		this.finalCapUtil = 100.*this.workforceS2/this.machineryS2;
		this.intermediateCapUtil = 100.*this.workforceS1/this.machineryS1;
		this.rawMaterialsInventoriesRate = 100.*this.rawMaterialEffectiveVolume/this.rawMaterialNormalVolume;

		this.vacancyRateIntermediate = 100.*this.vacanciesIntermediate/this.workforceAnticipatedIntermediate;
		this.vacancyRateFinal = 100.*this.vacanciesFinal/this.workforceAnticipatedFinal;
		this.vacancyRateAverage = 100.*this.vacanciesTotal/this.workforceAnticipatedTotal;

	}

}
