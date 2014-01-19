/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher and contributors.
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
 */

package jamel.agents.firms.util;


/**
 * The mediator facilitates the communication of informations and resources
 * between the components of the firm.<p>
 * Takes the place of the defunct BlackBoard object.<p>
 * 2013-11-09: creation.
 */
public interface Mediator {

	/**
	 * Returns the specified resource or the information.
	 * @param key  the name of the resource or information.
	 * @return an object (<code>null</code> if not found).
	 */
	Object get(String key);

	/**
	 * Adds a component.
	 * @param component  the component to add.
	 */
	void add(FirmComponent component);

}
