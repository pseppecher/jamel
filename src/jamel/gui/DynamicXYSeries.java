package jamel.gui;

import org.jfree.data.xy.XYSeries;

/**
 * A dynamic series.
 */
public abstract class DynamicXYSeries extends XYSeries {

	/**
	 * Constructs a new empty series, with the auto-sort flag set as requested,
	 * and duplicate values allowed.
	 *
	 * @param key
	 *            the series key (<code>null</code> not permitted).
	 * @param autoSort
	 *            a flag that controls whether or not the items in the
	 *            series are sorted.
	 */
	public DynamicXYSeries(String key, boolean autoSort) {
		super(key, autoSort);
	}

	/**
	 * Updates the series.
	 * 
	 * @param refresh
	 *            a boolean that indicates if the gui must be refreshed.
	 */
	abstract public void update(boolean refresh);

}
