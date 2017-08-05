package jamel.v170804.models.basicModel0.firms;

import jamel.util.JamelObject;
import jamel.v170804.models.basicModel0.households.Worker;

/**
 * A basic job offer.
 */
class BasicJobOffer extends JamelObject implements JobOffer {

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
		super(employer.getSimulation());
		this.employer = employer;
	}

	/**
	 * Closes this job offer. Must be called at the end of the period.
	 */
	@Override
	protected void close() {
		super.close();
	}

	/**
	 * Opens this job offer. Must be called at the beginning of the period.
	 */
	@Override
	protected void open() {
		this.wage = 0;
		this.vacancies = 0;
		super.open();
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
