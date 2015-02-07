package jamel.util;

/**
 * Represents a basic time period of the simulation.
 */
public interface Period extends Comparable<Period> {

	/**
	 * Returns <code>true</code> if the value of the time period equals <code>t</code>, <code>false</code> otherwise.
	 * @param t an integer.
	 * @return a boolean.
	 */
	boolean equals(int t);
	
	/**
	 * Returns <code>true</code> if the value of this time period equals the value of the time period <code>p</code>, <code>false</code> otherwise.
	 * @param p an other time period.
	 * @return a boolean.
	 */
	boolean equals(Period p);
	
	/**
	 * Returns the next period. The value of the next period equals the value of this period plus 1. 
	 * @return the next period.
	 */
	Period getNext();

	/**
	 * Returns the value of the time period.
	 * @return an integer.
	 */
	int intValue();
	
	/**
	 * Returns <code>true</code> if the value of the time period is higher than <code>t</code>, <code>false</code> otherwise.
	 * @param t an integer.
	 * @return a boolean.
	 */
	boolean isAfter(int t);

	/**
	 * Returns <code>true</code> if the value of this time period is higher than the value of the time period <code>p</code>, <code>false</code> otherwise.
	 * @param p an other time period.
	 * @return a boolean.
	 */
	boolean isAfter(Period p);

	/**
	 * Returns <code>true</code> if the value of the time period is lower than <code>t</code>, <code>false</code> otherwise.
	 * @param t an integer.
	 * @return a boolean.
	 */
	boolean isBefore(int t);

	/**
	 * Returns <code>true</code> if the value of this time period is less than the value of the time period <code>p</code>, <code>false</code> otherwise.
	 * @param p an other time period.
	 * @return a boolean.
	 */
	boolean isBefore(Period p);
	
	/**
	 * Returns <code>true</code> if the period is the present period, false otherwise. 
	 * @return a boolean.
	 */
	boolean isPresent();

	/**
	 * Returns a new time period the value of which equals the sum of the value of this period and the specified number of periods.  
	 * @param term the number of periods to be added to the value of this period. 
	 * @return a new time period.
	 */
	Period plus(int term);

}

// ***
