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

import java.util.ArrayList;
import java.util.List;

/**
 * A basic mediator.<p>
 * 2013-11-09: creation.
 */
public class BasicMediator implements Mediator {

	/** The list of the components. */
	private List<FirmComponent> componentList = new ArrayList<FirmComponent>(10);

	/**
	 * Returns the specified resource or the information.<p>
	 * Scans the list of components, seeking the resource.
	 * Returns null if the resource is not found.
	 * Generates a <code>RuntimeException</code> if there are multiple response. 
	 * @param key  the name of the resource or information.
	 * @return an object.
	 */
	@Override
	public Object get(String key) {
		Object result = null;
		for (FirmComponent component: componentList) {
			Object result2 = component.get(key);
			if (result2!=null) {
				if (result!=null) {
					throw new RuntimeException("I have already a result for the key "+key);
				}
				result=result2;
			}
		}
		return result;
	}

	@Override
	public void add(FirmComponent component) {
		if (component!=null) {
			this.componentList.add(component);			
		}
	}

}
