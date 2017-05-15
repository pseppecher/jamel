package jamel.basicModel.firms;

import jamel.basicModel.households.Worker;

/**
 * A basic job offer.
 */
class BasicJobOffer implements JobOffer {

	/**
	 * The employer.
	 */
	private final BasicFirm employer;

	/**
	 * The hirings.
	 */
	private int enrollment = 0;

	/**
	 * The size.
	 */
	private int vacancies = 0;

	/**
	 * The wage.
	 */
	private long wage = 0;

	/**
	 * Creates a new job offer.
	 * 
	 * @param employer
	 *            the employer.
	 */
	BasicJobOffer(final BasicFirm employer) {
		this.employer = employer;
	}

	/**
	 * Clears this job offer.
	 */
	void reset() {
		this.wage = 0;
		this.vacancies = 0;
	}

	/**
	 * Returns the enrollment (ie, the number of hirings for the period).
	 * 
	 * @return the enrollment.
	 */
	double getEnrollment() {
		return this.enrollment;
	}

	/**
	 * Returns the size of this job offer (ie, the number of vacancies).
	 * 
	 * @return the size of this job offer.
	 */
	double getVacancies() {
		return this.vacancies;
	}

	/**
	 * Sets the size of the offer.
	 * 
	 * @param size
	 *            the new size.
	 */
	void setVacancies(int size) {
		this.vacancies = size;
	}

	/**
	 * Sets the wage.
	 * 
	 * @param wage
	 *            the new wage.
	 */
	void setWage(long wage) {
		this.wage = wage;
	}

	@Override
	public LaborContract apply(final Worker worker) {
		if (!this.employer.isOpen()) {
			throw new RuntimeException("Closed.");
		}
		if (vacancies == 0) {
			throw new RuntimeException("Empty.");
		}
		this.vacancies--;
		this.enrollment++;
		return this.employer.getNewJobContract(worker);
	}

	@Override
	public Employer getEmployer() {
		return this.employer;
	}

	@Override
	public long getWage() {
		return this.wage;
	}

	/**
	 * Returns <code>true</code> if this offer is empty.
	 * 
	 * @return <code>true</code> if this offer is empty.
	 */
	@Override
	public boolean isEmpty() {
		return vacancies == 0;
	}

}
