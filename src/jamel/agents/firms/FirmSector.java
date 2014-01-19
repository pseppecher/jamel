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

import jamel.util.data.PeriodDataset;

/**
 * The industrial sector.
 */
public interface FirmSector {

	/**
	 * Closes the sector.<br>
	 * Each household is closed. 
	 * The sector data are updated.
	 * @param macroDataset  the macro dataset. 
	 */
	public abstract void close(PeriodDataset macroDataset);

	/**
	 * Returns the specified resource.
	 * @param key  the key of the resource to return.
	 * @return an object.
	 */
	public abstract Object get(String key);

	/**
	 * Returns the data for the given firm.
	 * @param name  the name of the firm
	 * @param keys  a string that contains the keys of the fields to return, separated by commas.
	 * @return a string containing the data of the firm, each field separated by commas.
	 */
	public abstract String getFirmData(String name, String keys);

	/**
	 * Returns the data for each firm of the sector.
	 * @param keys  a string that contains the keys of the fields to return, separated by commas.
	 * @return an array of string, each string containing the data of one firm, each field separated by commas.
	 */
	public abstract String[] getFirmsData(String keys);

	/**
	 * Kills the sector.
	 */
	public abstract void kill();

	/**
	 * Creates new firms according to the parameters.
	 * @param parameters  a string that contains the parameters of the new firms.
	 */
	public abstract void newFirms(String parameters);

	/**
	 * Opens the firms sector.<br>
	 * Each firm is opened.
	 */
	public abstract void open();

	/**
	 * Pays the dividend.<br>
	 * Each firm is called to pay the dividend.
	 */
	public abstract void payDividend();

	/**
	 * Plans the production.<br>
	 * Each firm is called to prepare the production.
	 */
	public abstract void planProduction();

	/**
	 * Executes the production.<br>
	 * Each firm is called to executes the production.
	 */
	public abstract void production();

	/**
	 * Sets the type of the firms.
	 * @param type the type to set.
	 */
	public abstract void setFirmType(String type);

	/**
	 * Sets the given firm verbose.<p>
	 * For debugging purpose.
	 * @param name  the name of the firm.
	 */
	public abstract void setVerbose(String name);
	
}