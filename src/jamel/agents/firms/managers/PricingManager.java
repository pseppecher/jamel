/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
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

package jamel.agents.firms.managers;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.util.Blackboard;

/**
 * A basic pricing manager.
 */
public class PricingManager extends JamelObject {

	/** The current unit price.*/
	protected double currentPrice;

	/** The black board. */
	protected final Blackboard blackBoard;

	/**
	 * Creates a new pricing manager.
	 * @param blackboard  the blackboard.
	 */
	public PricingManager(Blackboard blackboard) {
		this.blackBoard = blackboard;
	}

	/**
	 * Updates the unit price.
	 */
	public void updatePrice() {
		//final Float priceFlexibility = (Float)this.blackBoard.get(Labels.PRICE_FLEXIBILITY);
		final Float priceFlexibility = Float.parseFloat(Circuit.getParameter("Firms.priceFlexibility"));
		final Float inventoryRatio = (Float)this.blackBoard.get(Labels.INVENTORY_LEVEL_RATIO);
		final Double unitCost = (Double)this.blackBoard.get(Labels.UNIT_COST);
		if (this.currentPrice==0) {
			this.currentPrice = (1.+getRandom().nextFloat()/2.)*unitCost;
			if ( Double.isNaN(currentPrice) )
				throw new RuntimeException("This price is NaN.") ;
		}
		else {
			final float alpha1 = getRandom().nextFloat();
			final float alpha2 = getRandom().nextFloat();
			if (inventoryRatio<1-alpha1*alpha2) {
				this.currentPrice = this.currentPrice*(1f+alpha1*priceFlexibility);
			}
			else if (inventoryRatio>1+alpha1*alpha2) {
				this.currentPrice = this.currentPrice*(1f-alpha1*priceFlexibility);
				if (this.currentPrice<1) this.currentPrice = 1;
			}
		}
		this.blackBoard.put(Labels.PRICE, this.currentPrice);
	}

}




















