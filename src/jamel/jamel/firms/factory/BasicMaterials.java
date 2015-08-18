package jamel.jamel.firms.factory;

import jamel.basic.util.Timer;

/**
 * A heap of basic materials.
 */
class BasicMaterials implements Materials {

	/** The completion of the materials. */
	private final Rational completion;

	/** When the materials were produced. */
	private int productionPeriod;

	/** The value of the materials. */
	private long value;

	/** The volume of the materials. */
	private long volume;

	/**
	 * Constructs a new heap of materials.
	 * 
	 * @param volume
	 *            the volume of the materials.
	 * @param value
	 *            the value of the materials.
	 * @param completion
	 *            the completion of the materials.
	 * @param timer
	 *            the timer.
	 */
	public BasicMaterials(long volume, long value, Rational completion,
			Timer timer) {
		if (completion.doubleValue() <= 0 || completion.doubleValue() > 1) {
			throw new IllegalArgumentException("Bad completion: " + completion);
		}
		this.volume = volume;
		this.value = value;
		this.completion = completion;
		this.productionPeriod = timer.getPeriod().intValue();
	}

	@Override
	public void add(Materials stuff) {
		if (!stuff.getCompletion().equals(this.completion)) {
			throw new IllegalArgumentException("Bad completion: "
					+ stuff.getCompletion());
		}
		this.productionPeriod = Math.max(this.productionPeriod,
				stuff.getProductionPeriod());
		this.volume += stuff.getVolume();
		this.value += stuff.getBookValue();
		stuff.delete();
	}

	@Override
	public void delete() {
		this.volume = 0;
		this.value = 0;
	}

	@Override
	public void delete(long volume, long value) {
		this.value -= value;
		this.volume -= volume;
	}

	@Override
	public Long getBookValue() {
		return this.value;
	}

	@Override
	public Rational getCompletion() {
		return this.completion;
	}

	@Override
	public int getProductionPeriod() {
		return this.productionPeriod;
	}

	@Override
	public long getVolume() {
		return this.volume;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

}

// ***
