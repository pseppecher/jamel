package jamel.jamel.roles;

import jamel.basic.agent.Agent;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.LaborPower;

/**
 * Represents a worker.
 */
public interface Worker extends Agent {

	/**
	 * Receives a cheque in exchange for work done.
	 * 
	 * @param paycheck
	 *            the cheque.
	 */
	void earnWage(Cheque paycheck);

	/**
	 * Returns the labor power of the worker.
	 * 
	 * @return the labor power.
	 */
	LaborPower getLaborPower();

	/**
	 * Returns <code>true</code> if this worker is currently employed
	 * 
	 * @return <code>true</code> if this worker is currently employed.
	 */
	boolean isEmployed();

	/**
	 * Searches for a job.
	 */
	void jobSearch();

}

// ***
