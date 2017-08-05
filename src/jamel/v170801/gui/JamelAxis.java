
package jamel.v170801.gui;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.ui.RectangleEdge;

/**
 * An axis for displaying numerical data.
 * 
 * 2017-06-27.
 * Permet d'éviter le clignotement des ticks sur l'axe des temps.
 * 
 */
public class JamelAxis extends NumberAxis {

	/**
	 * Should be a parameter.
	 */
	final private Integer defaultNumberOfTicks = 3;

	/**
	 * Default constructor.
	 */
	public JamelAxis() {
		this(null);
	}

	/**
	 * Constructs a number axis, using default values where necessary.
	 *
	 * @param label
	 *            the axis label (<code>null</code> permitted).
	 */
	public JamelAxis(String label) {
		super(label);
	}

	@Override
	protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {

		if (defaultNumberOfTicks != null) {
			final double currentSize = this.getTickUnit().getSize();
			final double raw = this.getRange().getLength() / defaultNumberOfTicks;
			final double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
			final double newSize = Math.floor(raw / magnitude) * magnitude;

			if (newSize > currentSize * 2 || currentSize > newSize * 2) {

				final NumberTickUnit unit2 = new NumberTickUnit(newSize);

				setTickUnit(unit2, false, false);
			}
		} else {
			super.selectAutoTickUnit(g2, dataArea, edge);
		}

	}

}
