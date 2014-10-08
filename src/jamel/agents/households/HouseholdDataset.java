/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
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
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. 
 * JFreeChart is distributed under the terms of the GNU Lesser General Public Licence (LGPL). 
 * See <http://www.jfree.org>.]
 */

package jamel.agents.households;

import jamel.agents.firms.util.ProductionType;

/**
 * A class for the household data.
 * TODO use an hashmap instead.
 */
public class HouseholdDataset {	
		
	public long capital;
	
	/** The consumption budget. */
	public long consumptionBudget;
	
	/** The consumption value. */
	public long consumptionValue;
	
	/** The consumption volume. */
	public int consumptionVolume;
	
	/** The deposits. */
	public long deposits;
	
	/** The dividend. */
	public long dividend;
	
	public int employmentDuration;
	
	/** The employment status. */
	public int employmentStatus;
	
	/** The forced savings. */
	public long forcedSavings;
	
	public String name;

	public boolean optimism;

	public int period;

	/** The reservation wage. */
	public float reservationWage;

	public long savingTarget;
	
	public long savings;

	/** sector */
	public ProductionType sector;
	
	/** The unemployment duration. */
	public double unemploymentDuration;

	/** The wage. */
	public long wage;

	/**
	 * Returns the object for the given field. 
	 * @param  field  the name of the field. 
	 * @return an object.
	 * @throws NoSuchFieldException  if the field is not found.
	 */
	private Object getFieldValue(String field) throws NoSuchFieldException {
		Object value = null;
		try {
			value = this.getClass().getField(field).get(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		return value;
	}

	/**
	 * @param value - the value to add.
	 */
	public void addDividend(long value) {
		this.dividend+=value;
	}

	/**
	 * @param value - the value to add.
	 */
	public void addToConsumptionValue(long value) {
		this.consumptionValue+=value;
	}

	/**
	 * @param volume - the volume to add.
	 */
	public void addToConsumptionVolume(int volume) {
		this.consumptionVolume+=volume;
	}

	public void clear() {
		consumptionBudget=0;
		consumptionValue=0;
		consumptionVolume=0;
		deposits=0;
		dividend=0;
		employmentStatus=-1;
		forcedSavings=0;
		reservationWage=0;
		unemploymentDuration=0;
		employmentDuration=0;
		wage=0;
		capital=0;
		this.savingTarget=0;
	}

	public long getConsumptionBudget() {
		return consumptionBudget;
	}

	public long getConsumptionValue() {
		return consumptionValue;
	}

	public int getConsumptionVolume() {
		return this.consumptionVolume;
	}

	/**
	 * Returns the contains of the specified fields.
	 * @param sKeys  a string that contains the keys of the fields to return, separated by commas.
	 * @return a string that contains the values of the specified fields, separated by commas. 
	 */
	public String getData(String sKeys) {
		String result=null;
		String[] keys = sKeys.split(",");
		for (String key:keys) {
			String value = key;
			if (key.startsWith("%")) {
				try {
					final Object fieldValue = this.getFieldValue(key.substring(1));
					if (fieldValue!=null) {
						value=fieldValue.toString();
					}
					else {
						value="null";					
					}
				} catch (NoSuchFieldException e) {
					value="No Such Field: "+key;
				}
			}
			if (result==null){
				result=value;
			}
			else {
				result+=","+value;
			}
		}
		return result;
	}

	public long getDeposits() {
		return deposits;
	}

	public long getDividend() {
		return dividend;
	}

	public Integer getEmploymentStatus() {
		return employmentStatus;
	}

	public long getForcedSavings() {
		return forcedSavings;
	}

	public long getIncome() {
		return wage+dividend;
	}

	public boolean getOptimism() {
		return this.optimism;
	}

	public float getReservationWage() {
		return reservationWage;
	}

	public ProductionType getSector() {
		return this.sector;
	}

	public double getUnemploymentDuration() {
		return unemploymentDuration;
	}

	public long getWage() {
		return wage;
	}

	/**
	 * Sets the consumption budget.
	 * @param value - the value to set.
	 */
	public void setConsumptionBudget(long value) {
		this.consumptionBudget=value;
	}

	/**
	 * Sets the deposits.
	 * @param value - the value to set.
	 */
	public void setDeposits(long value) {
		this.deposits=value;
	}

	/**
	 * Sets the employment status.
	 * @param status - the status to set.
	 */
	public void setEmploymentStatus(int status) {
		this.employmentStatus=status;
	}

	/**
	 * Sets the forced savings.
	 * @param value - the value to set.
	 */
	public void setForcedSavings(long value) {
		this.forcedSavings=value;
	}

	public void setOptimism(boolean optimist) {
		this.optimism=optimist;
	}

	/**
	 * Sets the reservation wage.
	 * @param value - the value to set.
	 */
	public void setReservationWage(float value) {
		this.reservationWage=value;
	}

	public void setSavingTarget(long value) {
		this.savingTarget=value;
	}

	public void setSavings(long savings) {
		this.savings=savings;
	}

	/**
	 * Sets the sector.
	 * @param sector  the sector to set.
	 */
	public void setSector(ProductionType sector) {
		this.sector = sector;
	}

	/**
	 * Sets the duration of unemployment.
	 * @param duration - the duration to set.
	 */
	public void setUnemploymentDuration(float duration) {
		this.unemploymentDuration=duration;
	}

	/**
	 * Sets the wage.
	 * @param value - the value to set.
	 */
	public void setWage(long value) {
		if (value==0)
			throw new RuntimeException("Wage equals 0.");
		this.wage=value;
	}

}