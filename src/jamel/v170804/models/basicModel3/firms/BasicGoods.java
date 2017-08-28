package jamel.v170804.models.basicModel3.firms;

/**
 * A basic implementation of the Goods interface.
 */
class BasicGoods implements Goods {

	/**
	 * The value of commodities in this heap.
	 */
	private long value = 0;

	/**
	 * The volume of commodities in this heap.
	 */
	private long volume = 0;

	/**
	 * Adds the specified volume of commodities into this heap.
	 * 
	 * @param vol
	 *            of the commodities to be added.
	 * @param val
	 *            of the commodities to be added.
	 */
	void add(final int vol, final long val) {
		// TODO utiliser plutot en argument un objet ProductionProcess
		this.volume += vol;
		this.value += val;
	}

	@Override
	public void add(Goods goods) {
		if (!(goods instanceof BasicGoods)) {
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
	public Goods take(long newVolume) {
		if (newVolume > this.volume) {
			throw new RuntimeException("Not enough goods.");
		}
		final BasicGoods result = new BasicGoods();
		result.volume = newVolume;
		result.value = (newVolume * this.value) / this.volume;
		this.volume -= result.volume;
		this.value -= result.value;
		return result;
	}

}
