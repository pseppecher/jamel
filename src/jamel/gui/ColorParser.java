package jamel.gui;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.ChartColor;

/**
 * A simple color parser.
 */
public class ColorParser {

	/** The background color for panels. */
	final static public Color background = new Color(230, 230, 230);

	/**
	 * Transparent blue color.
	 */
	final static public Color transparentBlue = new Color(0f, 0f, 1f, 0.5f);
	
	/**
	 * Transparent green color.
	 */
	final static public Color transparentGreen = new Color(0f, 1f, 0f, 0.5f);

	/**
	 * Transparent red color.
	 */
	final static public Color transparentRed = new Color(1f, 0f, 0f, 0.5f);
	
	/**
	 * Parses the string and returns the specified color.
	 * 
	 * @param name
	 *            the name of the color to be returned.
	 * @return a color.
	 */
	public static Color getColor(String name) {
		Color result;
		try {
			result = (Color) ColorParser.class.getField(name).get(null);
		} catch (@SuppressWarnings("unused") NoSuchFieldException e1) {
			try {
				result = (Color) ChartColor.class.getField(name).get(null);
			} catch (@SuppressWarnings("unused") IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e2) {
				throw new IllegalArgumentException("Bad color: " + name);
			}
		} catch (@SuppressWarnings("unused") IllegalArgumentException | IllegalAccessException | SecurityException e3) {
			throw new IllegalArgumentException("Bad color: " + name);
		}

		return result;
	}

	/**
	 * Returns an array of paints with the specified colors.
	 * 
	 * @param colorKeys
	 *            the keys of the paints to return.
	 * @return an array of paints.
	 */
	public static Paint[] getColors(String... colorKeys) {
		final Paint[] colors;
		if (colorKeys.length > 0) {
			colors = new Paint[colorKeys.length];
			for (int count = 0; count < colors.length; count++) {
				colors[count] = ColorParser.getColor(colorKeys[count]);
			}
		} else {
			colors = null;
		}
		return colors;
	}

}
