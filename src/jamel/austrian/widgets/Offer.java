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

package jamel.austrian.widgets;

import jamel.austrian.roles.Offerer;


/**
 * A base class for offers on markets.
 * <p>
 * Encapsulates a volume offered and a unit price.
 * <p>
 * Last update: 14-Aug-2012.
 */
public class Offer implements Comparable<Offer> {

	/** The agent author of the offer. */
	private final Offerer author ;	

	/** The unit price. */
	private final float price ;	

	/** The offered volume. */
	private int volume ;
	

	/**
	 * Creates a new offer.
	 * @param offerer the agent author of the offer.
	 * @param volume the offered volume.
	 * @param price the unit price of the offer.
	 */
	public Offer(Offerer offerer, int volume, float price) {
		if (Double.isNaN(price)) throw new RuntimeException("The price is NaN.") ;
		if (price <= 0) throw new RuntimeException("The price is null or negative.") ;
		if (volume <= 0) throw new RuntimeException("The volume must be strictly positive.") ;
		this.author = offerer ;				
		this.volume = volume ;					
		this.price = price ;
	}
	
	/**
	 * Returns the agent author of the offer.
	 * @return the offerer.
	 */
	public Offerer getOfferer() { 
		return author ; 
	}

	/**
	 * Returns the offered volume.
	 * @return a double that represents the volume.
	 */
	public int getVolume() { 
		return volume ; 
	}

	/**
	 * Returns the unit price.
	 * @return a double that represents the unit price.
	 */
	public float getPrice() { 
		return price ;
	}

	/**
	 * Subtract a volume to the offered volume.<br>
	 * @param volume the volume to subtract.
	 */
	public void subtract(int volume) {		
		if (volume>this.volume) throw new IllegalArgumentException("The volume to subtract exceed the offered volume.") ;
		this.volume -= volume ;
	}
	
	/**
	 * Implements the Comparable interface so that offers can easily be sorted.
	 * @param otherOffer the offer to compare against.
	 * @return <code>1</code> if the price of the other offer is less than this,
	 * <code>0</code> if both have the same price 
	 * and <code>-1</code> this price is less than the others.
	 */
	public int compareTo(Offer otherOffer) {
		if (this.getPrice()<(otherOffer).getPrice()) return -1;
		if (this.getPrice()>(otherOffer).getPrice()) return 1;		
		return 0;
	}

	
	/**
	 * Returns a string representation of the offer.
	 * @return a string.
	 */
	public String toString() {
		return this.getOfferer().getName()+","+getVolume()+","+getPrice();
	}
	
}
