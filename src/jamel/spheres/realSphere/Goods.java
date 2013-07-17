/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
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

package jamel.spheres.realSphere;

/**
 * Represents a heap of commodities.
 */
public interface Goods {

	/**
	 * Adds a heap of commodities to this heap.
	 * @param aSource - the heap to add. 
	 */
	void add(Goods aSource);

	/**
	 * Consumes all the commodities the heap contains. 
	 */
	void consumption();

	/**
	 * Consumes a fraction of the heap of commodities. 
	 * @param aVolume - the volume of commodities to consume. 
	 */
	void consumption(int aVolume);

	/**
	 * Returns the unit cost of the commodities the heap contains.
	 * @return a value.
	 */
	double getUnitCost();

	/**
	 * Returns the total value of the commodities this heap contains.
	 * @return a value.
	 */
	long getValue();

	/**
	 * Returns the volume of commodities this heap contains.
	 * @return a volume.
	 */
	int getVolume();

	/**
	 * Returns a new heap of goods subtracted from this heap of goods.
	 * The value of the new heap is determined in proportion of its volume. 
	 * @param volume - the volume of the new heap.
	 * @return a new heap of goods.
	 */
	Goods newGoods(int volume);

	/**
	 * Sets the value of this heap.
	 * @param value - the value to set.
	 */
	void setValue(long value);
	
}
