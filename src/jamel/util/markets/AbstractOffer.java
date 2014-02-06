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

import jamel.agents.roles.Offerer;


/**
 * A base class for offers on markets.
 * <p>
 * Encapsulates a volume offered and a unit price.
 * <p>
 * Last update: 8-Dec-2010.
 */
public abstract class AbstractOffer implements Comparable<AbstractOffer> {

	/** The agent author of the offer. */
	private Offerer author ;	

	/** The unit price. */
	private final double price ;	

	/** The offered volume. */
	private int volume ;

	/**
	 * Creates a new offer.
	 * @param offerer the agent author of the offer.
	 * @param volume the offered volume.
	 * @param price the unit price of the offer.
	 */
	protected AbstractOffer(Offerer offerer, int volume, double price) {
		if (Double.isNaN(price)) 
			throw new RuntimeException("The price is NaN.") ;
		if (price == 0) 
			throw new RuntimeException("The price can't be null.") ;
		if (volume <= 0) 
			throw new RuntimeException("The volume must be strictly positive.") ;
		this.author = offerer ;				
		this.volume = volume ;					
		this.price = price ;
	}

	/**
	 * Returns the unit price.
	 * @return a double that represents the unit price.
	 */
	protected double getPrice() { 
		return this.price ;
	}

	/**
	 * Subtract a volume to the offered volume.<br>
	 * @param volume the volume to subtract.
	 */
	public void subtract(double volume) {
		if (volume>this.volume) throw new IllegalArgumentException("The volume to subtract exceed the offered volume.") ;
		this.volume -= volume ;
	}

	/**
	 * Returns the agent author of the offer.
	 * @return the offerer.
	 */
	protected Offerer getOfferer() { 
		return this.author ; 
	}

	/**
	 * Returns the offered volume.
	 * @return a double that represents the volume.
	 */
	public int getVolume() { 
		return this.volume ; 
	}

	/**
	 * 
	 */
	public void clear() {
		this.author = null;		
	}

}
