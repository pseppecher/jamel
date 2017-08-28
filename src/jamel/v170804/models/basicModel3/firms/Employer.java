package jamel.v170804.models.basicModel3.firms;

/**
 * Represents an employer.
 */
public interface Employer {

	/**
	 * Returns the job offer of this employer.
	 * 
	 * @return the job offer of this employer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns <code>true</code> if open.
	 * 
	 * @return <code>true</code> if open.
	 */
	boolean isOpen();

}
