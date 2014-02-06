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

package jamel.agents.firms.managers;

import jamel.Circuit;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;

/**
 * A basic pricing manager.<p>
 * The price is fixed by an adjustment procedure based on the observation of the level of inventories.
 * 2013-11-10: refactoring.
 */
public class BasicPricingManager extends AbstractPricingManager {
	
	/**
	 * Creates a new pricing manager.
	 * @param mediator  the mediator.
	 */
	public BasicPricingManager(Mediator mediator) {
		this.mediator=mediator;
	}

	/* (non-Javadoc)
	 * @see jamel.agents.firms.managers.PricingManager#updatePrice()
	 */
	@Override
	public void updatePrice() {
		final Float priceFlexibility = Float.parseFloat(Circuit.getParameter(PARAM_PRICE_FLEX));
		final Float inventoryRatio = (Float)this.mediator.get(Labels.INVENTORY_LEVEL_RATIO);		
		final Double unitCost = (Double)this.mediator.get(Labels.UNIT_COST);
		if (this.currentPrice==0) {
			this.currentPrice = (1.+getRandom().nextFloat()/2.)*unitCost;
		}
		else {
			final float alpha1 = getRandom().nextFloat();
			final float alpha2 = getRandom().nextFloat();
			if (inventoryRatio<1-alpha1*alpha2) {
				this.currentPrice = this.currentPrice*(1f+alpha1*priceFlexibility);
			}
			else if (inventoryRatio>1+alpha1*alpha2) {
				this.currentPrice = this.currentPrice*(1f-alpha1*priceFlexibility);
				if (this.currentPrice<1) this.currentPrice = 1d;
			}
		}
		if ( Double.isNaN(currentPrice) ) {
			throw new RuntimeException("This price is NaN.") ;
		}
	}

	/**
	 * Closes the manager.<p>
	 * Does nothing.
	 */
	@Override
	public void close() {		
	}

}




















