package jamelV3.jamel.roles;

import jamelV3.basic.agent.Agent;
import jamelV3.jamel.widgets.JobOffer;

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
