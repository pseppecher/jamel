/** 
 * ===================================================
 * JAMEL : a Java Agent-based MacroEconomic Laboratory.
 * ===================================================
 * 
 * (C) Copyright 2007-2010, by Pascal Seppecher.
 * 
 * Project Info:  http://p.seppecher.free.fr/jamel/ 
 */

package jamel.austrian.widgets;

/**
 * Represents a volume of commodities.
 * <p>
 * Encapsulates two numbers that represent the number of commodities and their (book) value.
 * <p>
 * Last update: 12-Dec-2010
 */
public class Goods {

	/** The volume of commodities. */
	private int volume ;

	/** The book value of the commodities.<br>
	 *  This is the total book value of all goods, not the value per unit. */
	private int value;

	
	/**
	 * Creates an empty volume of commodities.
	 */
	public Goods()	{		
		this.volume = 0 ;
		this.value = 0 ;
	}
	
	
	/**
	 * Creates a non-empty volume of commodities.
	 */
	public Goods(int volume, int value)	{
		if (volume<=0) throw new RuntimeException("Negative or zero commodities.");
		if (value<=0) throw new RuntimeException("Negative or zero value.");
		this.volume = volume ;
		this.value = value ;
	}

/*

	*//**
	 * Adds a volume of commodities to the current volume.<br>
	 * The total volume from the source is transfered.
	 * @param source the source volume. 
	 * @param value the value added with the transfer.
	 *//*
	public void add(Goods source, double value) {					
		this.volume += source.volume ;
		this.value += value; 
		source.consumption();
	}

	*//**
	 * Adds a volume of commodities to the current volume.<br>
	 * Only a part of the volume from the source is transfered.
	 * Generates a {@link RuntimeException} if the transfered volume exceeds the source volume.
	 * @param source the source volume.
	 * @param value the value added with the transfer.
	 * @param volume the volume to transfer.
	 *//*
	public void add(Goods source, int value, int volume) {
		if (volume<=0) new RuntimeException("The transfered volume is negative or null."); 
		if (value<=0) new RuntimeException("The transfered value is negative or null."); 
		this.volume += volume ;
		this.value += value; 
		source.consumption(volume);
	}*/
	
	
	/**
	 * Subtracts a volume of commodities to the current volume.<br>
	 * The new value is a rounded number, but the over-valuation will correct itself in the next period.
	 */
	public void subtract(int volume) {					
		if (volume<=0) throw new RuntimeException("Negative or zero commodities.");
		if (volume>this.volume) throw new RuntimeException("The number of goods to substract exceeds the total volume.");
		float unitValue = getUnitCost();
		this.volume -= volume ;
		value -= (int) (unitValue*volume);
	}

	/**
	 * Returns the variable unit cost of the commodities.<br>
	 * Fixed costs are not included.
	 */
	public float getUnitCost() {
		if (volume==0) return 0;
		return (float) value / (float) volume;
	}
	

	/**
	 * Returns the volume of commodities.
	 */
	public int getVolume() { 
		return volume ; 
	}

	/**
	 * Returns the value of the volume of commodities.
	 */
	public int getValue() {
		return value;
	}
}