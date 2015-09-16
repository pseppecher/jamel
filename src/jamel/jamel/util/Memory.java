package jamel.jamel.util;

import java.util.LinkedList;

/**
 * Data memory.
 * @param <N> the type of data.
 */
public class Memory<N extends Number> {

	/**
	 * List of elements in chronological order.
	 */
	final private LinkedList<N> list;

	/**
	 * The maximum number of elements in memory.
	 */
	private int maxSize = 12;

	/**
	 * Creates a new memory.
	 * @param size the maximum size of the memory.
	 */
	public Memory(int size) {
		this.list = new LinkedList<N>();
		maxSize = size;
	}

	/**
	 * Adds a new element.
	 * If the memory is full, the oldest element is removed (FIFO).
	 * @param value the element to be added.
	 */
	public void add(N value) {
		if (value == null) {
			throw new IllegalArgumentException("'value' is null.");
		}
		list.add(value);
		if (list.size() > this.maxSize) {
			list.removeFirst();
		}
	}

	/**
	 * Returns the last element in this memory.
	 * @return the last element in this memory.
	 */
	public N getLast() {
		return list.getLast();
	}

	/**
	 * Returns the mean value of the elements in this memory.
	 * @return the mean value of the elements in this memory.
	 */
	public double getMean() {
		final double result;
		if (getSize()==0) {
			result=0;
		}
		else {
			result = this.getSum()/this.getSize();
		}
		return result;
	}

	/**
	 * Returns the number of elements in this memory.
	 * @return the number of elements in this memory.
	 */
	public int getSize() {
		return list.size();
	}

	/**
	 * Returns the sum of all elements in this memory.
	 * @return the sum of all elements in this memory.
	 */
	public double getSum() {
		double sum = 0d;
		for (N value : list) {
			sum += value.doubleValue();
		}
		return sum;
	}

}

// ***
