package jamel.v170801.basicModel0.firms;

import java.util.List;

import jamel.Jamel;
import jamel.util.JamelObject;
import jamel.v170801.data.AgentDataset;
import jamel.v170801.util.BasicTimer;
import jamel.v170801.util.Timer;

/**
 * A very basic factory.
 */
class BasicFactory extends JamelObject implements Factory {

	/**
	 * The number of machines.
	 */
	private final int capacity = 10;

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
	private final int[] materials = new int[this.productionProcessLenght];

	/**
	 * A flag that indicates whether this factory is open or not.
	 */
	private boolean open = false;

	/**
	 * Length of the production process.
	 */
	private final int productionProcessLenght = 6;

	/**
	 * The productivity of one worker/machine.
	 */
	private final int productivity = 100;

	/**
	 * The timer of the factory.
	 */
	private final Timer timer;

	/**
	 * Inventories of unfinished and finished goods, in value.
	 */
	private final long[] values = new long[this.productionProcessLenght];

	/**
	 * Creates a new basic factory.
	 * 
	 * @param firm
	 *            the parent firm.
	 */
	public BasicFactory(BasicFirm firm) {
		super(((JamelObject) firm).getSimulation());
		this.dataset = firm.getDataset();
		this.timer = new BasicTimer(this.getSimulation().getPeriod());
	}

	/**
	 * Closes the factory at the end of the period.
	 */
	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;
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
		if (this.open) {
			throw new RuntimeException("Already open.");
		}

		this.timer.next();
		if (this.timer.getValue() != this.getPeriod()) {
			throw new RuntimeException("Not synchronized.");
		}

		if (!this.finishedGoods.isEmpty()) {
			throw new RuntimeException("The heap of product should be empty at the beginning of the period.");

			// TODO Ah ben ça alors ! Ca m'étonnerait fort !
		}

		this.open = true;
	}

	@Override
	public void production(final List<? extends LaborContract> contracts) {

		// Encapsulates the production process.

		// TODO vérifier (1) factory is open (2) used only once by period
		// TODO vérifier le fonctionnement de cette méthode en la traçant.
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

		this.dataset.put("productionValue", this.values[this.productionProcessLenght - 1]);
		this.dataset.put("productionVolume", this.materials[this.productionProcessLenght - 1]);
		this.finishedGoods.add(this.materials[this.productionProcessLenght - 1],
				this.values[this.productionProcessLenght - 1]);
		this.materials[this.productionProcessLenght - 1] = 0;
		this.values[this.productionProcessLenght - 1] = 0;

	}

}
