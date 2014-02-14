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

package jamel.agents.firms;

import jamel.agents.firms.util.ProductionType;

/**
 * The data of the firm.
 * TODO use an hashmap instead.
 */
public class FirmDataset {

	/** The number of period since the creation of the firm. */
	public int age;

	/** bankrupt */
	public boolean bankrupt;

	/** capital */
	public long capital;

	/** The date */
	public String date;

	/** debt */
	public long debt;

	/** debt target */
	public long debtTarget;

	/** deposit */
	public long deposit;

	/** dividend */
	public long dividend;

	/** doubtDebt */
	public long doubtDebt;

	/** The name of the class of the factory. */
	public String factory;

	/** grossProfit */
	public long grossProfit;

	/** The budget for the purchase of intermediate needs. */
	public long intermediateNeedsBudget;

	/** intermediatesNeedsVolume */
	public int intermediateNeedsVolume;

	/** the appreciation of the inventory Level */
	public String inventoryLevel;

	/** inventoryNormalVolume */
	public Float inventoryNormalVolume;

	/** the inventory Ratio */
	public Float inventoryRatio;

	/** value of inventories (finished goods) */
	public long invFiVal;

	/** The volume of finished goods in inventories. */
	public int invFiVol;

	/** value of inventories (unfinished goods) */
	public long invUnVal;

	/** jobOffers */
	public long jobOffers;

	/** machinerySize */
	public long machinery;

	/** The markup target. */
	public float markupTarget;

	/** maxProduction */
	public int maxProduction;

	/** name */
	public String name;

	/** the volume of commodities offered */
	public Integer offeredVol;

	/** optimism */
	public Boolean optimism;

	/** The period */
	public int period;

	/** price */
	public double price;

	/** production */
	public ProductionType production;

	/** The value of the production. */
	public long prodVal;

	/** productionVolume */
	public long prodVol;

	/** rawMaterialEffectiveVolume */
	public long rawMaterialEffectiveVolume;

	/** rawMaterialNormalVolume */
	public long rawMaterialNormalVolume;

	/** reserveTarget */
	public double reserveTarget;

	/** salesCostValue */
	public long salesCVal;

	/** salesPriceValue */
	public long salesPVal;

	/** salesVariation */
	public Integer salesVariation;

	/** salesVolume */
	public float salesVol;

	/** utilizationTarget */
	public float utilizationTarget;

	/** vacancies */
	public long vacancies;

	/** wage */
	public Double wage;

	/** wageBill */
	public long wageBill;

	/** workforce */
	public long workforce;

	/** anticipatedWorkforce */
	public long workforceTarget;

	/**
	 * Returns the object for the given field. 
	 * @param field  the name of the field. 
	 * @return  an object.
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

}