package jamel.jamel.firms.factory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jamel.basic.util.Timer;
import jamel.jamel.firms.Technology;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.Commodities;
import jamel.jamel.widgets.LaborPower;

/**
 * A basic machine.
 */
public class BasicMachine implements Machine {

	/**
	 * Creates and returns the specified materials.
	 * 
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
	private static Materials getNewMaterials(final long volume, final long value, final Rational completion,
			final Timer timer) {
		final Materials result;
		if (completion.equals(1)) {
			result = new FinishedGoods(volume, value);
		} else {
			result = new BasicMaterials(volume, value, completion, timer);
		}
		return result;
	}

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
	private double productivity;

	/** The timer. */
	private final Timer timer;

	/**
	 * The date when this machine was created. The start of its depreciation
	 * process.
	 */
	private final int startDate;

	/**
	 * The book value of this machine.
	 */
	private long bookValue;

	/**
	 * The random.
	 */
	private final Random random;

	/**
	 * Constructs a new basic machine.
	 * 
	 * @param productionTime
	 *            the production time.
	 * @param productivity
	 *            the productivity.
	 * @param acquisitionCost
	 *            the acquisition cost of the new machine.
	 * @param timer
	 *            the timer.
	 * @param random
	 *            the random.
	 */
	public BasicMachine(int productionTime, long productivity, long acquisitionCost, Timer timer, Random random) {
		this.random = random;
		this.productionTime = productionTime;
		this.productivity = productivity;
		this.increment = new Rational(1, productionTime);
		this.bookValue = acquisitionCost;
		this.timer = timer;
		if (timer.getPeriod().intValue()==0) {
			this.startDate = timer.getPeriod().intValue()-random.nextInt(100);			
		} else {
			this.startDate = timer.getPeriod().intValue();			
		}
		this.cancelled = false;
	}

	/**
	 * Constructs a new basic machine.
	 * 
	 * @param technology
	 *            the technology of the new machine.
	 * 
	 * @param input
	 *            the raw materials used to create the new machine.
	 * @param timer
	 *            the timer.
	 * @param random
	 *            the random.
	 */
	public BasicMachine(Technology technology, Commodities input, Timer timer, Random random) {
		this(technology.getProductionTime(), technology.getProductivity(), input.getValue(), timer, random);
		if (input.getVolume() > technology.getInputVolumeForANewMachine()) {
			throw new IllegalArgumentException("To many input to create this machine.");
		}
		if (input.getVolume() < technology.getInputVolumeForANewMachine()) {
			throw new IllegalArgumentException("Not enough input to create this machine.");
		}
		input.consume();
	}

	@Override
	public Long getBookValue() {
		return this.bookValue;
		// TODO: DEPRECIATION OVER TIME
	}

	@Override
	public long getProductivity() {
		return (long) this.productivity;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * The production function.
	 * <p>
	 * This production function implements important features:
	 * <ul>
	 * <li>Production takes time;</li>
	 * <li>Inventories are valuated at production cost.</li>
	 * </ul>
	 * 
	 * @param laborPower
	 *            {@link LaborPower} of a worker.
	 * @param inputs
	 *            list of inputs.
	 * @return list of outputs.
	 */
	@Override
	public List<Materials> work(final LaborPower laborPower, Materials... inputs) {
		if (laborPower.isExhausted()) {
			throw new IllegalArgumentException("This labor power is exhausted.");
		}
		if (this.lastUsed == this.timer.getPeriod().intValue()) {
			throw new AnachronismException("This machine has already been used in this period.");
		}
		this.lastUsed = this.timer.getPeriod().intValue();
		final List<Materials> result = new LinkedList<Materials>();
		long wageCost = laborPower.getWage();
		laborPower.expend();
		long newVolume = (long) (this.productivity) * this.productionTime;
		for (Materials input : inputs) {
			if (input != null && input.getVolume() > 0) {
				if (input.getProductionPeriod() >= this.timer.getPeriod().intValue()) {
					throw new AnachronismException(
							"Materials cannot be used as input in the same period they were produced.");
				}
				final long inputVolume = input.getVolume();
				final Rational inputCompletion = input.getCompletion();
				final Rational outputCompletion = inputCompletion.add(increment);
				final Materials output;
				if (inputVolume == newVolume) {
					output = getNewMaterials(newVolume, input.getBookValue() + wageCost, outputCompletion, timer);
					input.delete();
					newVolume = 0;
					wageCost = 0;
					result.add(output);
					break;
				} else if (inputVolume > newVolume) {
					final long inputValue = input.getBookValue() * newVolume / inputVolume;
					output = getNewMaterials(newVolume, inputValue + wageCost, outputCompletion, timer);
					input.delete(newVolume, inputValue);
					newVolume = 0;
					wageCost = 0;
					result.add(output);
					break;
				} else {
					final long wageCost2 = wageCost * inputVolume / newVolume;
					final long outputValue = input.getBookValue() + wageCost2;
					output = getNewMaterials(inputVolume, outputValue, outputCompletion, timer);
					input.delete();
					newVolume -= inputVolume;
					wageCost -= wageCost2;
					result.add(output);
				}
			}
		}
		if (newVolume > 0) {
			final Materials output = new BasicMaterials(newVolume, wageCost, increment, timer);
			result.add(output);
		}

		// final int age = timer.getPeriod().intValue() -startDate;
		/*
		 * if (age>180) { if (this.random.nextFloat() > 0.9) { this.cancelled =
		 * true; } } else if (age>120) { if (this.random.nextFloat() > 0.99) {
		 * this.cancelled = true; } } else if (age>60){ if
		 * (this.random.nextFloat() > 0.995) { this.cancelled = true; } }
		 */
		return result;
	}

	@Override
	public long depreciate() {
		final double age = timer.getPeriod().intValue() - startDate;// TODO FIXME
		final double cancellationProbability = Math.pow((age-50)/150,3); 
		//final double cancellationProbability = Math.pow((age)/180,5); 
		if (this.random.nextFloat() < cancellationProbability) {
			this.cancelled = true;
			//Jamel.println(age);
		}
		final long depreciation;
		if (this.cancelled) {
			depreciation = this.bookValue;
			this.bookValue = 0;
		} else {
			final int cancellationDate = this.startDate + 120;
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

}

// ***
