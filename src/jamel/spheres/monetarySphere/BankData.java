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

/**
 * A dataset for the bank.
 */
public class BankData {

	/** The number of bankruptcies. */
	private int bankruptcies;
	
	/** capital */
	private long capital;

	/** deposits */
	private long deposits;

	/** dividend */
	private long dividend;

	/** doubtfulDebts */
	private long doubtfulDebts;

	/** loans */
	private long loans;

	/** non performing loans */
	public long nPLoans;

	/**
	 * Adds a new bankruptcy.
	 */
	public void addBankruptcy() {
		bankruptcies++;
	}

	/**
	 * Returns the number of bankruptcies.
	 * @return the number of bankruptcies.
	 */
	public long getBankruptcies() {
		return bankruptcies;
	}

	/**
	 * @return the capital.
	 */
	public long getCapital() {
		return capital;
	}
	
	/**
	 * @return the deposits.
	 */
	public long getDeposits() {
		return deposits;
	}
	
	/**
	 * @return the dividend.
	 */
	public long getDividend() {
		return dividend;
	}

	/**
	 * @return the doubtful debts.
	 */
	public long getDoubtfulDebts() {
		return doubtfulDebts;
	}

	/**
	 * @return the loans.
	 */
	public long getLoans() {
		return loans;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setCapital(long value) {
		capital = value;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setDeposits(long value) {
		deposits = value;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setDividend(long value) {
		dividend = value;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setDoubtfulDebts(long value) {
		doubtfulDebts = value;
	}

	/**
	 * @param value - the value to set.
	 */
	public void setLoans(long value) {
		loans = value;
	}

}
