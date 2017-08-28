package jamel.v170804.models.basicModel3.firms;

import jamel.util.Agent;
import jamel.util.Simulation;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.models.basicModel3.banks.Cheque;
import jamel.v170804.models.basicModel3.households.Worker;

/**
 * Represents a firm.
 */
interface Firm extends Agent, Employer, Supplier {

	AgentDataset getDataset();

	LaborContract getNewJobContract(Worker worker);

	Simulation getSimulation();

	Goods supply(long purchase);

	void accept(Cheque cheque);

}
