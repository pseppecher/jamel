package jamel.basicModel.firms;

import jamel.basicModel.households.Worker;
import jamel.util.JamelObject;
import jamel.util.Simulation;

/**
 * A basic labor contract.
 */
class BasicLaborContract extends JamelObject implements LaborContract {

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
		super(simulation);
		this.employee = employee;
		this.wage = wage;
		this.end = this.getPeriod() + duration;
		this.employer = employer;
	}

	/**
	 * Breaches this contract.
	 */
	void breach() {
		if (!isValid()) {
			throw new RuntimeException("Invalid contract");
		}
		this.end = this.getPeriod();
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
		return (this.getPeriod() >= this.end);
	}

}
