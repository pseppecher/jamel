package jamel.basic.data;

import java.util.LinkedList;

/**
 * Data series.
 */
@SuppressWarnings("serial")
public class DataSeries extends LinkedList<Double>{
	
	/** The maximum number of data in the series. */
	private final int limit;
	
	/**
	 * Creates a new series/
	 * @param limit the maximum number of data.
	 */
	public DataSeries(int limit) {
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
