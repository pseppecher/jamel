/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.gui.charts;

import java.awt.Color;

import org.jfree.chart.ChartColor;

/**
 * Class to extend the number of Colors available to the charts. This
 * extends the java.awt.Color object and extends the number of final
 * Colors publically accessible.
 */
@SuppressWarnings("serial")
public class JamelColor extends ChartColor{

    /** averageColor */
	public final static Color averageColor = LIGHT_GREEN;

	/** finalColor */
	public final static Color finalColor = LIGHT_BLUE;

	/** intermediateColor */
	public final static Color intermediateColor = LIGHT_RED;
	
	/** LIGHT_TRANSPARENT_BLUE */
	public static final Color LIGHT_TRANSPARENT_BLUE = new Color(0.5f,0.5f,1,0.7f) ;
	
	/** LIGHT_TRANSPARENT_GREEN */
	public static final Color LIGHT_TRANSPARENT_GREEN = new Color(0.5f,1,0.5f,0.7f) ;

	/** LIGHT_TRANSPARENT_RED */
	public static final Color LIGHT_TRANSPARENT_RED = new Color(1,0.5f,0.5f,0.7f) ;
	
	/** ULTRA_LIGHT_BLUE */
	public static final Color ULTRA_LIGHT_BLUE = new Color(0xDD, 0xDD, 0xFF);
	
	/** ULTRA_LIGHT_GREEN */
	public static final Color ULTRA_LIGHT_GREEN = new Color(0xDD, 0xFF, 0xDD);

	/** ULTRA_LIGHT_RED */
	public static final Color ULTRA_LIGHT_RED = new Color(0xFF, 0xDD, 0xDD);
	
	/** ULTRA_TRANSPARENT_BLUE */
	public static final Color ULTRA_TRANSPARENT_BLUE = new Color(0.9f,0.9f,1,0.7f) ;
	
	/** ULTRA_TRANSPARENT_GREEN */
	public static final Color ULTRA_TRANSPARENT_GREEN = new Color(0.9f,1,0.9f,0.7f) ;

	/** ULTRA_TRANSPARENT_RED */
	public static final Color ULTRA_TRANSPARENT_RED = new Color(1,0.9f,0.9f,0.7f) ;
	
	/** VERY_LIGHT_RED */
	public static final Color VERY_LIGHT_RED = ChartColor.VERY_LIGHT_RED ;
	
	/** VERY_TRANSPARENT_BLUE */
	public static final Color VERY_TRANSPARENT_BLUE = new Color(0.7f,0.7f,1,0.7f) ;

	/** VERY_TRANSPARENT_GREEN */
	public static final Color VERY_TRANSPARENT_GREEN = new Color(0.7f,1,0.7f,0.7f) ;

	/** VERY_TRANSPARENT_RED */
	public static final Color VERY_TRANSPARENT_RED = new Color(1,0.7f,0.7f,0.7f) ;

	/**
     * Creates a Color with an opaque sRGB with red, green and blue values in
     * range 0-255.
     *
     * @param r  the red component in range 0x00-0xFF.
     * @param g  the green component in range 0x00-0xFF.
     * @param b  the blue component in range 0x00-0xFF.
     */
    public JamelColor(int r, int g, int b) {
        super(r, g, b);
    }
	
}
