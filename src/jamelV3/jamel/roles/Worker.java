package jamelV3.jamel.roles;

import jamelV3.basic.agent.Agent;
import jamelV3.jamel.widgets.Cheque;
import jamelV3.jamel.widgets.LaborPower;

/**
 * Represents a worker.
 */
public interface Worker extends Agent {

	/**
	 * Receives a cheque in exchange for work done. 
	 * @param paycheck the cheque.
	 */
	void earnWage(Cheque paycheck);

	/**
	 * Returns the labor power of the worker.
	 * @return the labor power.
	 */
	LaborPower getLaborPower();

	/**
	 * Searches for a job.
	 */
	void jobSearch();

}

// ***
