package jamel.v170804.data;

/**
 * An implementation of <code>Dataset</code> based on an array of
 * <code>Double</code>.
 */
class BasicDataset implements Dataset {

	/**
	 * The data.
	 */
	final private Double[] data;

	/**
	 * The keys.
	 */
	final private DataKeys keys;

	/**
	 * Creates the dataset.
	 * 
	 * @param keys
	 *            the keys of the dataset to create.
	 */
	public BasicDataset(DataKeys keys) {
		this.keys = keys;
		this.data = new Double[keys.size()];
	}

	@Override
	public void clear() {
		for (int i = 0; i < data.length; i++) {
			this.data[i] = null;
		}
	}

	@Override
	public Double get(String key) {
		return this.data[keys.indexOf(key)];
	}

	@Override
	public void put(int index, Number value) {
		if (index < 0 || index >= this.data.length) {
			throw new IllegalArgumentException("Index out of range: " + index);
		}
		this.data[index] = value.doubleValue();
	}

}
