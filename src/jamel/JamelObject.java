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


import java.util.Random;

import org.jfree.data.time.Month;

import jamel.util.Timer;
import jamel.util.Timer.JamelPeriod;

/**
 * An abstract class with methods to get time and random.
 */
public abstract class JamelObject {

	/** The random. */
	private static Random random;

	/** The timer. */
	private static  Timer timer;

	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public static JamelPeriod getCurrentPeriod() {
		return timer.getCurrentPeriod();
	}

	/**
	 * Returns the random.
	 * @return the random.
	 */
	public static Random getRandom() {
		return random;
	}

	/**
	 * Sets the random.
	 * @param random2  the random to set.
	 */
	public static void setRandom(Random random2) {
		random = random2;
	}

	/**
	 * Sets the timer.
	 * @param aTimer - the timer to set.
	 */
	public static void setTimer(Timer aTimer) {
		timer = aTimer;
	}

	/**
	 * Goes to the next period.
	 */
	void nextPeriod() {
		timer.nextPeriod();
	}

	/**
	 * Returns the origin of the timer.
	 * @return the origin.
	 */
	public JamelPeriod getOrigin() {
		return timer.getOrigin();
	}

	/**
	 * Returns a new period for the given month.
	 * @param month - the month of the new period.
	 * @return a new period.
	 */
	public JamelPeriod newJamelPeriod(Month month) {
		return timer.newJamelPeriod(month);
	}

}
