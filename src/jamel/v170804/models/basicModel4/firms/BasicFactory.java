package jamel.v170804.models.basicModel4.firms;

import java.util.List;

import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.v170804.data.AgentDataset;
import jamel.v170804.models.basicModel4.interfaces.Goods;
import jamel.v170804.models.basicModel4.interfaces.LaborContract;

/**
 * A very basic factory.
 */
class BasicFactory extends JamelObject implements Factory {

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = BasicFirmKeys.getInstance();

	/**
	 * The number of machines.
	 */
	private final int capacity;

	/**
	 * The dataset.
	 */
	private final AgentDataset dataset;

	/**
	 * The heap of product.
	 */
	private final BasicGoods finishedGoods = new BasicGoods();

	/**
	 * Inventories of unfinished and finished goods, in volume.
	 */
	private final int[] materials;

	/**
	 * Length of the production process.
	 */
	private final int productionProcessLenght;

	/**
	 * The productivity of one worker/machine.
	 */
	private final double productivity;

	/**
	 * Inventories of unfinished and finished goods, in value.
	 */
	private final long[] values;

	/**
	 * The total amount paid in wages in the current period.
	 */
	private Long wageBill = null;

	/**
	 * Creates a new basic factory.
	 * 
	 * @param parameters
	 *            the parameters of the firm.
	 * @param firm
	 *            the parent firm.
	 */
	public BasicFactory(final Parameters parameters, final Firm firm) {
		super(((JamelObject) firm).getSimulation());
		this.dataset = firm.getDataset();
		this.capacity = parameters.getIntAttribute("capacity");
		this.productivity = parameters.getIntAttribute("productivity");
		this.productionProcessLenght = parameters.getIntAttribute("processLenght");
		this.values = new long[this.productionProcessLenght];
		this.materials = new int[this.productionProcessLenght];
	}

	/**
	 * Closes the factory at the end of the period.
	 */
	@Override
	public void close() {
		super.close();
	}

	@Override
	public int getCapacity() {
		return this.capacity;
	}

	@Override
	public Goods getInventories() {
		return this.finishedGoods;
	}

	@Override
	public double getProductionAtFullCapacity() {
		return this.getCapacity() * this.getProductivity() ;
	}

	@Override
	public double getProductivity() {
		return this.productivity;
	}

	@Override
	public long getValue() {
		long result = this.finishedGoods.getValue();
		for (int i = 0; i < this.values.length; i++) {
			result += this.values[i];
		}
		return result;
	}

	@Override
	public Long getWageBill() {
		return this.wageBill;
	}

	/**
	 * Opens the factory at the beginning of the period.
	 */
	@Override
	public void open() {
		super.open();
		this.wageBill = null;
	}

	@Override
	public void production(final List<? extends LaborContract> contracts) {

		this.checkOpen();

		// Encapsulates the production process.

		// TODO vérifier (1) factory is used only once by period
		// TODO vérifier (2) le fonctionnement de cette méthode en la traçant.
		if (contracts.size() > this.capacity) {
			throw new RuntimeException("Too many workers");
		}
		final int[] newMaterials = new int[this.productionProcessLenght];
		final long[] newValues = new long[this.productionProcessLenght];

		final double volume = this.productivity * this.productionProcessLenght;

		int stage = this.productionProcessLenght - 1;

		if (this.wageBill != null) {
			throw new RuntimeException("Inconsistency");
		}
		this.wageBill = 0l;
		for (final LaborContract contract : contracts) {
			while (true) {
				if (stage == 0 || this.materials[stage - 1] >= volume) {
					contract.getWorker().work();
					this.wageBill += contract.getWage();
					if (stage != 0) {
						final long valueTransfered = (long) (this.values[stage - 1] * volume
								/ this.materials[stage - 1]);
						this.values[stage - 1] -= valueTransfered;
						newValues[stage] += valueTransfered;
						this.materials[stage - 1] -= volume;
					}
					newMaterials[stage] += volume;
					newValues[stage] += contract.getWage();
					break;
				}
				stage--;
			}
			stage--;
			if (stage == -1) {
				stage = this.productionProcessLenght - 1;
			}
		}

		// Compilation des productions de la période avec les productions des
		// périodes antérieures.

		for (stage = 0; stage < this.productionProcessLenght; stage++) {
			this.materials[stage] += newMaterials[stage];
			this.values[stage] += newValues[stage];
		}

		this.dataset.put(keys.productionValue, this.values[this.productionProcessLenght - 1]);
		this.dataset.put(keys.productionVolume, this.materials[this.productionProcessLenght - 1]);
		this.dataset.put(keys.capacity, this.capacity);
		this.finishedGoods.add(this.materials[this.productionProcessLenght - 1],
				this.values[this.productionProcessLenght - 1]);
		this.materials[this.productionProcessLenght - 1] = 0;
		this.values[this.productionProcessLenght - 1] = 0;

	}

}
