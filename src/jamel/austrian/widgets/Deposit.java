/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2012, Pascal Seppecher.
 * 
 * Project Info <http://hp.gredeg.cnrs.fr/Pascal_Seppecher/jamel/index.php>. 
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


/**
 * Represents a monetary deposit.
 * <p>
 * Encapsulates a number (a int integer) that represents a monetary deposit.
 */

public class Deposit {

	/** The deposit amount. */
	private int deposit ;

	/**
	 * Creates a new empty deposit.
	 */
	public Deposit() {
		this.deposit = 0 ;
	}

	/**
	 * Debits the deposit of the given amount.
	 * @param debitAmount - the amount to debit.
	 */
	public void debit(int debitAmount) {
		if (debitAmount<=0) throw new RuntimeException("Null or negative debit: "+debitAmount);
		if (deposit < debitAmount) throw new RuntimeException("Not enough money. Deposit: "+deposit+" , Debit amount: "+debitAmount);
		deposit-=debitAmount;
	}

	/**
	 * Credits the deposit with the given amount.
	 * @param creditAmount - the amount to credit.
	 */
	public void credit(int creditAmount) {
		if (creditAmount<0) throw new RuntimeException("Negative credit.");
		deposit += creditAmount ;
	}	

	/**
	 * Returns the available amount.
	 */
	public int getAmount() {
		if (deposit<0) throw new RuntimeException("Negative deposit.");
		return deposit ; 
	}
	
	/**
	 * Sets the deposit to zero.
	 */
	public void reset() {
		this.deposit = 0;
	}

}
