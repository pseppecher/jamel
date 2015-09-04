package jamel.jamel.firms.managers;

import jamel.basic.util.Timer;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;

/**
 * The workforce manager.
 */
public abstract class WorkforceManager extends AbstractManager{
	
	/**
	 * Creates a new workforce manager.
	 * @param name the name of the manager.
	 * @param timer the timer.
	 */
	public WorkforceManager(String name, Timer timer) {
		super(name, timer);
	}

	/**
	 * Returns the current {@linkplain JobOffer}.
	 * @return a {@link JobOffer}.
	 */
	public abstract JobOffer getJobOffer();

	/**
	 * Returns an array containing all of the labor powers of the workforce. 
	 * @return an array of {@linkplain LaborPower}.
	 */
	public abstract LaborPower[] getLaborPowers();

	/**
	 * Returns the payroll (= the future wage bill).
	 * @return a long that represents the payroll.
	 */
	public abstract long getPayroll();

	/**
	 * Layoffs all the workforce.
	 */
	public abstract void layoff();

	/**
	 * Pays the workers in the workforce.
	 */
	public abstract void payWorkers();

	/**
	 * Updates the wage.
	 */
	public abstract void updateWage();

	/**
	 * Updates the workforce according to the current production target.
	 */
	public abstract void updateWorkforce();

}

// ***