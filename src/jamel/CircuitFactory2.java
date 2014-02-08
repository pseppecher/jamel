/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel;

/**
 * A <code>Circuit</code> factory for multi-simulations. 
 */
public class CircuitFactory2 extends CircuitFactory {

	/**
	 * Creates a new factory.
	 * @param simulator  the parent simulator.
	 */
	public CircuitFactory2(Simulator simulator) {
		super(simulator);
		this.END_FONT="";
		this.FONT_BLUE="";
		this.FONT_GREEN="";
	}
	
	/**
	 * Prints a line of the scenario in the console panel.
	 * @param string  the line to be printed.
	 */
	protected void printLine(String string) {
		System.out.println(string);
	}

}
