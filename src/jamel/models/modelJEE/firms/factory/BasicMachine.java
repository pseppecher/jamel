package jamel.models.modelJEE.firms.factory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jamel.models.util.Commodities;
import jamel.models.util.JobContract;
import jamel.util.JamelObject;
import jamel.util.Simulation;

/**
 * A machine.
 */
public class BasicMachine extends JamelObject implements Machine {

	/**
	 * Creates and returns the specified materials.
	 * 
	 * @param volume
	 *            the volume of materials to be created.
	 * @param value
	 *            the volume of materials to be created.
	 * @param completion
	 *            the completion of the new materials.
	 * @param simulation
	 *            the simulation.
	 * @return the new materials.
	 */
	private static Materials getNewMaterials(final long volume, final long value, final Rational completion,
			final Simulation simulation) {
		if (volume < 0) {
			throw new IllegalArgumentException("Bad volume: " + volume);
		}
		if (value < 0) {
			throw new IllegalArgumentException("Bad value: " + value);
		}
		final Materials result;
		if (completion.equals(1)) {
			result = new FinishedGoods(volume, value);
		} else {
			result = new BasicMaterials(volume, value, completion, simulation);
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
	 * The date when this machine was created. The start of its depreciation
	 * process.
	 */
	private final int start;

	/**
	 * The timelife of the machine.
	 */
	private final int timelife;

	/**
	 * Creates a new final machine.
	 * 
	 * @param technology
	 *            the technology.
	 * @param input
	 *            the input to consumed in the creation of this machine.
	 * @param simulation
	 *            the simulation.
	 * @param random
	 *            the random.
	 */
	public BasicMachine(Technology technology, Commodities input, Simulation simulation, Random random) {

		super(simulation);
		if (technology == null) {
			throw new IllegalArgumentException("Technology is null.");
		}
		final long acquisitionCost;
		if ((input == null && this.getPeriod() == 0)) {
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
		this.productionTime = technology.getProductionTime();
		this.productivity = technology.getProductivity();
		this.increment = new Rational(1, productionTime);
		this.bookValue = acquisitionCost;
		if (getPeriod() == 0) {
			this.start = getPeriod() - random.nextInt((int) technology.getTimelifeMean());
		} else {
			this.start = getPeriod();
		}
		this.cancelled = false;

		this.cancellationDate = this.start + (int) technology.getTimelifeMean();

		this.timelife = (int) (technology.getTimelifeMean() + random.nextGaussian() * technology.getTimelifeStDev());
	}

	@Override
	public long depreciate() {
		if (getPeriod() > this.start + this.timelife) {
			this.cancelled = true;
		}
		final long depreciation;
		if (this.cancelled) {
			depreciation = this.bookValue;
			this.bookValue = 0;
		} else {
			final int remainingTime = cancellationDate - getPeriod();
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
	public Double getUnitProductCost(Map<String, Double> costs) {
		// TODO verifier le bon fonctionnement de cette methode.
		Double result;
		final Double wage = costs.get("Wage");
		if (wage == null) {
			result = null;
		} else {
			result = productivity / wage;
		}
		return result;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
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
	public List<Materials> work(final JobContract jobContract, Materials... workInProcess) {
		if (getPeriod() > this.start + this.timelife) {
			throw new IllegalArgumentException("Broken machine.");
		}
		if (this.lastUsed == getPeriod()) {
			throw new RuntimeException("This machine has already been used in this period.");
		}

		this.lastUsed = getPeriod();


		// On calcule la valeur des intrants: force de travail et inputs
		// matériels.

		long inputValue = jobContract.getWage();

		// Depense de la force de travail.

		jobContract.getWorker().work();

		// Completion des materiaux.

		final List<Materials> outputs = new LinkedList<Materials>();
		long newVolume = (long) (this.productivity) * this.productionTime;
		for (Materials materials : workInProcess) {
			if (materials != null && materials.getVolume() > 0) {
				if (materials.getProductionPeriod() >= getPeriod()) {
					throw new RuntimeException(
							"Materials cannot be used as input in the same period they were produced.");
				}
				final long materialsVolume = materials.getVolume();
				final Rational materialsCompletion = materials.getCompletion();
				final Rational outputCompletion = materialsCompletion.add(increment);
				final Materials output;
				if (materialsVolume == newVolume) {
					output = getNewMaterials(newVolume, materials.getBookValue() + inputValue, outputCompletion,
							getSimulation());
					materials.delete();
					newVolume = 0;
					inputValue = 0;
					outputs.add(output);
					break;
				} else if (materialsVolume > newVolume) {
					final long materialsValue = (long) (1d * materials.getBookValue() * newVolume / materialsVolume);
					output = getNewMaterials(newVolume, materialsValue + inputValue, outputCompletion, getSimulation());
					materials.delete(newVolume, materialsValue);
					newVolume = 0;
					inputValue = 0;
					outputs.add(output);
					break;
				} else {
					final long inputValue2 = inputValue * materialsVolume / newVolume;
					output = getNewMaterials(materialsVolume, materials.getBookValue() + inputValue2, outputCompletion,
							getSimulation());
					materials.delete();
					newVolume -= materialsVolume;
					inputValue -= inputValue2;
					outputs.add(output);
				}
			}
		}
		if (newVolume > 0) {
			final Materials output = new BasicMaterials(newVolume, inputValue, increment, getSimulation());
			outputs.add(output);
			inputValue = 0;
		}
		if (inputValue > 0) {
			throw new RuntimeException("Residual value should be zero.");
		}
		return outputs;
	}

}
