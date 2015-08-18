package jamel.jamel.firms.factory;

import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;

/**
 * A basic factory.
 */
public class BasicFactory implements Factory {

	/**
	 * A basic implementation of {@link WorkInProgress}.
	 * 
	 * TODO: objet ˆ supprimer, remplacer par une classe anonyme.
	 * TODO: externaliser l'interface.
	 */
	private class BasicWorkInProgress extends HashMap<Rational, Materials>
			implements WorkInProgress {

		/**
		 * Creates a new {@link BasicWorkInProgress}.
		 * 
		 * @param productionTime
		 *            the production time (i.e. the number of stages in the
		 *            production process).
		 */
		public BasicWorkInProgress(int productionTime) {
			super();
			for (int i = 1; i < productionTime; i++) {
				final Rational completion = new Rational(i, productionTime);
				final Materials materials = new BasicMaterials(0, 0,
						completion, timer);
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
		public Materials get(Rational completion) {
			if (!this.containsKey(completion)) {
				throw new RuntimeException("This completion not exists: "
						+ completion);
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
				final Rational completion = new Rational(maxCompletion - i,
						productionTime);
				final Materials input = this.get(completion);
				inputs[i] = input;
			}
			return inputs;
		}

		@Override
		public void put(WorkInProgress workInProgress) {
			for (Entry<Rational, Materials> entry : this.entrySet()) {
				entry.getValue().add(workInProgress.get(entry.getKey()));
			}
		}

		@Override
		public void putStuff(Materials stuff) {
			final Materials materials = this.get(stuff.getCompletion());
			if (materials == null) {
				throw new RuntimeException("Materials not found: "
						+ stuff.getCompletion());
			}
			materials.add(stuff);
		}

		@Override
		public String toString() {
			String result = "";
			for (Materials m : this.values()) {
				result = result + m.getCompletion() + ": " + m.getVolume()
						+ "; ";
			}
			return result;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

	}

	/**
	 * An interface for the work-in-progress materials.
	 * <p>
	 * "Material that has entered the production process but is not yet a
	 * finished product. Work in progress (WIP) therefore refers to all
	 * materials and partly finished products that are at various stages of the
	 * production process. WIP excludes inventory of raw materials at the start
	 * of the production cycle and finished products inventory at the end of the
	 * production cycle." (ref: <a href="http://www.investopedia.com/terms/w/
	 * workinprogress.asp">www.investopedia.com</a>)
	 */
	private interface WorkInProgress extends Asset {

		/**
		 * Deletes the content of this WIP.
		 */
		void delete();

		/**
		 * Returns a heap of materials of the specified completion.
		 * 
		 * @param completion
		 *            the completion of the materials to be returned.
		 * @return a heap of materials of the specified completion.
		 */
		Materials get(Rational completion);

		/**
		 * Returns an array of materials, sorted by completion in descending
		 * order.
		 * 
		 * @param maxCompletion
		 *            the completion of the most advanced materials to be
		 *            returned.
		 * @return an array of materials, sorted by completion in descending
		 *         order.
		 */
		Materials[] getStuff(int maxCompletion);

		/**
		 * Puts all materials of the specified WIP into this WIP.
		 * 
		 * @param workInProgress
		 *            the materials to be added.
		 */
		void put(WorkInProgress workInProgress);

		/**
		 * Puts the specified stuff into this WIP.
		 * 
		 * @param stuff
		 *            the materials to be added.
		 */
		void putStuff(Materials stuff);

	}

	/**
	 * A comparator to sort the machinery according to the productivity of the
	 * machines (higher productivities first).
	 */
	static final Comparator<Machine> productivityComparator = new Comparator<Machine>() {

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

	/** The factory dataset. */
	private AgentDataset dataset;

	/**
	 * Finished goods inventory.
	 * <p>
	 * "Finished goods are goods that have been completed by the manufacturing
	 * process, or purchased in a completed form, but which have not yet been
	 * sold to customers."
	 * 
	 * (ref: <a href =
	 * "http://www.accountingtools.com/definition-finished-goods-inve">
	 * www.accountingtools.com</a>)
	 * */
	private final Commodities finishedGoods;

	/** The machinery (a collection of machines) */
	private final List<Machine> machinery = new ArrayList<Machine>();

	/**
	 * The minimum duration of the production process (the number of stages of
	 * this process).
	 */
	private final int productionTime;

	/** The timer. */
	private final Timer timer;

	/** The work in progress. */
	private final WorkInProgress workInProgress;

	/** A flag that indicates either the factory is closed or not. */
	protected boolean canceled = false;

	/**
	 * Creates a new {@link BasicFactory}.
	 * 
	 * @param productionTime
	 *            the production time.
	 * @param capacity
	 *            the capacity of production.
	 * @param productivity
	 *            the productivity.
	 * @param timer
	 *            the timer.
	 */
	public BasicFactory(int productionTime, int capacity, long productivity,
			Timer timer) {

		this.timer = timer;
		this.productionTime = productionTime;

		for (int i = 0; i < capacity; i++) {
			this.machinery.add(new BasicMachine(productionTime, productivity,
					timer));
		}

		this.workInProgress = new BasicWorkInProgress(productionTime);
		this.finishedGoods = new FinishedGoods();

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

	@Override
	public void bankrupt() {
		if (this.canceled) {
			throw new RuntimeException("This factory is already closed.");
		}
		this.dataset.put("inventories.losses.val", (double) this.getValue());
		this.dataset.put("inventories.fg.losses", (double) this.finishedGoods.getValue());
		this.dataset.put("inventories.inProcess.losses", (double) this.workInProgress.getBookValue());
		this.dataset.put("inventories.fg.losses.vol", (double) this.finishedGoods.getVolume());
		this.finishedGoods.consume();
		this.workInProgress.delete();
		this.canceled = true;
	}

	@Override
	public void close() {
		this.dataset.put("inventories.inProcess.val",
				(double) this.workInProgress.getBookValue());
		this.dataset.put("inventories.fg.val",
				(double) this.finishedGoods.getValue());
		this.dataset.put("inventories.fg.vol",
				(double) this.finishedGoods.getVolume());
	}

	@Override
	public int getCapacity() {
		return this.machinery.size();
	}

	@Override
	public Commodities getCommodities(long demand) {
		if (this.canceled) {
			throw new RuntimeException("This factory is definitively closed.");
		}
		return this.finishedGoods.detach(demand);
	}

	@Override
	public AgentDataset getData() {
		return this.dataset;
	}

	@Override
	public long getFinishedGoodsVolume() {
		return this.finishedGoods.getVolume();
	}

	@Override
	public double getInventoryLosses() {
		return this.dataset.get("inventories.losses.val");
	}

	@Override
	public double getInventoryRatio(float normalLevel) {
		final double normalVolume = normalLevel
				* this.getMaxUtilAverageProduction();
		this.dataset.put("inventories.fg.vol.normal", normalVolume);
		return (this.getFinishedGoodsVolume()) / normalVolume;
	}

	@Override
	public double getMaxUtilAverageProduction() {
		final Double maxUtilAverageProduction;
		if (this.machinery.size() == 0) {
			maxUtilAverageProduction = null;
		} else {
			double sum = 0;
			for (Machine machine : this.machinery) {
				sum += machine.getProductivity();
			}
			maxUtilAverageProduction = sum;
		}
		return maxUtilAverageProduction;
	}

	@Override
	public double getUnitCost() {
		return this.finishedGoods.getUnitCost();
	}

	@Override
	public long getValue() {
		return this.workInProgress.getBookValue()
				+ this.finishedGoods.getValue();
	}

	@Override
	public void investment(InvestmentProcess investmentProcess) {
		throw new RuntimeException("Not yet implemented"); // TODO: IMPLEMENT ME
	}

	@Override
	public void open() {
		this.dataset = new BasicAgentDataset("Factory");
		this.dataset.put("inventories.losses.val", 0d);
		this.dataset.put("inventories.fg.losses", 0d);
		this.dataset.put("inventories.inProcess.losses", 0d);
		this.dataset.put("inventories.fg.losses.vol", 0d);
	}

	@Override
	public void process(LaborPower... laborPowers) {

		if (laborPowers.length > machinery.size()) {
			throw new IllegalArgumentException(
					"The workforce exceeds the capacity");
		}

		Collections.sort(this.machinery, productivityComparator);
		int machineID = 0;
		int completion = productionTime - 1;

		final WorkInProgress newWorkInProgress = new BasicWorkInProgress(
				this.productionTime);
		final Commodities production = new FinishedGoods();

		// Expends each labor power.
		for (LaborPower laborPower : laborPowers) {

			final Materials[] inputs = this.workInProgress.getStuff(completion);

			final List<Materials> outputs = machinery.get(machineID).work(
					laborPower, inputs);

			// Dispatches finished and not finished goods.
			for (Materials output : outputs) {
				if (output.getCompletion().equals(1)) {
					production.put((Commodities) output);
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

		this.dataset.put("workforce", (double) laborPowers.length);
		this.dataset.put("production.vol", (double) production.getVolume());
		this.dataset.put("production.val", (double) production.getValue());
		this.dataset.put("productivity", (double) this.getProductivity());
		this.dataset.put("capacity", (double) this.machinery.size());
		this.dataset.put("production.vol.atFullCapacity",
				this.getMaxUtilAverageProduction());

		this.finishedGoods.put(production);
		this.workInProgress.put(newWorkInProgress);

	}

	@Override
	public void scrap(double threshold) {
		throw new RuntimeException("Not yet implemented"); // TODO: IMPLEMENT ME
	}

}

// ***
