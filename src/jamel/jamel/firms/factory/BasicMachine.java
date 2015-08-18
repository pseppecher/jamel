package jamel.jamel.firms.factory;

import java.util.LinkedList;
import java.util.List;

import jamel.basic.util.Timer;
import jamel.jamel.util.AnachronismException;
import jamel.jamel.widgets.LaborPower;

/**
 * A basic machine.
 */
class BasicMachine implements Machine {

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
	private static Materials getNewMaterials(final long volume,
			final long value, final Rational completion, final Timer timer) {
		final Materials result;
		if (completion.equals(1)) {
			result = new FinishedGoods(volume, value);
		} else {
			result = new BasicMaterials(volume, value, completion, timer);
		}
		return result;
	}

	/** How much materials are completed by period of production. */
	private final Rational increment;

	/** The last time this machine was used. */
	private int lastUsed = -1;

	/** The production time, or the number of stages in the production process. */
	private int productionTime;

	/**
	 * The productivity, or the volume of finished goods the machine can produce
	 * each period on average.
	 */
	private final long productivity;

	/** The timer. */
	private final Timer timer;

	/**
	 * Constructs a new basic machine.
	 * 
	 * @param productionTime
	 *            the production time.
	 * @param productivity
	 *            the productivity.
	 * @param timer
	 *            the timer.
	 */
	public BasicMachine(int productionTime, long productivity, Timer timer) {
		this.productionTime = productionTime;
		this.productivity = productivity;
		this.increment = new Rational(1, productionTime);
		this.timer = timer;
	}

	@Override
	public long getProductivity() {
		return this.productivity;
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
	 *            {@linkLaborPower} of a worker.
	 * @param inputs
	 *            list of inputs.
	 * @return list of outputs.
	 */
	@Override
	public List<Materials> work(final LaborPower laborPower,
			Materials... inputs) {
		if (laborPower.isExhausted()) {
			throw new IllegalArgumentException("This labor power is exhausted.");
		}
		if (this.lastUsed == this.timer.getPeriod().intValue()) {
			throw new AnachronismException(
					"This machine has already been used in this period.");
		}
		this.lastUsed = this.timer.getPeriod().intValue();
		final List<Materials> result = new LinkedList<Materials>();
		long wageCost = laborPower.getWage();
		laborPower.expend();
		long newVolume = this.productivity * this.productionTime;
		for (Materials input : inputs) {
			if (input != null && input.getVolume() > 0) {
				if (input.getProductionPeriod() >= this.timer.getPeriod()
						.intValue()) {
					throw new AnachronismException(
							"Materials cannot be used as input in the same period they were produced.");
				}
				final long inputVolume = input.getVolume();
				final Rational inputCompletion = input.getCompletion();
				final Rational outputCompletion = inputCompletion
						.add(increment);
				final Materials output;
				if (inputVolume == newVolume) {
					output = getNewMaterials(newVolume, input.getBookValue()
							+ wageCost, outputCompletion, timer);
					input.delete();
					newVolume = 0;
					wageCost = 0;
					result.add(output);
					break;
				} else if (inputVolume > newVolume) {
					final long inputValue = input.getBookValue() * newVolume
							/ inputVolume;
					output = getNewMaterials(newVolume, inputValue + wageCost,
							outputCompletion, timer);
					input.delete(newVolume, inputValue);
					newVolume = 0;
					wageCost = 0;
					result.add(output);
					break;
				} else {
					final long wageCost2 = wageCost * inputVolume / newVolume;
					final long outputValue = input.getBookValue() + wageCost2;
					output = getNewMaterials(inputVolume, outputValue,
							outputCompletion, timer);
					input.delete();
					newVolume -= inputVolume;
					wageCost -= wageCost2;
					result.add(output);
				}
			}
		}
		if (newVolume > 0) {
			final Materials output = new BasicMaterials(newVolume, wageCost,
					increment, timer);
			result.add(output);
		}
		return result;
	}

}

// ***
