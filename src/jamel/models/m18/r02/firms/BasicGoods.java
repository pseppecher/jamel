package jamel.models.m18.r02.firms;

import jamel.Jamel;
import jamel.models.m18.r02.firms.BasicFactory.Materials;
import jamel.models.util.Commodities;

/**
 * A basic implementation of the Goods interface.
 * 
 * 2018-01-28 : intégration de la "qualité" des biens.
 * 
 */
public class BasicGoods implements Commodities {

	/**
	 * The quality, or type, of the goods in this heap.
	 */
	private final String quality;

	/**
	 * The value of commodities in this heap.
	 */
	private long value = 0;

	/**
	 * The volume of commodities in this heap.
	 */
	private long volume = 0;

	/**
	 * Creates a new empty heap of goods of the specified quality.
	 * 
	 * @param quality
	 *            the quality of the goods in this heap.
	 */
	public BasicGoods(final String quality) {
		this.quality = quality;
	}

	@Override
	public void add(Commodities goods) {
		if (!(goods instanceof BasicGoods) || !(this.quality.equals(((BasicGoods) goods).quality))) {
			Jamel.println("this good: "+this.quality,"the added good: "+((BasicGoods) goods).quality);
			throw new RuntimeException("Incompatibility.");
		}
		this.volume += goods.getVolume();
		this.value += goods.getValue();
		goods.consume();
	}

	@Override
	public void consume() {
		this.volume = 0;
		this.value = 0;
	}

	@Override
	public void consume(long newVolume) {
		if (newVolume > this.volume) {
			throw new RuntimeException("Not enough goods.");
		}
		final long resultVolume = newVolume;
		final long resultValue = (newVolume * this.value) / this.volume;
		this.volume -= resultVolume;
		this.value -= resultValue;
	}

	/**
	 * Returns the quality of the goods in this heap.
	 * 
	 * @return the quality of the goods in this heap.
	 */
	public String getQuality() {
		return this.quality;
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
	public boolean isEmpty() {
		return this.volume == 0;
	}

	/**
	 * Adds new commodities into this heap.
	 * 
	 * @param materials
	 *            a heap of materials that will be transformed in new
	 *            commodities.
	 */
	public void put(Materials materials) {
		if (!(this.quality.equals(materials.getQuality()))) {
			throw new RuntimeException("Incompatibility.");
		}
		this.value += materials.getValue();
		this.volume += materials.getVolume();
		materials.consume();
	}

	@Override
	public void setValue(long value) {
		if (value <= 0) {
			throw new RuntimeException("Bad value");
		}
		if (this.volume == 0) {
			throw new RuntimeException("This heap is empty.");
		}
		this.value = value;
	}

	@Override
	public Commodities take(long newVolume) {
		if (newVolume > this.volume) {
			throw new RuntimeException("Not enough goods.");
		}
		final BasicGoods result = new BasicGoods(this.quality);
		result.volume = newVolume;
		result.value = (newVolume * this.value) / this.volume;
		this.volume -= result.volume;
		this.value -= result.value;
		return result;
	}

	@Override
	public Double valuePerUnit() {
		return (this.volume == 0) ? null : ((double) this.value) / this.volume;
	}

}
