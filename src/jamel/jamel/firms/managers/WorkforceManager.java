package jamel.jamel.firms.managers;

import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.LaborPower;

/**
 * The workforce manager.
 */
public interface WorkforceManager {

	/**
	 * Closes the manager.
	 */
	void close();

	/**
	 * Returns the average wage paid of the current workforce.
	 * @return the average wage paid of the current workforce.
	 */
	Double getAverageWage();

	/**
	 * Returns the job offer.
	 * @return the job offer.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns an array containing all of the labor powers of the workforce. 
	 * @return an array of labor powers.
	 */
	LaborPower[] getLaborPowers();

	/**
	 * Returns the payroll (= the future wage bill).
	 * @return the payroll (= the future wage bill).
	 */
	long getPayroll();

	/**
	 * Returns the wage.
	 * @return the wage.
	 */
	Double getWage();

	/**
	 * Returns the wageBill of the period.
	 * @return the wageBill of the period.
	 */
	long getWageBill();

	/**
	 * Returns the size of the workforce (the number of employees)
	 * @return the size of the workforce (the number of employees)
	 */
	int getWorkforceSize();

	/**
	 * Layoffs all the workforce.
	 */
	void layoff();

	/**
	 * Pays the workers.
	 */
	void payWorkers();

	/**
	 * Updates the workforce according to the current production target.
	 */
	void updateWorkforce();

	/**
	 * Updates the wage.
	 */
	public void updateWage();

}