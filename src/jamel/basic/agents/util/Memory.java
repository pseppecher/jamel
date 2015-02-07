package jamel.basic.agents.util;

/**
 * The memory of an agent or an object.
 */
public interface Memory {
	
	/**
	 * Adds the specified value to the previous value.
	 * If there is no previous mapping for this key, the mapping is created with the given value.
	 * @param key key of the data with which the specified value is to be added.
	 * @param value the value to be added.
	 */
	public void add(String key, double value);

	/**
	 * Checks if the tow given series of data are consistent.
	 * @param key1 the key for the first series.
	 * @param key2 the key for the second series.
	 * @return <code>true</code> if the series are consistent, <code>false</code> otherwise.
	 */
	public boolean checkConsistency(String key1, String key2);

	/**
	 * Returns <code>true</code> if this memory contains data for the specified key.
	 * @param key the key whose presence in this memory is to be tested.
	 * @return <code>true</code> if this memory contains data for the specified key.
	 */
	public boolean containsKey(String key);
	
	/**
	 * Returns the current <code>Double</code> value for the specified key. 
	 * @param key the key of the value to return.
	 * @return the current <code>Double</code> value for the specified key.
	 */
	public Double get(String key);

	/**
	 * Returns the mean of the specified series for the given period.
	 * @param key the key of the series.
	 * @param start the value of the last period to consider.
	 * @param lim the number of periods to consider.
	 * @return the mean of the values.
	 */
	public Double getMean(String key, int start,int lim);
	
	/**
	 * Returns the sum of the specified series for the given period.
	 * @param key the key of the series.
	 * @param start the value of the last period to consider.
	 * @param lim the number of periods to consider.
	 * @return the sum of the values.
	 */
	public Double getSum(String key, int start,int lim);

	/**
	 * Associates the specified value with the specified key in this memory. 
	 * If the memory previously contained a value for the key, the old value is replaced.
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 */
	public void put(String key, double value);
	
}

// ***
