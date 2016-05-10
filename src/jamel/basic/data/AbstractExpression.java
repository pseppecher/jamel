package jamel.basic.data;

/**
 * An abstract expression.
 * 
 * @author pascal
 *
 */
abstract class AbstractExpression implements Expression {
	
	// 2016-05-01

	/**
	 * The name of the expression.
	 */
	private String name;

	/**
	 * The query that created this expression.
	 */
	private final String query;

	/**
	 * Creates a new expression.
	 * 
	 * @param query
	 *            the query.
	 */
	AbstractExpression(String query) {
		this.query = query;
	}

	@Override
	final public String getName() {
		return this.name;
	}

	@Override
	final public String getQuery() {
		return this.query;
	}

	@Override
	final public void setName(String name) {
		this.name = name;
	}

}