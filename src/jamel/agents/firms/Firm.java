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

package jamel.agents.firms;

import java.util.LinkedList;
import java.util.Map;

import jamel.agents.roles.Employer;
import jamel.agents.roles.Provider;

/**
 * An interface for the firms.
 */
public interface Firm extends Employer, Provider {

	/**
	 * Buys the raw materials.
	 */
	void buyRawMaterials();

	/**
	 * Closes the firm.<br>
	 * Completes some technical operations at the end of the period.
	 */
	void close();

	/**
	 * Returns the data.
	 * @return the data.
	 */
	FirmDataset getData();

	/**
	 * Returns a string that contains parameters for the creation of a new firm.
	 * @return a string.
	 */
	String getParametersString();

	/**
	 * Goes bankrupt.
	 */
	void goBankrupt();
	
	
	/**
	 * Returns a flag that indicates if the firm is bankrupt or not.
	 * @return <code>true</code> if the firm is bankrupt.
	 */
	boolean isBankrupt();

	/**
	 * Kills the firm.
	 */
	void kill();

	/** 
	 * Opens the household for a new period.<br>
	 * Initializes data and executes events.
	 * @param eList - a list of strings that describes the events for the current period. 
	 */
	void open(LinkedList<String> eList);

	/**
	 * Pays the dividend.
	 */
	void payDividend();

	/**
	 * Prepares the production.
	 */
	void prepareProduction();

	/**
	 * Produces.
	 */
	void production();

	/**
	 * Returns a map that contains the parameters.
	 * @return a map that contains the parameters.
	 */
	Map<String, Object> getParameters();

}
