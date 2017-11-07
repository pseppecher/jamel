package jamel.models.util;

/**
 * Represents a labor contract.
 */
public interface JobContract {

	/**
	 * Breaks the contract.
	 */
	void breach();

	/**
	 * Returns the starting period of this contract.
	 * 
	 * @return the starting period of this contract.
	 */
	int getStart();

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
	 * Returns {@code true} if the contract is valid, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the contract is valid, {@code false}
	 *         otherwise.
	 */
	boolean isValid();

}
