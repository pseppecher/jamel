package jamel.v170804.models.basicModel4.firms;

import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.v170804.models.basicModel4.interfaces.Employer;
import jamel.v170804.models.basicModel4.interfaces.JobOffer;
import jamel.v170804.models.basicModel4.interfaces.LaborContract;
import jamel.v170804.models.basicModel4.interfaces.Worker;

/**
 * A basic job offer.
 */
class BasicJobOffer extends JamelObject implements JobOffer {

	/**
	 * The employer.
	 */
	private final Firm employer;

	/**
	 * The hirings.
	 */
	private int enrollment = 0;

	/**
	 * The size of the job offer (= the initial number of vacancies, before the matching process).
	 */
	private int initialVacancies = 0;

	/**
	 * The current number of vacancies.
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
	BasicJobOffer(final Firm employer) {
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
		this.vacancies = 0;
		this.initialVacancies = 0;
		this.enrollment=0;
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
	 * Returns the initial number of vacancies.
	 * 
	 * @return the initial number of vacancies
	 */
	double getInitialVacancies() {
		return this.initialVacancies;
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
		this.initialVacancies = size;
		this.vacancies = size;
	}

	/**
	 * Sets the wage.
	 * 
	 * @param wage
	 *            the new wage.
	 */
	void setWage(long wage) {
		ArgChecks.negativeOr0NotPermitted(wage, "wage");
		this.wage = wage;
	}

	@Override
	public LaborContract apply(final Worker worker) {
		this.checkOpen();
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
