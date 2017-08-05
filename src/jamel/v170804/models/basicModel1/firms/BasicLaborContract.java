package jamel.v170804.models.basicModel1.firms;

import jamel.util.Simulation;
import jamel.v170804.models.basicModel1.households.Worker;

/**
 * A basic labor contract.
 */
class BasicLaborContract implements LaborContract {

	/**
	 * The employee.
	 */
	final private Worker employee;

	/**
	 * The employer.
	 */
	final private Employer employer;

	/**
	 * The end of this contract.
	 */
	private int end;

	/**
	 * The simulation.
	 */
	private Simulation simulation;

	/**
	 * The wage paid to the employee.
	 */
	final private long wage;

	/**
	 * Creates a new employment contract.
	 * 
	 * @param simulation
	 *            the simulation.
	 * @param employer
	 *            the employer.
	 * @param employee
	 *            the employee.
	 * @param wage
	 *            the wage.
	 * @param duration
	 *            the duration.
	 */
	BasicLaborContract(Simulation simulation, Employer employer, Worker employee, long wage, int duration) {
		this.simulation = simulation;
		this.employee = employee;
		this.wage = wage;
		this.end = this.simulation.getPeriod() + duration;
		this.employer = employer;
	}

	/**
	 * Breaches this contract.
	 */
	void breach() {
		if (!isValid()) {
			throw new RuntimeException("Invalid contract");
		}
		this.end = this.simulation.getPeriod();
	}

	@Override
	public Employer getEmployer() {
		return this.employer;
	}

	@Override
	public long getWage() {
		return this.wage;
	}

	@Override
	public Worker getWorker() {
		return this.employee;
	}

	@Override
	public boolean isValid() {
		return (this.simulation.getPeriod() >= this.end);
	}

}
