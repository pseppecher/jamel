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

/**
 * Contains the labels of the parameters of the firms.
 */
public class Labels {

	/** ACCOUNT */
	public static final String ACCOUNT = "the bank account";

	/** CAPITAL */
	public static final String CAPITAL = "the capital";

	/** Period closure. */
	public static final String CLOSURE = "period closure";

	/** Sales at cost value. */
	public static final String COST_OF_GOODS_SOLD = "Sales at cost value";

	/** the debt of the firm */
	public static final String DEBT = "the debt of the firm";				

	/** DEBT_TARGET */
	public static final String DEBT_TARGET = "Debt Target";

	/** the dividend */
	public static final String DIVIDEND = "the dividend";

	/** pays the dividend to the owner of the firm */
	public static final String DO_PAY_DIVIDEND = "pays the dividend to the owner of the firm";

	/** FIRM */
	public static final String FIRM = "the firm itself";

	/** The gross profit. */
	public static final String GROSS_PROFIT = "The gross profit";

	/** The average of gross profit on the last periods. */
	public static final String GROSS_PROFIT_AVERAGE = "average gross profit on the last periods";

	/** inventoryNormalVolume */
	public static final String INVENTORIES_NORMAL_VOLUME = "inventoryNormalVolume";

	/** the inventories of finished goods */
	public static final String INVENTORIES_OF_FINISHED_GOODS = "the inventories of finished goods";

	/** the total value of inventories (finished goods, unfinished goods and raw materials) */
	public static final String INVENTORIES_TOTAL_VALUE = "the total value of inventories (finished goods, unfinished goods and raw materials)";

	/** Value of the inventory stock of finished goods. */
	public static final String INVENTORY_FG_VALUE = "Value of the inventory stock of finished goods";

	/** Volume of the inventory stock of finished goods. */
	public static final String INVENTORY_FG_VOLUME = "Volume of the inventory stock of finished goods";

	/** The inventory level ratio. */
	public static final String INVENTORY_LEVEL_RATIO = "inventory level ratio";

	/** Value of inventory of unfinished goods. */
	public static final String INVENTORY_UG_VALUE = "Value of inventory of unfinished goods";

	/** The number of jobs offered. */
	public static final String JOBS_OFFERED = "Jobs offered";

	/** The current number of machines. */
	public static final String MACHINERY = "Number of machines";

	/** the amount of money available */
	public static final String MONEY = "the amount of money available";				

	/** The offer on the market of goods. */
	public static final String OFFER_OF_GOODS = "The offer on the market of goods";

	/** The rate of utilization of the capacities of production targeted. */
	//public static final String UTIL_RATE_TARGET = "utilization rate targeted";
	
	/** The offer of job on the labor market. */
	public static final String OFFER_OF_JOB = "The offer of job on the labor market";

	/** The volume of commodities offered at the beginning of the market phase. */
	public static final String OFFERED_VOLUME = "The volume of commodities offered at the beginning of the market phasis.";

	/** Period opening. */
	public static final String OPENING = "period opening";

	/** optimism */
	public static final String OPTIMISM = "The optimism.";

	/** OWNER */
	public static final String OWNER = "the firm owner.";

	/** The payroll. */
	public static final String PAYROLL = "The payroll";

	/** The current price. */
	public static final String PRICE = "current price";

	/** The label for the sector of the firm. */
	public static final String PRODUCTION = "production";

	/** The production level. */
	public static final String PRODUCTION_LEVEL = "production level";

	/** The maximum production level according to the current resources of the factory. */
	public static final String PRODUCTION_LEVEL_MAX = "The maximum production level according to the current resources of the factory";

	/** The average production volume at full utilization of capacity. */
	public static final String PRODUCTION_MAX = "the average production volume at full utilization of capacity";

	/** The value of the production. */
	public static final String PRODUCTION_VALUE = "Value of the production";

	/** The volume of the production. */
	public static final String PRODUCTION_VOLUME = "Volume of the production";

	/** The new purchased raw materials. */
	public static final String RAW_MATERIALS = "The new purchased raw materials";

	/** The budget for the purchase of raw materials. */
	public static final String RAW_MATERIALS_BUDGET = "raw materials budget";

	/** The volume of raw materials needed. */
	public static final String RAW_MATERIALS_NEEDS = "raw materials needs";

	/** The raw materials inventory volume */
	public static final String RAW_MATERIALS_VOLUME = "raw materials inventory volume";

	/** The sales to max production ratio */
	public static final String SALES_RATIO = "The sales to max production ratio";

	@SuppressWarnings("javadoc")
	public static String SALES_RATIO_NORMAL = "The normal level of the sales ratio";

	/** Sales at price value. */
	public static final String SALES_VALUE = "Sales at price value";

	/** Sales variation (volume) */
	public static final String SALES_VARIATION = "Sales variation (volume)";

	/** Volume of sales. */
	public static final String SALES_VOLUME = "Volume of sales";

	/** The unit cost. */
	public static final String UNIT_COST = "unit cost";

	/** The vacancies. */
	public static final String VACANCIES = "vacancies";

	/** The wage. */
	public static final String WAGE = "The wage";

	/** The effective wagebill. */
	public static final String WAGEBILL = "effective wage bill";

	/** The wage bill budget. */
	public static final String WAGEBILL_BUDGET = "wage bill budget";

	/** The workforce employed. */
	public static final String WORKFORCE = "effective workforce";

	/** The workforce targeted. */
	public static final String WORKFORCE_TARGET = "workforce target";

	@SuppressWarnings("javadoc")
	public static final String VERBOSE = "verbose";

}
