package jamel.jamel.aggregates;

import jamel.jamel.widgets.JobOffer;

/**
 * The employer sector.
 */
public interface Employers {

	/**
	 * Returns a sample of job offers.
	 * @param size the sample to be returned.
	 * @return an array that contains the job offers.
	 */
	JobOffer[] getJobOffers(int size);

}

// ***
