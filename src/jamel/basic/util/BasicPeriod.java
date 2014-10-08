package jamel.basic.util;

import jamel.util.Circuit;
import jamel.util.Period;

/**
 * An implementation of the Period interface.
 */
public class BasicPeriod implements Period {

	/** The value of the period. */
	private final int value;
	
	/**
	 * Creates a period.
	 * @param t the value of the new period. 
	 */
	public BasicPeriod(int t) {
		this.value = t;
	}

	@Override
	public boolean equals(int t) {
		return this.value==t;
	}

	@Override
	public boolean equals(Period p) {
		return this.value==p.getValue();
	}

	/**
	 * Returns the next period.
	 * @return a period.
	 */
	public Period getNext() {
		return new BasicPeriod(this.value+1);
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public boolean isAfter(int t) {
		return this.value>t;
	}

	@Override
	public boolean isAfter(Period period2) {
		return this.value>period2.getValue();
	}

	@Override
	public boolean isBefore(int t) {
		return this.value<t;
	}

	@Override
	public boolean isBefore(Period p) {
		return this.value<p.getValue();
	}

	@Override
	public Period plus(int term) {
		return new BasicPeriod(this.value+term);
	}

	@Override
	public boolean isPresent() {
		return this.equals(Circuit.getCurrentPeriod());
	}
	
}
