package jamel.models.modelJEE.firms.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jamel.Jamel;
import jamel.data.AgentDataset;
import jamel.models.modelJEE.firms.BasicFirmKeys;
import jamel.models.modelJEE.util.Memory;
import jamel.models.util.Commodities;
import jamel.models.util.JobContract;
import jamel.util.JamelObject;
import jamel.util.Simulation;

/**
 * A basic factory.
 */
@SuppressWarnings("javadoc")
public class BasicFactory extends JamelObject {

	/**
	 * A basic implementation of {@link WorkInProgress}.
	 * 
	 * TODO: objet � supprimer, remplacer par une classe anonyme. TODO:
	 * externaliser l'interface.
	 */
	private class BasicWorkInProgress extends HashMap<Rational, Materials> implements WorkInProgress {

		/**
		 * Creates a new {@link BasicWorkInProgress}.
		 * 
		 * @param productionTime
		 *            the production time (i.e. the number of stages in the
		 *            production process).
		 */
		public BasicWorkInProgress(int productionTime) {
			super(productionTime);
			for (int i = 1; i < productionTime; i++) {
				final Rational completion = new Rational(i, productionTime);
				final Materials materials = new BasicMaterials(0, 0, completion, getSimulation());
				this.put(completion, materials);
			}
		}

		@Override
		public void delete() {
			for (Materials material : this.values()) {
				material.delete();
			}
		}

		@Override
		public void depreciate(double d) {
			for (Materials materials : this.values()) {
				final long newValue = (long) (materials.getBookValue() * d);
				materials.setValue(newValue);
			}
		}

		@Override
		public Materials get(Rational completion) {
			if (!this.containsKey(completion)) {
				throw new RuntimeException("This completion not exists: " + completion);
			}
			return super.get(completion);
		}

		@Override
		public Long getBookValue() {
			long value = 0;
			for (Materials materials : this.values()) {
				value += materials.getBookValue();
			}
			return value;
		}

		@Override
		public Materials[] getStuff(int maxCompletion) {
			final Materials[] inputs = new Materials[maxCompletion];
			for (int i = 0; i < maxCompletion; i++) {
				final Rational completion = new Rational(maxCompletion - i, productionTime);
				final Materials input = this.get(completion);
				inputs[i] = input;
			}
			return inputs;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void put(WorkInProgress input) {
			for (Entry<Rational, Materials> entry : this.entrySet()) {
				entry.getValue().add(input.get(entry.getKey()));
			}
		}

		@Override
		public void putStuff(Materials stuff) {
			final Materials materials = this.get(stuff.getCompletion());
			if (materials == null) {
				throw new RuntimeException("Materials not found: " + stuff.getCompletion());
			}
			materials.add(stuff);
		}

		@Override
		public String toString() {
			String result = "";
			for (Materials m : this.values()) {
				result = result + m.getCompletion() + ": " + m.getVolume() + "; ";
			}
			return result;
		}

	}

	/**
	 * The data keys.
	 */
	private static final BasicFirmKeys keys = BasicFirmKeys.getInstance();

	/**
	 * A comparator to sort the machinery according to the productivity of the
	 * machines (higher productivities first).
	 */
	private static final Comparator<Machine> productivityComparator = new Comparator<Machine>() {

		@Override
		public int compare(Machine machine0, Machine machine1) {
			final int result;
			if (machine0.getProductivity() == machine1.getProductivity()) {
				result = 0;
			} else if (machine0.getProductivity() < machine1.getProductivity()) {
				result = 1;
			} else {
				result = -1;
			}
			return result;
		}
	};

	final private AgentDataset dataset;

	final private Memory<Long> depreciationMemory = new Memory<Long>(12);

	private float depreciationRate;

	/**
	 * Finished goods inventory.
	 * <p>
	 * "Finished goods are goods that have been completed by the manufacturing
	 * process, or purchased in a completed form, but which have not yet been
	 * sold to customers."
	 * 
	 * (ref: <a href =
	 * "http://www.accountingtools.com/definition-finished-goods-inve"> www.
	 * accountingtools.com</a>)
	 */
	private final Commodities finishedGoods;

	private long fixedCapitalDepreciation = 0;

	private long inventoriesDepreciation = 0;

	/** The machinery (a collection of machines) */
	private final List<Machine> machinery = new ArrayList<Machine>();

	private Double normalLevel = null;

	private Memory<Double> potentialOutputMemory = new Memory<Double>(12);

	/**
	 * The minimum duration of the production process (the number of stages of
	 * this process).
	 */
	private final int productionTime;

	/** The work in progress. */
	private final WorkInProgress workInProgress;

	/**
	 * Creates a new {@link BasicFactory}.
	 * 
	 * @param productionTime
	 *            the production time.
	 * @param timer
	 *            the timer.
	 * @param random
	 *            the random.
	 * @param machines
	 *            the machines.
	 */
	public BasicFactory(AgentDataset dataset, int productionTime, Simulation simulation, Random random,
			Machine[] machines) {
		super(simulation);
		this.dataset = dataset;
		this.productionTime = productionTime;
		// FIXME productionTime devrait être déduit de l'examen des machines.
		this.expandCapacity(machines);
		this.workInProgress = new BasicWorkInProgress(productionTime);
		this.finishedGoods = new FinishedGoods();
	}

	/**
	 * Depreciates the machines.
	 */
	private void depreciation() {
		/*
		 * 2016-04-03:
		 * Refactoring.
		 */

		final List<Machine> cancelled = new LinkedList<Machine>();
		for (Machine machine : machinery) {
			fixedCapitalDepreciation += machine.depreciate();
			if (machine.isCancelled()) {
				cancelled.add(machine);
			}
		}
		/*
		 * 2016-03-16:
		 * L'entreprise procède à la dépréciation des invendus.
		 * 
		 * 2016-04-03:
		 * Refactoring.
		 */
		// if (this.depreciationRate > 0)
		{
			final long value1 = this.finishedGoods.getValue();
			if (value1 > 0) {
				final double inventoryRatio = getInventoryRatio();
				if (inventoryRatio > 1) {
					final float r = 1 - this.depreciationRate;
					final long value2 = Math.max(1, (long) (value1 * (1 + inventoryRatio * r - r) / inventoryRatio));
					this.finishedGoods.setValue(value2);
					this.inventoriesDepreciation += value1 - value2;
				}
			}
		}

		// ***

		this.machinery.removeAll(cancelled);

	}

	/**
	 * Returns the value of the machinery.
	 * 
	 * @return the value of the machinery.
	 */
	private long getMachineryValue() {
		long value = 0;
		for (Machine machine : machinery) {
			value += machine.getBookValue();
		}
		if (value < 0) {
			throw new RuntimeException("Illegal value: " + value);
		}
		return value;
	}

	/**
	 * Returns the average productivity of the machinery.
	 * 
	 * @return the average productivity of the machinery.
	 */
	private Double getProductivity() {
		final Double productivity;
		if (this.machinery.size() == 0) {
			productivity = null;
		} else {
			double sum = 0;
			for (Machine machine : this.machinery) {
				sum += machine.getProductivity();
			}
			productivity = sum / this.machinery.size();
		}
		return productivity;
	}

	/**
	 * Closes the factory.
	 */
	public void close() {
		this.potentialOutputMemory.add(this.getPotentialOutput());
		this.dataset.put(keys.machineryValue, this.getMachineryValue());
		this.dataset.put(keys.inProcessValue, this.workInProgress.getBookValue());
		this.dataset.put(keys.inventoriesValue, this.finishedGoods.getValue());
		this.dataset.put(keys.inventoriesVolume, this.finishedGoods.getVolume());
		this.dataset.put(keys.inventoriesNormalVolume, this.normalLevel * this.getPotentialOutput());

		// this.dataset.put("fixedCapital.depreciation",
		// this.fixedCapitalDepreciation);
		// this.dataset.put("inventories.depreciation",
		// this.inventoriesDepreciation);
		this.depreciationMemory.add(this.fixedCapitalDepreciation);
	}

	/**
	 * Expands the capacity of this factory by adding the specified machines to
	 * its machinery.
	 * 
	 * @param machines
	 *            the new machines to be added.
	 */
	public void expandCapacity(Machine[] machines) {
		for (Machine machine : machines) {
			this.machinery.add(machine);
		}
	}

	/**
	 * Returns the capacity of the factory, i.e. its maximum number of workers,
	 * i.e its number of machines.
	 * 
	 * @return the capacity.
	 */
	public int getCapacity() {

		return this.machinery.size();
	}

	/**
	 * Subtracts and returns the specified volume of commodities from the
	 * inventories.
	 * 
	 * @param demand
	 *            the volume of commodities.
	 * @return the demanded commodities.
	 */
	public Commodities getCommodities(long demand) {

		return this.finishedGoods.take(demand);
	}

	/**
	 * Returns the volume of finished goods.
	 * 
	 * @return the volume of finished goods.
	 */
	public long getFinishedGoodsVolume() {

		return this.finishedGoods.getVolume();
	}

	/**
	 * Returns the inventory ratio.
	 * <p>
	 * If inventoryRatio upper than 1 : the volume of finished goods exceeds the
	 * normal volume,<br>
	 * If inventoryRatio = 1 : the volume of finished goods meets the normal
	 * volume,<br>
	 * If inventoryRatio lower than 1 : the volume of finished goods is under
	 * the normal volume.
	 * 
	 * @return the inventory ratio.
	 */
	public double getInventoryRatio() {

		final double normalVolume = normalLevel * this.getPotentialOutput();
		return (this.getFinishedGoodsVolume()) / normalVolume;
	}

	public long[] getMachinery() {
		final long[] productivities = new long[machinery.size()];
		Collections.sort(this.machinery, productivityComparator);
		int index = 0;
		for (Machine machine : this.machinery) {
			productivities[index] = machine.getProductivity();
			index++;
		}
		return productivities;
	}

	/**
	 * Returns the average volume of production (finished goods) at the maximum
	 * utilization of the production capacity.
	 * 
	 * @return a volume.
	 */

	/**
	 * Returns the average volume of production (finished goods) at the maximum
	 * utilization of the production capacity.
	 * 
	 * @return a volume.
	 */
	public double getPotentialOutput() {
		final Double potentialOutput;
		if (this.machinery.size() == 0) {
			potentialOutput = 0.;
		} else {
			double sum = 0;
			for (Machine machine : this.machinery) {
				sum += machine.getProductivity();
			}
			potentialOutput = sum;
		}
		return potentialOutput;
	}

	/**
	 * Returns the unit cost of the finished goods in the inventory. The unit
	 * cost is
	 * "The cost incurred by a company to produce, store and sell one unit of a
	 * particular product."
	 * 
	 * @see <a href="http://www.investopedia.com/terms/u/unitcost.asp">www.
	 *      investopedia.com/terms/u/unitcost.asp</a>
	 * @return the unit cost.
	 */
	public Double getUnitCost() {
		return this.finishedGoods.valuePerUnit();
	}

	/**
	 * Returns the total value of finished and unfinished goods present in the
	 * inventories. (= "the raw materials, work-in-process goods and completely
	 * finished goods that are considered to be the portion of a business's
	 * assets that are ready or will be ready for sale")
	 * 
	 * @see <a href="http://www.investopedia.com/articles/04/031004.asp">
	 *      investopedia.com</a>
	 * @return the total value.
	 */
	public long getValue() {
		final long val = this.workInProgress.getBookValue() + this.finishedGoods.getValue() + getMachineryValue();
		if (val < 0) {
			Jamel.println("this.workInProgress.getBookValue()", this.workInProgress.getBookValue());
			Jamel.println("this.finishedGoods.getValue()", this.finishedGoods.getValue());
			Jamel.println("getMachineryValue()", getMachineryValue());
			throw new RuntimeException("A factory cannot have a negative value.");
		}
		return val;
	}

	/**
	 * Opens the factory.
	 * 
	 * @param dataset
	 */
	public void open() {
		this.inventoriesDepreciation = 0;
		this.fixedCapitalDepreciation = 0;
		depreciation();
	}

	/**
	 * Produces new goods by the expense of the given units of labor power.
	 * 
	 * @param laborPowers
	 *            the labor powers.
	 */
	public void process(List<JobContract> workforce) {
		if (workforce.size() > machinery.size()) {
			throw new IllegalArgumentException("Workforce: " + workforce.size() + ", capacity: " + machinery.size());
		}

		final WorkInProgress newWorkInProgress = new BasicWorkInProgress(this.productionTime);
		final Commodities production = new FinishedGoods();

		if (workforce.size() != 0) {

			// Calcul des couts unitaires de tous les inputs.

			final Map<String, Double> costs = new HashMap<String, Double>();

			// Calcul du salaire moyen.

			double payroll = 0;
			for (JobContract jobContract : workforce) {
				payroll += jobContract.getWage();
			}

			/*
			// TODO revoir ce cost ci-dessous / très moche !
			costs.put("Wage", payroll / workforce.size());
			
			// Construction du comparateur pour la structure des couts.
			
			// TODO : Que c'est moche !!!!
			// TODO : de toute façon les machines ont des productivités égale !! 
			
			final Comparator<Machine> machineComparator = new Comparator<Machine>() {
			
				@Override
				public int compare(Machine machine0, Machine machine1) {
					final int result;
					if (machine0.getUnitProductCost(costs) == null && machine1.getUnitProductCost(costs) == null) {
						result = 0;
					} else if (machine0.getUnitProductCost(costs) == null) {
						result = 1;
					} else if (machine1.getUnitProductCost(costs) == null) {
						result = -1;
					} else if (machine0.getUnitProductCost(costs) == machine1.getUnitProductCost(costs)) {
						result = 0;
					} else if (machine0.getUnitProductCost(costs) < machine1.getUnitProductCost(costs)) {
						result = -1;
					} else {
						result = 1;
					}
					return result;
				}
			
			};
			
			// Rangemenent des machines par rentabilite decroissante.
			
			Collections.sort(this.machinery, machineComparator);
			*/

			// FIXME: test this procedure when the machines will be
			// heterogeneous.

			int machineID = 0;
			int completion = productionTime - 1;

			// Expends each labor power.

			for (JobContract jobContract : workforce) {

				final Materials[] inputs = this.workInProgress.getStuff(completion);

				final List<Materials> outputs = machinery.get(machineID).work(jobContract,
						inputs);

				// Dispatches finished and not finished goods.

				for (Materials output : outputs) {
					if (output.getCompletion().equals(1)) {
						production.add((Commodities) output);
					} else {
						newWorkInProgress.putStuff(output);
					}
				}

				machineID++;
				completion--;
				if (completion < 0) {
					completion = productionTime - 1;
				}

			}

		}

		this.dataset.put(keys.workforce, (double) workforce.size());
		this.dataset.put(keys.productionVolume, (double) production.getVolume());
		this.dataset.put(keys.productionValue, (double) production.getValue());
		/*if (this.getProductivity() != null) {
			// Productivity is null when the machinery is empty.
			this.dataset.put(keys.pr"productivity", (double) this.getProductivity());
		}*/

		this.finishedGoods.add(production);
		this.workInProgress.put(newWorkInProgress);

	}

	/**
	 * Sets the normal level of inventories.
	 * 
	 * @param normalLevel
	 *            the normal level, expressed as a number of period of
	 *            production at full capacity.
	 * 
	 */
	public void setInventoryNormalLevel(double normalLevel) {
		this.normalLevel = normalLevel;
	}

}

// ***
