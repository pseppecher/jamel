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

package jamel.gui;

import jamel.JamelObject;
import jamel.util.Timer.JamelPeriod;

import java.util.Date;

import org.jfree.data.time.Month;

/**
 * A class to manage the visibility range for time charts.
 */
public class ViewManager extends JamelObject {

	/** The current period. */
	private JamelPeriod current = getOrigin();

	/** The end of the view. */
	private JamelPeriod end = getOrigin() ;

	/** The origin. */
	private final JamelPeriod origin = getOrigin() ;

	/** The start of the view. */
	private JamelPeriod start = end.getFuturePeriod(-300);

	/**
	 * Returns the range.
	 * @return the range.
	 */
	private int getRange() {
		return end.getValue()-start.getValue();
	}

	/**
	 * Returns the end.
	 * @return the end.
	 */
	public JamelPeriod getEnd() {
		return end;
	}


	/**
	 * Returns the start.
	 * @return the start.
	 */
	public JamelPeriod getStart() {
		return start ;
	}

	/**
	 * Sets the current Date.
	 */
	public void update() {
		current = getCurrentPeriod() ;
		int range = getRange();
		end = current ;
		start = end.getFuturePeriod(-range) ;
	}


	/**
	 * Sets the dates.
	 * @param startDate - the start.
	 * @param endDate - the end.
	 */
	public void setDates(Date startDate, Date endDate) {
		start = newJamelPeriod(new Month(startDate));
		end = newJamelPeriod(new Month(endDate));
	}

	/**
	 * Sets the range.
	 * @param range - the range to set.
	 */
	public void setRange(int range) {
		this.start = this.end.getFuturePeriod(-range);
	}

	/**
	 * Sets the largest view.
	 */
	public void zoomAll() {
		start = origin ;
		end = current ;
	}

}






















