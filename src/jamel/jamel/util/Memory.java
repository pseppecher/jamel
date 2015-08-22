package jamel.jamel.util;

import java.util.LinkedList;

/**
 * Une alternative ˆ {@link Memory}.
 * 
 * @param <N>
 *            the type parameter.
 * 
 *<p>
 *TODO: rename, document
 */
@SuppressWarnings("javadoc")
public class Memory<N extends Number> {

	final private LinkedList<N> list;

	private int maxSize = 12;

	/**
	 * Creates a new memory.
	 */
	public Memory(int size) {
		this.list = new LinkedList<N>();
		maxSize = size;
	}

	public void add(N value) {
		if (value == null) {
			throw new IllegalArgumentException("'value' is null.");
		}
		list.add(value);
		if (list.size() > this.maxSize) {
			list.removeFirst();
		}
	}

	public N getLast() {
		return list.getLast();
	}

	public int getSize() {
		return list.size();
	}

	public double getSum() {
		double sum = 0d;
		for (N value : list) {
			sum += value.doubleValue();
		}
		return sum;
	}

}

// ***
