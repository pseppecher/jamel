package jamel.data;

import org.jfree.data.xy.VectorSeries;

/**
 * An abstract class for sector series.
 */
abstract public class VectorDynamicXYSeries extends VectorSeries implements DynamicSeries {

	/**
	 * Constructs a new empty series.
	 *
	 * @param key
	 *            the series key ({@code null} not permitted).
	 */
	public VectorDynamicXYSeries(String key) {
		super(key);
	}

}
