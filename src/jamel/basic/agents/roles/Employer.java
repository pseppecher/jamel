package jamel.basic.agents.roles;

import jamel.basic.util.JobOffer;

/**
 * Represents an employer.
 */
public interface Employer extends Agent {

	/**
	 * Returns the job offer of this employer, or <code>null</code> if this employer has no vacancies available.
	 * @return a JobOffer.
	 */
	JobOffer getJobOffer();

}

//***
