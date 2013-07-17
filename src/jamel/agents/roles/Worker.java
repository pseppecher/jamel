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

package jamel.agents.roles;

import jamel.spheres.monetarySphere.Check;
import jamel.spheres.realSphere.Machine;
import jamel.util.markets.EmploymentContract;

/**
 * A worker is an agent who can search a job, receive the notification 
 * of his hiring, receive a check for wage payment, work on a machine, 
 * and receive the notification of his layoff.
 */
public interface Worker extends AccountHolder {

	/**
	 * Returns the name of the agent.
	 * @return a string that represents the name.
	 */
	String getName();

	/**
	 * Receives the notification of his hiring.
	 * @param newContract the new job contract.
	 */
	void notifyHiring(EmploymentContract newContract);

	/**
	 * Receives the notification of his layoff.
	 */
	void notifyLayoff();

	/**
	 * Receives a cheque for paiment of the wage.
	 * @param cheque the cheque.
	 */
	void receiveWage(Check cheque);

	/**
	 * Works on a machine.
	 * @param machine the machine.
	 */
	void work(Machine machine);

	/**
	 * Looks for a job on the labor market.
	 */
	public void jobSearch();
	
}
