package jamel.v170804.models.basicModel1.firms;

import jamel.v170804.models.basicModel1.households.Worker;

/**
 * Represents a labor contract.
 */
public interface LaborContract {

	/**
	 * Returns the employer.
	 * 
	 * @return the employer.
	 */
	Employer getEmployer();

	/**
	 * Returns the wage.
	 * 
	 * @return the wage.
	 */
	long getWage();

	/**
	 * Returns the worker.
	 * 
	 * @return the worker.
	 */
	Worker getWorker();

	/**
	 * Returns <code>true</code> if the contract is valid.
	 * 
	 * @return <code>true</code> if the contract is valid.
	 */
	boolean isValid();

}
