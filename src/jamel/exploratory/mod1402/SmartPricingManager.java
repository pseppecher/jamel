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

package jamel.exploratory.mod1402;

import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.managers.AbstractPricingManager;
import jamel.agents.firms.util.Mediator;

/**
 * A pricing manager that uses a trial and error process to find the good price, observing both sales and inventories.<p>
 * (new version of the old PricingManager131201).
 */
public class SmartPricingManager extends AbstractPricingManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRICE_SPAN_MAX = "Firms.price.spanMax";

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRICE_SPAN_MIN = "Firms.price.spanMin";

	/**
	 * Returns a new price chosen at random in the given interval.
	 * @param lowPrice  the lower price
	 * @param highPrice  the higher price
	 * @return the new price.
	 */
	private static double getNewPrice(Double lowPrice, Double highPrice) {
		if (lowPrice>highPrice) {
			throw new RuntimeException("lowPrice > highPrice (lowPrice = "+lowPrice+", highPrice = "+highPrice+" ).");
		}
		return lowPrice+getRandom().nextFloat()*(highPrice-lowPrice);
	}

	/** The higher price. */
	private Double highPrice = null;

	/** The lower price. */
	private Double lowPrice = null;

	/** The ratio volume of sales to volume of commodities offered. */
	private Float salesRatio;

	/** The number of rounds before the next price update. */
	private int span=0;

	/**
	 * Creates a new pricing manager.
	 * @param mediator  the mediator.
	 */
	public SmartPricingManager(Mediator mediator) {
		this.mediator=mediator;
	}

	/**
	 * Closes the pricing manager.<p>
	 * Calculates the sales ratio.
	 */
	@Override
	public void close() {
		Integer salesVolume=(Integer)this.mediator.get(Labels.SALES_VOLUME);
		final Integer offeredVolume=(Integer)this.mediator.get(Labels.OFFERED_VOLUME);
		if (offeredVolume!=0) {
			this.salesRatio=((float)salesVolume/offeredVolume);
		}
		else {
			this.salesRatio=null;			
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String string = this.getClass().getCanonicalName();
		string+=", lowPrice="+lowPrice;
		string+=", highPrice="+highPrice;
		string+=", currentPrice="+currentPrice;
		return string;
	}

	/**
	 * Updates the unit price.
	 */
	@Override
	public void updatePrice() {
		final Float priceFlexibility = Float.parseFloat(Circuit.getParameter(PARAM_PRICE_FLEX));
		final int spanMin = Integer.parseInt(Circuit.getParameter(PARAM_PRICE_SPAN_MIN));
		final int spanMax = Integer.parseInt(Circuit.getParameter(PARAM_PRICE_SPAN_MAX));
		if (spanMin>spanMax) {
			throw new RuntimeException("Scenario Error: Firms.price.spanMax must be higher then Firms.price.spanMin.");
		}
		span--;
		if (currentPrice==0){
			this.currentPrice = (Double)this.mediator.get(Labels.UNIT_COST);
			this.highPrice = (1f+priceFlexibility)*this.currentPrice;
			this.lowPrice = (1f-priceFlexibility)*this.currentPrice;
		}
		else {
			if ((salesRatio!=null) && (span<=0)) {
				final Float inventoryRatio = (Float)this.mediator.get(Labels.INVENTORY_LEVEL_RATIO);
				if ((salesRatio==1)) {
					this.lowPrice = this.currentPrice;
					if (inventoryRatio<1) {
						this.currentPrice = getNewPrice(this.lowPrice,this.highPrice);
						if (spanMax>spanMin) {
							span=spanMin+getRandom().nextInt(spanMax-spanMin);
						} else {
							span=spanMin;
						}
					}
					this.highPrice =  this.highPrice*(1f+priceFlexibility);
				}
				else {
					this.highPrice = this.currentPrice;
					if (inventoryRatio>1) {
						this.currentPrice = getNewPrice(this.lowPrice,this.highPrice);
						if (spanMax>spanMin) {
							span=spanMin+getRandom().nextInt(spanMax-spanMin);
						} else {
							span=spanMin;
						}
					}					
					this.lowPrice =  this.lowPrice*(1f-priceFlexibility);
				}
			}
		}
	}

	@Override
	public Object get(String key) {
		Object result=super.get(key);
		if (key.equals(Labels.VERBOSE)) {
			this.verbose();
		}
		return result;
	}

	/**
	 * 
	 */
	private void verbose() {
		System.out.println(getCurrentPeriod().getValue()+","+highPrice+","+lowPrice+","+currentPrice);
	}
	
	

}




















