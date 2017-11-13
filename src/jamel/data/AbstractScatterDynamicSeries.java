package jamel.data;

import org.jfree.data.xy.XYSeries;

/**
 * An abstract class for scatter series.
 */
abstract public class AbstractScatterDynamicSeries extends XYSeries implements DynamicSeries {

	/**
	 * Constructs a new empty series, with the auto-sort flag set as requested,
	 * and duplicate values allowed.
	 *
	 * @param key
	 *            the series key ({@code null} not permitted).
	 * @param autoSort
	 *            a flag that controls whether or not the items in the series
	 *            are sorted.
	 */
	public AbstractScatterDynamicSeries(String key, boolean autoSort) {
		super(key, autoSort);
	}

}
