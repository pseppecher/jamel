/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2010, Pascal Seppecher.
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

package jamel.austrian.markets;

import jamel.austrian.banks.CommercialBank;
import jamel.austrian.markets.MarketSector;
import jamel.austrian.sfc.SFCAgent;
import jamel.austrian.util.ComparablePoint;
import jamel.austrian.widgets.Offer;
import jamel.basic.Circuit;
import jamel.basic.data.BasicAgentDataset;

import java.util.Collections;
import java.util.LinkedList;

import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;

/**
 * A base class representing a market. Each market is constituted by a list of offers.
 * <p>
 * In Jamel, there is no auctioneer, and markets are non-clearing markets.
 * <p>
 * Last update: 14-Aug-2012.
 */
public class Market extends SFCAgent {

	/** The type. */
	@SuppressWarnings("unused")
	private final String type;
	
	/** The stage at which the goods are sold<br>
	 *  NOT the stage at which they are produced! */
	private final int stage;

	/** The collection of offers. */
	private final LinkedList<Offer> offersList ;
	
	/** The list of offers as it was at the opening of the market. */
	private final LinkedList<Offer> offersList0 = new LinkedList<Offer>();
	
	/** The list of offers and the respective sales performance (z-coordinate). */
	private final LinkedList<ComparablePoint> supply = new LinkedList<ComparablePoint>();
	
	/** The total volume of the market (at opening). */
	private int offeredVolume;
	
	/** The total volume that is offered in the market (currently). */
	private int volume;
	
	/** The average size of the offers in the market (in number of goods). */
	private int averageOfferSize = 0;  //TODO: remove?
	
	/** The asked market value of the goods (at opening). */
	private float value;
	
	/** The cumulated prices. */
	@SuppressWarnings("unused")
	private float pricesSum = 0;
	
	/** The median of the asked prices. */
	private Double medianPrice;
	
	/** The median of the asked prices in the previous time period. */
	private Double lastMedianPrice;
	
	/** The sales volume of the market. */
	private int sales;
	
	/** The turnover of the market. */
	private int turnover;
	
	/** The sales volume in the previous time period. */
	private int lastSales;
	
	/** The turnover in the previous time period. */
	private int lastTurnover;

	/** The initial average price. */
	private float initialPrice = Float.NaN;


	/**
	 * Opens the market.
	 */
	public void open() {
		
		// market handling
		offersList.clear(); 
		volume = 0 ;
		
		// data handling
		data = new BasicAgentDataset(this.name);
		lastSales = sales;
		lastTurnover = turnover;
		offersList0.clear();
		supply.clear();
		offeredVolume = 0;
		averageOfferSize = 0;
		value = 0;
		sales = 0;
		turnover = 0;
		
		// Records the last valid price level.
		if (medianPrice!=null) lastMedianPrice = medianPrice;
		medianPrice = null;
	}


	/**
	 * Adds a new offer to the market.
	 */
	public void newOffer(Offer newOffer) {
		
		// market handling
		volume += newOffer.getVolume() ;
		offersList.add(newOffer);
		
		// data handling
		offersList0.add(newOffer);
		supply.add( new ComparablePoint(newOffer.getVolume(),newOffer.getPrice(),newOffer.getVolume()) );
		offeredVolume += newOffer.getVolume();
		value += newOffer.getVolume()*newOffer.getPrice();
		pricesSum += newOffer.getPrice();
	}

	
	/**
	 * Creates a new market.
	 */
	public Market(String name, Circuit aCircuit, MarketSector sector) {
		super(name, aCircuit, sector);
		this.type = null;
		this.stage = Integer.MIN_VALUE; //or anything.
		this.offersList = new LinkedList<Offer>() ;
	}


	/**
	 * Creates a new market for goods.
	 */
	public Market(String type, int stage, Circuit aCircuit, MarketSector sector) {
		super(type+stage, aCircuit, sector);
		this.type = type;
		this.stage = stage;		
		this.offersList = new LinkedList<Offer>() ;
	}


	/**
	 * Consults a limited number of offers in the market and returns the best offer found.<br>
	 * The selection probability is proportional to relative size of the offer.<br>
	 * @param offerer a searched offerer.
	 * @param cheapest: if true, select the cheapest offer found.
	 * @return the best offer found.<br>
	 * null if there are no offers.
	 */								
	public Offer searchOffer(boolean cheapest) {

		if (offersList.size() == 0) return null ;

		LinkedList<Offer> selection = new LinkedList<Offer>();

		for (int i = 0; i < parameters.numSearch+1	; i++) {
			Offer offer = getRandomOffer() ;
			if (offer==null) break ;
			if (offer.getVolume() == 0) throw new RuntimeException("Offer should have been removed.");
			selection.add(offer);
		}

		if (selection.size() == 0) return null ;
		Collections.sort(selection);
		if (cheapest) return selection.getFirst() ;
		return selection.getLast() ;		
	}


	/**
	 * Consults a number of offers and returns the selection of offers.<br>
	 * @param sorted the results are returned in ascending price order.
	 * @param equalSelection all offers have an equal selection probability.
	 * @return the selection of offers;	null if no offers were found.
	 */									
	public LinkedList<Offer> searchOffers(boolean sorted, boolean equalSelection) {
		if (offersList.size() == 0) return null;
		LinkedList<Offer> selection = new LinkedList<Offer>();
		for (int i = 0; i < parameters.numSearch; i++) {
			Offer offer;
			if (equalSelection) offer = getRandomOffer2();
			else offer = getRandomOffer();
			if (offer==null) break;
			if (offer.getVolume() == 0) throw new RuntimeException("Offer should have been removed.");
			if (selection.contains(offer)) continue;
			selection.add(offer);
		}
		if (selection.size() == 0) return null ;
		if (!sorted) return selection;
		Collections.sort(selection);
		return selection ;		
	}


	/**
	 * The selection probability is proportional to the relative sizes of the offers.
	 * @return a randomly chosen offer from the list of offers.
	 */
	private Offer getRandomOffer()	{

		if (offersList.size()==0 | volume == 0) return null;
		int choice = getRandom().nextInt(volume)+1 ;
		int count = 0 ;
		for (Offer offer : offersList) {
			count += offer.getVolume() ;
			if (count>=choice) return offer;
		}
		throw new NullPointerException("Offer not found") ;
	}


	/**
	 * The selection probability is equal for all offers.
	 * @return a randomly chosen offer from the list of offers.
	 */
	public Offer getRandomOffer2()	{

		if (offersList.size()==0 | volume == 0) return null;

		int choice;
		LinkedList<Offer> copyList = new LinkedList<Offer>();
		copyList.addAll(offersList);
		do {
			choice = getRandom().nextInt(copyList.size());
			if (copyList.get(choice).getVolume() != 0) return copyList.get(choice);
			copyList.remove(choice);
		} while(true);

	}


	/**
	 * Updates the volume of an offer.
	 */
	public void updateOffer(Offer offer, int quantity) {

		offer.subtract(quantity);
		volume -= quantity;
	}


	/**
	 * Registers a sale and removes the offer from the market if applicable.
	 */
	public void registerSale(Offer offer, int soldQuantity, boolean keepZeros) {	
		
		//data handling
		int index = offersList0.indexOf(offer);
		supply.get(index).z -= soldQuantity;
		sales += soldQuantity;
		turnover += (int) (offer.getPrice()) * soldQuantity;
		
		//market handling
		if (!keepZeros & offer.getVolume() == 0)	{
			offersList.remove(offer);
			offer = null; 	//TODO: Why doesn't this nullify the offer field in the offerer object? 
		}
	}
	

	/**
	 * Removes an offer from the market.
	 */
	public void removeOffer(Offer offer) {
		volume-=offer.getVolume();
		offer.subtract(offer.getVolume());
		offersList.remove(offer);
		offer = null;
	}


	/**
	 * Computes the market data.
	 */
	public void close(){
		
		if (offeredVolume > 0 && Float.isNaN(initialPrice)) initialPrice = value / offeredVolume;

		if (supply.size()>0) {
			
			//Double[] array = new Double[1];
			LinkedList<Double> pricesList = new LinkedList<Double>();
			LinkedList<Double> offeredGoodsList = new LinkedList<Double>();
			LinkedList<Double> salesList = new LinkedList<Double>();
			LinkedList<Double> salesRatioList = new LinkedList<Double>();
			LinkedList<Double> supplyCurveQ = new LinkedList<Double>();

			Collections.sort(supply);
			double sumX = 0;
			for(ComparablePoint p : supply)	{
				double quantity = p.x;
				double price = p.y;
				double sales = p.x - p.z;
				double salesRatio = (1 - p.z / p.x);
				sumX += p.x;
				supplyCurveQ.add(sumX);
				offeredGoodsList.add(quantity);
				pricesList.add(price);
				salesList.add(sales);
				salesRatioList.add(salesRatio);
			}
			
			averageOfferSize = (int) (sumX / supply.size());

			/*data.put(name+"supplyq", offeredGoodsList.toArray(array));
			data.put(name+"supplyQ", supplyCurveQ.toArray(array));
			data.put(name+"prices", pricesList.toArray(array));
			data.put(name+"sales", salesList.toArray(array));
			data.put(name+"salesRatio", salesRatioList.toArray(array));*/ 

			BoxAndWhiskerItem priceStats = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(pricesList);
			medianPrice = priceStats.getMedian().doubleValue();
			
			if (name.equals("savings"))	data.put("medianSavingsRate",medianPrice);
			if (name.equals("loans")) data.put("medianLendingRate",medianPrice);

			data.put("MAX_PRICE",priceStats.getMaxRegularValue().doubleValue());
			data.put("MEAN_PRICE",priceStats.getMean().doubleValue());
			data.put("MIN_PRICE",priceStats.getMinRegularValue().doubleValue());	
		}
		
		else{
			medianPrice = null;
		}	
		
		data.put("turnover",(double) turnover);
		data.put("salesVolume",(double) sales);
		data.put("offers",(double) offeredVolume);
	}


	/**
	 * Returns the median of the prices in the market (or the last valid median price).<br>
	 * Returns NaN if no median price has ever been recorded.
	 */
	public float getPriceLevel(){
		if (medianPrice == null && lastMedianPrice == null) return Float.NaN;
		if (medianPrice == null) return lastMedianPrice.floatValue();
		return medianPrice.floatValue();
	}
	
	
	/**
	 * Returns the average number of offered goods of the firms in the market.
	 */
	public int getAverageFirmSize(){
		return averageOfferSize;
	}
	
	
	/**
	 * Returns the volume of the market (number of offered goods).
	 */
	public int getMarketVolume(){
		return offeredVolume;
	}
	
	
	/**
	 * Returns the sales in the market.
	 */
	public int getSales(){
		return sales;
	}
	
	
	/**
	 * Returns the sales of the previous time period.
	 */
	public int getLastSales(){
		return lastSales;
	}
	
	
	/**
	 * Returns the turnover in the market.
	 */
	public int getTurnover(){
		return turnover;
	}
	
	
	/**
	 * Returns the turnover of the previous time period.
	 */
	public int getLastTurnover(){
		return lastTurnover;
	}
	

	/**
	 * Returns the stage of production.
	 */
	public int getStage(){
		return this.stage;
	}

	/** 
	 * Calculates the expenditure in the market based on the average price
	 * @return The extrapolated expenditure. 
	 */
	public float getQIndex(){
		float qIndex = initialPrice * sales;
		if (Float.isNaN(qIndex)) return 0F;
		return qIndex;
	}


	@Override
	public void getNewBank(CommercialBank bank) {
		// Never called.
	}

}