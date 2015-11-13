package jamel.jamel.firms.factory;

import jamel.Jamel;
import jamel.jamel.widgets.Commodities;

/**
 * Represents a heap of finished goods.
 */
public class FinishedGoods implements Materials, Commodities {

	/** Finished goods are completed. */
	private final static Rational completion = new Rational(1, 1);

	/** The value of the goods. */
	private long value = 0;

	/** The volume of the goods. */
	private long volume = 0;

	/**
	 * Creates an empty heap of finished goods.
	 */
	public FinishedGoods() {
	}

	/**
	 * Creates a new heap of finished goods.
	 * 
	 * @param volume
	 *            the volume of the heap of finished goods to be created.
	 * @param value
	 *            the value of the heap of finished goods to be created.
	 */
	public FinishedGoods(long volume, long value) {
		if (volume<0) {
			throw new IllegalArgumentException("Volume: "+volume);
		}
		if (value<0) {
			throw new IllegalArgumentException("Value: "+value);
		}
		this.volume = volume;
		this.value = value;
	}

	@Override
	public void add(Materials stuff) {
		throw new RuntimeException("Not used.");
	}

	@Override
	public void consume() {
		value = 0;
		volume = 0;
	}

	@Override
	public void consume(long consumption) {
		if (consumption < 0) {
			throw new IllegalArgumentException("Consumption: "+consumption);
		}
		if (consumption > this.volume) {
			throw new RuntimeException("Overconsumption.");
		}
		if (consumption == this.volume) {
			consume();
		} else {
			this.value -= (this.value * consumption) / this.volume;
			this.volume -= consumption;
		}

	}

	@Override
	public void delete() {
		throw new RuntimeException("Not used.");
	}

	@Override
	public void delete(long volume1, long value1) {
		throw new RuntimeException("Not used.");
	}

	@Override
	public Commodities detach(long demand) {
		if (demand > this.volume) {
			throw new IllegalArgumentException("Demand cannot exceed supply.");
		}
		if (demand < 0) {
			throw new IllegalArgumentException("Bad volume: "+demand);
		}
		final double demand2 = demand;
		final long newValue = (long) (demand2 * this.value / this.volume);
		if (newValue < 0) {
			Jamel.println("demand",demand);
			Jamel.println("value",value);
			Jamel.println("volume",volume);
			throw new IllegalArgumentException("Bad value: "+newValue);
		}
		final Commodities result = new FinishedGoods(demand, newValue);
		this.volume -= result.getVolume();
		this.value -= result.getValue();
		return result;
	}

	@Override
	public Long getBookValue() {
		return this.value;
	}

	@Override
	public Rational getCompletion() {
		return completion;
	}

	@Override
	public int getProductionPeriod() {
		throw new RuntimeException("Not used.");
	}

	@Override
	public double getUnitCost() {
		return ((double) this.value) / this.volume;
	}

	@Override
	public long getValue() {
		if (this.value<0) {
			throw new RuntimeException("Illegal value: "+this.value);
		}
		return this.value;
	}

	@Override
	public long getVolume() {
		return this.volume;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void put(Commodities input) {
		this.value += input.getValue();
		this.volume += input.getVolume();
		input.consume();
	}

	@Override
	public void setValue(long value) {
		if (value<0) {
			throw new IllegalArgumentException("Value: "+value);
		}
		this.value = value;
	}

}

// ***
