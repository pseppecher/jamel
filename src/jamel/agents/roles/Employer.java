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

import jamel.agents.firms.util.ProductionType;
import jamel.util.markets.JobOffer;

/**
 * An employer is an agent who can post job offers on the labour market, 
 * and receive a job application from a worker.
 */
public interface Employer extends Offerer{

	/**
	 * Returns the type of production of this provider.
	 * @return a type of production.
	 */
	ProductionType getProduction();

	/**
	 * Returns the offer of the employer on the labor market.
	 * @return the offer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns the name of the agent.
	 * @return a string that represents the name.
	 */
	String getName();

	/**
	 * Returns a flag that indicates if the employer is bankrupt or not.
	 * @return <code>true</code> if the employer is bankrupt.
	 */
	boolean isBankrupt();

	/**
	 * Receives a job application.
	 * @param worker the job seeker.
	 * @param jobOffer the related job offer.
	 */
	void jobApplication(Worker worker, JobOffer jobOffer);

}
