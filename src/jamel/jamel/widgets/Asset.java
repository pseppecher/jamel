package jamel.jamel.widgets;

/**
 * "In accounting, book value is the value of an asset according to its balance sheet account balance."
 * (ref: <a href = "https://en.wikipedia.org/wiki/Book_value">wikipedia.org</a>)
 */
public interface Asset {

	/**
	 * Returns the book value of the asset.
	 * 
	 * @return the book value of the asset.
	 */
	Long getBookValue();

	/**
	 * Returns <code>true</code> if this asset is cancelled, <code>false</code>
	 * otherwise.
	 * 
	 * @return <code>true</code> if this asset is cancelled, <code>false</code>
	 *         otherwise.
	 */
	boolean isCancelled();

}

// ***
