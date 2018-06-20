package jamel.models.m18.r08.firms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jamel.Jamel;
import jamel.models.m18.r08.data.PeriodDataset;
import jamel.models.m18.r08.roles.Worker;
import jamel.models.m18.r08.util.BasicAmount;
import jamel.models.m18.r08.util.Commodities;
import jamel.models.m18.r08.util.JobContract;
import jamel.util.ArgChecks;
import jamel.util.JamelObject;
import jamel.util.Parameters;
import jamel.util.Simulation;

/*
 * 2018-21-03: jamel/models/m18/r03/firms/BasicFactory.java
 * modifiée pour intégrer Technology.
 */
/**
 * A not-so-basic factory.
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
		 * The productivity of this machine.
		 */
		final private int productivity;

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
			if (!(input instanceof BasicGoods) || !(technology.getQualityOfInputForTheCreationOfANewMachine()
					.equals(((BasicGoods) input).getQuality()))) {
				throw new RuntimeException("Bad input");
			}
			final long valueBefore = input.getValue();
			input.consume(technology.getInputVolumeForANewMachine());
			this.value = new BasicAmount(valueBefore - input.getValue());
			this.estimatedExpiration = getPeriod() + technology.getMachineTimeLife();
			this.expiration = (int) (estimatedExpiration
					+ getRandom().nextGaussian() * technology.getMachineTimeLifeStDev());
			this.productivity = technology.getProductivity();
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
			this.productivity = technology.getProductivity();
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
			final long volume = this.productivity * productionProcessLenght;
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
	private static final FirmKeys keys = FirmKeys.getInstance();

	/**
	 * The dataset.
	 */
	private PeriodDataset dataset;

	/**
	 * The amount of the depreciation of fixed capital and inventories during
	 * the present period.
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
	 * Stock de biens d'investissement destiné aux futures machines.
	 */
	private final Commodities inputForNewMachines;

	/**
	 * The number of new machines created since the start of the period.
	 */
	private int investmentSize = 0;

	/**
	 * The machines.
	 */
	private final LinkedList<Machine> machinery = new LinkedList<>();

	/**
	 * TODO should be a parameter.
	 */
	final private float overHeadRatio = 0.0f;

	/**
	 * The total amount paid in wages for overhead labour in the current period.
	 */
	private Long overheadWageBill;

	/**
	 * The volume of the production at full capacity.
	 */
	private double productionAtFullCapacity;

	/**
	 * The lenght of the production process.
	 */
	final private int productionProcessLenght;

	/**
	 * The technology.
	 */
	final private BasicTechnology technology;

	/**
	 * The total amount paid in wages in the current period.
	 */
	private Long wageBill = null;

	/**
	 * Creates a new basic factory.
	 * 
	 * @param parameters
	 *            the parameters of the firm.
	 * @param simulation
	 *            the simulation.
	 */
	public BasicFactory(final Parameters parameters, final Simulation simulation) {
		super(simulation);
		if (parameters == null) {
			throw new IllegalArgumentException("parameters is null");
		}
		final int capacity = parameters.getIntAttribute("capacity");
		this.goodsQuality = parameters.getString("quality");
		this.technology = (BasicTechnology) this.getSimulation().getSector(this.goodsQuality);
		this.finishedGoods = new BasicGoods(this.goodsQuality);
		this.productionProcessLenght = this.technology.getProductionProcessLenght();
		// this.productivity = parameters.getIntAttribute("productivity");
		// parameters.getIntAttribute("processLenght");
		// this.qualityOfInputForTheCreationOfANewMachine =
		// parameters.getString("machines.creation.input.quality");
		// this.machineTimeLife = parameters.getInt("machines.timeLife.mean");
		// this.machineTimeLifeStDev =
		// parameters.getInt("machines.timeLife.stDev");
		// this.inputVolumeForANewMachine =
		// parameters.getInt("machines.creation.input.volume");
		// ***
		for (int i = 0; i < capacity; i++) {
			final int expiration0 = (int) (-getRandom().nextInt(this.technology.getMachineTimeLife())
					+ this.technology.getMachineTimeLife()
					+ getRandom().nextGaussian() * this.technology.getMachineTimeLifeStDev());
			final int expiration = (expiration0 < 0)
					? (int) (expiration0 + this.technology.getMachineTimeLife()
							+ getRandom().nextGaussian() * this.technology.getMachineTimeLifeStDev())
					: expiration0;
			final Machine machine = new Machine(0, expiration, expiration);
			this.machinery.add(machine);
			this.productionAtFullCapacity += machine.productivity;
		}
		this.inProcess = new Materials[this.technology.getProductionProcessLenght()];
		for (int i = 0; i < this.inProcess.length; i++) {
			this.inProcess[i] = new Materials(i);
		}
		this.inputForNewMachines = new BasicGoods(this.getTechnology().getQualityOfInputForTheCreationOfANewMachine());
	}

	int getOverhead() {
		return Math.round(this.getCapacity() * this.overHeadRatio);
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
				this.productionAtFullCapacity -= machine.productivity;
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
	 * @param input
	 *            the input that will be consumed in the process of machine
	 *            creation.
	 */
	public void expandCapacity(Commodities input) {
		this.inputForNewMachines.add(input);
		if (this.inputForNewMachines.getVolume() >= this.getTechnology().getInputVolumeForANewMachine()) {
			final int size = (int) (this.inputForNewMachines.getVolume()
					/ this.getTechnology().getInputVolumeForANewMachine());
			this.expandCapacity(size, this.inputForNewMachines);
		}
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
		if (this.investmentSize != 0) {
			throw new RuntimeException("Inconsistency");
		}
		for (int i = 0; i < size; i++) {
			final Machine machine = new Machine(input);
			this.machinery.addFirst(machine);
			this.investmentSize++;
			this.productionAtFullCapacity += machine.productivity;
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
	 * Returns the volume of investment goods in inventories available as input
	 * for the creation of new machines.
	 * 
	 * @return the volume of investment goods in inventories available as input
	 *         for the creation of new machines.
	 */
	public long getInputVolume() {
		return this.inputForNewMachines.getVolume();
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
	 * Returns the total value of inventories (finished goods, in process,
	 * inputs).
	 * 
	 * @return the total value of inventories (finished goods, in process,
	 *         inputs).
	 */
	public double getInventoryTotalValue() {
		return this.finishedGoods.getValue() + this.getInProcessValue() + this.inputForNewMachines.getValue();
	}

	/**
	 * Returns the number of new machines created since the start of the period.
	 * 
	 * @return the number of new machines created since the start of the period.
	 */
	public int getInvestmentSize() {
		return this.investmentSize;
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

	public Long getOverheadWages() {
		return this.overheadWageBill;
	}

	/**
	 * Returns the volume of the production at full capacity.
	 * 
	 * @return the volume of the production at full capacity.
	 */
	public double getProductionAtFullCapacity() {
		return this.productionAtFullCapacity;
	}

	/**
	 * Returns the technology.
	 * 
	 * @return the technology.
	 */
	public BasicTechnology getTechnology() {
		return this.technology;
	}

	/**
	 * Computes and returns the current value of the factory.
	 * 
	 * @return the current value of the factory.
	 */
	public long getValue() {
		return this.finishedGoods.getValue() + getInProcessValue() + getMachineryValue()
				+ this.inputForNewMachines.getValue();
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
		this.overheadWageBill = null;
		this.depreciation = null;
		this.investmentSize = 0;
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

		final int overhead = this.getOverhead();

		// TODO vérifier le fonctionnement de cette méthode en la traçant.
		if (contracts.size() > overhead + this.machinery.size()) {
			throw new RuntimeException("Too many workers");
		}

		int stage = this.technology.getProductionProcessLenght();

		this.wageBill = 0l;
		this.overheadWageBill = 0l;
		final ListIterator<? extends JobContract> workforceIterator = contracts.listIterator();

		// Overhead labour
		int i = 0;
		while (workforceIterator.hasNext() && i < overhead) {
			i++;
			final JobContract contract = workforceIterator.next();
			contract.getWorker().work();
			this.wageBill += contract.getWage();
			this.overheadWageBill += contract.getWage();
		}
		// ***

		final ListIterator<? extends Machine> machineryIterator = this.machinery.listIterator();

		final Materials[] newMaterials = new Materials[this.technology.getProductionProcessLenght()];
		while (workforceIterator.hasNext()) {
			final JobContract contract = workforceIterator.next();
			final Machine machine = machineryIterator.next();
			final double volume = machine.productivity * this.productionProcessLenght;
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

	public void depreciateInventories(double unitCost) {
		// 2018-04-9 : dépréciation des stocks, utilisé en cas de faillite.
		if (unitCost <= 0) {
			throw new IllegalArgumentException("Bad value: " + unitCost);
		}
		if (this.depreciation == null) {
			throw new RuntimeException("Depreciation should not be null at this point.");
		}
		final long newValue = (long) (this.finishedGoods.getVolume() * unitCost);
		final long oldValue = this.finishedGoods.getValue();
		final long newDepreciation = oldValue - newValue;
		this.finishedGoods.setValue(newValue);
		this.depreciation += newDepreciation;
	}

}
