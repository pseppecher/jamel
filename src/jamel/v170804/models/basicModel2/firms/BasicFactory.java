package jamel.v170804.models.basicModel2.firms;

import java.util.List;

import jamel.Jamel;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.v170804.data.AgentDataset;

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
	private final int productivity;

	/**
	 * Inventories of unfinished and finished goods, in value.
	 */
	private final long[] values;

	/**
	 * Creates a new basic factory.
	 * 
	 * @param parameters
	 *            the parameters of the firm.
	 * @param firm
	 *            the parent firm.
	 */
	public BasicFactory(final Parameters parameters, final BasicFirm firm) {
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
	public int getProductivity() {
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

	/**
	 * Opens the factory at the beginning of the period.
	 */
	@Override
	public void open() {
		super.open();
	}

	@Override
	public void production(final List<? extends LaborContract> contracts) {

		this.checkOpen();

		// Encapsulates the production process.

		// TODO vérifier (1) factory is used only once by period
		// TODO vérifier (2) le fonctionnement de cette méthode en la traçant.
		if (contracts.size() > this.capacity) {
			Jamel.println();
			Jamel.println("contracts.size() == " + contracts.size());
			Jamel.println("this.capacity == " + this.capacity);
			Jamel.println();
			throw new RuntimeException("Overcapacity");
		}
		final int[] newMaterials = new int[this.productionProcessLenght];
		final long[] newValues = new long[this.productionProcessLenght];

		int stage = this.productionProcessLenght - 1;
		for (final LaborContract contract : contracts) {
			while (true) {
				if (stage == 0 || this.materials[stage - 1] >= this.productivity) {
					contract.getWorker().work();
					if (stage != 0) {
						final long valueTransfered = this.values[stage - 1] * this.productivity
								/ this.materials[stage - 1];
						this.values[stage - 1] -= valueTransfered;
						newValues[stage] += valueTransfered;
						this.materials[stage - 1] -= this.productivity;
					}
					newMaterials[stage] += this.productivity;
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
