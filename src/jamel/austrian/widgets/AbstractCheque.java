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
 * aint with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */

package jamel.austrian.widgets;

import jamel.austrian.banks.CommercialBank;
import jamel.austrian.roles.AccountHolder;



/**
 * An abstract class for cheques.
 */
public abstract class AbstractCheque {

	/** The amount of the cheque. */
	private final int amount;
	
	/** The issuing bank. */
	public CommercialBank issuingBank;

	/**
	 * Creates a new cheque.
	 */
	protected AbstractCheque(int aAmount, CommercialBank issuingBank) {
		this.amount = aAmount;
		this.issuingBank = issuingBank;
	}

	/**
	 * Returns the amount of the cheque.
	 */
	public int getAmount() {
		return amount;
	}
	
	
	/**
	 * Returns the amount of the cheque.
	 */
	public CommercialBank getIssuingBank() {
		return issuingBank;
	}
	
	/**
	 * Returns the drawer of the cheque.
	 */
	public abstract AccountHolder getIssuer();

}
