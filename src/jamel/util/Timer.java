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

import java.util.*; 
import org.jfree.data.time.Month;
import org.jfree.data.time.Year;

/**
 * The timer.
 */
public class Timer {

	/**
	 * Represents a period of the timer.
	 * A period equals one month.
	 */
	public class JamelPeriod {
	
		/** The month. */
		final private Month month;
	
		/** The number of period since the origin. */
		final private int value;
	
		/**
		 * Creates a new period.
		 * @param periodValue - the value of the period.
		 */
		private JamelPeriod(int periodValue) {
			this.value = periodValue;
			int years = periodValue/12;
			int months = periodValue-years*12;
			int yearValue = firstYear+years;
			int monthValue = firstMonth+months;
			if (monthValue>12) {
				monthValue-=12;
				yearValue++;
			}
			this.month = new Month(monthValue,yearValue);
		}
	
		/**
		 * Creates a new period.
		 * @param aMonthValue - the value of the month.
		 * @param aYearValue - the value of the year.
		 */
		private JamelPeriod(int aMonthValue, int aYearValue) {
			if ((aMonthValue<0)|(aMonthValue>12)) 
				throw new RuntimeException("Bad month value.");
			this.month = new Month(aMonthValue,aYearValue);
			this.value = (this.month.getYearValue()-firstYear)*12+this.month.getMonth()-firstMonth;
		}
	
		/**
		 * Creates a new period.
		 * @param aMonth - a month.
		 */
		private JamelPeriod(Month aMonth) {
			this.month = aMonth;
			this.value = (this.month.getYearValue()-firstYear)*12+this.month.getMonth()-firstMonth;		
		}
	
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj==this) {
				return true;
			}
			if (obj instanceof JamelPeriod) {
				JamelPeriod other = (JamelPeriod) obj;
				if (this.month != other.month) {
					if (this.month == null || !this.month.equals(other.month)) {
						if (this.value == other.value) {
							throw new RuntimeException("Inconsistency");
						}
						return false;
					}
				}
				if (this.value != other.value) {
					throw new RuntimeException("Inconsistency");
				}
				return true;
			}
			return false;
		}
	
		/**
		 * Returns the date of the period.
		 * @return the date of the period.
		 */
		public Date getDate() {
			return month.getEnd();
		}
	
		/**
		 * Return the month.
		 * @return the month.
		 */
		public Month getMonth() {
			return this.month;
		}
	
		/**
		 * Returns a new period <code>i</code> periods after this period.
		 * @param i - the number of periods.
		 * @return the new period.
		 */
		public JamelPeriod getFuturePeriod(int i) {
			return new JamelPeriod(this.value+i);
		}
	
		/**
		 * Returns the value of the period, ie the number of periods since the origin.
		 * @return the value of the period.
		 */
		public int getValue() {
			return this.value;
		}
	
		/**
		 * Returns the year.
		 * @return the year.
		 */
		public Year getYear() {
			return this.month.getYear();
		}
		
		/**
		 * Return <code>true</code> if this period is after the other, <code>false</code> else.
		 * @param other - the other period.
		 * @return a boolean.
		 */
		public boolean isAfter(JamelPeriod other) {
			return (this.value>other.value);
		}
	
		/**
		 * Return <code>true</code> if this period is before the other, <code>false</code> else.
		 * @param other - the other period.
		 * @return a boolean.
		 */
		public boolean isBefore(JamelPeriod other) {
			return (this.value<other.value);
		}

		/**
		 * Returns <code>true</code> if the period is the current period. 
		 * @return a boolean.
		 */
		public boolean isCurrentPeriod() {
			return equals(currentPeriod);
		}

		/**
		 * Checks if the month value of this period equals the given month value.
		 * @param monthValue - the value of the month.
		 * @return <code>true</code> if the month value of the period equals the given month value.
		 */
		public boolean isMonth(int monthValue) {
			if ((monthValue<1)|(monthValue>12))
				throw new RuntimeException("Bad parameter.");
			return (this.month.getMonth()==monthValue);
		}

		/**
		 * Returns the next period.
		 * @return the next period.
		 */
		public JamelPeriod next() {
			return new JamelPeriod((Month) month.next());
		}

		/**
		 * Returns a string that contains the date of the period at the format <code>yyyy-mm</code>.
		 */
		public String toString() {
			final int month = this.month.getMonth();
			final int yearValue = this.month.getYearValue();
			if (month<10) return yearValue+"-0"+month;
			return yearValue+"-"+month;
		}

		/**
		 * Returns a flag that indicates if this period is just before the given period.
		 * @param otherPeriod - the given period.
		 * @return a boolean.
		 */
		public boolean isJustBefore(JamelPeriod otherPeriod) {
			return this.value==otherPeriod.value-1;
		}
	
	}

	/** The current period. */
	private JamelPeriod currentPeriod;

	/** The first month. */
	private final int firstMonth =12;
	
	/** The first year. */
	private final int firstYear = 1999;

	/** The first period. */
	private final JamelPeriod origin ;

	/**
	 * Creates a new timer.
	 */
	public Timer() {
		origin = new JamelPeriod(firstMonth,firstYear);
		currentPeriod = origin;
	}

	/**
	 * Returns the current period.
	 * @return the current period.
	 */
	public JamelPeriod getCurrentPeriod() {
		return currentPeriod;
	}

	/**
	 * Returns the origin of the timer.
	 * @return the origin.
	 */
	public JamelPeriod getOrigin() {
		return origin;
	}

	/**
	 * Returns a new period for the given month.
	 * @param aMonth - the month of the new period.
	 * @return a new period.
	 */
	public JamelPeriod newJamelPeriod(Month aMonth) {
		return new JamelPeriod(aMonth);
	}

	/**
	 * Change the current period.
	 */
	public void nextPeriod() {
		currentPeriod = currentPeriod.next();
	}
	
}