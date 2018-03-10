package jamel.models.m18.r01.firms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jamel.Jamel;
import jamel.models.m18.r01.data.PeriodDataset;
import jamel.models.m18.r01.util.BasicAmount;
import jamel.models.util.Commodities;
import jamel.models.util.JobContract;
import jamel.models.util.Worker;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.Parameters;

/**
 * A not-so-basic factory.
 * 
 * The machines break at the end of their time life.
 */
class BasicFactory extends JamelObject {

	/**
	 * A machine.
	 */
	private class Machine {

		/**
		 * The estimated expiration date.
		 */
		final private int estimatedExpiration;

		/**
		 * The date when the machine will brake.
		 */
		final private int expiration;

		/**
		 * The book value of this machine.
		 */
		final private BasicAmount value;

		/**
		 * Creates a new machine.
		 * 
		 * @param input
		 *            the value of the new machine.
		 */
		private Machine(final Commodities input) {
			if (!(input instanceof BasicGoods) || !(BasicFactory.this.qualityOfInputForTheCreationOfANewMachine
					.equals(((BasicGoods) input).getQuality()))) {

			}
			final long valueBefore = input.getValue();
			input.consume(inputVolumeForANewMachine);
			this.value = new BasicAmount(valueBefore - input.getValue());
			this.estimatedExpiration = getPeriod() + machineTimeLife;
			this.expiration = (int) (estimatedExpiration + getRandom().nextGaussian() * machineTimeLifeStDev);
		}

		/**
		 * Creates a new machine.
		 * 
		 * @param value
		 *            the value of the new machine.
		 *
		 * @param expiration
		 *            the date when the machine will brake.
		 * 
		 * @param estimatedExpiration
		 *            the estimation of the date when the machine will brake.
		 */
		private Machine(final long value, final int expiration, final int estimatedExpiration) {
			this.value = new BasicAmount(value);
			this.expiration = expiration;
			this.estimatedExpiration = estimatedExpiration;
		}

		/**
		 * Depreciates the machine and returns the amount of the depreciation.
		 * 
		 * @return the amount of the depreciation.
		 */
		private long depreciate() {
			long machineDepreciation;
			if (getPeriod() > this.expiration || getPeriod() > this.estimatedExpiration) {
				machineDepreciation = this.value.getAmount();
			} else {
				final int remainingTime = 1 + this.estimatedExpiration - getPeriod();
				machineDepreciation = this.value.getAmount() / remainingTime;
			}
			this.value.minus(machineDepreciation);
			return machineDepreciation;
		}

		/**
		 * Returns a new heap of materials.
		 * 
		 * @param contract
		 *            the job contract of the worker.
		 * @param input
		 *            the input materials.
		 * @return a new heap of materials.
		 */
		private Materials work(JobContract contract, Materials input) {
			if (getPeriod() > this.expiration) {
				throw new RuntimeException("This machine is broken");
			}
			final Materials output;
			final int outputStage;
			if (input == null) {
				outputStage = 0;
			} else {
				outputStage = input.getStage() + 1;
			}
			final long volume = BasicFactory.this.productivity * BasicFactory.this.productionProcessLenght;
			output = new Materials(outputStage);
			output.production(contract.getWorker(), contract.getWage(), input, volume);
			return output;
		}

	}

	/**
	 * A heap of unfinished materials.
	 */
	class Materials {

		/**
		 * The period of production of the materials.
		 */
		private int produced = 0;

		/**
		 * The production stage of the materials in this heap.
		 */
		final private int stage;

		/**
		 * The value of the materials in this heap.
		 */
		private long value;

		/**
		 * The volume of the materials in this heap.
		 */
		private long volume;

		/**
		 * Creates a new empty heap of materials.
		 * 
		 * @param stage
		 *            the production stage of the materials in this heap.
		 */
		private Materials(int stage) {
			this.stage = stage;
		}

		/**
		 * Returns the production stage of the materials in this heap.
		 * 
		 * @return
		 * 		the production stage of the materials in this heap.
		 */
		private int getStage() {
			return this.stage;
		}

		/**
		 * Produces new materials. The new materials are added to this heap.
		 * 
		 * @param worker
		 *            the worker.
		 * @param wage
		 *            the wage of the worker.
		 * @param input
		 *            the input materials.
		 * @param production
		 *            the volume of the new materials.
		 */
		private void production(Worker worker, long wage, Materials input, long production) {
			if ((this.stage == 0 && input != null) || (this.stage > 0 && input == null)) {
				throw new RuntimeException("Inconsistency");
			}
			this.produced = getPeriod();
			worker.work();
			this.value += wage;
			this.volume += production;
			if (this.stage > 0) {
				if (input.stage != this.stage - 1) {
					throw new RuntimeException("Inconsistency");
				}
				if (input.produced >= getPeriod()) {
					throw new RuntimeException("The input was produced during the current period");
				}
				if (input.volume < production) {
					throw new RuntimeException("Not enough input");
				}
				final long deltaValue = input.value * production / input.volume;
				this.value += deltaValue;
				input.value -= deltaValue;
				input.volume -= production;
			}
		}

		/**
		 * Puts the specified materials into this heap.
		 * 
		 * @param input
		 *            the materials to be added to this heap.
		 */
		private void put(Materials input) {
			ArgChecks.nullNotPermitted(input, "materials");
			if (input.stage != this.stage) {
				Jamel.println("stages: ", stage, input.stage);
				throw new RuntimeException("Stages are not equal");
			}
			this.volume += input.volume;
			this.value += input.value;
			input.volume = 0;
			input.value = 0;
			this.produced = Math.max(this.produced, input.produced);
		}

		/**
		 * Consumes all materials in this heap.
		 */
		public void consume() {
			if (this.stage != productionProcessLenght - 1) {
				throw new RuntimeException("Unfinished good cannot be consumed");
			}
			this.value = 0;
			this.volume = 0;
		}

		/**
		 * Returns the quality of the goods.
		 * 
		 * @return the quality of the goods.
		 */
		public String getQuality() {
			return BasicFactory.this.goodsQuality;
		}

		/**
		 * Returns the value of the materials in this heap.
		 * 
		 * @return the value of the materials in this heap.
		 */
		public long getValue() {
			return this.value;
		}

		/**
		 * Returns the volume of the materials in this heap.
		 * 
		 * @return the volume of the materials in this heap.
		 */
		public double getVolume() {
			return this.volume;
		}

	}

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = BasicFirmKeys.getInstance();

	/**
	 * The dataset.
	 */
	private PeriodDataset dataset;

	/**
	 * The amount of the depreciation of fixed capital during the present
	 * period.
	 */
	private Long depreciation = null;

	/**
	 * The heap of product.
	 */
	private final BasicGoods finishedGoods;

	/**
	 * The type of goods produced by this factory.
	 */
	final private String goodsQuality;

	/**
	 * Inventories of unfinished and finished goods, in volume.
	 */
	private final Materials[] inProcess;

	/**
	 * The number of goods required to create a new machine.
	 */
	final private int inputVolumeForANewMachine;

	/**
	 * The machines.
	 */
	private final List<Machine> machinery = new LinkedList<>();

	/**
	 * The average time life of the machines.
	 */
	final private int machineTimeLife;

	/**
	 * The standard deviation of the time life of the machines.
	 */
	final private int machineTimeLifeStDev;

	/**
	 * The lenght of the production process.
	 */
	final private int productionProcessLenght;

	/**
	 * The productivity of one machine.
	 */
	final private int productivity;

	/**
	 * The quality of the goods required as input for the creation of a new
	 * machine.
	 */
	final private String qualityOfInputForTheCreationOfANewMachine;

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
		if (parameters == null) {
			throw new IllegalArgumentException("parameters is null");
		}
		final int capacity = parameters.getIntAttribute("capacity");
		this.productivity = parameters.getIntAttribute("productivity");
		this.productionProcessLenght = parameters.getIntAttribute("processLenght");
		this.goodsQuality = parameters.getString("quality");
		this.finishedGoods = new BasicGoods(this.goodsQuality);
		this.qualityOfInputForTheCreationOfANewMachine = parameters.getString("machines.creation.input.quality");
		this.machineTimeLife = parameters.getInt("machines.timeLife.mean");
		this.machineTimeLifeStDev = parameters.getInt("machines.timeLife.stDev");
		this.inputVolumeForANewMachine = parameters.getInt("machines.creation.input.volume");
		// ***
		for (int i = 0; i < capacity; i++) {
			final int expiration0 = (int) (-getRandom().nextInt(machineTimeLife) + machineTimeLife
					+ getRandom().nextGaussian() * machineTimeLifeStDev);
			final int expiration = (expiration0 < 0)
					? (int) (expiration0 + machineTimeLife + getRandom().nextGaussian() * machineTimeLifeStDev)
					: expiration0;
			final Machine machine = new Machine(0, expiration, expiration);
			this.machinery.add(machine);
		}
		this.inProcess = new Materials[productionProcessLenght];
		for (int i = 0; i < this.inProcess.length; i++) {
			this.inProcess[i] = new Materials(i);
		}
	}

	/**
	 * Depreciates the machines.
	 */
	public void depreciation() {
		final Iterator<Machine> it = this.machinery.iterator();
		if (this.depreciation != null) {
			throw new RuntimeException("Depreciation should be null at this point.");
		}
		int broken = 0;
		this.depreciation = 0l;
		while (it.hasNext()) {
			final Machine machine = it.next();
			this.depreciation += machine.depreciate();
			if (getPeriod() > machine.expiration) {
				it.remove();
				broken++;
			}
		}
		this.dataset.put(keys.brokenMachines, broken);
		this.dataset.put(keys.depreciation, this.depreciation);
	}

	/**
	 * Expands the capacity.
	 * 
	 * @param size
	 *            the number of new machines to be created.
	 * @param input
	 *            the input that will be consumed in the process of machine
	 *            creation.
	 */
	public void expandCapacity(int size, Commodities input) {
		for (int i = 0; i < size; i++) {
			final Machine machine = new Machine(input);
			this.machinery.add(machine);
		}
	}

	/**
	 * Returns the capacity of the firm.
	 * 
	 * @return the capacity of the firm.
	 */
	public int getCapacity() {
		return this.machinery.size();
	}

	/**
	 * Returns the value of the depreciation since the start of the period.
	 * 
	 * @return the value of the depreciation since the start of the period.
	 */
	public long getDepreciation() {
		return this.depreciation;
	}

	/**
	 * Returns the value of in process stuff.
	 * 
	 * @return the value of in process stuff.
	 */
	public long getInProcessValue() {
		long result = 0;
		for (int i = 0; i < this.inProcess.length; i++) {
			result += this.inProcess[i].value;
		}
		return result;
	}

	/**
	 * Returns the input volume required for a new machine.
	 * 
	 * @return the input volume required for a new machine.
	 */
	public int getInputVolumeForANewMachine() {
		return this.inputVolumeForANewMachine;
	}

	/**
	 * Returns the inventories of finished goods.
	 * 
	 * @return the inventories of finished goods.
	 */
	public Commodities getInventories() {
		return this.finishedGoods;
	}

	/**
	 * Returns the value of the machines.
	 * 
	 * @return the value of the machines.
	 */
	public long getMachineryValue() {
		long result = 0;
		for (final Machine machine : this.machinery) {
			result += machine.value.getAmount();
		}
		return result;
	}

	/**
	 * Returns the machine average time life.
	 * 
	 * @return the machine average time life.
	 */
	public int getMachineTimeLife() {
		return this.machineTimeLife;
	}

	/**
	 * Returns the volume of the production at full capacity.
	 * 
	 * @return the volume of the production at full capacity.
	 */
	public double getProductionAtFullCapacity() {
		return this.getCapacity() * this.getProductivity();
	}

	/**
	 * Returns the productivity.
	 * 
	 * @return the productivity.
	 */
	public double getProductivity() {
		return this.productivity;
	}

	/**
	 * Returns the quality of the input (investment goods) required to create a
	 * new machine.
	 * 
	 * @return the quality of the input (investment goods) required to create a
	 *         new machine.
	 */
	public String getQualityOfInputForTheCreationOfANewMachine() {
		return this.qualityOfInputForTheCreationOfANewMachine;
	}

	/**
	 * Computes and returns the current value of the factory.
	 * 
	 * @return the current value of the factory.
	 */
	public long getValue() {
		return this.finishedGoods.getValue() + getInProcessValue() + getMachineryValue();
	}

	/**
	 * Returns the wagebill of the period.
	 * 
	 * @return the wagebill of the period.
	 */
	public Long getWageBill() {
		return this.wageBill;
	}

	/**
	 * Opens the factory at the beginning of the period.
	 * 
	 * @param periodDataset
	 *            the period dataset.
	 */
	public void open(PeriodDataset periodDataset) {
		this.wageBill = null;
		this.depreciation = null;
		this.dataset = periodDataset;
	}

	/**
	 * Executes the production process.
	 * 
	 * @param contracts
	 *            the list of the job contracts of the employees.
	 */
	public void production(final List<? extends JobContract> contracts) {

		if (this.wageBill != null) {
			throw new RuntimeException("Inconsistency");
			// On vérifie ainsi que la méthode n'a pas encore été appelée.
		}

		// TODO vérifier le fonctionnement de cette méthode en la traçant.
		if (contracts.size() > this.machinery.size()) {
			throw new RuntimeException("Too many workers");
		}

		final double volume = this.productivity * this.productionProcessLenght;

		int stage = this.productionProcessLenght;

		this.wageBill = 0l;
		final ListIterator<? extends JobContract> workforceIterator = contracts.listIterator();
		final ListIterator<? extends Machine> machineryIterator = this.machinery.listIterator();

		final Materials[] newMaterials = new Materials[productionProcessLenght];
		while (workforceIterator.hasNext()) {
			final JobContract contract = workforceIterator.next();
			final Machine machine = machineryIterator.next();
			while (true) {
				if (stage == 1 || this.inProcess[stage - 2].getVolume() >= volume) {
					final Materials input;
					if (stage == 1) {
						input = null;
					} else {
						input = this.inProcess[stage - 2];
					}
					final Materials product = machine.work(contract, input);

					if (newMaterials[stage - 1] == null) {
						newMaterials[stage - 1] = product;
					} else {
						newMaterials[stage - 1].put(product);
					}
					this.wageBill += contract.getWage();
					break;
				}
				stage--;
			}
			if (stage == 1) {
				stage = this.productionProcessLenght;
			} else {
				stage--;
			}
		}

		// Compilation des productions de la période avec les productions des
		// périodes antérieures.

		for (stage = 0; stage < this.productionProcessLenght; stage++) {
			// ***
			// Jamel.println("Stage", stage);
			// ***
			if (newMaterials[stage] != null) {
				this.inProcess[stage].put(newMaterials[stage]);
			}
		}

		this.dataset.put(keys.productionValue, this.inProcess[this.productionProcessLenght - 1].getValue());
		this.dataset.put(keys.productionVolume, this.inProcess[this.productionProcessLenght - 1].getVolume());
		this.dataset.put(keys.capacity, this.machinery.size());
		this.finishedGoods.put(this.inProcess[this.productionProcessLenght - 1]);
	}

}
