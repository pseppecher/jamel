package jamel.basic.agents.roles;

import jamel.basic.agents.util.LaborPower;
import jamel.basic.util.Cheque;

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
