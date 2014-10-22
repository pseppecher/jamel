package jamel.basic.util;

import jamel.basic.agents.util.LaborPower;

/**
 * Represents a job contract.
 */
public interface JobContract {

	/**
	 * Breaks the contract.
	 */
	void breach();

	/**
	 * Returns the labor power of the worker.
	 * @return the labor power.
	 */
	LaborPower getLaborPower();

	/**
	 * Returns the wage.
	 * @return the wage.
	 */
	long getWage();

	/**
	 * Returns <code>true</code> if the contract is valid, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	boolean isValid();

	/**
	 * Pays the wage to the worker.
	 * @param cheque the cheque for the wage.
	 */
	void payWage(Cheque cheque);

}

// ***
