package jamel.models.m18.r08.util;

import jamel.models.m18.r08.roles.Worker;

/**
 * Represents the job offer of an employer.
 */
public interface JobOffer {

	/**
	 * Submits the application of the specified worker.
	 * 
	 * @param applicant
	 *            the applicant.
	 * @return the job contract.
	 */
	public JobContract apply(Worker applicant);

	/**
	 * Returns the wage offered.
	 * 
	 * @return the wage offered.
	 */
	public long getWage();

	/**
	 * Returns {@code true} if this offer is empty.
	 * 
	 * @return {@code true} if this offer is empty.
	 */
	public boolean isEmpty();

	/**
	 * Returns the number of vacancies.
	 * 
	 * @return the number of vacancies.
	 */
	public int size();

}
