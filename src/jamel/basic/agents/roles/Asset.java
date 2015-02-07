package jamel.basic.agents.roles;

/**
 * "In accounting, book value is the value of an asset according to its balance sheet account balance." 
 * (<a href="https://en.wikipedia.org/wiki/Book_value">https://en.wikipedia.org/wiki/Book_value</a>)
 */
public interface Asset {

	/**
	 * Returns the book value of the asset.
	 * @return the book value of the asset.
	 */
	long getBookValue();

}
