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

package jamel.spheres.realSphere;

import jamel.agents.firms.util.FirmComponent;

/**
 * An interface for factories.
 */
public interface Factory extends FirmComponent{

	/**
	 * Closes the factories.
	 */
	void close();

	/**
	 * Returns the total value of the factory.
	 * This total value is the sum of the value of the inventory of finished goods
	 * and the value of unfinished goods embedded in the processes of production. 
	 * @return a value.
	 */
	long getWorth();

	/**
	 * Kills the factory.
	 */
	void kill();

	/**
	 * Completes some technical operations at the beginning of the period.
	 */
	void open();

	/**
	 * Production function of the factory.<br>
	 * Summons each employee and makes him work on a machine.
	 */
	void production();
	
}
