package jamel.models.util;

/**
 * Represents a worker.
 */
public interface Worker extends AccountHolder {

	/**
	 * Returns {@code true} if the worker accepts the job, {@code false}
	 * otherwise.
	 * 
	 * @param jobOffer
	 *            the job offer.
	 * @return {@code true} if the worker accepts the job, {@code false}
	 *         otherwise.
	 */
	boolean acceptJob(JobOffer jobOffer);

	/**
	 * Receives its pay cheque.
	 * 
	 * @param cheque
	 *            the pay cheque.
	 */
	void acceptPayCheque(Cheque cheque);

	/**
	 * Returns {@code true} if this worker is employed.
	 * 
	 * @return {@code true} if this worker is employed.
	 */
	boolean isEmployed();

	/**
	 * Works.
	 */
	void work();

}
