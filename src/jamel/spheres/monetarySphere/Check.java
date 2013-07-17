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

package jamel.spheres.monetarySphere;

import jamel.JamelObject;
import jamel.agents.roles.AccountHolder;
import jamel.util.Timer.JamelPeriod;


/**
 * An abstract class for checks.
 */
public abstract class Check extends JamelObject{

	/** The amount of the check. */
	private final long amount;

	/** The issue date.  */
	private final JamelPeriod issueDate;

	/** A flag that indicates whether or not the check is valid. */
	private boolean isValid;

	/** The payee. */
	private final AccountHolder payee;

	/**
	 * Creates a new check.
	 * @param aAmount - the amount.
	 * @param aPayee - the payee.
	 */
	protected Check(long aAmount, AccountHolder aPayee) {
		if (aAmount<=0) throw new RuntimeException("The amount is not positive.");
		if (aPayee==null) throw new RuntimeException("The payee is null.");
		this.amount = aAmount;
		this.payee = aPayee;
		this.issueDate = getCurrentPeriod();
		this.isValid=true;
	}

	/**
	 * Cancels the check.
	 */
	protected void cancel() {
		this.isValid=false;
	}

	/**
	 * Transfer the amount of the check to the given account.
	 * @param aAccount - the payee account.
	 */
	protected abstract void transferTo(Account aAccount);

	/**
	 * Returns the amount.
	 * @return the amount.
	 */
	public long getAmount() {
		return amount;
	}

	/**
	 * Returns the payee.
	 * @return the payee.
	 */
	public AccountHolder getPayee() {
		return payee;
	}

	/**
	 * Returns a flag that indicates whether or not the check is valid.
	 * @return a boolean.
	 */
	public boolean isValid(){
		if (!this.issueDate.isCurrentPeriod()) throw new RuntimeException("Time inconsistency.");
		return this.isValid;
	}

	public String toString() {
		return "Drawer: "
		+getDrawer()
		+", Payee: "
		+getPayee()
		+", Amount: "
		+amount
		+", Issue date:"+issueDate.toString()
		+", Is valid:"+isValid();
	}

	/**
	 * Returns the drawer of this check.
	 * @return the drawer.
	 */
	public abstract AccountHolder getDrawer();

}
