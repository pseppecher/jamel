package jamel.jamel.widgets;

import jamel.jamel.roles.Worker;

/**
 * Represents a job offer.
 */
public interface JobOffer {

	/**
	 * Submits the application of the specified worker. 
	 * @param applicant the applicant.
	 * @return the job contract.
	 */
	JobContract apply(Worker applicant);

	/**
	 * Returns the name of the employer.
	 * @return the name of the employer.
	 */
	Object getEmployerName();

	/**
	 * Returns the wage.
	 * @return the wage.
	 */
	long getWage();

}

// ***
