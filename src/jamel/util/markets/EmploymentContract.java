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

package jamel.util.markets;

import jamel.JamelObject;
import jamel.agents.roles.Employer;
import jamel.agents.roles.Worker;
import jamel.util.Timer.JamelPeriod;

/**
 * Represents a job contract.
 * <p>
 * Encapsulates the reference to the employer and the employee, the wage and the end of the contract.
 */
public class EmploymentContract extends JamelObject {

	/** The employee. */
	final private Worker employee ;

	/** The employer. */
	final private Employer employer ;

	/** The period in which the contract will end. */
	private JamelPeriod end ;

	/** The period in which the contract was signed. */
	private final JamelPeriod start ;

	/** The wage. */
	private final long wage ;

	/**
	 * Creates a new employment contract.
	 * @param employer the employer.
	 * @param employee the employee.
	 * @param wage the wage.
	 * @param term the term.
	 */
	public EmploymentContract(Employer employer,Worker employee,long wage,int term)	{
		this.employer = employer ;
		this.employee = employee ;
		this.wage = wage ;
		this.start = getCurrentPeriod() ;
		this.end = this.start.getFuturePeriod(term) ;
	}

	/**
	 * Breaches the contract.<br>
	 * Used by the employer to layoff a worker.
	 */
	public void breach() {
		if (!isValid()) throw new RuntimeException("Not Valid Contract") ;
		this.end = getCurrentPeriod() ;
	}

	/**
	 * Returns the employee.
	 * @return the employee.
	 */
	public Worker getEmployee() { 
		return this.employee ; 
	}

	/**
	 * Returns the employer. 
	 * @return the employer.
	 */
	public Employer getEmployer() { 
		return this.employer ; 
	}

	/**
	 * Returns the end of the contract.
	 * @return a period.
	 */
	public JamelPeriod getEnd() { 
		return this.end ; 
	}

	/**
	 * Returns the start of the contract.
	 * @return a period.
	 */
	public JamelPeriod getStart() {
		return this.start;
	}

	/**
	 * Returns the wage to be paid to the employee.
	 * @return a long integer that represents the wage.
	 */
	public long getWage() { 
		return this.wage ; 
	}

	/**
	 * Returns a flag that indicates whether or not the contract is valid.
	 * @return a boolean.
	 */
	public boolean isValid() { 
		return (getCurrentPeriod().isBefore(this.end)) ; 
	}

}