package jamel.basic.data;

import java.util.List;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 * An extension of XYSeries.
 */
@SuppressWarnings("serial")
public class JamelXYSeries extends XYSeries {

	/**
	 * Constructs a new empty series, with the auto-sort flag set as <code>false</code>, and duplicate values allowed.
	 * @param key the series key (<code>null</code> not permitted).
	 */
	public JamelXYSeries(@SuppressWarnings("rawtypes") Comparable key) {
		super(key,false);
	}

	/**
	 * Updates the series. 
	 * All data items in the series are removed and replaced by the new data.
	 * Useful for scatter charts. 
	 * @param newData the new data.
	 */
	@SuppressWarnings("unchecked")
	public void update(List<XYDataItem> newData) {
		this.data.clear();
		if (newData!=null) {
			this.data.addAll(newData);
		}
		this.fireSeriesChanged();
	}

}
