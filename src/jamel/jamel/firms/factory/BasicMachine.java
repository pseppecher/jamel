package jamel.jamel.firms.factory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import jamel.basic.util.Timer;
import jamel.jamel.firms.Technology;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * A machine that uses external resources (inputs) in the production process.
 */
public class BasicMachine implements Machine {

	/**
	 * Creates and returns the specified materials.
	 * 
	 * @param type
	 *            the type of product.
	 * @param volume
	 *            the volume of materials to be created.
	 * @param value
	 *            the volume of materials to be created.
	 * @param completion
	 *            the completion of the new materials.
	 * @param timer
	 *            the timer.
	 * @return the new materials.
	 */
	private static Materials getNewMaterials(final String type, final long volume, final long value,
			final Rational completion, final Timer timer) {
		if (volume < 0) {
			throw new IllegalArgumentException("Bad volume: " + volume);
		}
		if (value < 0) {
			throw new IllegalArgumentException("Bad value: " + value);
		}
		final Materials result;
		if (completion.equals(1)) {
			result = new FinishedGoods(type, volume, value);
		} else {
			result = new BasicMaterials(type, volume, value, completion, timer);
		}
		return result;
	}

	/**
	 * The book value of this machine.
	 */
	private long bookValue;

	/**
	 * The cancellation date (when the depreciation process will lead the book
	 * value of this machine to zero).
	 */
	private int cancellationDate;

	/**
	 * A flag that indicates whether this machine is cancelled or not.
	 */
	private boolean cancelled;

	/** How much materials are completed by period of production. */
	private final Rational increment;

	/** The last time this machine was used. */
	private int lastUsed = -1;

	/**
	 * The production time, or the number of stages in the production process.
	 */
	final private int productionTime;

	/**
	 * The productivity, or the volume of finished goods the machine can produce
	 * each period on average.
	 */
	final private double productivity;

	/**
	 * The external resources used as inputs in the production process.
	 */
	private final Map<String, Commodities> resources = new HashMap<String, Commodities>();

	/**
	 * The date when this machine was created. The start of its depreciation
	 * process.
	 */
	private final int start;

	/**
	 * A map that associates input-keys to technical coefficient values.
	 */
	private final Map<String, Float> technicalCoefficients = new HashMap<String, Float>();

	/**
	 * The timelife of the machine.
	 */
	private final int timelife;

	/** The timer. */
	private final Timer timer;

	/**
	 * The type of production.
	 */
	private final String typeOfProduction;

	/**
	 * Creates a new final machine.
	 * 
	 * @param technology
	 *            the technology.
	 * @param input
	 *            the input to consumed in the creation of this machine.
	 * @param timer
	 *            the timer.
	 * @param random
	 *            the random.
	 */
	public BasicMachine(Technology technology, Commodities input, Timer timer, Random random) {

		if (technology == null) {
			throw new IllegalArgumentException("Technology is null.");
		}
		final long acquisitionCost;
		if ((input == null && timer.getPeriod().intValue() == 0)) {
			acquisitionCost = 0;
		} else {
			if (input.getVolume() > technology.getInputVolumeForANewMachine()) {
				throw new IllegalArgumentException("To many input to create this machine.");
			}
			if (input.getVolume() < technology.getInputVolumeForANewMachine()) {
				throw new IllegalArgumentException("Not enough input to create this machine.");
			}
			if (input.getValue() < 0) {
				throw new IllegalArgumentException("Illegal value: " + input.getValue());
			}
			acquisitionCost = input.getValue();
			input.consume();
		}
		this.typeOfProduction = technology.getTypeOfProduction();
		this.productionTime = technology.getProductionTime();
		this.productivity = technology.getProductivity();
		this.increment = new Rational(1, productionTime);
		this.bookValue = acquisitionCost;
		this.timer = timer;
		if (timer.getPeriod().intValue() == 0) {
			this.start = timer.getPeriod().intValue() - random.nextInt((int) technology.getTimelifeMean());
		} else {
			this.start = timer.getPeriod().intValue();
		}
		this.cancelled = false;

		this.cancellationDate = this.start + (int) technology.getTimelifeMean();

		this.timelife = (int) (technology.getTimelifeMean() + random.nextGaussian() * technology.getTimelifeStDev());
		final Map<String, Float> techCoef = technology.getTechnicalCoefficients();
		for (String resourceKey : techCoef.keySet()) {
			this.technicalCoefficients.put(resourceKey, techCoef.get(resourceKey));
			this.resources.put(resourceKey, new FinishedGoods(resourceKey));
		}
	}

	/**
	 * Adds an input resource.
	 * 
	 * @param input
	 *            the input resource.
	 */
	@Override
	public void addResource(Commodities input) {
		final String key = input.getType();
		if (!technicalCoefficients.containsKey(key)) {
			throw new RuntimeException();
		}
		this.resources.put(key, input);
	}

	@Override
	public long depreciate() {
		if (timer.getPeriod().intValue() > this.start + this.timelife) {
			this.cancelled = true;
		}
		final long depreciation;
		if (this.cancelled) {
			depreciation = this.bookValue;
			this.bookValue = 0;
		} else {
			final int remainingTime = cancellationDate - timer.getPeriod().intValue();
			if (remainingTime > 0) {
				depreciation = this.bookValue / remainingTime;
				this.bookValue -= depreciation;
			} else {
				if (this.bookValue > 0) {
					throw new RuntimeException("Expected book value: 0 but was: " + this.bookValue + ".");
				}
				depreciation = 0;
			}
		}
		return depreciation;
	}

	@Override
	public Long getBookValue() {
		if (this.bookValue < 0) {
			throw new RuntimeException("Illegal value: " + this.bookValue);
		}
		return this.bookValue;
	}

	@Override
	public long getProductivity() {
		return (long) this.productivity;
	}

	@Override
	public String[] getResources() {
		return this.technicalCoefficients.keySet().toArray(new String[0]);
	}

	@Override
	public HashMap<String, Float> getTechnicalCoefficients() {
		return new HashMap<String, Float>(this.technicalCoefficients);
	}

	@Override
	public String getTypeOfProduction() {
		return this.typeOfProduction;
	}

	@Override
	public Double getUnitProductCost(Map<String, Double> costs) {
		// TODO verifier le bon fonctionnement de cette methode.
		Double result;
		final Double wage = costs.get("Wage");
		if (wage == null) {
			result = null;
		} else {
			result = productivity / wage;
			for (String resourceKey : technicalCoefficients.keySet()) {
				Commodities resource = resources.get(resourceKey);
				if (resource == null) {
					result = null;
					break;
				}
				// FIXME: what if resource.getVolume()==0 ?
				result += resource.getUnitCost() * technicalCoefficients.get(resourceKey);
			}
		}
		return result;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public FinishedGoods scrap() {
		throw new RuntimeException("Not used");
		/*
		 * if (isCancelled()) { throw new RuntimeException(
		 * "This machine is already cancelled."); } final FinishedGoods result =
		 * new FinishedGoods(scrap, 0); this.cancelled = true; this.bookValue =
		 * 0; return result;
		 */
	}

	/**
	 * The production function. La différence avec la méthode de la classe
	 * supérieure, c'est que cette méthode consomme des biens intermédiaires
	 * comme inputs.
	 * 
	 * @param laborPower
	 *            {@link LaborPower} of a worker.
	 * @param workInProcess
	 *            list of materials.
	 * @return list of outputs.
	 */
	@Override
	public List<Materials> work(final LaborPower laborPower, Materials... workInProcess) {
		if (timer.getPeriod().intValue() > this.start + this.timelife) {
			throw new IllegalArgumentException("Broken machine.");
		}
		if (laborPower.isExhausted()) {
			throw new IllegalArgumentException("This labor power is exhausted.");
		}
		if (this.lastUsed == this.timer.getPeriod().intValue()) {
			throw new AnachronismException("This machine has already been used in this period.");
		}

		this.lastUsed = this.timer.getPeriod().intValue();

		// On calcule la valeur des intrants: force de travail et inputs
		// matériels.

		long inputValue = laborPower.getWage();

		// Depense de la force de travail.

		laborPower.expend();

		// Consommation des inputs.

		for (Entry<String, Float> entry : technicalCoefficients.entrySet()) {
			final String inputKey = entry.getKey();
			final float coef = entry.getValue();
			final long inputVolume = (long) (this.productivity * coef);
			final Commodities input = resources.get(inputKey).detach(inputVolume);
			inputValue += input.getValue();
			input.consume();
		}

		// Completion des materiaux.

		final List<Materials> outputs = new LinkedList<Materials>();
		long newVolume = (long) (this.productivity) * this.productionTime;
		for (Materials materials : workInProcess) {
			if (materials != null && materials.getVolume() > 0) {
				if (!materials.getType().equals(typeOfProduction)) {
					throw new RuntimeException(
							"Input bad type: " + materials.getType() + "; expected: " + typeOfProduction);
				}
				if (materials.getProductionPeriod() >= this.timer.getPeriod().intValue()) {
					throw new AnachronismException(
							"Materials cannot be used as input in the same period they were produced.");
				}
				final long materialsVolume = materials.getVolume();
				final Rational materialsCompletion = materials.getCompletion();
				final Rational outputCompletion = materialsCompletion.add(increment);
				final Materials output;
				if (materialsVolume == newVolume) {
					output = getNewMaterials(this.typeOfProduction, newVolume, materials.getBookValue() + inputValue,
							outputCompletion, timer);
					materials.delete();
					newVolume = 0;
					inputValue = 0;
					outputs.add(output);
					break;
				} else if (materialsVolume > newVolume) {
					final long materialsValue = (long) (1d * materials.getBookValue() * newVolume / materialsVolume);
					output = getNewMaterials(this.typeOfProduction, newVolume, materialsValue + inputValue,
							outputCompletion, timer);
					materials.delete(newVolume, materialsValue);
					newVolume = 0;
					inputValue = 0;
					outputs.add(output);
					break;
				} else {
					final long inputValue2 = inputValue * materialsVolume / newVolume;
					output = getNewMaterials(this.typeOfProduction, materialsVolume,
							materials.getBookValue() + inputValue2, outputCompletion, timer);
					materials.delete();
					newVolume -= materialsVolume;
					inputValue -= inputValue2;
					outputs.add(output);
				}
			}
		}
		if (newVolume > 0) {
			final Materials output = new BasicMaterials(typeOfProduction, newVolume, inputValue, increment, timer);
			outputs.add(output);
			inputValue = 0;
		}
		if (inputValue > 0) {
			throw new RuntimeException("Residual value should be zero.");
		}
		return outputs;
	}

}
