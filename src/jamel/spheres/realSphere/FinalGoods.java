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
 * Represents a heap of final goods.
 */
public class FinalGoods extends AbstractGoods {

	/**
	 * Adds a heap of goods to the current heap.
	 * Generates an exception if the heap to add is not composed of final goods.
	 * @param source - the heap to add. 
	 */
	@Override
	public void add(Goods source) {
		if (!this.getClass().isInstance(source)) 
			throw new RuntimeException("Incompatible types of goods: "+this.getClass().getName()+" and "+source.getClass().getName());
		super.add(source);
	}

	/**
	 * Returns a new heap of goods subtracted from this heap of goods.
	 * @param transferedVolume - the volume to transfer.
	 * @return a new heap of goods.
	 */
	@Override
	public Goods newGoods(int transferedVolume) {
		if (volume==0) 
			throw new RuntimeException("The volume to transfer is zero.")  ;
		if (transferedVolume>volume) 
			throw new RuntimeException("The volume to transfer exceeds the volume of this heap.")  ;
		final FinalGoods newGoods = new FinalGoods();
		final long transferedValue = (this.value*transferedVolume)/volume;
		this.volume -= transferedVolume ;
		this.value -= transferedValue ;
		newGoods.volume = transferedVolume ;
		newGoods.value = transferedValue ;
		return newGoods;
	}

}













