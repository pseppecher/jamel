package jamelV3.jamel.firms.util;

import jamelV3.jamel.widgets.Commodities;

/**
 * A basic implementation of the Commodities.
 */
public class BasicCommodities implements Commodities {
	
	/** The value. */
	private long value=0;
	
	/** The volume; */
	private long volume=0;
	
	/**
	 * Sets the value.
	 * @param value the value to set.
	 */
	protected void setValue(long value) {
		this.value=value;
	}

	/**
	 * Sets the volume.
	 * @param volume the volume to set.
	 */
	protected void setVolume(long volume) {
		this.volume=volume;
	}
	
	@Override
	public void consume() {
		this.value=0;
		this.volume=0;
	}

	@Override
	public Commodities detach(final long demand) {
		if (demand>this.volume) {
			throw new IllegalArgumentException("Demand cannot exceed supply.");
		}
		final BasicCommodities result = new BasicCommodities();
		result.volume=demand;
		result.value=demand*this.value/this.volume;
		this.volume-=result.volume;
		this.value-=result.value;
		return result;
	}

	@Override
	public double getUnitCost() {
		return ((double) value)/volume;
	}
	
	@Override
	public long getValue() {
		return this.value;
	}
	
	@Override
	public long getVolume() {
		return this.volume;
	}

	@Override
	public void put(Commodities input) {
		this.value+=input.getValue();
		this.volume+=input.getVolume();
		input.consume();
	}
	
}

// ***
