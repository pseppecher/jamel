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
 * Represents a volume of commodities.
 * <p>
 * Encapsulates two numbers that represent a volume of commodities and its value.
 * A nonempty volume of commodities can be created by a {@link ProductionProcess}, at the end of the production cycle.
 * A nonempty volume of commodities can be partially or totally transfered from a Commodity object to another.
 * A nonempty volume of commodities is destroyed in the act of consumption.
 */
abstract class AbstractGoods implements Goods {

	/** The value of commodities. */
	protected long value;

	/** The volume of commodities. */
	protected int volume ;

	/**
	 * Creates a new empty heap of commodities.
	 */
	public AbstractGoods() {
		this.value = 0;
		this.volume = 0;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setValue(long value) {
		this.value = value;
	}

	/**
	 * @param volume the volume to set
	 */
	protected void setVolume(int volume) {
		this.volume = volume;
	}

	/**
	 * Adds a volume of commodities to the current volume.<br>
	 * The total volume from the source is transfered.
	 * @param source - the source volume. 
	 */
	@Override
	public void add(Goods source) {
		this.volume += source.getVolume() ;
		this.value += source.getValue(); 
		source.consumption();
	}

	/**
	 * Consumes the total volume of commodities. 
	 */
	public void consumption() { 
		this.volume = 0 ;
		this.value = 0;
	}

	/**
	 * Consumes a fraction of the volume of commodities. 
	 * @param aVolume - the volume to consume. 
	 */
	public void consumption(int aVolume) { 
		if (aVolume>volume) throw new RuntimeException("The consumed volume exceeds the source volume.")  ;
		value = (value*(volume-aVolume))/volume;
		volume -= aVolume ;
	}

	/**
	 * Returns the unit cost of the commodities.
	 * @return a double.
	 */
	public double getUnitCost() {
		if (volume==0) return 0;
		return ((double)value)/volume;
	}


	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * Returns the value of the specified volume.
	 * @param volume2 the volume to evaluate.
	 * @return a double that represent the value.
	 */
	public double getValue(double volume2) {
		return (value*volume2)/volume;
	}

	/**
	 * @return the volume
	 */
	public int getVolume() {
		return volume;
	}



}
