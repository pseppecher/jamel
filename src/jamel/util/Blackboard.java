/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2013, Pascal Seppecher.
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

package jamel.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import jamel.JamelObject;

/**
 * A shared repository of resources and contributed information.
 */
public class Blackboard extends JamelObject {

	/** A map that contains the elements. */
	private final HashMap<String,BlackBoardElement> map = new HashMap<String,BlackBoardElement>();

	/**
	 * Returns <code>true</code> if this blackboard contains a valid element for the specified key.
	 * @param key  the key whose presence in this blackboard is to be tested.
	 * @return <code>true</code> if this blackboard contains a valid element for the specified key.
	 */
	public boolean containsKey(String key) {
		boolean b = false;
		if (this.map.containsKey(key)) {
			if (!this.map.get(key).isValid()) throw new RuntimeException("Invalid element.");
			b=true;
		}
		return b;
	}

	/**
	 * Returns the value to which the specified key is associated, or <code>null</code> if this blackboard contains no valid element for the key.
	 * @param key  the key whose associated value is to be returned.
	 * @return the value to which the specified key is associated, or <code>null</code> if this blackboard contains no valid element for the key.
	 */
	public Object get(String key) {
		Object o = null;
		if (this.map.containsKey(key)) {
			final BlackBoardElement blackBoardElement = this.map.get(key);
			if (!blackBoardElement.isValid()) throw new RuntimeException("Invalid element.");
			o=blackBoardElement.getValue();
		}
		return o;
	}

	/**
	 * Associates the specified value with the specified key in this blackboard. 
	 * If the blackboard previously contained an element for the key, the old element is replaced.
	 * The value is valid only for the current period.
	 * @param key  key with which the specified value is to be associated.
	 * @param value  value to be associated with the specified key (<code>null</code> not permitted).
	 */
	public void put(String key, Object value) {
		this.map.put(key, new BlackBoardElement(value));
	}

	/**
	 * Associates the specified value with the specified key in this blackboard. 
	 * If the blackboard previously contained an element for the key, the old element is replaced.
	 * @param key  key with which the specified value is to be associated.
	 * @param value  value to be associated with the specified key.
	 * @param validity  the number of periods the value will be available.
	 */
	public void put(String key, Object value, Integer validity) {
		this.map.put(key, new BlackBoardElement(value,validity));
	}

	/**
	 * Returns the object associated with the key (if any).
	 * @param key  the key.
	 * @return the object.
	 */
	public Object remove(String key) {
		return this.map.remove(key).getValue();
	}

	/**
	 * Removes all invalid elements.
	 */
	public void cleanUp() {
		final Set<String> blackList=new HashSet<String>();
		for(Entry<String, BlackBoardElement> entry : this.map.entrySet()) {
			if (!entry.getValue().isValid()) {
				blackList.add(entry.getKey());
			}
		}
		for(String key:blackList) {
			map.remove(key).cancel();
		}
	}

}











