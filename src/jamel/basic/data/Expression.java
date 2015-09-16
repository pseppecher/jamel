package jamel.basic.data;

/**
 * Represents a mathematical or statistical expression.
 */
public interface Expression {
	
	/**
	 * Returns the current value of this expression.
	 * @return the current value of this expression.
	 */
	public Double value();
	
	/**
	 * Returns the query used to create this expression.
	 * @return the query used to create this expression.
	 */
	public String getQuery();
	
}

// ***