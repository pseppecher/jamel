/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2011, Pascal Seppecher.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/>. 
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

package jamel.util.data;

import java.util.HashMap;

import org.jfree.data.xy.XYSeries;

/**
 * A class that provides cross-section series.
 * <p>
 * Last update: 28-Dec-2010
 */
@SuppressWarnings("serial")
public class CrossSectionSeries extends HashMap<String, Double[]> {

	/**
	 * Returns an array that contains for each item, three values.
	 * @param xLabel the x value label.
	 * @param yLabel the y value label.
	 * @param zLabel the z value label.
	 * @return an array that contains, for each item, three values.
	 */
	public double[][] get(String xLabel, String yLabel, String zLabel) {
		Double[] xRow = this.get(xLabel);
		Double[] yRow = this.get(yLabel);
		Double[] zRow = this.get(zLabel);
		if ((xRow==null) | (yRow==null) | (zRow==null)) return null;
		if ( (xRow.length != yRow.length) | (yRow.length != zRow.length) )
			throw new RuntimeException("Rows have not the same length.");
		int lenght = xRow.length;
		double[][] data = new double[3][lenght];;
		for (int count=0; count<lenght; count++) {
			data[0][count] = xRow[count];
			data[1][count] = yRow[count];
			data[2][count] = zRow[count];
			count++;
		}
		return data;
	}

	/**
	 * Returns a XYSeries.
	 * @param xLabel the label of x data.
	 * @param yLabel the label of y data.
	 * @return a XYSeries.
	 */
	public XYSeries get(String xLabel, String yLabel) {
		XYSeries newSeries = new XYSeries("XY Data",false) ;
		Double[] xRow = this.get(xLabel);
		Double[] yRow = this.get(yLabel);
		if ( (xRow == null) || (yRow == null) ) return null; 
		if ( xRow.length != yRow.length )
			throw new RuntimeException("Rows have not the same length.");
		int lenght = xRow.length;
		for (int count=0; count<lenght; count++) {
				newSeries.add(xRow[count], yRow[count]) ;
		}
		return newSeries;
	}

}
