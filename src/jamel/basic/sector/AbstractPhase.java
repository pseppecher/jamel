package jamel.basic.sector;

/**
 * An abstract phase.
 */
abstract public class AbstractPhase implements Phase {
	
	/** The name of the phase. */
	final private String name;
	
	/** The sector. */
	final private Sector sector;
	
	/**
	 * Creates a new phase.
	 * @param name the phase name.
	 * @param sector the sector.
	 */
	public AbstractPhase(String name, Sector sector) {
		this.name=name;
		this.sector=sector;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Sector getSector() {
		return this.sector;
	}

}

