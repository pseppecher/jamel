package jamel.basic.gui;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

/**
 * A basic color paint scale.
 */
public class ColorPaintScale implements PaintScale {

	/**
	 * Returns the hue of the specified color.
	 * @param color  the color the hue of which is to be returned.
	 * @return the hue of the specified color. 
	 */
	static private float getHue(Color color) {
		final float[] hsv = new float[3];
		return Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),hsv)[0];
	}

	/** The lower bound. */
	final private double lowerBound;

	/** The color of the paint scale. */
	final private float lowerHue;

	/** The upper bound. */
	final private double upperBound;

	/** The color of the paint scale. */
	final private float upperHue;

	/**
	 * Creates a new paint scale for values in the specified range.
	 * @param lowerBound  the lower bound.
	 * @param upperBound  the upper bound.
	 * @param lowerColor  the lower color.
	 * @param upperColor  the upper color.
	 * @throws IllegalArgumentException If <code>lowerBound</code> is not less than <code>upperBound</code>
	 */
	public ColorPaintScale(double lowerBound, double upperBound, Color lowerColor, Color upperColor) {
		if (lowerBound >= upperBound) {
			throw new IllegalArgumentException(
					"Requires lowerBound < upperBound.");
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.lowerHue = getHue(lowerColor);
		this.upperHue = getHue(upperColor);
	}

	/**
	 * Tests this <code>GrayPaintScale</code> instance for equality with an
	 * arbitrary object.  This method returns <code>true</code> if and only
	 * if:
	 * <ul>
	 * <li><code>obj</code> is not <code>null</code>;</li>
	 * <li><code>obj</code> is an instance of <code>GrayPaintScale</code>;</li>
	 * </ul>
	 * @param obj  the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ColorPaintScale)) {
			return false;
		}
		ColorPaintScale that = (ColorPaintScale) obj;
		if (this.lowerBound != that.lowerBound) {
			return false;
		}
		if (this.upperBound != that.upperBound) {
			return false;
		}
		if (this.lowerHue != that.lowerHue) {
			return false;
		}
		if (this.upperHue != that.upperHue) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the lower bound.
	 * @return The lower bound.
	 * @see #getUpperBound()
	 */
	@Override
	public double getLowerBound() {
		return this.lowerBound;
	}

	/**
	 * Returns a paint for the specified value.
	 * @param value  the value (must be within the range specified by the lower and upper bounds for the scale).
	 * @return A paint for the specified value.
	 */
	@Override
	public Paint getPaint(double value) {
		final double v = Math.min(Math.max(value, this.lowerBound), this.upperBound);
		final float range = upperHue-lowerHue;
		final float g = lowerHue+(float) (range*((v - this.lowerBound) / (this.upperBound - this.lowerBound)));
		final int rgb = Color.HSBtoRGB(g,0.4f,1f);
		return new Color(rgb);
	}

	/**
	 * Returns the upper bound.
	 * @return The upper bound.
	 * @see #getLowerBound()
	 */
	@Override
	public double getUpperBound() {
		return this.upperBound;
	}

	@Override
	public int hashCode() {
		return (int) (17*this.lowerBound+this.upperBound);
	}

}

// ***
