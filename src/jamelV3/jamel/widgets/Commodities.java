package jamelV3.jamel.widgets;

/**
 * Represents a heap of finished goods.
 */
public interface Commodities {

	/**
	 * Consumes all the commodities in the heap.
	 */
	void consume();

	/**
	 * Creates a new heap of commodities of the specified volume by subtracting them from this heap.
	 * @param demand the volume of commodities to detach.
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

}

// ***
