package jamelV3.jamel.sectors;

import jamelV3.jamel.widgets.JobOffer;

/**
 * The employer sector.
 */
public interface EmployerSector {

	/**
	 * Returns a sample of job offers.
	 * @param size the sample to be returned.
	 * @return an array that contains the job offers.
	 */
	JobOffer[] getJobOffers(int size);

}

// ***
