package jamel.v170804.models.basicModel4.firms;

import jamel.util.Agent;
import jamel.util.Simulation;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.models.basicModel4.interfaces.Cheque;
import jamel.v170804.models.basicModel4.interfaces.Employer;
import jamel.v170804.models.basicModel4.interfaces.Goods;
import jamel.v170804.models.basicModel4.interfaces.LaborContract;
import jamel.v170804.models.basicModel4.interfaces.Supplier;
import jamel.v170804.models.basicModel4.interfaces.Worker;

/**
 * Represents a firm.
 */
interface Firm extends Agent, Employer, Supplier {

	/**
	 * Accepts the specified cheque.
	 * 
	 * @param cheque
	 *            the cheque to be accepted.
	 */
	void accept(Cheque cheque);

	/**
	 * Returns the dataset of the firm.
	 * 
	 * @return the dataset of the firm.
	 */
	AgentDataset getDataset();

	/**
	 * Returns a new job contract for the specified worker.
	 * 
	 * @param worker
	 *            the new employee.
	 * @return a new job contract.
	 */
	LaborContract getNewJobContract(Worker worker);

	@Override
	Simulation getSimulation();

	/**
	 * Returns the specified volume of goods.
	 * 
	 * @param volume
	 *            the volume of goods to be returned.
	 * @return the specified volume of goods.
	 */
	Goods supply(long volume);

}
