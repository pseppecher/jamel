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

package jamel.agents.firms.managers;

import jamel.agents.firms.util.FirmComponent;
import jamel.agents.roles.Worker;
import jamel.util.markets.JobOffer;

/**
 * The workforce manager.
 */
public interface WorkforceManager extends FirmComponent{

	public abstract Object get(String key);

	/**
	 * Receives a job application.
	 * @param worker - the applicant.
	 * @param offer2 - the related job offer.
	 */
	public abstract void jobApplication(Worker worker, JobOffer offer2);

	/**
	 * Kills the manager.
	 */
	public abstract void kill();

	/**
	 * Fires all the employees.
	 */
	public abstract void layoffAll();

	/**
	 * Creates a new offer on the labor market.
	 */
	public abstract void newJobOffer();

	/**
	 * Opens the manager.<br>
	 * Called at the beginning of the period to initialize the manager. 
	 */
	public abstract void open();

	/**
	 * Pays all employees.
	 */
	public abstract void payWorkers();

	/**
	 * Updates the available workforce.
	 */
	public abstract void updateWorkforce();

}