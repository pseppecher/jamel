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
 */
public class HouseholdDataset {	
		
	/** The consumption budget. */
	private long consumptionBudget;
	
	/** The consumption value. */
	private long consumptionValue;
	
	/** The consumption volume. */
	private int consumptionVolume;
	
	/** The deposits. */
	private long deposits;
	
	/** The dividend. */
	private long dividend;
	
	/** The employment status. */
	private int employmentStatus;
	
	/** The forced savings. */
	private long forcedSavings;
	
	/** The reservation wage. */
	private float reservationWage;
	
	/** sector */
	private ProductionType sector;
	
	/** The unemployment duration. */
	private double unemploymentDuration;

	/** The wage. */
	private long wage;

	private boolean optimism;

	public long savingTarget;

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
		wage=0;		
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

	/**
	 * Sets the reservation wage.
	 * @param value - the value to set.
	 */
	public void setReservationWage(float value) {
		this.reservationWage=value;
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

	public boolean getOptimism() {
		return this.optimism;
	}

	public void setOptimism(boolean optimist) {
		this.optimism=optimist;
	}

	public void setSavingTarget(long value) {
		this.savingTarget=value;
	}	
	
}