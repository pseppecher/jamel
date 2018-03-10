package jamel.gui;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.ui.RectangleEdge;

/**
 * An axis for displaying numerical data.
 * 
 * 2017-06-27.
 * Permet d'éviter le clignotement des ticks sur l'axe des temps.
 * 
 */
public class JamelAxis extends NumberAxis {

	/**
	 * If this axis ticks are integer.
	 */
	private boolean integerUnit = false;

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

		final double e = this.getRange().getLength();
		final double ea = Math.pow(10, Math.floor(Math.log10(e / 3)));
		double ep = ea;
		if (ea * 10 < e) {
			ep = ea * 5;
		} else if (ea * 5 < e) {
			ep = ea * 2;
		}
		if (this.integerUnit && ep < 1) {
			ep = 1;
		}
		final NumberTickUnit unit2 = new NumberTickUnit(ep);
		setTickUnit(unit2, false, false);

	}

	/**
	 * Formats this axis ticks to be integer or not.
	 * 
	 * @param b
	 *            a boolean.
	 */
	public void setIntegerUnit(boolean b) {
		this.integerUnit = b;
	}

}
