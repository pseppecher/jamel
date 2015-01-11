package jamel.basic.agents.util;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A convenient class to store some past data.
 */
@SuppressWarnings("serial")
public class Memory extends LinkedList<Double>{
	
	/** The maximum number of data in the series. */
	private final int limit;
	
	/**
	 * Creates a new series/
	 * @param limit the maximum number of data.
	 */
	public Memory(int limit) {
		super();
		this.limit=limit;
	}
	
	/**
	 * Appends the specified value to the end of this series.
	 * @param val the value to be appended to this series.
	 * @return <code>true</code>
	 */
	public boolean add(double val) {
		final boolean result = super.add(val);
		if (this.size()>this.limit) {
			this.removeFirst();
		}
		return result;			
	}
	
	/**
	 * Returns the mean of all values in the series.
	 * @return the mean.
	 */
	public double getMean() {
		double sum=0;
		int i=0;
		for (Double num:this) {
			sum+=num;
			i++;
		}
		return sum/i;
	}

	/**
	 * Returns the mean of the i last values.
	 * @param i the number of values to consider.
	 * @return the mean of the i last values.
	 */
	public double getMean(int i) {
		final ListIterator<Double> iterator = this.listIterator(this.size());
		double sum = 0d;
		int j=0;
		while (true) {
			if (iterator.hasPrevious()) {
				j++;
				sum += iterator.previous();
			}
			else {
				break;
			}
			if (j==3) {
				break;
			}
		}
		return sum/j;
	}

	/**
	 * Returns the sum of all values in the series.
	 * @return the sum.
	 */
	public double getSum() {
		double sum=0;
		for (Double num:this) {
			sum+=num;
		}
		return sum;
	}

}

// ***
