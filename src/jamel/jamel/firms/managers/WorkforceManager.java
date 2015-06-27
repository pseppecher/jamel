package jamel.jamel.firms.managers;

import jamel.basic.agent.AgentDataset;
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
	 * Returns the dataset of the manager.
	 * @return the dataset of the manager.
	 */
	AgentDataset getData();

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
	 * Layoffs all the workforce.
	 */
	void layoff();

	/**
	 * Opens the manager.
	 */
	void open();

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