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
 * aint with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.firms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import jamel.austrian.banks.CommercialBank;
import jamel.austrian.markets.Market;
import jamel.austrian.roles.Creditor;
import jamel.austrian.roles.Seller;
import jamel.austrian.roles.Shareholder;
import jamel.austrian.roles.Worker;
import jamel.austrian.sfc.SFCAgent;
import jamel.austrian.util.Alphabet;
import jamel.austrian.widgets.AbstractCheque;
import jamel.austrian.widgets.CreditContract;
import jamel.austrian.widgets.Goods;
import jamel.austrian.widgets.InvestmentProject;
import jamel.austrian.widgets.Machine;
import jamel.austrian.widgets.Offer;
import jamel.austrian.widgets.Quality;
import jamel.austrian.widgets.TimeDeposit;
import jamel.basic.Circuit;
import jamel.basic.data.BasicAgentDataset;


/**
 * Represents a firm.
 */
public class BasicFirm extends SFCAgent implements Firm {

	/**
	 * A manager for investments.
	 */
	private class InvestmentManager {
		
		/** The list of machines. */
		private LinkedList<Machine> machinery ;	

		/** The capacity of the machines. */
		private int capacity;
		
		/** The number of workers used in production. */
		private int workforce;

		/** The number of intermediate goods used in production. */
		private int intermediateInputs;

		/** The goods that have been produced. */
		private Goods products;

		/** The current offer in the labor market. */
		private Offer jobOffer;

		/** The offered wage. */
		private float offeredWage;

		/** The offered wage in the next time period. */
		private float nextOfferedWage;

		/** The number of applications that the firm aims at. */
		private int applicationTarget;

		/** The number of applications received. */
		private int applications;

		/** Indicates whether the current job offer has been rejected by a household. */
		private boolean rejectedOffer;

		/** The number of produced goods during the past six time periods. */
		private final LinkedList<Integer> productionRecord;
		
		private final LinkedList<Integer> productionSchedule;
		
		/** The targeted production quantity. */
		private int productionTarget;

		/** A counter of how many production steps have been ordered. */
		private int productionScheduler;

		/** A counter of how many production steps have been executed. */
		private int productionCounter;

		private int estimatedInputPrice;

		private final LinkedList<Integer> inputPrices;

		private int applicationReserveTarget;

		private boolean expansion;

		private int nextInvestmentBlock;

		private int offeringGap;

		private boolean laborShortage;
		
		/**
		 * Creates the investment manager.
		 */
		private InvestmentManager(int offeringTarget, int productionUnit, float initialWage, boolean physicalCapital) {
			machinery = new LinkedList<Machine>() ;
			productionSchedule = new LinkedList<Integer>();
			for (int i=productionUnit; i<offeringTarget; i+= productionUnit) productionSchedule.add(i);
			productionRecord = new LinkedList<Integer>();
			for (int i=0; i<6; i++) productionRecord.add(1);  // parameter tauI
			inputPrices = new LinkedList<Integer>();
			for (int i=0; i<6; i++) inputPrices.add(10000);
			nextOfferedWage = initialWage;
			useCapital = physicalCapital;
		}


		/**
		 * Completes some technical operations at the beginning of the period.
		 */
		private void open() {
			capacity = 0;
			Iterator<Machine> iter = machinery.iterator() ;
			while (iter.hasNext()) {								
				Machine machine = iter.next() ;
				if (machine.isExpired()) iter.remove() ; 
				else capacity += machine.getCapacity();
			}
			Collections.sort(machinery);
			if (products != null) salesManager.inventory.add(products);
			products = null;
			currentProductionLevel = 0;
			intermediateInputs = 0;
			workforce=0;									
			applications=0; 			
			offeredWage = nextOfferedWage;
			laborShortage = false;
			rejectedOffer = false;	
			expansion = false;
		}

		
		/**
		 * Defines the units in which production will be done.<br>
		 * 
		 */
		private void planProduction0() {
			
			// initialize
			productionSchedule.clear();
			Collections.sort(machinery);
			int productionScheduler = 0;
			int productionTarget = 0;
			
		
			// set the production schedule.
			int i=0;
			for (Machine machine:machinery) {
				productionSchedule.add(machine.getCapacity() * productionFunction.getPrX());
				i += machine.getCapacity() * productionFunction.getPrX();
			}
			while (i<salesManager.nextOfferingTarget){ 
				productionSchedule.add(salesManager.productionUnit);		
				i += salesManager.productionUnit;
			}


			// estimate input prices
			int ipx = 0;
			for (int inputPrice : inputPrices) ipx += inputPrice;
			estimatedInputPrice = ipx / inputPrices.size();
			// calculate the funds that are necessary to make the first investment
			if (!useCapital) nextInvestmentBlock = (int) offeredWage;
			else{
				int requiredInputs = productionSchedule.get(productionScheduler) / productionFunction.getPrX();
				int inputExpenditure = requiredInputs * estimatedInputPrice;
				nextInvestmentBlock = ((int) offeredWage + inputExpenditure);	 
			}

			
			int offeringGap = salesManager.nextOfferingTarget - salesManager.offeredGoods; 
			if (offeringGap <= 0) return;  // applicable in case of down-sizing

	
			int investmentReserveTarget = 0;	
			while (offeringGap >= productionTarget + productionSchedule.get(productionScheduler)){

				investmentReserveTarget += nextInvestmentBlock;
				if (financialManager.getAvailableFunds(false) >= investmentReserveTarget){

					productionTarget += productionSchedule.get(productionScheduler); 
					productionScheduler++; 
					if (productionScheduler == productionSchedule.size()) return;  
					if (useCapital) {
						int requiredInputs = productionSchedule.get(productionScheduler) / productionFunction.getPrX();
						int inputExpenditure = requiredInputs * estimatedInputPrice;
						nextInvestmentBlock = ((int) offeredWage + inputExpenditure);	 
					}
				}
				else break;
			}	

			financialManager.setInvestmentReserve(investmentReserveTarget);

		}
		
		/**
		 * Defines the units in which production will be done.<br>
		 * 
		 */
		private void planProduction() {
			
			// initialize
			productionSchedule.clear();
			Collections.sort(machinery);
			productionScheduler = 0;
			productionTarget = 0;
			productionCounter = 0;
			applicationReserveTarget = 0;
			
			// set the production schedule.
			int i=0;
			for (Machine machine:machinery) {
				productionSchedule.add(machine.getCapacity() * productionFunction.getPrX());
				i += machine.getCapacity() * productionFunction.getPrX();
			}
			while (i<salesManager.nextOfferingTarget){ 
				productionSchedule.add(salesManager.productionUnit);		
				i += salesManager.productionUnit;
			}


			// estimate input prices
			int ipx = 0;
			for (int inputPrice : inputPrices) ipx += inputPrice;
			estimatedInputPrice = ipx / inputPrices.size();
			// calculate the funds that are necessary to make the first investment
			if (!useCapital) nextInvestmentBlock = (int) offeredWage;
			else{
				int requiredInputs = productionSchedule.get(productionScheduler) / productionFunction.getPrX();
				int inputExpenditure = requiredInputs * estimatedInputPrice;
				nextInvestmentBlock = ((int) offeredWage + inputExpenditure);	 
			}

			
			offeringGap = salesManager.nextOfferingTarget - salesManager.offeredGoods; 
			if (offeringGap <= 0) return;  // applicable in case of down-sizing

	
			int investmentReserveTarget = 0;	
			while (offeringGap >= productionTarget + productionSchedule.get(productionScheduler)){

				investmentReserveTarget += nextInvestmentBlock;
				if (financialManager.getAvailableFunds(false) >= investmentReserveTarget){

					((FirmSector) sector).registerInvestmentDemand(BasicFirm.this);
					productionTarget += productionSchedule.get(productionScheduler); 
					productionScheduler++; 
					if (productionScheduler == productionSchedule.size()) return;  
					if (useCapital) {
						int requiredInputs = productionSchedule.get(productionScheduler) / productionFunction.getPrX();
						int inputExpenditure = requiredInputs * estimatedInputPrice;
						nextInvestmentBlock = ((int) offeredWage + inputExpenditure);	 
					}
				}
				else break;
			}	

			financialManager.setInvestmentReserve(investmentReserveTarget);

			int m=0, l=0;
			do {
				m += productionSchedule.get(productionScheduler + l);
				if (m <= offeringGap - productionTarget) applicationReserveTarget++;	
				else break;
				l++;
			} while (m < offeringGap - productionTarget);

			// any application < this target is not released. 
			// reason: firms with high capacity and low sales get stuck because they only
			// register during the expansion phase and then they are out of applicants.

		}
		
		

		/**
		 * Calculates the workforce requirement and creates a new offer in the labor market.
		 */
		private void offerJobs() {
				
			if (type.equals("A")) {
				applicationTarget = offeringGap;
				if (applicationTarget == 0) return;
			}
			applicationTarget = productionSchedule.size();
			jobOffer = new Offer(BasicFirm.this, applicationTarget, offeredWage) ;
			getMarket("labor").newOffer(jobOffer);
		}


		
		/**
		 * Updates the job offer.
		 */
		private void updateJobOffer(int subtractedNumber){
			getMarket("labor").updateOffer(jobOffer, subtractedNumber);
		}
		
	
		/**
		 * Removes the job offers from the labor market.
		 */
		private void removeJobOffers(){
			getMarket("labor").removeOffer(jobOffer);
		}
		
		
		/**
		 * Updates the production plan after a sale.
		 */
		private void notifySale(){
			
			offeringGap += 1;
			if (offeringGap <= 0) return;
			if (productionScheduler ==  productionSchedule.size()) return; // applicable in case of down-sizing.

			if (offeringGap == productionTarget + productionSchedule.get(productionScheduler)){

				if (financialManager.getAvailableFunds(false) >= nextInvestmentBlock){
					((FirmSector) sector).registerInvestmentDemand(BasicFirm.this);
					productionTarget += productionSchedule.get(productionScheduler); 
					productionScheduler++; 
					if (productionScheduler ==  productionSchedule.size()) {
						applicationReserveTarget = 0;
						return;
					}
					if (useCapital) {
						int requiredInputs = productionSchedule.get(productionScheduler) / productionFunction.getPrX();
						int inputExpenditure = requiredInputs * estimatedInputPrice;
						nextInvestmentBlock = ((int) offeredWage + inputExpenditure);	 
					}
				}	
			}	

			
			
			// re-calculate the applicationReserveTarget
			if (offeringGap % productionFunction.getPrX() == 0){

				applicationReserveTarget = 0;
				int l=0, m=0;
				do {
					m += productionSchedule.get(productionScheduler + l);
					if (m <= offeringGap - productionTarget) applicationReserveTarget++;	
					else break;
					l++;
				} while (m < offeringGap - productionTarget);
			}
		}
		
		
		/** 
		 * Releases some job applicants.
		 */
		private void laborRelease() {
			
			if (expansion) applicationReserveTarget = 0;	
			int excessApplications = jobApplications.size() - applicationReserveTarget;
			if (excessApplications <= 0) return;
		
			int laborRelease = (int) Math.ceil(excessApplications);
			for (int i=0; i<laborRelease; i++){
				if (jobApplications.size()==0) return;
				jobApplications.removeFirst().notifyRejection(BasicFirm.this);
				if (i>2) updateJobOffer(-1);
			}  
		}
		
		
		/**
		 * Registers the demand for extending the production.
		 */
		private void regsiterExpansionDemand() {
			
			expansion = true;
			if (productionScheduler ==  productionSchedule.size()) return;
			
			while (offeringGap >= productionTarget + productionSchedule.get(productionScheduler)){
				((FirmSector) sector).registerInvestmentDemand(BasicFirm.this);
				productionTarget += productionSchedule.get(productionScheduler);
				productionScheduler++;
				if (productionScheduler==productionSchedule.size())	break;
			}
		}


		/**
		 * New input: machines.
		 */
		private void addMachine(Machine machine) {
			
			machinery.add(machine);
			capacity += machine.getCapacity();
		}	


		/**
		 * New input: intermediate goods and/or labor.
		 */
		private void addFactors(int dl, int dx) {
			
			workforce += dl;
			intermediateInputs += dx;
			getMarket("labor").registerSale(jobOffer, dl, true);
			if (useCapital) currentProductionLevel = productionFunction.getPrX() * intermediateInputs;	
			else currentProductionLevel = (int) (productionFunction.getPrL()) * workforce;
			productionCounter++;
		}

		
		/**
		 * Registers the prices of input factors.
		 */
		private void registerPrices(LinkedList<Offer> goodsOffers) {
			for (Offer offer:goodsOffers) {
				inputPrices.add((int) offer.getPrice());
				inputPrices.removeFirst();
			}
		}
		

		/**
		 * The firm produces.
		 */
		private void produce() {
			
			if (currentProductionLevel>0) {
				products = new Goods(currentProductionLevel, financialManager.OPEX);
				productionRecord.removeFirst();
				productionRecord.add(products.getVolume());
			}
		}


		/**
		 * Updates the offered wage
		 */
		private void updateOfferedWage() {

			
			// Shortage considerations
			if (laborShortage & rejectedOffer) {
				float deltaW = parameters.wFlex * getRandom().nextFloat();	
				nextOfferedWage += deltaW;			
			}
			
			else if (!laborShortage){
				float deltaW = parameters.wFlex * getRandom().nextFloat();	
				nextOfferedWage -= deltaW;
				if (nextOfferedWage<100) nextOfferedWage=100;
			}

			// Cost considerations
			if (!useCapital){ 
				if (nextOfferedWage / productionFunction.getPrL() > salesManager.nextPrice *(1 - (parameters.delta + parameters.firmMarkup))){
					nextOfferedWage = productionFunction.getPrL() * salesManager.nextPrice *(1 - (parameters.delta + parameters.firmMarkup));
				}
			}
		}
		

		/**
		 * Returns the marginal output for a considered investment.
		 */
		private float getMarginalOutput(int dl, int dx) {
			if (useCapital) return productionFunction.getPrX() * dx;
			return productionFunction.getPrL() * dl;
		}
		

		/**
		 * Returns the number of products.
		 */
		private int getProductionVolume() {
			if (products==null) return 0;
			return products.getVolume();
		}


		/**
		 * Returns the value of the products.
		 */
		private int getProductionValue() {
			if (products==null) return 0;
			return products.getValue();
		}
		
		
		/**
		 * Returns the average output of the past time periods.<br>
		 * Rounds down to the closest integer.
		 */
		private int getAverageProduction() {
			int cumulatedProduction = 0;
			for (int i:productionRecord) cumulatedProduction+=i;
			return cumulatedProduction/productionRecord.size();
		}


		/**
		 * Returns the processing capacity of the firm.
		 */
		private int getProcessingCapacity() {
			if (!useCapital) return 0;
			return capacity;
		}


		/**
		 * Returns the number of workers who work without machines.
		 */
		private int getExcessLabor() {
			return workforce-machinery.size();
		}


		/**
		 * Removes all assets.			
		 */
		private void goBankrupt() {	
			capacity = 0;
			machinery.clear();
			products = null; 
		}

	}


	/**
	 * A manager for sales.
	 */
	private class SalesManager {

		/** The target market(s) of the firm. */
		private final LinkedList<Market> targetMarkets ;

		/**  The inventory. */
		private final LinkedList<Goods> inventory;

		/** The current offer */
		private Offer offer ;

		/** The number of offered goods. */
		private int offeredGoods ;

		/** The number of sold goods. */
		private int sales ;
		
		/** The inventory ratio. */
		private float inventoryRatio;
		
		/** States whether the firm did not reach its investment target. */
		private SalesPerformance salesPerformance;

		/** States if and why a firm did not reach its investment target. */
		private OfferingStatus offeringStatus;

		/** The current unit price.*/
		private float currentPrice;

		/** The price in the next time period. */
		private float nextPrice;

		/** The last price before the last strategic re-orientation. */
		private float priorToRevisionPrice;

		/**  The current offering target. */
		private int offeringTarget;

		/** The targeted quantity in the next time period. */
		private int nextOfferingTarget; 

		/** The targeted quantity in the second next time period. */
		private int secondNextOfferingTarget; 
		
		/**  The range of the offering target. */
		private int targetRange;

		/** The number of goods that are produced in one production process<br>
		 *  Corresponds to the output that one worker can produce w/o machines. */
		private int productionUnit;
				
		/** Tells the salesManager to raise the price when unit costs become prohibitive. */
		private boolean costWarning = false;
		
		/** The number of consecutive time periods without sales. */
		private int zeroSalesPeriods;  

		/** The number of time periods since the last strategic readjustment. */
		private int periodsSinceRevision = 6;

		private int minimalOfferingTarget;

		
		/**
		 * Creates a new salesManager.
		 */
		private SalesManager(String type, int stage, int unit, int target, float askedPrice){

			targetMarkets = new LinkedList<Market>();
			inventory = new LinkedList<Goods>();
			productionUnit = unit;
			minimalOfferingTarget = target;
			secondNextOfferingTarget = target;
			nextOfferingTarget = target;
			nextPrice = askedPrice;
			
			
			if (type.equals("A")){
				targetMarkets.add(getMarket("machines"));
				return;	//if no 'return' here then the capital producers also offer to households.
			}

			int nextLowerStage = stage - 1;		
			targetMarkets.add(getMarket(type+nextLowerStage));

			// Only needed if intermediate goods have multiple specificity.
			/*if (nextLowerStage==0){
				targetMarkets.add(getMarket(type+nextLowerStage));
				return;
			}
			for (int i=typeNumber-1; i<=typeNumber+1; i++){
				if (i>=0){
					String targetType = Alphabet.getLetter(i);
					targetMarkets.add(getMarket(targetType+nextLowerStage));
				}
			}*/
		}


		/**
		 * Opens the sales manager and prepares the next time period.
		 */
		private void open(){

			currentPrice = nextPrice;
			offeringTarget = nextOfferingTarget; 
			offeredGoods = getInventoryVolume();
			nextOfferingTarget = secondNextOfferingTarget;
			
			targetRange = investmentManager.productionSchedule.getFirst();								
			if (offeredGoods <= offeringTarget - targetRange) offeringStatus = OfferingStatus.BELOW_TARGET;
			else offeringStatus = OfferingStatus.ON_OR_ABOVE_TARGET;						
				
			sales = 0;
			periodsSinceRevision ++;
			costWarning = false;
		}


		/**
		 * Creates new offers in the targeted market(s).
		 */
		private void offerCommodities() {
			if (offeredGoods == 0) offer = null;
			else {
				offer = new Offer(BasicFirm.this, offeredGoods, currentPrice);
				for (Market goodsMarket:targetMarkets) goodsMarket.newOffer(offer);		
			}
		}


		/** 
		 * Adjusts the sales strategy according to the whether the firm uses capital or not.
		 */
		private void setProductionMode(){  
			
			if (inputMarket == null) return;
			Market market = getMarket(inputMarket);
			if (market == null) return;		// type-A firms do not get past this point.
		
			if (market.getMarketVolume()>0){
				if (!useCapital){
					useCapital = true;
					productionUnit = productionFunction.getPrX() * productionFunction.getCapL();
					minimalOfferingTarget = parameters.standardFirmSize * productionUnit;
		
					while (nextOfferingTarget % productionUnit > 0)	nextOfferingTarget += 1; 
					if (nextOfferingTarget < minimalOfferingTarget) nextOfferingTarget = minimalOfferingTarget;	
					secondNextOfferingTarget = nextOfferingTarget;
					
					if (periodsSinceRevision <= 6) nextPrice = priorToRevisionPrice;	//TODO: current?
				}
				return;
			}
			
			if (useCapital){
				useCapital = false;
				productionUnit = (int) (productionFunction.getPrL());
				minimalOfferingTarget = parameters.standardFirmSize * productionUnit;
				
				while (nextOfferingTarget % productionUnit > 0) nextOfferingTarget -=1; // actual down-sizing happens later automatically
				secondNextOfferingTarget = nextOfferingTarget;	
				
				priorToRevisionPrice = currentPrice; 
				nextPrice = investmentManager.offeredWage/((1 - (parameters.delta + parameters.firmMarkup))*productionUnit);
				periodsSinceRevision = 0;
			}
		}

	

		/**
		 * Sells one unit of the goods in salesManager.<br>
		 * Updates the inventory and the costs of goods sold.<br>
		 * The accounting method applied is first-in first-out (FIFO).
		 */
		private void sell(AbstractCheque cheque) {

			if (cheque.getAmount() != (int) (salesManager.currentPrice ))throw new RuntimeException("Bad cheque amount.");
			
			sales += 1;
			int costsOfGoodsSold = (int) inventory.getFirst().getUnitCost();
			inventory.getFirst().subtract(1);
			if (inventory.getFirst().getVolume()==0) inventory.removeFirst();
			for (Market goodsMarket:targetMarkets) {
				goodsMarket.updateOffer(this.offer, 1);	
				goodsMarket.registerSale(this.offer, 1, false);
			}
			investmentManager.notifySale();	
			financialManager.notifySale(costsOfGoodsSold, cheque);
			
			
			
		}


		/** 
		 * Updates the sales ratio, the asked price, and the targeted quantity. 
		 */
		private void updatePrice(){  

			int inventories = getInventoryVolume();
			if (offeredGoods == 0) inventoryRatio = Float.NaN;
			else inventoryRatio = (float) inventories / (float) offeredGoods;
			if (sales == 0) zeroSalesPeriods+=1;
			else zeroSalesPeriods = 0;

			if (inventories<parameters.sigma1*offeredGoods) salesPerformance = SalesPerformance.HIGH;  
			else if (inventories>parameters.sigma2*offeredGoods) salesPerformance = SalesPerformance.LOW;
			else salesPerformance = SalesPerformance.OK;
			
			
			// Price adjustment:
			float deltaP = parameters.pFlex * getRandom().nextFloat();
			if (salesPerformance.isHigh() | costWarning) {
				nextPrice += deltaP;  	
			}
			else if (salesPerformance.isLow()) {
				nextPrice -= deltaP;  	
				if (nextPrice<1) nextPrice = 1;  
			}

			// This ensures that those firms who rely exclusively on labor do not lower their prices too much,
			// which would prevent them from bidding up wages.
			if (!useCapital){
				float medianWage = (float) getMarketSector().getPrevailingWage();
				if (nextPrice < medianWage/((1 - (parameters.delta + parameters.firmMarkup))*productionFunction.getPrL())){
					nextPrice = medianWage/((1 - (parameters.delta + parameters.firmMarkup))*productionFunction.getPrL()) + parameters.delta*currentPrice; 
				}
			}
			
			
			// Quantity adjustment:
			if (salesPerformance.isHigh() && offeringStatus.isOk()) {
				secondNextOfferingTarget += 2 * productionUnit; 
			}
			else if (salesPerformance.isLow() && offeringStatus.isLow()) {	
				secondNextOfferingTarget -= productionUnit;			
				if (secondNextOfferingTarget < minimalOfferingTarget) secondNextOfferingTarget = minimalOfferingTarget;
			}		// This is important! There must be constant expansion demand at the highest stages
					// even if they are liquidity constrained. 	
			
			
			
			if (type.equals("A")){ // type-A firms get here before making investments.
				nextOfferingTarget = secondNextOfferingTarget;  
			}
		}			


		/** 
		 * Returns the number of inventories. 
		 */
		private int getInventoryVolume(){
			if (inventory.size()==0) return 0;
			int volume = 0;
			for (Goods inventories:inventory){				
				volume +=inventories.getVolume();
			}
			return volume;
		}


		/** 
		 * Returns the book value of the inventories.
		 */
		private int getInventoryValue(){
			if (inventory.size()==0) return 0;
			int value = 0;
			for (Goods inventories:inventory){				
				value +=inventories.getValue();
			}
			return value;
		}
		
		
		/**
		 * Removes all assets.			
		 */
		private void goBankrupt() {
			inventory.clear();										
		}

	}

	/**
	 * A manager for financial issues.
	 */
	private class FinancialManager{
		
		/** The original equity of the firm. */
		private int subscribedCapital;

		/** The firm's money holdings at the beginning of a time period. */
		private int initialMoney;

		/** The book value of fixed assets */
		private int assetValue;

		/** The total liabilities of the firm. */
		private int liabilities;

		/** The firm's net worth. */
		private int equity; 

		/** The list of credit contracts for operations. */
		private final LinkedList<CreditContract> operatingLoans ;	

		/** The list of credit contracts for investment. */
		private final LinkedList<CreditContract> investmentLoans ;	

		/** The list of credit offers that are available to finance a specific investment. */
		private LinkedList<Offer> availableOffers ;

		/** The amount of credit that is required for a specific investment. */
		private int requiredCredit;

		/** The amount of credit that is available for a specific investment. */
		private int availableCredit;

		/** Funds obtained in the credit market */
		private int newCredit ;	

		/** New credit for buying machines */
		private int newInvestmentCredit ;	

		/** New credit for operating expenditure */
		private int newOperatingCredit ;	

		/** Existing credit for buying machines */
		private int investmentCredit ;	

		/** Existing credit for operating expenditure */
		private int operatingCredit ;	

		/** The credit contracts that require some sort of payment in the current time period. */
		private LinkedList<CreditContract> obligations;
		
		/** The sum of the credit obligations of the firm (redemption+interest). */
		private int requiredPayments;

		/** The revenue of the firm. */
		private int revenue;

		/** The depreciation costs that are incurred in the current time period. */
		private int depreciationCosts;

		/** The interest payments that have been made in the current time period. */
		private int financingCosts;

		/** Gross Profit (Revenue - Cost of Goods Sold - Depreciation) */
		private int EBIT ;

		/** Net Profit (EBIT - Financing Costs) */
		private int netProfit ;

		/** Operating expenditure (wages and intermediate inputs) */
		private int OPEX;

		/** Capital expenditure (fixed and circulating capital) */
		private int CAPEX ;

		/** The upcoming depreciation costs. */
		private final LinkedList<Integer> writeDowns;

		/** The calculated dividend. */
		private int dividend;

		/** The redemption payments that have been made in the current time period. */
		private int redemption;

		/** Cash flow from operations. */
		@SuppressWarnings("unused")
		private int cashFlow1;

		/** Cash flow from investment. */
		@SuppressWarnings("unused")
		private int cashFlow2;

		/** Cash flow from financing. */
		@SuppressWarnings("unused")
		private int cashFlow3;

		/** The creditworthiness of the firm. */
		private Quality debtorQuality;

		/** Money that cannot be paid out as dividend due to capital requirements. */
		private int tiedUpMoney;

		/** Indicates whether an investment is financially feasible. */
		private boolean isFeasible;

		@SuppressWarnings("unused")
		private int internalFinancing;

		/** Currently not in use. */
		private float riskPremium;

		private int investmentReserve;

		/** The total costs (in monetary units). */
		private float totalCosts;

		/** The fixed costs (in monetary units). */
		private int fixedCosts;

		/** The contribution margin of the current investment. */
		private int contributionMargin;

		/** Indicates the reason why an investment was not feasible. */
		private int exit;

		/** The costs of goods sold. */
		private int costsOfGoodsSold;

		private double leverage;

		private int retainedEarnings;
		
		/** Effectuated dividend payments. */
		private int dividendPaid;

		public int wagePayments;

		
		/**
		 * Creates a new credit manager.
		 */
		private FinancialManager(int startupCapital) {
			this.operatingLoans = new LinkedList<CreditContract>() ;
			this.investmentLoans = new LinkedList<CreditContract>() ;
			this.writeDowns= new LinkedList<Integer>() ;
			this.debtorQuality = Quality.GOOD;
			for (int i = 1; i<productionFunction.getTauK(); i++) writeDowns.add(0);
			this.subscribedCapital = startupCapital;
			this.tiedUpMoney = startupCapital;
		}


		/** Does some updating at the beginning of the period. */
		private void open(){
			
			initialMoney = bank.getBalance(BasicFirm.this);
			revenue = 0;
			cashFlow1 = 0;
			cashFlow2 = 0;
			cashFlow3 = 0;
			CAPEX = 0;
			OPEX = 0;
			wagePayments = 0;
			retainedEarnings = 0;
			dividendPaid = 0;
			dividend = 0;
			netProfit = 0;
			EBIT = 0;
			depreciationCosts = 0;
			costsOfGoodsSold = 0;
			financingCosts = 0;
			internalFinancing = 0;
			availableCredit = 0;
			newCredit = 0;
			newInvestmentCredit = 0;
			newOperatingCredit =0;
			redemption = 0;
			writeDowns.add(0);  // here: writedDowns.size() == tauK
			fixedCosts = 0;
			obligations = new LinkedList<CreditContract>();
			requiredPayments = 0;			
			for (CreditContract contract : operatingLoans){
				if (contract.isDue()){
					int obligation = (int) (contract.getInterestRate()*contract.getVolume())
							+ contract.getRedemption()  // this is zero!
							+ contract.getDeferredInterest();
					if (obligation>0){
						obligations.add(contract);
						requiredPayments += obligation;
						fixedCosts += (int) (contract.getInterestRate()*contract.getVolume());
					}
				}
			}
			for (CreditContract contract : investmentLoans){
				if (contract.isDue()){
					int obligation = (int) (contract.getInterestRate()*contract.getVolume())
							+ contract.getRedemption()
							+ contract.getDeferredInterest();
					if (obligation>0){
						obligations.add(contract);
						requiredPayments += obligation;
						fixedCosts += (int) (contract.getInterestRate()*contract.getVolume());
					}
				}
			}
			
			totalCosts = fixedCosts;
			contributionMargin=0;
		}

		
		/** 
		 * Sets the investment reserve.
		 */
		private void setInvestmentReserve(int investmentReserveTarget){	
			int currentMoney = bank.getBalance(BasicFirm.this);
			if (currentMoney >= investmentReserveTarget) investmentReserve = investmentReserveTarget;
			else investmentReserve = currentMoney;
		}
		
		
		/** 
		 * Returns the funds which are available for investment (internal funds only).<br>
		 * The funds in the investment reserve can only be used for operations, not for fixed-capital investments.
		 */
		private int getAvailableFunds(boolean forMachines){
			int cash = bank.getBalance(BasicFirm.this);
			if (forMachines) return cash - requiredPayments - investmentReserve;
			return cash - requiredPayments; 
		}
		
		
		private void registerInvestmentCredit(){
			newInvestmentCredit = newCredit;
		}
		
		
		private void notifySale (int costOfGoodsSold, AbstractCheque cheque){
		
			costsOfGoodsSold += costOfGoodsSold;
			revenue += cheque.getAmount();	
		}
		
		
	
		
		
		
		/** 
		 * Calculates the dividend and orders the payment. 
		 */
		private void calculateDividend(){
			
			int currentMoney = bank.getBalance(BasicFirm.this);
			int inventoryValue = salesManager.getInventoryValue();
			
			
			// Calculate the cash reserve for meeting the capital requirement.
			int nonMoneyAssets = inventoryValue + assetValue;	
			if ( nonMoneyAssets < liabilities + subscribedCapital ){
				if (currentMoney + nonMoneyAssets > liabilities + subscribedCapital){
					tiedUpMoney = liabilities + subscribedCapital - nonMoneyAssets; // > 0
				}
				else tiedUpMoney =  currentMoney ;
			}
			else tiedUpMoney = 0;

		
			int reservedMoney = investmentReserve+requiredPayments;
			
			// Calculate the dividend.
			if (tiedUpMoney > reservedMoney){ // what ever is greater is relevant.
				if (currentMoney <= tiedUpMoney) dividend = 0;
				else dividend = currentMoney - tiedUpMoney;
			}
			else{
				if (currentMoney <= reservedMoney) dividend = 0;
				else dividend = currentMoney - reservedMoney;
			}
			
			payDividend(dividend);
		}
		

			
		/** 
		 * Makes the dividend payment. 
		 */
		private void payDividend(int dividend){
		
			if (dividend==0) return;

			int dividendPaid=0;
			for (Shareholder owner:owners.keySet()){
				int individualDividend = (int) (dividend * owners.get(owner).floatValue());
				if (individualDividend>0) owner.receiveDividend(bank.newCheque(BasicFirm.this, individualDividend ));
				dividendPaid+=individualDividend;
				this.dividendPaid += individualDividend;
			}


			int dividendRemainder = dividend - dividendPaid;
			if (dividendRemainder>owners.size()) throw new RuntimeException("Problem with dividend calculation.");
			if (dividendRemainder>0) {
				LinkedList<Shareholder> copyOfOwners = new LinkedList<Shareholder>();
				copyOfOwners.addAll(owners.keySet());
				Collections.shuffle(copyOfOwners);
				while (dividendRemainder>0){
					copyOfOwners.removeFirst().receiveDividend(bank.newCheque(BasicFirm.this, 1));
					dividendPaid+=1;
					this.dividendPaid += 1;
					dividendRemainder-=1;
				}
			}
			if (this.dividendPaid!=this.dividend) throw new RuntimeException("False dividend calculation. Dividend = "+dividend);
		}

		


		/**
		 * Starts a new round of financing.<br>
		 * Even when different investment alternatives are considered the firm uses the same financing sources.
		 */
		private void newInvestmentRound(){
			availableOffers = new LinkedList<Offer>();
			availableCredit = 0;
		}


		/** 
		 * @return the monthly average of the total financing costs of an investment.<br>
		 * Loops over getFundingOffers() below.
		 * @param requiredExpenditure the requiredExpenditure of the considered investment.
		 * @param horizon the time horizon of the considered investment.
		 */
		private int checkFeasibility(int requiredExpenditure, int horizon, boolean machinery) {

			exit = 0;
			requiredCredit = 0;
			riskPremium = 0;
			isFeasible = false;
			int availableFunds = getAvailableFunds(machinery); // indicates whether debt obligations are covered.
		
			// Debt obligations are prioritized.
			if (availableFunds < 0) {
				exit = 1;
				return 0;
			}

			//internal financing is preferred.
			if (availableFunds >= requiredExpenditure){
				exit = 2;
				isFeasible = true;
				return 0;  		
			}

			//if the firm is illiquid it can't get more credit.
			if (debtorQuality == Quality.BAD) {
				exit = 3;
				return 0;
			}
			
			requiredCredit = requiredExpenditure - availableFunds; // >0
			
			riskPremium = 0;
			/*if (coverageRatio == 0) riskPremium = 0;
			else riskPremium = 1f / coverageRatio;*/
			
			
			//computes the financing costs (out of existing offers).
			int financingCosts;
			if (requiredCredit<= availableCredit){
				isFeasible = true;
				financingCosts = getFinancingCosts(horizon);
				exit = 4;
				return financingCosts ;
			}

			//looks for external finance.
			do if (!getFundingOffers()) break ; 			//'break' aborts the loop if getFundingOffers()==false.
			while (true) ;		        					//getFundingOffers() is at least conducted once.

			//returns if credit availability is insufficient.
			if (availableCredit < requiredCredit) {
				exit = 5;
				return 0;
			}

			//computes the financing costs.
			exit = 6;
			isFeasible = true;
			financingCosts = getFinancingCosts(horizon);
			return financingCosts ;
		}


		/** 
		 * Called from above.<p>
		 * The loop is necessary because the firm may require more credit than one bank offers.<br>
		 * Partial financing from different banks is possible.
		 */
		private boolean getFundingOffers(){

			//The firms consider the entire selection because they might need more than one offer
			//and if there are only few offers in the market they might not find the second cheapest one.
			final LinkedList<Offer> creditOffers = getMarket("loans").searchOffers(true, true) ;
			if (creditOffers == null) return false; //there are no more offers in the market.

			Iterator<Offer> iter = creditOffers.iterator() ;
			while (iter.hasNext()) {								
				Offer creditOffer = iter.next() ;
				if (availableOffers.contains(creditOffer)) iter.remove() ; 
			}
			if (creditOffers.size()==0) return false; //the offers which are found are duplicates.


			Offer creditOffer = creditOffers.removeFirst();
			availableOffers.add(creditOffer);
			availableCredit += creditOffer.getVolume();

			/*if (creditOffers.size()>1){
				Offer offer2 = creditOffers.removeLast();
				Bank bank2 = (Bank) offer2.getOfferer();
				bank2.notifyRejection();
			}*/

			if (availableCredit < requiredCredit) return true;
			return false;
		}


		/**
		 * Calculates the financing costs based on the available credit offers.
		 * @param requiredCredit
		 * @param horizon
		 * @return the financing costs
		 */
		private int getFinancingCosts(int horizon){
			int remainingCreditRequirement = requiredCredit;
			int financingCosts = 0;
			for (Offer creditOffer:availableOffers){
				if (creditOffer.getVolume() < remainingCreditRequirement){		
					float interest = creditOffer.getPrice() + creditOffer.getPrice() * riskPremium;
					// the average financing costs per time period 
					float averageCosts = interest * creditOffer.getVolume() * (horizon+1) / (2*horizon);
					financingCosts += averageCosts;
					remainingCreditRequirement -= creditOffer.getVolume();															
				}
				else{
					float interest = creditOffer.getPrice() + creditOffer.getPrice() * riskPremium;
					// the average financing costs per time period 
					float averageCosts = interest * remainingCreditRequirement * (horizon+1) / (2*horizon);
					financingCosts += averageCosts;
				}
			}
			return financingCosts;
		}
		
		
		/** 
		 * Indicates whether the investment is profitable
		 * @return true if profitable or if  
		 */
		private boolean checkProfitability(InvestmentAlternative project) {
			
			// This is the investment condition as documented in the thesis.
			float expectedMarginalRevenue = salesManager.currentPrice * project.marginalOutput;
			float unitCosts = (project.requiredExpenditure+project.fixedCostComponent) / project.marginalOutput;
			float currentContributionMargin = contributionMargin + expectedMarginalRevenue / unitCosts;
			
			if (currentContributionMargin < fixedCosts + writeDowns.getFirst()) return true;
			
			// You could also choose something more simple.
			//if (contributionMargin < fixedCosts) return true;
			
			/*boolean profitable;
			int productionlevel = currentProductionLevel;
			float oldAverageCost = (totalCosts) / (currentProductionLevel);
			float wage = investmentManager.offeredWage;
			float unitCosts = (project.requiredExpenditure+project.fixedCostComponent) / project.marginalOutput;
			float unitCostsExFin = (project.requiredExpenditure) / project.marginalOutput;
			float expectedMarginalRevenue = salesManager.currentPrice * project.marginalOutput;
			if (newAverageCost<threshold) profitable=true; else profitable = false;*/
	
			
			// This condition is not documented in the dissertation.
			// Putting a ceiling on external financing in operations investments reduces the volatility of of the price levels.
			if ((float) project.fixedCostComponent / (float) project.requiredExpenditure > 0.1f){ 
				return false;
			}
			
			// This defines the profitability consideration.
			float threshold = salesManager.currentPrice*(1-parameters.firmMarkup);
			float newAverageCost = (totalCosts+project.requiredExpenditure+project.fixedCostComponent) 
								 / (currentProductionLevel+project.marginalOutput);

			if (newAverageCost < threshold) return true;
			
			// Gives a cost warning to the sales manager if unit costs become prohibitive.
			salesManager.costWarning = true;
			return false;
		}


		/** 
		 * Finances the investment. <br>
		 * If necessary takes up credit from the credit market. 
		 */
		private void acquireFunding(InvestmentAlternative investment){

			if (investment.externalFinance==0){
				internalFinancing += investment.requiredExpenditure;
				return;
			}

			int remainingCreditRequirement = investment.externalFinance;
			while (remainingCreditRequirement>0){

				Offer creditOffer = availableOffers.removeFirst();
				Creditor creditor = (Creditor) creditOffer.getOfferer();
				float interestRate = creditOffer.getPrice() + creditOffer.getPrice() * investment.riskPremium;

				if (creditOffer.getVolume() <= remainingCreditRequirement){
					CreditContract newContract;
					if (investment.marginalOutput > 0) {
						newContract = new CreditContract(
								creditor,
								BasicFirm.this, 
								creditOffer.getVolume(), 
								interestRate, 
								investment.horizon,
								true,
								timer);
						operatingLoans.add(newContract);
					}
					else {
						newContract = new CreditContract(
								creditor,
								BasicFirm.this, 
								creditOffer.getVolume(), 
								interestRate, 
								investment.horizon,
								false,
								timer);
						investmentLoans.add(newContract);
						obligations.add(newContract);
						newInvestmentCredit+=newContract.getVolume();
						int obligation = (int) (newContract.getInterestRate()*newContract.getVolume()) + newContract.getRedemption();
						requiredPayments += obligation;
					}
					AbstractCheque cheque = creditor.acceptDebtor(newContract);
					bank.deposit(BasicFirm.this, cheque ) ;
					newCredit += cheque.getAmount() ;
					remainingCreditRequirement -= cheque.getAmount();
				}
				else{
					CreditContract newContract;
					if (investment.marginalOutput > 0){
						newContract = new CreditContract(
								creditor,
								BasicFirm.this, 
								remainingCreditRequirement,
								interestRate, 
								investment.horizon,
								true,
								timer);
						operatingLoans.add(newContract);
					}
					else{
						 newContract = new CreditContract(
									creditor,
									BasicFirm.this, 
									remainingCreditRequirement, 
									interestRate, 
									investment.horizon,
									false,
									timer);
						investmentLoans.add(newContract);
						obligations.add(newContract);
						newInvestmentCredit+=newContract.getVolume();
						int obligation = (int) (newContract.getInterestRate()*newContract.getVolume()) + newContract.getRedemption();
						requiredPayments += obligation;
					}
					AbstractCheque cheque = creditor.acceptDebtor(newContract);
					bank.deposit(BasicFirm.this, cheque);
					newCredit += cheque.getAmount();
					remainingCreditRequirement = 0;
				}
			}

			internalFinancing += (investment.requiredExpenditure-investment.externalFinance);

		}
		
		
		/** Adds a fixed asset to the list of assets.<br>
		 *  Assets are represented by their remaining book value. */
		private void registerFixedAssets(int price) {
			assetValue += price;
			CAPEX += price;
			int tauK = productionFunction.getTauK();
			int monthlyWriteDown = price / tauK;
			int remainder = price % tauK;
			for (int i = 0; i<tauK; i++){ //i=0 -> write down in time period of purchase.
				int previousValue = writeDowns.get(i);
				if (i<remainder) writeDowns.set(i, previousValue+monthlyWriteDown+1);
				else writeDowns.set(i, previousValue+monthlyWriteDown);
			}
		}
		
		
		/** Registers an investment in intermediate goods and labor. */
		private void registerInvestment(InvestmentAlternative investment) {
			wagePayments += (int) investmentManager.offeredWage;
			OPEX += investment.requiredExpenditure;
			totalCosts += investment.requiredExpenditure;
			totalCosts += investment.fixedCostComponent; 
			contributionMargin = currentProductionLevel * (int) salesManager.currentPrice - OPEX;
		}
		
		
		
		
		


		/**
		 * Pays the debt obligations.<br>
		 * Files bankruptcy if obligations can't be paid.
		 */
		private void payInterest() {

			if (obligations.size()==0) return;
			int cash = bank.getBalance(BasicFirm.this);

			if (cash >= requiredPayments){
				for (CreditContract contract : obligations){
					Creditor creditor = contract.getCreditor();
					int interestAmount =  (int) (contract.getVolume()*contract.getInterestRate()) + contract.getDeferredInterest();
					int redemptionAmount = contract.getRedemption();
					if (interestAmount>0){
						AbstractCheque cheque1 = bank.newCheque(BasicFirm.this, interestAmount);
						creditor.receiveInterestPayment(cheque1);
						contract.interestPayment(interestAmount);
					}
					if (redemptionAmount>0){		
						AbstractCheque cheque2 = bank.newCheque(BasicFirm.this, redemptionAmount);
						creditor.receiveRedemption(cheque2);
						contract.subtract(redemptionAmount);  
					}								
					financingCosts += interestAmount;
					redemption += redemptionAmount;
				}
				debtorQuality = Quality.GOOD;
				clearing();
			}

			else {
				//The firm is illiquid. 
				//The haircut describes the percentage of the required payments which each creditor gets.
				float haircut = (float) cash / (float) requiredPayments;  // <1
				int intendedPayments = 0;
				for (CreditContract contract : obligations){	
					int requiredPayment = (int) (contract.getInterestRate()*contract.getVolume()) 
								+ contract.getDeferredInterest()
								+ contract.getRedemption();
					intendedPayments += (int) (haircut * requiredPayment);
				}
				int remainder = cash - intendedPayments;			

				int i = 0;
				for (CreditContract contract : obligations){ //Note that the order in which debt contracts are served is the order in which they were signed.
					i++;
					Creditor creditor = contract.getCreditor();
					int requiredInterest =  (int) (contract.getVolume()*contract.getInterestRate()) + contract.getDeferredInterest();
					int requiredRedemption = contract.getRedemption();
					int payment = (int) (haircut * (requiredInterest+requiredRedemption));
					if (i<=remainder) payment += 1;	

					//The outstanding amount of debt is only reduced if interest has been paid.
					if (payment >= requiredInterest){
						int redemptionAmount = payment - requiredInterest;
						if (requiredInterest>0) {
							AbstractCheque cheque1 = bank.newCheque(BasicFirm.this, requiredInterest);		
							creditor.receiveInterestPayment(cheque1);
							contract.interestPayment(requiredInterest);
						}
						if (redemptionAmount>0){
							AbstractCheque cheque2 = bank.newCheque(BasicFirm.this, redemptionAmount);
							creditor.receiveRedemption(cheque2);
							contract.subtract(redemptionAmount);  
						}								
						if (requiredRedemption > redemptionAmount) contract.deferPayment();
						financingCosts += requiredInterest;
						redemption += redemptionAmount;
					}
					else{
						if (payment>0) {
							AbstractCheque cheque1 = bank.newCheque(BasicFirm.this, payment);
							creditor.receiveInterestPayment(cheque1);
							contract.interestPayment(payment);
						}
						contract.deferInterest(requiredInterest - payment);
						financingCosts += payment;
					}


				}

				if (bank.getBalance(BasicFirm.this)!=0) throw new RuntimeException("Money should have been paid out.");

				if (debtorQuality==Quality.GOOD) debtorQuality = Quality.DOUBTFUL;
				else if (debtorQuality==Quality.DOUBTFUL) debtorQuality = Quality.BAD;	
				else if (debtorQuality==Quality.BAD) {
					((FirmSector) sector).bankruptcy(BasicFirm.this);
					for (CreditContract contract : operatingLoans) contract.getCreditor().notifyDefault(contract);
					for (CreditContract contract : investmentLoans) contract.getCreditor().notifyDefault(contract);
					operatingLoans.clear();
					investmentLoans.clear();
					bankruptType=1;
					BasicFirm.this.goBankrupt(0);

				}

				if (bank.getBalance(BasicFirm.this)!=0) throw new RuntimeException("Firm should have paid out all funds.");
			}

		}

		

		/** 
		 * Settles the credit lines if applicable. 
		 */
		private void clearing(){
			
			for (CreditContract contract : operatingLoans){
				
				if (!contract.isDue()) continue;
				Creditor creditor = contract.getCreditor();
				int creditVolume = contract.getVolume();
				if (bank.getBalance(BasicFirm.this) >= creditVolume){		
					AbstractCheque cheque = bank.newCheque(BasicFirm.this, creditVolume);
					creditor.receiveRedemption(cheque);
					contract.subtract(creditVolume); 
					redemption += creditVolume;
				}
				else {
					int remainingMoney = bank.getBalance(BasicFirm.this);
					if (remainingMoney > 0){
						AbstractCheque cheque = bank.newCheque(BasicFirm.this, remainingMoney);
						creditor.receiveRedemption(cheque);
						contract.subtract(remainingMoney); 
						redemption += remainingMoney;
					}
					contract.deferPayment();
				}
			}
		}
		
		

		/** 
		 * Prepares the financial statements. 
		 */
		private void updateProfits(){

			// Liabilities:
			operatingCredit = 0; 
			investmentCredit = 0;
			for (CreditContract contract : operatingLoans) operatingCredit += contract.getVolume();
			for (CreditContract contract : investmentLoans)	investmentCredit += contract.getVolume();
			liabilities = operatingCredit + investmentCredit;
			newOperatingCredit = newCredit - newInvestmentCredit;

			
			// Assets:
			int inventoryValue = salesManager.getInventoryValue(); //TODO: move the asset change up!
			int productionValue = investmentManager.getProductionValue();
			
			
			// Costs:
			depreciationCosts = writeDowns.removeFirst();
			assetValue -= depreciationCosts;
			

			// Income statement:
			EBIT = revenue - depreciationCosts - costsOfGoodsSold;
			netProfit = EBIT - financingCosts;
		

			// Redemption payments have already been prioritized and dividends have been paid.

			// Cash flow:
			cashFlow1 = revenue - OPEX;														// from operations
			cashFlow2 = -CAPEX; 											 				// from investment
			cashFlow3 = newCredit - financingCosts - redemption - dividend;					// from financing
			//all are ex post quantities 


			// Equity:
			equity =  bank.getBalance(BasicFirm.this)
					+ productionValue 
					+ inventoryValue 
					+ assetValue 
					- liabilities;
			
			leverage = (float) liabilities / (float) equity;
		}


		

		/** 
		 * Updates the credit contracts.
		 */
		private void close() {
			Iterator<CreditContract> iter = operatingLoans.iterator(); 
			while (iter.hasNext()) if (iter.next().isSettled()) iter.remove();
			Iterator<CreditContract> iter2 = investmentLoans.iterator();
			while (iter2.hasNext()) if (iter2.next().isSettled()) iter2.remove();
		}
		
		
		
		/**
		 * Removes all assets.			
		 */
		private void goBankrupt() {
			assetValue = 0;
			liabilities = 0;
			equity = 0;
			leverage = 0;
			subscribedCapital = 0;
		}

	}


	/** Describes an investment project. */
	private class InvestmentAlternative{

		private final String bundle;

		private float marginalOutput;

		private int requiredExpenditure;

		private int fixedCostComponent;

		private int externalFinance;

		private float riskPremium;

		private int horizon;

		private InvestmentAlternative(String bundle, 
				float marginalOutput,  
				int expenditure, 
				int fixedCostComponent, 
				int externalFinance, 
				float riskPremium, 
				int horizon){
			this.bundle = bundle;
			this.marginalOutput = marginalOutput;
			this.requiredExpenditure = expenditure;
			this.fixedCostComponent = fixedCostComponent;
			this.externalFinance = externalFinance;
			this.riskPremium = riskPremium;
			this.horizon = horizon;
		}

	}

	/** Enumerates the investment constraints of the firm.*/
	enum InvestmentConstraint {

		UNCONSTRAINED, INPUT_CONSTRAINED, CAPACITY_CONSTRAINED, 
		LIQUIDITY_CONSTRAINED, COST_CONSTRAINED, NONE, NO_DEMAND;

		boolean isUnconstrained() {
			return (this==UNCONSTRAINED);
		}

		boolean isCostConstrained() {
			return (this==COST_CONSTRAINED);
		}

		boolean isCapacityConstrained() {
			return (this==CAPACITY_CONSTRAINED);
		}
		
		boolean isInputConstrained() {
			return (this==INPUT_CONSTRAINED);
		}
				
		boolean isLiquidityConstrained() {
			return (this==LIQUIDITY_CONSTRAINED);
		}

		boolean isNone() {
			return (this==NONE);
		}

		boolean isUninvested() {
			return (this==NO_DEMAND);
		}

	}

	/** Enumerates the sales performance of the firm.*/
	enum SalesPerformance {

		LOW, HIGH, OK;

		boolean isOk() {
			return (this==OK);
		}

		boolean isHigh() {
			return (this==HIGH);
		}

		boolean isLow() {
			return (this==LOW);
		}

	}


	/** Enumerates the sales performance of the firm.*/
	enum OfferingStatus {

		BELOW_TARGET, ON_OR_ABOVE_TARGET;

		boolean isOk() {
			return (this==ON_OR_ABOVE_TARGET);
		}

		boolean isLow() {
			return (this==BELOW_TARGET);
		}

	}


	/** The name of the firm. */
	private final int ID;

	/** The line of production in which the firm operates. */
	private final String type;

	/** The stage at which the firm operates. */
	private final int stage;

	/** The ownership structure of the firm (in percentages of ownership). */
	private final HashMap<Shareholder,Number> owners ;

	/** The bank which manages the payments of the firm. */
	private CommercialBank bank ;

	/** The production manager. */
	private final InvestmentManager investmentManager ;

	/** The sales manager. */
	private final SalesManager salesManager ;

	/** The financial manager. */
	private final FinancialManager financialManager;

	/** The market for intermediate goods.*/
	private String inputMarket ;

	/** States if and why a firm did not reach its investment target.<br>
	 *  Concerns operating investments. */
	private InvestmentConstraint investmentConstraint;
	
	/** States if and why a firm did not reach its investment target.<br>
	 *  Concerns the purchase of machines. */
	private InvestmentConstraint investmentConstraint2;

	/** The job applications that the firm has received. */
	private final LinkedList<Worker> jobApplications; 

	/** Production at the current level of investment. */
	private int currentProductionLevel;	
	
	/** Indicates whether the firm uses capital. */
	private boolean useCapital;

	@SuppressWarnings("unused")
	private boolean monitoredFirm;

	/** Indicates whether the firm is bankrupt. */
	private boolean isBankrupt = false;
	
	@SuppressWarnings("unused")
	private int bankruptSince;
	
	@SuppressWarnings("unused")
	private int bankruptType;


	/**
	 * Creates a new firm.
	 */
	public BasicFirm( 
			Integer ID, 
			Circuit aCircuit,
			FirmSector aSector,
			HashMap<Shareholder,Number> owners,
			Integer money,
			String type,
			Integer stage) {
		super("Company"+ID, aCircuit,  aSector);
		this.ID = ID;
		this.type = type;
		this.stage = stage;
		bank = getBankingSector().selectRandomBank();
		bank.getNewAccount(this, money);
		this.owners = owners;
		for (Shareholder owner:owners.keySet()) owner.newEquityHolding(this);
		inputMarket = null;
		jobApplications = new LinkedList<Worker>();
		int productionUnit =  (int) productionFunction.getPrL();
		int initialWorkers;
		int offeringTarget;
		if (!type.equals("A")) {
			inputMarket = type+stage;
			initialWorkers = parameters.standardFirmSize;
			offeringTarget = initialWorkers * productionUnit;
		}
		else{
			initialWorkers = parameters.muK * parameters.standardFirmSize;
			offeringTarget = initialWorkers * productionUnit;
		}
		float offeredWage = (0.9f * money) / initialWorkers + getRandom().nextInt(100);  
		investmentManager = new InvestmentManager(offeringTarget, productionUnit, offeredWage,false) ; 
		salesManager = new SalesManager(
				type, 
				stage, 
				productionUnit,
				offeringTarget, 
				offeredWage/((1-(parameters.delta+parameters.firmMarkup))*productionFunction.getPrL())); 
		financialManager = new FinancialManager(money);
		monitoredFirm = false;
	}
	


	/**
	 * Creates a new firm.
	 */
	public BasicFirm( 
			InvestmentProject project,
			Circuit aCircuit,
			FirmSector aSector,
			Boolean monitor) {
		super("Company"+project.getSpecificID(), aCircuit, aSector);
		this.ID = project.getSpecificID();
		type = project.getType();
		stage = project.getStage();
		int nextStage = stage - 1;
		bank = getBankingSector().selectRandomBank();
		bank.getNewAccount(this,0);
		AbstractCheque cheque = getInvestmentBank().underwrite(project.getID());
		bank.deposit(this, cheque);
		owners = project.getOwners();
		for (Shareholder owner:owners.keySet()) owner.newEquityHolding(this);
		float offeredWage = (float) getMarketSector().getPrevailingWage();
		jobApplications = new LinkedList<Worker>();
		inputMarket = null;
		Market targetMarket;
		int productionUnit;
		int offeringTarget; 
		if (!type.equals("A")) {
			inputMarket = type+stage;
			targetMarket = getMarket(type+nextStage); 
			if (!project.physicalCapital()) productionUnit = (int) (productionFunction.getPrL());
			else productionUnit = productionFunction.getPrX() * productionFunction.getCapL();
			offeringTarget = productionUnit * project.getFirmSize(); 
		}
		else {
			targetMarket = getMarket("machines"); 
			productionUnit = (int) (productionFunction.getPrL());
			offeringTarget = parameters.muK; //Type-A firms are created so as to be able to offer on entire machine. TODO: Revise? 
		}
		investmentManager = new InvestmentManager(offeringTarget, productionUnit, offeredWage, project.physicalCapital()); 
		float askedPrice = targetMarket.getPriceLevel();
		if (Float.isNaN(askedPrice)) askedPrice = offeredWage/((1-(parameters.delta+parameters.firmMarkup))*productionFunction.getPrL());
		salesManager = new SalesManager(
				type, 
				stage, 
				productionUnit,
				offeringTarget,
				askedPrice); 
		financialManager = new FinancialManager(cheque.getAmount());
		monitoredFirm = monitor;
	}



	/** 
	 * Opens the household for a new period.<br>
	 * Initializes data and executes events.
	 * @param eList - a list of strings that describes the events for the current period. 
	 */
	public void open() {
		investmentConstraint = InvestmentConstraint.NO_DEMAND;
		investmentConstraint2 = InvestmentConstraint.NO_DEMAND;
		investmentManager.open();
		financialManager.open();
		salesManager.open();
		data = new BasicAgentDataset(this.name);
	}


	/**
	 * Offers commodities in the goods market.
	 */
	public void makeOffer(){
		salesManager.offerCommodities();
	}
	

	/**
	 * The firm revises its strategies.<br>
	 * If there is an acute shortage of intermediate goods the firm switches to pure labor production.
	 */
	public void setProductionMode() {
		salesManager.setProductionMode();
		investmentManager.planProduction0();
		financialManager.calculateDividend();
		if (type.equals("A")) investmentManager.planProduction();
	}


	/**
	 * Buys machinery.
	 */
	public boolean buyMachinery() {
	
		if (!useCapital) return false;
		if (getMarket("machines") == null) return false;
		
		int prX =  productionFunction.getPrX();
		int tauK =  productionFunction.getTauK();
		int capL =  productionFunction.getCapL();

		int averageProduction = investmentManager.getAverageProduction();
		int targetedCapacity = (int) Math.floor((double) averageProduction / (double) prX);
		int installedCapacity = investmentManager.capacity;
		int currentWorkforceRequirement = investmentManager.machinery.size() + (targetedCapacity-installedCapacity) / capL;
		if (targetedCapacity - installedCapacity < prX){
			investmentConstraint2 = InvestmentConstraint.NO_DEMAND;
			return false;
		}
		
		// a little bit of redundancy here, but the method is able to handle different machine sizes.
		TreeMap<Float, InvestmentAlternative> investmentAlternatives = new TreeMap<Float, InvestmentAlternative>();
		int totalCosts, savedLaborCosts, newCapacity = 0;
		
		// maxExponent = 1 makes sure that capital cannot be accumulated and the capacity function remains
		// linear, as specified in the dissertation.
		int maxExponent = 1;
		/*int maxExponent = (int) Math.floor(Math.log(targetedCapacity - installedCapacity) / Math.log(prX)); // firms start off with multiple small machines instead of fewer big machines
		if (maxExponent == 0) maxExponent++;*/
		
		int maxMachineParts = maxExponent * parameters.muK;
		int machineParts = 0;
		int availableInputs = 0;
		LinkedList<Offer> inputOffers = new LinkedList<Offer>();
		financialManager.newInvestmentRound();
		
		
		do{
			machineParts += parameters.muK;
			
			while (availableInputs < machineParts){
				Market market = getMarket("machines");
				LinkedList<Offer> newOffers = null;
				if (market != null){
					newOffers = market.searchOffers(true, false);
					/*LinkedList<Offer> newOffers2 = market.searchOffers(true, false);
					if (newOffers2!=null){
						newOffers.addAll(newOffers2);
						Collections.sort(newOffers);
					}*/
				}
				if (newOffers==null) break; 	// no more offers available.
				Iterator<Offer> iter = newOffers.iterator() ;
				while (iter.hasNext()) {								
					Offer newOffer = iter.next() ;
					if (inputOffers.contains(newOffer)) iter.remove() ; 
				}
				if (newOffers.size()==0) break; // the offers which are found are duplicates.
				Offer goodsOffer = newOffers.removeFirst();
				availableInputs += goodsOffer.getVolume();
				inputOffers.add(goodsOffer);
			}	
			
			if (availableInputs<machineParts){
				investmentConstraint2 = InvestmentConstraint.INPUT_CONSTRAINED;
				return false;
			}

			int requiredExpenditure = 0;
			int counter=0;
			int registeredInputs = 0;
			while (machineParts>registeredInputs){
				if (inputOffers.get(counter).getVolume() >= machineParts - registeredInputs){ 
					requiredExpenditure += (int) (inputOffers.get(counter).getPrice())* (machineParts - registeredInputs);
					registeredInputs = machineParts;
				}
				else{
					requiredExpenditure += (int) (inputOffers.get(counter).getPrice())*inputOffers.get(counter).getVolume();
					registeredInputs += inputOffers.get(counter).getVolume();
					counter++;
				}
			}

			int averageCosts = requiredExpenditure / tauK;
			int averageFinancingCosts = financialManager.checkFeasibility(requiredExpenditure,tauK,true);
			totalCosts = averageFinancingCosts + averageCosts;

			if (financialManager.isFeasible){
				InvestmentAlternative investmentAlternative = new InvestmentAlternative(
						String.valueOf(machineParts),
						0,
						requiredExpenditure,
						averageFinancingCosts+averageCosts,
						financialManager.requiredCredit,
						financialManager.riskPremium,
						tauK); 
				
				int machineCapacity = (int) Math.pow(parameters.capK, machineParts / parameters.muK);
				newCapacity = investmentManager.capacity + machineCapacity;
				int newWorkforceRequirement = investmentManager.machinery.size() + 1 + (targetedCapacity-newCapacity) / capL;
				savedLaborCosts = (int) (investmentManager.offeredWage) * (currentWorkforceRequirement-newWorkforceRequirement);
				float costRatio = (float) savedLaborCosts / (float) totalCosts;
				investmentAlternatives.put(costRatio,investmentAlternative);
				
			}
			else break;
		} while (machineParts < maxMachineParts);
			
		if (investmentAlternatives.size()==0) {
			investmentConstraint2 = InvestmentConstraint.LIQUIDITY_CONSTRAINED;
			return false;
		}
		
		while (investmentAlternatives.firstKey() < 1) {
			investmentAlternatives.remove(investmentAlternatives.firstKey());
			if (investmentAlternatives.size()==0) {
				investmentConstraint2 = InvestmentConstraint.COST_CONSTRAINED;
				return false;
			}
		}
		
		InvestmentAlternative investmentProject = investmentAlternatives.get(investmentAlternatives.lastKey());
		
		financialManager.acquireFunding(investmentProject);
		int actualMachineParts = Integer.parseInt(investmentProject.bundle);
		for (int i=1; i <= actualMachineParts; i++){
		
			if (inputOffers.getFirst().getVolume()==0) inputOffers.removeFirst();
			Offer goodsOffer = inputOffers.getFirst();
			Seller provider = (Seller) goodsOffer.getOfferer();
			int price = (int) goodsOffer.getPrice();
			provider.sell(bank.newCheque(this,price));
		}
		investmentManager.addMachine(new Machine(actualMachineParts / parameters.muK, parameters.capK, tauK, timer));
		financialManager.registerFixedAssets(investmentProject.requiredExpenditure);
		
		if (newCapacity >= targetedCapacity) {
			investmentConstraint2 = InvestmentConstraint.UNCONSTRAINED;
			return false;
		}
		return true;
	}
	
	
	/**
	 * Updates prices and quantities in the market for machines.
	 */
	public void updateMachinePrice() {
		if (!type.equals("A")){
			financialManager.registerInvestmentCredit();
			return;
		}
		salesManager.updatePrice();
	}
	
	
	/**
	 * Plans the production.
	 */
	public void planProduction() {
		if (type.equals("A")) return;
		investmentManager.planProduction();
	}
	
	
	/**
	 * Offers jobs in the labor market.
	 */
	public void offerJobs(){
		jobApplications.clear();
		investmentManager.offerJobs();
	}
	
	
	/**
	 * Receives a job application.
	 */
	public void receiveApplication(Worker applicant) {

		if (investmentManager.jobOffer.getVolume()==0) throw new RuntimeException ("Offer should have been removed.");
		jobApplications.add(applicant);
		investmentManager.applications ++;
		investmentManager.updateJobOffer(1);
		if (!useCapital & investmentConstraint.isInputConstrained()){
			((FirmSector) sector).registerInvestmentDemand(this);
		}
		if (useCapital & investmentConstraint.isCapacityConstrained()){
			((FirmSector) sector).registerInvestmentDemand(this);
		}
	}


	/**
	 * Takes note that his job offer has been rejected.
	 */
	public void notifyRejection() {
		investmentManager.rejectedOffer = true;
	}


	/** Invests. 
	 * @return true if additional investments are desired.<br>
	 *  	   false if the desire for investments is saturated.
	 */
	public boolean invest() {
		
		if (investmentManager.productionTarget<=0) throw new RuntimeException ("No demand.");
		if (currentProductionLevel >= investmentManager.productionTarget) throw new RuntimeException ("No demand.");

		financialManager.newInvestmentRound();
		investmentConstraint =  InvestmentConstraint.NONE;
		InvestmentAlternative investmentProject = null;
			
		int requiredInputs = 0;
		LinkedList<Offer> goodsOffers = new LinkedList<Offer>();
		
		if (useCapital){
		
			int productionUnit = investmentManager.productionSchedule.get(investmentManager.productionCounter);
			requiredInputs = productionUnit / productionFunction.getPrX();	
			int availableInputs = 0;
			
			do{
				Market market = getMarket(inputMarket);
				LinkedList<Offer> newOffers = null;
				if (market != null) newOffers = market.searchOffers(true, false);
				if (newOffers==null) break; 	// no more offers available.
				Iterator<Offer> iter = newOffers.iterator() ;
				while (iter.hasNext()) {								
					Offer newOffer = iter.next() ;
					if (goodsOffers.contains(newOffer)) iter.remove() ; 
				}
				if (newOffers.size()==0) break; // the offers which are found are duplicates.
				Offer goodsOffer = newOffers.removeFirst();
				availableInputs += goodsOffer.getVolume();
				goodsOffers.add(goodsOffer);
			}	while (availableInputs<requiredInputs);
			
	

			
			if (availableInputs<requiredInputs){
				investmentConstraint = InvestmentConstraint.INPUT_CONSTRAINED;
				investmentManager.removeJobOffers();
				while (jobApplications.size()>0) jobApplications.removeFirst().notifyRejection(this);
				return false; // this is final. 
			}

			if (jobApplications.size()==0) {
				

				investmentConstraint = InvestmentConstraint.CAPACITY_CONSTRAINED;
				investmentManager.laborShortage = true;
				return false;
			}


			float marginalOutput = investmentManager.getMarginalOutput(1,requiredInputs);
			int requiredExpenditure = (int) (investmentManager.offeredWage);
			int counter=0;
			int registeredInputs = 0;
			while (requiredInputs>registeredInputs){
				if (goodsOffers.get(counter).getVolume() >= requiredInputs - registeredInputs){ 
					requiredExpenditure += (int) (goodsOffers.get(counter).getPrice())* (requiredInputs - registeredInputs);
					registeredInputs = requiredInputs;
				}
				else{
					requiredExpenditure += (int) (goodsOffers.get(counter).getPrice())*goodsOffers.get(counter).getVolume();
					registeredInputs += goodsOffers.get(counter).getVolume();
					counter++;
				}
			}
			
			int financingCosts = financialManager.checkFeasibility(requiredExpenditure,1,false);
			
		
			if (financialManager.isFeasible){
				investmentProject = new InvestmentAlternative(
						"xl",
						marginalOutput,
						requiredExpenditure,
						financingCosts,
						financialManager.requiredCredit,
						financialManager.riskPremium,
						1); 
			}
			else{
				investmentConstraint = InvestmentConstraint.LIQUIDITY_CONSTRAINED;
				if (investmentManager.expansion & financialManager.exit == 3){ // this is final
					investmentManager.removeJobOffers();
					while (jobApplications.size()>0) jobApplications.removeFirst().notifyRejection(this);
				}
				if (financialManager.exit == 5){ // this is final too.
					investmentManager.removeJobOffers();
					while (jobApplications.size()>0) jobApplications.removeFirst().notifyRejection(this);
				}
				return false;
			}
		
		}

		else{

			if (jobApplications.size()==0) {
				investmentConstraint = InvestmentConstraint.INPUT_CONSTRAINED;
				investmentManager.laborShortage = true;
				return false;
			}
			
			float marginalOutput = investmentManager.getMarginalOutput(1,0);
			int requiredExpenditure = (int) investmentManager.offeredWage;
			int financingCosts = financialManager.checkFeasibility(requiredExpenditure,1,false);
			
			if (financialManager.isFeasible){
				investmentProject = new InvestmentAlternative(
						"l",
						marginalOutput,
						requiredExpenditure,
						financingCosts,
						financialManager.requiredCredit, 
						financialManager.riskPremium,
						1); 
			}
			else{
				if (type.equals("A")){	// type-A firms cannot become liquid again.
					investmentManager.removeJobOffers();
					while (jobApplications.size()>0) jobApplications.removeFirst().notifyRejection(this);
				}
				investmentConstraint = InvestmentConstraint.LIQUIDITY_CONSTRAINED;
				return false;
			}
		}	

		boolean isProfitable = financialManager.checkProfitability(investmentProject);
		
		if (!isProfitable) {
			investmentConstraint = InvestmentConstraint.COST_CONSTRAINED;
			investmentManager.removeJobOffers();	
			while (jobApplications.size()>0) jobApplications.removeFirst().notifyRejection(this);
			return false;
		}


		// The firm makes the preferred investment.
		if (investmentProject.bundle.equals("l")){
			
			financialManager.acquireFunding(investmentProject); 
			Worker jobApplicant = jobApplications.removeFirst();
			int wage = (int) investmentManager.offeredWage;
			jobApplicant.notifyHiring(bank.newCheque(this,wage));
			investmentManager.addFactors(1,0);
			financialManager.registerInvestment(investmentProject);
		}


		if (investmentProject.bundle.equals("xl")){ 

			financialManager.acquireFunding(investmentProject);
			Worker jobApplicant = jobApplications.removeFirst();
			int wage = (int) investmentManager.offeredWage;
			jobApplicant.notifyHiring(bank.newCheque(this,wage));
			investmentManager.registerPrices(goodsOffers);
			for (int i=1; i<=requiredInputs; i++){
				if (goodsOffers.getFirst().getVolume()==0) goodsOffers.removeFirst();
				Offer goodsOffer = goodsOffers.getFirst();
				Seller provider = (Seller) goodsOffer.getOfferer();
				int price = (int) goodsOffer.getPrice();
				provider.sell(bank.newCheque(this,price));
			}
			investmentManager.addFactors(1,requiredInputs);
			financialManager.registerInvestment(investmentProject);
		}
		
			
		// The investment demand is now satisfied (but it may be raised again if a product is sold). 
		if (currentProductionLevel >= investmentManager.productionTarget){
			if (currentProductionLevel > investmentManager.productionTarget) throw new RuntimeException("Overproduction.");
			investmentConstraint = InvestmentConstraint.UNCONSTRAINED;
			return false;
		}

		// The investment demand is not yet satisfied. 
		return true;
	}


	/**
	 * Sells one unit of the offered commodity to another agent.<br>
	 * Puts the received money in the bank bank.
	 */
	public void sell(AbstractCheque cheque ) {
		bank.deposit(this,cheque) ;
		salesManager.sell(cheque);
	}


	/** Releases some job applicants. */
	public void laborRelease() {
		investmentManager.laborRelease();
	}
	
	
	/** Releases some job applicants. */
	public void registerExpansionDemand() {
		investmentManager.regsiterExpansionDemand();
	}


	/** Updates the prices and the quantity target. */
	public void updatePrice() { 
		if (!type.equals("A")) salesManager.updatePrice();
		investmentManager.updateOfferedWage();
	}


	/** Makes interest and redemption payments. */
	public void payInterest() { 		
		financialManager.payInterest() ;	
	}


	/** Produces. */
	public void production() { 		
		investmentManager.produce() ;	
	}


	/** Updates profits. */
	public void updateProfits() { 		
		financialManager.updateProfits() ;	
	}

	/**
	 * Closes the firm.<br>
	 * Completes some technical operations at the end of the period and updates the data.
	 */
	public void close() {

		if (investmentConstraint.isNone()) throw new RuntimeException("Unfinished investment loop.");

		// Firms with zero sales and zero debt cancel their operations.
		if (salesManager.zeroSalesPeriods>6 && financialManager.operatingLoans.size()==0 && financialManager.investmentLoans.size()==0 && !isBankrupt){
			int remainingFunds = bank.getBalance(this);
			((FirmSector) sector).bankruptcy(this);
			bankruptType=2;
			goBankrupt(remainingFunds);
		}
		

		financialManager.close();

		//For data handling. (Can occur when a firm has lowered the offering target and had low sales.)
		if (investmentManager.productionTarget < 0) investmentManager.productionTarget = 0; 

		updateData();
	}
	
	
	/**
	 * Goes bankrupt.
	 */
	public void goBankrupt(int remainingFunds) {
		isBankrupt = true;
		bankruptSince = timer.getPeriod().intValue();
		if (remainingFunds>0) {
			financialManager.dividend += remainingFunds;
			financialManager.payDividend(remainingFunds);		
		}
		investmentManager.goBankrupt();
		salesManager.goBankrupt();
		financialManager.goBankrupt();
		for (Shareholder owner:owners.keySet()) owner.notifyDefault(this);
	}


	/**
	 * Updates the data.
	 */
	private void updateData(){
			
		data.put("moneyHoldings" ,(double) bank.getBalance(this));
		data.put("debt" ,(double) financialManager.liabilities);
		data.put("acquiredFunds" ,(double) financialManager.newCredit);
		data.put("grossProfit" ,(double) financialManager.EBIT);
		data.put("netProfit" ,(double) financialManager.netProfit);
		data.put("depreciation" ,(double) financialManager.depreciationCosts);
		data.put("costsOfGoodsSold" ,(double) financialManager.costsOfGoodsSold);
		
		if (financialManager.netProfit>0) {
			data.put("profits" , (double) financialManager.netProfit);
			data.put("losses" , 0d);
		}
		else {
			data.put("profits" , 0d);
			data.put("losses" , (double) financialManager.netProfit);
		}

		data.put("machinerySize" ,(double) investmentManager.machinery.size());
		data.put("processingCapacity" ,(double) investmentManager.getProcessingCapacity());
		data.put("pureLaborProduction" ,(double) investmentManager.getExcessLabor());
		data.put("intermediateInputs" ,(double) investmentManager.intermediateInputs);

		data.put("price" ,(double) salesManager.currentPrice);
		data.put("nextOfferingTarget" ,(double) salesManager.nextOfferingTarget);
		data.put("productionTarget" ,(double) investmentManager.productionTarget);
		data.put("productionVolume" ,(double) investmentManager.getProductionVolume());
		data.put("productionValue" ,(double) investmentManager.getProductionValue());
		data.put("offeringTarget" ,(double) salesManager.offeringTarget);
		data.put("offeredGoods" ,(double) salesManager.offeredGoods);
		data.put("salesVolume" ,(double)salesManager.sales);
		data.put("inventoriesVolume" ,(double) salesManager.getInventoryVolume());
		data.put("inventoriesValue" ,(double) salesManager.getInventoryValue());
		data.put("inventoryRatio" ,(double) salesManager.inventoryRatio);
		data.put("revenue" ,(double) financialManager.revenue);
		data.put("equity" ,(double) financialManager.equity);
		data.put("dividendsPaid" ,(double) financialManager.dividendPaid);
		data.put("interestPaid" ,(double) financialManager.financingCosts);
		data.put("redemptionPaid" ,(double) financialManager.redemption);
		data.put("leverage", financialManager.leverage);
		data.put("registeredCapital" ,(double) financialManager.subscribedCapital);
		data.put("fixedAssets" ,(double) financialManager.assetValue);
		data.put("machineInvestment" ,(double) financialManager.CAPEX);
		data.put("operationsInvestment" ,(double) financialManager.OPEX);
		data.put("intermediateGoodsInvestment" ,(double) financialManager.OPEX-financialManager.wagePayments);
		data.put("tiedUpMoney" ,(double) financialManager.OPEX);
		data.put("initialMoney" ,(double) financialManager.initialMoney);
		data.put("retainedEarnings" ,(double) financialManager.retainedEarnings);
		data.put("newOperatingCredit" ,(double) financialManager.newOperatingCredit);
		data.put("newInvestmentCredit" ,(double) financialManager.newInvestmentCredit);
		data.put("numberOfLoans" ,(double) financialManager.operatingLoans.size());

		data.put("investmentReserve" ,(double) financialManager.investmentReserve);

		data.put("applicationTarget" ,(double) investmentManager.applicationTarget);
		data.put("applications" ,(double) investmentManager.applications);
		data.put("hiring" ,(double) investmentManager.workforce);
		data.put("remainingApplications" ,(double) jobApplications.size());
		data.put("offeredWage" ,(double)investmentManager.offeredWage);
		
		data.put("firms", 1d);
		data.put("stage", (double) stage);
		data.put("type", (double) getType());
		
		if (investmentConstraint.isUnconstrained()) data.put("unconstrained",1d); else data.put("unconstrained",0d);
		if (investmentConstraint.isUninvested()) data.put("noDemand",1d); else data.put("noDemand",0d);
		if (investmentConstraint.isCapacityConstrained()) data.put("capacityConstrained",1d); else data.put("capacityConstrained",0d);
		if (investmentConstraint.isCostConstrained()) data.put("costConstrained",1d); else data.put("costConstrained",0d);
		if (investmentConstraint.isInputConstrained()) data.put("inputConstrained",1d); else data.put("inputConstrained",0d);
		if (investmentConstraint.isLiquidityConstrained()) data.put("liquidityConstrained",1d); else data.put("liquidityConstrained",0d);
		
		if (investmentConstraint2.isUnconstrained()) data.put("unconstrained_2",1d); else data.put("unconstrained_2",0d);
		if (investmentConstraint2.isUninvested()) data.put("noDemand_2",1d); else data.put("noDemand_2",0d);
		if (investmentConstraint2.isCapacityConstrained()) data.put("capacityConstrained_2",1d); else data.put("capacityConstrained_2",0d);
		if (investmentConstraint2.isCostConstrained()) data.put("costConstrained_2",1d); else data.put("costConstrained_2",0d);
		if (investmentConstraint2.isInputConstrained()) data.put("inputConstrained_2",1d); else data.put("inputConstrained_2",0d);
		if (investmentConstraint2.isLiquidityConstrained()) data.put("liquidityConstrained_2",1d); else data.put("liquidityConstrained_2",0d);
		
		if (salesManager.salesPerformance.isHigh()) data.put("highSales",1d); else data.put("highSales",0d);
		if (salesManager.salesPerformance.isLow()) data.put("lowSales",1d); else data.put("lowSales",0d);
		if (salesManager.salesPerformance.isOk()) data.put("normalSales",1d); else data.put("normalSales",0d);
		
		if (salesManager.offeringStatus.isLow()) data.put("lowOffers",1d); else data.put("lowOffers",0d);
		if (salesManager.offeringStatus.isOk()) data.put("highOffers",1d); else data.put("highOffers",0d);
		
		if (salesManager.salesPerformance.isLow()) data.put("lowSales",1d); else data.put("lowSales",0d);
		if (salesManager.salesPerformance.isOk()) data.put("normalSales",1d); else data.put("normalSales",0d);
		
		if (financialManager.debtorQuality.isTripleA()) data.put("AAA",1d); else data.put("AAA",0d);
		if (financialManager.debtorQuality.isGood()) data.put("goodDebtorQuality",1d); else data.put("goodDebtorQuality",0d);
		if (financialManager.debtorQuality.isDoubtFul()) data.put("doubtfulDebtorQuality",1d); else data.put("doubtfulDebtorQuality",0d);
		if (financialManager.debtorQuality.isBad()) data.put("badDebtorQuality",1d); else data.put("badDebtorQuality",0d);
		
		if (stage==1 && !type.equals("A")) data.put("consumerProducer",1d); else data.put("consumerProducer",0d);
		if (!useCapital) data.put("noCapital",1d); else data.put("noCapital",0d);
		
		if (useCapital & investmentConstraint.isCapacityConstrained()){
			data.put("vacancies" ,(double) (investmentManager.productionScheduler - investmentManager.productionCounter));
		}
		else if (!useCapital & investmentConstraint.isInputConstrained()){
			data.put("vacancies" ,(double) (investmentManager.productionScheduler - investmentManager.productionCounter));
		}
		else data.put("vacancies" ,0d);
		
	}


	/**
	 * Returns the name of the firm.
	 */
	public int getID() {
		return ID;
	}


	/**
	 * Returns the name of the firm.
	 */
	public int getType() {
		return Alphabet.getPosition(type);
	}


	/**
	 * Returns the type of the firm.
	 */
	public String getTypeAlphabetic() {
		return type;
	}


	/**
	 * Returns the stage of the firm.<br>
	 * (not the stage where it sells its products).
	 */
	public int getStage() {
		return stage;
	}
	
	
	/**
	 * Returns the equity value of a specific owner.
	 */
	public int getEquityValue(Shareholder owner) {
		if (!owners.containsKey(owner)) throw new RuntimeException("Invalid ownership");
		float share = owners.get(owner).floatValue();
		return (int) (financialManager.equity*share);
	}

	
	/**
	 * Transfers the firm's checking account to another bank.
	 */
	public void getNewBank(CommercialBank newBank) {
		this.bank = newBank;
	}


	/**
	 * Makes the firm show its data in the console.
	 */
	public void setMonitor() {
		this.monitoredFirm = true;
	}

	
	/**
	 * Kills the firm.
	 */
	public void kill() {	
		bank.closeAccount(this);
		investmentManager.goBankrupt(); //TODO: necessary?
	}


	/**
	 * Never called.
	 */
	public void acquireFunding(TimeDeposit timeDeposit, AbstractCheque cheque) {
		
	}

}