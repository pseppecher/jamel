package jamel.data;

/**
 * A dynamic series.
 */
public interface DynamicSeries {

	/**
	 * Updates the series.
	 * 
	 * @param refresh
	 *            a boolean that indicates if the gui must be refreshed.
	 */
	abstract public void update(boolean refresh);

}
