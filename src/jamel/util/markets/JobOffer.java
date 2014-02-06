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

package jamel.util.markets;

import jamel.agents.roles.Employer;

/**
 * Represents an offer on a labour market.
 * <p>
 * Last update: 8-Dec-2010.
 */
public class JobOffer extends AbstractOffer {

	/**
	 * Creates a new offer.
	 * @param employer the author of the offer.
	 * @param volume the offered volume.
	 * @param wage the offered wage.
	 */
	public JobOffer(Employer employer, int volume, long wage) {
		super(employer, volume, wage);
	}

	/**
	 * Implements the Comparable interface so that offers can easily be sorted.
	 * @param otherOffer the offer to compare against.
	 * @return <code>-1</code> if the price of the other offer is less than this,
	 * <code>0</code> if both have the same price 
	 * and <code>1</code> this price is less than the other.
	 */
	public int compareTo(AbstractOffer otherOffer) {
		if (this.getPrice()>((JobOffer)otherOffer).getPrice()) return -1;
		if (this.getPrice()<((JobOffer)otherOffer).getPrice()) return 1;		
		return 0;
	}

	/**
	 * Returns the author of the offer.
	 * @return the employer author of the offer.
	 */
	public Employer getEmployer() {
		return (Employer) getOfferer();
	}

	/**
	 * Returns the offered wage.
	 * @return the offered wage.
	 */
	public long getWage() {
		return (long)getPrice();
	}
	
	/**
	 * Returns a string representation of the offer.
	 * @return a string.
	 */
	public String toString() {
		return this.getEmployer().getName()+","+getVolume()+","+getPrice();
	}
	
}
