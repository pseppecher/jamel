package jamel.jamel.firms.managers;

import jamel.basic.data.AgentDataset;
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
	 * Returns the dataset of this manager.
	 * @return an {@link AgentDataset}.
	 */
	AgentDataset getData();

	/**
	 * Returns the current {@linkplain JobOffer}.
	 * @return a {@link JobOffer}.
	 */
	JobOffer getJobOffer();

	/**
	 * Returns an array containing all of the labor powers of the workforce. 
	 * @return an array of {@linkplain LaborPower}.
	 */
	LaborPower[] getLaborPowers();

	/**
	 * Returns the payroll (= the future wage bill).
	 * @return a long that represents the payroll.
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
	 * Pays the workers in the workforce.
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