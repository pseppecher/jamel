package jamel.models.util;

/**
 * Represents a heap of commodities.
 */
public interface Commodities {

	/**
	 * Appends the content of the specified heap of commodities to this heap of
	 * commodities.
	 * The input heap will be empty after this calls return.
	 * 
	 * @param input
	 *            the heap of commodities the content of which is to be appended
	 *            to this heap.
	 */
	void add(Commodities input);

	/**
	 * Consumes all commodities in this heap.
	 */
	void consume();

	/**
	 * Consumes the specified volume of commodities in this heap.
	 * 
	 * @param volume
	 *            the volume of commodities to be consumed.
	 */
	void consume(long volume);

	/**
	 * Returns the unit cost of the commodities in this heap.
	 * 
	 * @return the unit cost.
	 */
	Double valuePerUnit();

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
	 * Returns {@Code true} if this heap of commodities is empty.
	 * 
	 * @return {@Code true} if this heap of commodities is empty.
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
	 * Creates a new heap of commodities of the specified volume by subtracting
	 * them from this heap.
	 * 
	 * @param demand
	 *            the volume of commodities to be detached.
	 * @return a new heap of commodities of the specified volume.
	 */
	Commodities take(long demand);

}
