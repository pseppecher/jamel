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

import jamel.JamelObject;
import jamel.agents.firms.Labels;
import jamel.agents.firms.util.Mediator;

/**
 * An abstract implementation of the {@link PricingManager} interface.<p>
 * 2013-11-10: creation.
 */
public abstract class AbstractPricingManager extends JamelObject implements PricingManager {

	@SuppressWarnings("javadoc")
	protected static final String PARAM_PRICE_FLEX = "Firms.price.flexibility";

	/** The current unit price.*/
	protected double currentPrice=0;

	/** The mediator. */
	protected Mediator mediator;

	@Override
	public Object get(String key) {
		Object result=null;
		if (key.equals(Labels.PRICE)) {
			result=this.currentPrice;
		}
		else if (key.equals(Labels.CLOSURE)) {
			this.close();
		}
		return result;
	}
	
}




















