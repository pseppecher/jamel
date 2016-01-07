package jamel.jamel.firms.factory;

import jamel.Jamel;
import jamel.basic.data.AgentDataset;
import jamel.basic.data.BasicAgentDataset;
import jamel.basic.util.Timer;
import jamel.jamel.firms.managers.AbstractManager;
import jamel.jamel.widgets.Asset;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A basic factory.
 */
public class BasicFactory extends AbstractManager implements Factory {

	/**
	 * A basic implementation of {@link WorkInProgress}.
	 * 
	 * TODO: objet � supprimer, remplacer par une classe anonyme. TODO:
	 * externaliser l'interface.
	 */
	private class BasicWorkInProgress extends HashMap<Rational, Materials>implements WorkInProgress {

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
				final Materials materials = new BasicMaterials(typeOfProduction, 0, 0, completion, timer);
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
	 * An interface for the work-in-progress materials.
	 * <p>
	 * "Material that has entered the production process but is not yet a
	 * finished product. Work in progress (WIP) therefore refers to all
	 * materials and partly finished products that are at various stages of the
	 * production process. WIP excludes inventory of raw materials at the start
	 * of the production cycle and finished products inventory at the end of the
	 * production cycle." (ref:
	 * <a href="http://www.investopedia.com/terms/w/workinprogress.asp">www.
	 * investopedia.com</a>)
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

	/** The machinery (a collection of machines) */
	private final List<Machine> machinery = new ArrayList<Machine>();

	/**
	 * The minimum duration of the production process (the number of stages of
	 * this process).
	 */
	private final int productionTime;

	/**
	 * The resources needs at full capacity.
	 */
	final private LinkedHashMap<String, Long> resourceNeeds = new LinkedHashMap<String, Long>();

	/**
	 * The collection of the input resources.
	 */
	private final Map<String, Commodities> resources = new HashMap<String, Commodities>();

	/**
	 * The type of production.
	 */
	private final String typeOfProduction;

	/** The work in progress. */
	private final WorkInProgress workInProgress;

	/** A flag that indicates either the factory is closed or not. */
	protected boolean canceled = false;

	/**
	 * L'objectif de taille des stocks de ressources, exprimé en nombre de mois
	 * à pleine utilisation des capacités de production.
	 */
	private final float resourcesTarget = 2f;

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
	public BasicFactory(int productionTime, Timer timer, Random random, Machine[] machines) {
		super("Factory", timer);
		this.productionTime = productionTime;
		// FIXME productionTime devrait être déduit de l'examen des machines.
		this.typeOfProduction = machines[0].getTypeOfProduction();
		if (this.typeOfProduction == null) {
			throw new IllegalArgumentException("typeOfProduction is null");
		}
		this.expandCapacity(machines);
		this.workInProgress = new BasicWorkInProgress(productionTime);
		this.finishedGoods = new FinishedGoods(this.typeOfProduction);
	}

	/**
	 * Depreciates the machines.
	 */
	private void depreciation() {
		long depreciation = 0;
		final List<Machine> cancelled = new LinkedList<Machine>();
		for (Machine machine : machinery) {
			depreciation += machine.depreciate();
			if (machine.isCancelled()) {
				cancelled.add(machine);
			}
		}
		this.machinery.removeAll(cancelled);
		this.dataset.put("machines.deleted", cancelled.size());
		this.dataset.put("depreciation", depreciation);
	}

	private Long getResourcesValue() {
		long value = 0;
		for (Commodities res : this.resources.values()) {
			value += res.getValue();
		}
		return value;
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
	 * Updates the needs in resources according to the technical coefficients of
	 * each machine. The need is the volume of each input required to run the
	 * machine.
	 */
	private void updateResourceNeeds() {
		this.resourceNeeds.clear();
		for (Machine machine : machinery) {
			final Map<String, Float> coefs = machine.getTechnicalCoefficients();
			final long productivity = machine.getProductivity();
			for (String key : coefs.keySet()) {
				final float coef = coefs.get(key);
				final long inputVolume = (long) (productivity * coef);
				final long volume;
				if (this.resourceNeeds.containsKey(key)) {
					volume = this.resourceNeeds.get(key) + inputVolume;
				} else {
					volume = inputVolume;
				}
				this.resourceNeeds.put(key, volume);
			}
		}
	}

	@Override
	public Map<String, Long> getNeeds() {
		final Map<String, Long> needs = new LinkedHashMap<String, Long>();
		for (String inputKey : this.resourceNeeds.keySet()) {
			final long inputVol = Math.max(0,
					(long) (this.resourceNeeds.get(inputKey) * this.resourcesTarget) - this.resources.get(inputKey).getVolume());
			needs.put(inputKey, inputVol);
		}
		return needs;
	}

	/**
	 * Performs some consistency tests.
	 */
	@Override
	protected void checkConsistency() {
		super.checkConsistency();
		if (this.canceled) {
			throw new RuntimeException("This factory is definitively closed.");
		}
	}

	@Override
	public Object askFor(String key) {
		final Object result;
		if ("machinery".equals(key)) {
			// FIXME: ici on donne les productivites des machines, rangees par
			// ordre decroissant. Ca sert à la firme quand elle prend la
			// decision d'investir.
			// Mais ce n'est pas la productivite qui compte, mais la
			// rentabilite, compte tenu
			// de la structure courante des couts.
			// A revoir imperativement des lors que les machines seront
			// reellement heterogenes.
			final long[] productivities = new long[machinery.size()];
			Collections.sort(this.machinery, productivityComparator);
			int index = 0;
			for (Machine machine : this.machinery) {
				productivities[index] = machine.getProductivity();
				index++;
			}
			result = productivities;
		} else if ("capacity".equals(key)) {
			result = this.machinery.size();
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void close() {
		checkConsistency();
		this.dataset.put("machinery.val", this.getMachineryValue());
		this.dataset.put("inventories.input.val", this.getResourcesValue());
		this.dataset.put("inventories.inProcess.val", this.workInProgress.getBookValue());
		this.dataset.put("inventories.fg.val", this.finishedGoods.getValue());
		this.dataset.put("inventories.fg.vol", this.finishedGoods.getVolume());
	}

	@Override
	public void delete() {
		checkConsistency();
		this.dataset.put("inventories.losses.val", this.getValue());
		this.dataset.put("inventories.fg.losses", this.finishedGoods.getValue());
		this.dataset.put("inventories.inProcess.losses", this.workInProgress.getBookValue());
		this.dataset.put("inventories.fg.losses.vol", this.finishedGoods.getVolume());
		this.finishedGoods.consume();
		this.workInProgress.delete();
		if (this.machinery.size() > 0) {
			throw new RuntimeException("The destruction of the machinery is not yet implemented.");
		}
	}

	@Override
	public void expandCapacity(Machine[] machines) {
		// On prend chacune des machines, on la vérifie, on la branche sur les
		// ressources de l'usine et on l'enregistre.
		for (Machine machine : machines) {
			if (!this.typeOfProduction.equals(machine.getTypeOfProduction())) {
				// Cas ou on essaie de rajouter une machine qui n'est pas du bon
				// type.
				throw new RuntimeException("Bad type of production: " + machine.getTypeOfProduction());
			}
			// On recupere la liste des ressources dont la machine a besoin.
			final String[] inputTypes = machine.getResources();
			if (inputTypes != null) {
				//
				for (String inputKey : inputTypes) {
					if (!this.resources.containsKey(inputKey)) {
						// Si cette ressource n'existe pas, on la cree et on
						// l'enregistre.
						final Commodities newResource = new FinishedGoods(inputKey);
						this.resources.put(inputKey, newResource);
					}
					// On 'branche' la resource sur la machine.
					machine.addResource(this.resources.get(inputKey));
				}
			}
			// La machine est prete, on peut l'ajouter au stock des machines.
			this.machinery.add(machine);
		}
	}

	@Override
	public int getCapacity() {
		checkConsistency();
		return this.machinery.size();
	}

	@Override
	public Commodities getCommodities(long demand) {
		checkConsistency();
		return this.finishedGoods.detach(demand);
	}

	@Override
	public int getCurrentMaxCapacity() {
		// Retourne une estimation basse de la capacité maximale de la firme
		// compte tenu du niveau des stocks d'input.
		final int result;
		double rate = 1.;
		for (String key : this.resourceNeeds.keySet()) {
			final double need = this.resourceNeeds.get(key);
			final double stock = this.resources.get(key).getVolume();
			if (stock < need) {
				double newRate = stock / need;
				rate = Math.min(rate, newRate);
			}
		}
		if (rate < 1.) {
			result = (int) (this.getCapacity() * (rate * 0.5));
			// 0.5 = on minore largement la capacité pour éviter d'avoir plus de
			// main d'oeuvre que nécessaire.
		} else {
			result = this.getCapacity();
		}
		return result;
	}

	@Override
	public AgentDataset getData() {
		checkConsistency();
		return this.dataset;
	}

	@Override
	public long getFinishedGoodsVolume() {
		checkConsistency();
		return this.finishedGoods.getVolume();
	}

	@Override
	public long getInventoryLosses() {
		return this.dataset.get("inventories.losses.val").longValue();
		// TODO use askable ?
	}

	@Override
	public double getInventoryRatio(float normalLevel) { // TODO use askable ?
		checkConsistency();
		final double normalVolume = normalLevel * this.getMaxUtilAverageProduction();
		this.dataset.put("inventories.fg.vol.normal", normalVolume);
		return (this.getFinishedGoodsVolume()) / normalVolume;
	}

	@Override
	public double getMaxUtilAverageProduction() {
		checkConsistency();
		final Double potentialOutPut;
		if (this.machinery.size() == 0) {
			potentialOutPut = 0.;
		} else {
			double sum = 0;
			for (Machine machine : this.machinery) {
				sum += machine.getProductivity();
			}
			potentialOutPut = sum;
		}
		return potentialOutPut;
	}

	@Override
	public Double getUnitCost() {
		checkConsistency();
		return this.finishedGoods.getUnitCost();
	}

	@Override
	public long getValue() {
		final long val = getResourcesValue() + this.workInProgress.getBookValue() + this.finishedGoods.getValue() + getMachineryValue();
		if (val < 0) {
			Jamel.println("this.workInProgress.getBookValue()", this.workInProgress.getBookValue());
			Jamel.println("this.finishedGoods.getValue()", this.finishedGoods.getValue());
			Jamel.println("getMachineryValue()", getMachineryValue());
			throw new RuntimeException("A factory cannot have a negative value.");
		}
		return val;
	}

	@Override
	public void open() {
		super.open();
		this.dataset = new BasicAgentDataset("Factory");
		this.dataset.put("inventories.losses.val", 0);
		this.dataset.put("inventories.fg.losses", 0);
		this.dataset.put("inventories.inProcess.losses", 0);
		this.dataset.put("inventories.fg.losses.vol", 0);
		this.dataset.put("desinvestment.val", 0);
		this.dataset.put("scrap.val", 0);
		this.dataset.put("scrap.vol", 0);
		depreciation();
		updateResourceNeeds();
	}

	@Override
	public void process(LaborPower... laborPowers) {
		checkConsistency();

		if (laborPowers.length > machinery.size()) {
			throw new IllegalArgumentException("Workforce: " + laborPowers.length + ", capacity: " + machinery.size());
		}

		final WorkInProgress newWorkInProgress = new BasicWorkInProgress(this.productionTime);
		final Commodities production = new FinishedGoods(this.typeOfProduction);

		if (laborPowers.length != 0) {

			// Calcul des couts unitaires de tous les inputs.

			final Map<String, Double> costs = new HashMap<String, Double>();
			for (String resourceKey : this.resources.keySet()) {
				costs.put(resourceKey, this.resources.get(resourceKey).getUnitCost());
			}

			// Calcul du salaire moyen.

			double payroll = 0;
			for (LaborPower laborPower : laborPowers) {
				payroll += laborPower.getWage();
			}
			costs.put("Wage", payroll / laborPowers.length);

			// Construction du comparateur pour la structure des couts.

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

			// FIXME: test this procedure when the machines will be
			// heterogeneous.

			int machineID = 0;
			int completion = productionTime - 1;

			// Expends each labor power.

			for (LaborPower laborPower : laborPowers) {

				final Materials[] inputs = this.workInProgress.getStuff(completion);

				final List<Materials> outputs = machinery.get(machineID).work(laborPower, inputs);

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

		}

		this.dataset.put("workforce", (double) laborPowers.length);
		this.dataset.put("production.vol", (double) production.getVolume());
		this.dataset.put("production.val", (double) production.getValue());
		if (this.getProductivity() != null) {
			// Productivity is null when the machinery is empty.
			this.dataset.put("productivity", (double) this.getProductivity());
		}
		this.dataset.put("capacity", (double) this.machinery.size());
		this.dataset.put("production.vol.atFullCapacity", this.getMaxUtilAverageProduction());

		this.finishedGoods.put(production);
		this.workInProgress.put(newWorkInProgress);

	}

	@Override
	public void scrap(double nMachines) {
		throw new RuntimeException("Not used.");
		/*
		 * long desinvestmentValue = 0; long scrapValue = 0; long scrapVolume =
		 * 0; if (nMachines <= 0) { throw new IllegalArgumentException(
		 * "Bad number of machines to be scraped: " + nMachines); } if
		 * (nMachines > this.machinery.size()) { throw new
		 * IllegalArgumentException("The number of machines to be scraped is " +
		 * nMachines + ", but the machinery size is " + this.machinery.size());
		 * } final double unitCost = this.finishedGoods.getUnitCost();
		 * Collections.sort(this.machinery, productivityComparator); for (int i
		 * = 0; i < nMachines; i++) { // On supprime la dernière machine de la
		 * liste. final Machine machine =
		 * this.machinery.remove(this.machinery.size() - 1); desinvestmentValue
		 * += machine.getBookValue(); final FinishedGoods scrap =
		 * machine.scrap(); scrap.setValue((long) (unitCost *
		 * scrap.getVolume())); scrapValue += scrap.getBookValue(); scrapVolume
		 * += scrap.getVolume(); this.finishedGoods.put(scrap); }
		 * this.dataset.put("desinvestment.val", desinvestmentValue);
		 * this.dataset.put("scrap.val", scrapValue);
		 * this.dataset.put("scrap.vol", scrapVolume); //
		 * this.dataset.put("machines.deleted", cancelled.size()); //
		 * this.dataset.put("depreciation", depreciation);
		 */
	}

	@Override
	public void putResources(String key, Commodities input) {
		this.resources.get(key).put(input);
		// Vérifier cette methode.
	}

}

// ***
