package jamel.gui;

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
	void update(boolean refresh);

}
