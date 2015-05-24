package jamelV3.basic.gui;

import org.jfree.data.xy.XYSeries;

/**
 * A XYSeries that implements the JamelSeries interface.
 */
public abstract class JamelXYSeries extends XYSeries implements JamelSeries {

	/**
	 * Constructs a new empty series, with the auto-sort flag set as <code>false</code>, and duplicate values allowed.
	 * @param key the series key (<code>null</code> not permitted).
	 */
	public JamelXYSeries(String key) {
		super(key,false);
	}

}

// ***
