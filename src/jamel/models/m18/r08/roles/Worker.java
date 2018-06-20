package jamel.models.m18.r08.roles;

import jamel.models.m18.r08.util.AccountHolder;
import jamel.models.m18.r08.util.Cheque;
import jamel.models.m18.r08.util.JobOffer;

/**
 * Represents a worker.
 */
public interface Worker extends AccountHolder {

	/**
	 * Receives its pay cheque.
	 * 
	 * @param cheque
	 *            the pay cheque.
	 */
	void acceptPayCheque(Cheque cheque);

	/**
	 * Adds a new job offer (called by an employer).
	 * 
	 * @param jobOffer
	 *            the new job offer.
	 */
	void addJobOffer(JobOffer jobOffer);

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

	/**
	 * Chooses a job among the received job offers.
	 */
	public void chooseJob();

}
