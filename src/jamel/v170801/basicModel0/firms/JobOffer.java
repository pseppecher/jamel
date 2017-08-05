package jamel.v170801.basicModel0.firms;

import jamel.v170801.basicModel0.households.Worker;

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
	public LaborContract apply(Worker applicant);

	/**
	 * Returns the employer.
	 * @return the employer.
	 */
	public Employer getEmployer();

	/**
	 * Returns the wage offered.
	 * 
	 * @return the wage offered.
	 */
	public long getWage();
	
	/**
	 * Returns <code>true</code> if this offer is empty.
	 * 
	 * @return <code>true</code> if this offer is empty.
	 */
	public boolean isEmpty();

}
