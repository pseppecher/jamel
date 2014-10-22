package jamel.basic.util;

import jamel.basic.agents.roles.Worker;

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
	 * Returns the number of vacancies.
	 * @return the number of vacancies.
	 */
	int getVacancies();
	
	/**
	 * Returns the wage.
	 * @return the wage.
	 */
	long getWage();

}

// ***
