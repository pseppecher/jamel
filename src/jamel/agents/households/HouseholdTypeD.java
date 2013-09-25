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

package jamel.agents.households;

import jamel.spheres.monetarySphere.Check;
import jamel.util.data.Labels;

/**
 * A household that doesn't search a job when receives dividends.
 */
class HouseholdTypeD extends HouseholdTypeB {

	/**
	 * Creates a new household.
	 * @param aName  the name.
	 */
	public HouseholdTypeD(String aName) {
		super(aName);
	}

	/**
	 * Looks for a job.<p>
	 * If capitalist, the household doesn't search for a job.
	 */
	@Override
	public void jobSearch() {
		if (this.data.getEmploymentStatus()!=Labels.STATUS_CAPITALIST) {
			super.jobSearch();
		}
	}
	
	/**
	 * Receives a dividend.<p>
	 * If employed, the household quits his job. 
	 * @param check  the check.
	 */
	@Override
	public void receiveDividend(Check check) {
		super.receiveDividend(check);
		if (this.jobContract!=null) {
			if (this.jobContract.isValid())
				this.jobContract.breach();
			this.jobContract=null;
			this.unemploymentDuration=0;
			this.reservationWage=check.getAmount();
		}
		this.data.setEmploymentStatus(Labels.STATUS_CAPITALIST);
	}

}