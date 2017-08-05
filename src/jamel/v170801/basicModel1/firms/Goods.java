package jamel.v170801.basicModel1.firms;

/**
 * Represents a heap of commodities.
 */
public interface Goods {

	/**
	 * Adds the specified commodities to this heap.
	 * 
	 * @param goods
	 *            the commodities to be added.
	 */
	void add(Goods goods);

	/**
	 * Consumes this heap of commodities.
	 */
	void consume();

	/**
	 * Returns the value of this heap of commodities.
	 * 
	 * @return the value of this heap of commodities.
	 */
	long getValue();

	/**
	 * Returns the volume of commodities in this heap.
	 * 
	 * @return the volume of commodities in this heap.
	 */
	long getVolume();

	/**
	 * Returns <code>true</code> if this heap of commodities is empty.
	 * 
	 * @return <code>true</code> if this heap of commodities is empty.
	 */
	boolean isEmpty();

	/**
	 * Sets the value of the goods in this heap.
	 * 
	 * @param value
	 *            the value to be set.
	 */
	void setValue(long value);

	/**
	 * Returns the specified volume of goods.
	 * 
	 * @param volume
	 *            the volume of goods to be returned.
	 * @return the specified volume of goods.
	 */
	Goods take(long volume);

}
