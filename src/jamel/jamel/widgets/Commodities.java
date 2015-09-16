package jamel.jamel.widgets;

/**
 * Represents a heap of finished goods.
 */
public interface Commodities {

	/**
	 * Consumes all the commodities in this heap.
	 */
	void consume();

	/**
	 * Consumes the specified volume of commodities in this heap.
	 * @param volume the volume of commodities to be consumed.
	 */
	void consume(long volume);

	/**
	 * Creates a new heap of commodities of the specified volume by subtracting them from this heap.
	 * @param demand the volume of commodities to be detached.
	 * @return a new heap of commodities of the specified volume.
	 */
	Commodities detach(long demand);

	/**
	 * Returns the unit cost of the commodities in this heap.
	 * @return the unit cost.
	 */
	double getUnitCost();

	/**
	 * Returns the total value of the commodities in the heap.
	 * @return the total value.
	 */
	long getValue();

	/**
	 * Returns the total volume of the commodities in the heap.
	 * @return the total volume.
	 */
	long getVolume();

	/**
	 * Appends the content of the specified heap of commodities to this heap of commodities.
	 * The input heap will be empty after this calls return.
	 * @param input the heap of commodities the content of which is to be appended to this heap.
	 */
	void put(Commodities input);

	/**
	 * Sets the total value of the commodities in the heap.
	 * @param value the value to set.
	 */
	void setValue(long value);

}

// ***
