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

import jamel.JamelObject;
import jamel.util.Timer.JamelPeriod;

/**
 * An element of a {@link Blackboard}.
 */
public class BlackBoardElement extends JamelObject {

	/** The period of creation of this element. */
	final private JamelPeriod period;
	
	/** The validity of the element, as a number of periods since the creation of the element.*/
	private Integer validity;
	
	/** The value of the element. */
	private Object value;

	/**
	 * Creates a new element with a validity of 1.
	 * @param value  the value.
	 */
	public BlackBoardElement(Object value) {
		this.value=value;
		this.period=getCurrentPeriod();
		this.validity=1;
	}

	/**
	 * Creates a new element.
	 * @param value  the value.
	 * @param validity  the validity.
	 */
	public BlackBoardElement(Object value, Integer validity) {
		this.value=value;
		this.period=getCurrentPeriod();
		this.validity=validity;
	}

	/**
	 * Tests the validity of the element.
	 * @return <code>true</code> if the element is valid.
	 */
	public boolean isValid() {
		boolean b = false;
		if (this.validity==null) {
			b = true;
		}
		else {
			b = this.period.getValue()+this.validity>getCurrentPeriod().getValue(); 
		}
		return b;
	}

	/**
	 * Returns the value of the element.
	 * @return the value of the element.
	 */
	public Object getValue() {
		if (!isValid())
			throw new RuntimeException("Out of date content");
		return value;
	}
	
	/**
	 * Cancels the element.
	 */
	public void cancel() {
		this.value=null;
		this.validity=0;
		
	}

}
