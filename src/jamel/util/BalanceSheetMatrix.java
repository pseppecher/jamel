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

package jamel.util;

import jamel.JamelObject;
import jamel.util.data.PeriodDataset;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * The balance sheet matrix.
 */
public class BalanceSheetMatrix extends JamelObject{

	/** The capital of the bank. */
	private Long bankCapital = new Long(0);

	/** The deposits at the bank. */
	private Long bankDeposits = new Long(0);

	/** The loans granted by the bank. */
	private Long bankLoans = new Long(0);

	/** The net worth of the bank. */
	private Long bankNetWorth = new Long(0);

	/** The sum of the Bank column. */
	private Long bankSum = new Long(0);

	/** The date of the balance sheet. */
	private String date;

	/** The capital of the firms. */
	private Long firmsCapital = new Long(0);

	/** The deposits of the firms. */
	private Long firmsDeposits = new Long(0);

	/** The total value of inventories. */
	private Long firmsInventories = new Long(0);

	/** The loans granted to the firms. */
	private Long firmsLoans = new Long(0);

	/** The net worth of the firms. */
	private Long firmsNetWorth = new Long(0);

	/** The sum of the Firm column. */
	private Long firmsSum = new Long(0);

	/** The capital of the households. */
	private Long householdsCapital = new Long(0);

	/** The deposits of the households. */
	private Long householdsDeposits = new Long(0);

	/** The net worth of the households. */
	private Long householdsNetWorth = new Long(0);

	/** The sum of the Households column. */
	private Long householdsSum = new Long(0);

	/** The number format. */
	final private NumberFormat nf = NumberFormat.getInstance(Locale.US);

	/** The sum of the capital row. */
	private Long sumCapital = new Long(0);

	/** The sum of the Deposits row. */
	private Long sumDeposits = new Long(0);

	/** The sum of the Inventories row. */
	private Long sumInventories = new Long(0);

	/** The sum of the Loans row. */
	private Long sumLoans = new Long(0);

	/** The sum of the Net Worth row. */
	private Long sumNetWorth = new Long(0);

	/** The sum of the Sum column. */
	private Long sumSum = new Long(0);

	/**
	 * Clears the matrix.
	 */
	public void clear() {
		
		date=null;

		bankCapital=null ;
		bankDeposits=null ;
		bankLoans=null ;
		bankNetWorth=null ;
		bankSum=null ;
		
		firmsCapital=null ;
		firmsDeposits=null ;
		firmsInventories=null ;
		firmsLoans=null ;
		firmsNetWorth=null ;
		firmsSum=null ;
		
		householdsCapital=null ;
		householdsDeposits=null ;
		householdsNetWorth=null ;
		householdsSum=null ;
		
		sumCapital=null ;
		sumDeposits=null ;
		sumInventories=null ;
		sumLoans=null ;
		sumNetWorth=null ;
		sumSum=null ;
		
	}

	/**
	 * Sets the total deposits at the bank.
	 * @param longValue - the value to set.
	 */
	public void setBankDeposits(long longValue) {
		this.bankDeposits = -longValue;		
	}

	/**
	 * Sets the loans granted by the bank.
	 * @param longValue - the value to set.
	 */
	public void setBankLoans(long longValue) {
		this.bankLoans = longValue;
	}

	/**
	 * Sets the date of the balance sheet.
	 * @param aDate - a string that contains the date.
	 */
	public void setDate(String aDate) {
		this.date = aDate;
	}

	/**
	 * Sets the deposits of the firms.
	 * @param longValue - the value to set.
	 */
	public void setFirmsDeposits(long longValue) {
		this.firmsDeposits = longValue;
	}

	/**
	 * Sets the inventories of the firms.
	 * @param longValue - the value to set.
	 */
	public void setFirmsInventories(long longValue) {
		this.firmsInventories = longValue;
	}

	/**
	 * Sets the loans granted to the firms.
	 * @param longValue - the value to set.
	 */
	public void setFirmsLoans(long longValue) {
		this.firmsLoans = -longValue;
	}

	/**
	 * Sets the deposits of the households.
	 * @param longValue - the value to set.
	 */
	public void setHouseholdsDeposits(long longValue) {
		this.householdsDeposits = longValue;
	}

	/**
	 * Returns a html representation of the balance sheet matrix.
	 * @return a string that contains the html representation of the matrix.
	 */
	public String toHtml() {
		String warning = "";
		if (bankCapital>0) warning = "<font color=red>";
		final StringBuffer table = new StringBuffer();
		table.append("<STYLE TYPE=\"text/css\">.boldtable, .boldtable TD, .boldtable TH" +
				"{font-family:sans-serif;font-size:12pt;}" +
				"</STYLE>");		
		table.append("<BR><BR><BR><BR><TABLE border=0 align=center class=boldtable cellspacing=10>");
		table.append("<CAPTION>Balance sheet matrix ("+date+")</CAPTION>");
		table.append("<TR><TD colspan=5><HR>");
		table.append("<TR><TH WIDTH=120>" +
				"<TH WIDTH=110>" + "Households" +
				"<TH WIDTH=110>" + "Firms" +
				"<TH WIDTH=110>" + "Bank" +
				"<TH WIDTH=110>" + "Sum");
		table.append("<TR><TD colspan=5><HR>");
		table.append("<TR><TH>Inventories" +
				"<TD align=right>" +
				"<TD align=right>" + nf.format(firmsInventories) + 
				"<TD align=right>" +
				"<TD align=right>"+ nf.format(sumInventories));
		table.append("<TR><TH>Money Deposits" +
				"<TD align=right>" + nf.format(householdsDeposits) +
				"<TD align=right>" + nf.format(firmsDeposits) +
				"<TD align=right>" + nf.format(bankDeposits) +
				"<TD align=right>" + nf.format(sumDeposits));
		table.append("<TR><TH>Loans" +
				"<TD>" +
				"<TD align=right>" + nf.format(firmsLoans) +
				"<TD align=right>" + nf.format(bankLoans) +
				"<TD align=right>" + nf.format(sumLoans));
		table.append("<TR><TH>Capital" +
				"<TD align=right>" + nf.format(householdsCapital) +
				"<TD align=right>" + nf.format(firmsCapital) +
				"<TD align=right>" + warning + nf.format(bankCapital) +
				"<TD align=right>" + nf.format(sumCapital));
		table.append("<TR><TH>Net Worth" +
				"<TD align=right>" + nf.format(householdsNetWorth) +
				"<TD align=right>" + nf.format(firmsNetWorth) +
				"<TD align=right>" + nf.format(bankNetWorth) +
				"<TD align=right>" + nf.format(sumNetWorth)
				);
		table.append("<TR><TD colspan=5><HR>");
		table.append("<TR><TH>Sum" +
				"<TD align=right>" + nf.format(householdsSum) +
				"<TD align=right>" + nf.format(firmsSum) +
				"<TD align=right>" + nf.format(bankSum) +
				"<TD align=right>" + nf.format(sumSum)
				);
		table.append("<TR><TD colspan=5><HR>");
		table.append("</TABLE>");
		return table.toString();
	}

	/**
	 * Updates the matrix and checks its consistency.
	 * @param periodDataset the data.
	 */
	public void update(PeriodDataset periodDataset) {
		this.clear();
		this.setDate(getCurrentPeriod().toString());
		this.setFirmsInventories(periodDataset.invFinVal+periodDataset.invUnfVal);
		this.setFirmsDeposits(periodDataset.fDeposits);
		this.setHouseholdsDeposits(periodDataset.hDeposits);
		this.setBankDeposits(periodDataset.DEPOSITS);
		this.setFirmsLoans(periodDataset.LOANS);
		this.setBankLoans(periodDataset.LOANS);
		this.sumInventories = this.firmsInventories;
		this.sumDeposits = this.firmsDeposits+this.householdsDeposits+this.bankDeposits;
		this.sumLoans = this.firmsLoans+this.bankLoans;
		this.firmsCapital = -(this.firmsInventories+this.firmsDeposits+this.firmsLoans);
		this.bankCapital = -(this.bankDeposits+this.bankLoans);
		this.householdsCapital = -(this.firmsCapital+this.bankCapital);
		this.sumCapital = this.householdsCapital+this.firmsCapital+this.bankCapital;
		this.householdsNetWorth = -(this.householdsDeposits+this.householdsCapital);
		this.firmsNetWorth = - (this.firmsInventories+this.firmsDeposits+this.firmsLoans+this.firmsCapital);
		this.bankNetWorth = -(this.bankDeposits+this.bankLoans+this.bankCapital);
		this.sumNetWorth = this.householdsNetWorth+this.firmsNetWorth+this.bankNetWorth;
		this.householdsSum = this.householdsDeposits+this.householdsCapital+this.householdsNetWorth;
		this.firmsSum = this.firmsInventories+this.firmsDeposits+this.firmsLoans+this.firmsCapital+this.firmsNetWorth;
		this.bankSum = this.bankDeposits+this.bankLoans+this.bankCapital+this.bankNetWorth;
		this.sumSum = this.sumInventories+this.sumDeposits+this.sumLoans+this.sumNetWorth;
		if (this.sumDeposits!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.sumLoans!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.sumCapital!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.householdsSum!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.firmsSum!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.bankSum!=0) 
			throw new RuntimeException("Inconsistency");
		if (this.householdsNetWorth!=-sumInventories) 
			throw new RuntimeException("Inconsistency");
	}

}
