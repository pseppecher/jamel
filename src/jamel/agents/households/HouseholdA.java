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

package jamel.agents.households;

import jamel.Circuit;
import jamel.agents.roles.Provider;

import java.util.Collections;

/**
 * An extension of the basic household.
 */
public class HouseholdA extends BasicHousehold {

	/**
	 * Creates a new household.
	 * @param aName  the name.
	 */
	public HouseholdA(String aName) {
		super(aName);
	}

	/**
	 * Updates the list of the providers.
	 */
	@Override
	protected void updateProvidersList() {
		while (providers.size()<maxProviders){
			final Provider provider = (Provider) Circuit.getResource(Circuit.SELECT_A_PROVIDER_OF_FINAL_GOODS);
			if (provider==null) break;
			if (!this.providers.contains(provider)) {
				this.providers.add(provider);
			}
		}
		Collections.sort(providers, PROVIDER_COMPARATOR);
		if (this.providers.size()>maxProviders-1)
			this.providers.removeLast();
	}
	
}