package jamel.basic.gui;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

/**
 * A basic transparent paint scale.
 */
public class BasicPaintScale implements PaintScale {

	/** The color of the paint scale. */
	private Color color;

	/** The lower bound. */
	private double lowerBound;

	/** The upper bound. */
	private double upperBound;

	/**
	 * Creates a new paint scale for values in the specified range.
	 * 
	 * @param color
	 *            the color.
	 * @param lowerBound
	 *            the lower bound.
	 * @param upperBound
	 *            the upper bound.
	 * @throws IllegalArgumentException
	 *             If <code>lowerBound</code> is not less than
	 *             <code>upperBound</code>
	 */
	public BasicPaintScale(Color color, double lowerBound, double upperBound) {
		if (lowerBound >= upperBound) {
			throw new IllegalArgumentException("Requires lowerBound < upperBound.");
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.color = color;
	}

	/**
	 * Tests this <code>GrayPaintScale</code> instance for equality with an
	 * arbitrary object. This method returns <code>true</code> if and only if:
	 * <ul>
	 * <li><code>obj</code> is not <code>null</code>;</li>
	 * <li><code>obj</code> is an instance of <code>GrayPaintScale</code>;</li>
	 * </ul>
	 * 
	 * @param obj
	 *            the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BasicPaintScale)) {
			return false;
		}
		BasicPaintScale that = (BasicPaintScale) obj;
		if (this.lowerBound != that.lowerBound) {
			return false;
		}
		if (this.upperBound != that.upperBound) {
			return false;
		}
		if (!this.color.equals(that.color)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the lower bound.
	 * 
	 * @return The lower bound.
	 * @see #getUpperBound()
	 */
	@Override
	public double getLowerBound() {
		return this.lowerBound;
	}

	/**
	 * Returns a paint for the specified value.
	 * 
	 * @param value
	 *            the value (must be within the range specified by the lower and
	 *            upper bounds for the scale).
	 * @return A paint for the specified value.
	 */
	@Override
	public Paint getPaint(double value) {
		double v = Math.max(value, this.lowerBound);
		v = Math.min(v, this.upperBound);
		int g = (int) ((v - this.lowerBound) / (this.upperBound - this.lowerBound) * 255.0);
		return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), g);
	}

	/**
	 * Returns the upper bound.
	 * 
	 * @return The upper bound.
	 * @see #getLowerBound()
	 */
	@Override
	public double getUpperBound() {
		return this.upperBound;
	}

	@Override
	public int hashCode() {
		return (int) (lowerBound) * 17 + (int) (upperBound) * 31 + color.hashCode();
	}

}

// ***
