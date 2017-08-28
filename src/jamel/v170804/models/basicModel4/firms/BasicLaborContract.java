package jamel.v170804.models.basicModel4.firms;

import jamel.util.ArgChecks;
import jamel.util.Simulation;
import jamel.v170804.models.basicModel4.interfaces.Employer;
import jamel.v170804.models.basicModel4.interfaces.LaborContract;
import jamel.v170804.models.basicModel4.interfaces.Worker;

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
	 * The start of this contract.
	 */
	final private int start;

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
	BasicLaborContract(final Simulation simulation, final Employer employer, final Worker employee, long wage, int duration) {
		ArgChecks.nullNotPermitted(simulation , "simulation");
		this.simulation = simulation;
		ArgChecks.nullNotPermitted(employee , "employee");
		this.employee = employee;
		ArgChecks.negativeOr0NotPermitted(wage , "wage");
		this.wage = wage;
		ArgChecks.negativeOr0NotPermitted(duration , "duration");
		this.start = this.simulation.getPeriod();
		this.end = this.start + duration;
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
	public int getStart() {
		return this.start;
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
		return (this.simulation.getPeriod() < this.end);
	}

}
