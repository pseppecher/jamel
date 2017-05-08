package jamel.util;

import java.lang.reflect.Method;

/**
 * A basic agent.
 */
public class BasicAgent extends JamelObject implements Agent {

	/**
	 * Returns the specified phase method.
	 * 
	 * @param phase
	 *            the name of the phase.
	 * @return the method that should be called by the specified phase.
	 */
	static public Method getPhaseMethod(final String phase) {
		final Method result;

		try {
			switch (phase) {
			case "opening":
				result = BasicAgent.class.getMethod("open");
				break;
			case "closure":
				result = BasicAgent.class.getMethod("close");
				break;
			default:
				result = null;

			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Something went wrong while creating the method for this phase: " + phase, e);
		}

		return result;
	}

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The parent sector.
	 */
	@SuppressWarnings("unused")
	final private Sector sector;

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * Creates a new basic agent.
	 * 
	 * @param sector
	 *            the parent sector.
	 * @param id
	 *            the id of the agent.
	 */
	public BasicAgent(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		this.dataset = new AgentDataset(this);
	}

	/**
	 * Closes this agent.
	 */
	public void close() {
		// C'est ici qu'on met à jour les données de la période.
		this.dataset.put("countAgent", 1);
		this.dataset.put("alea", this.getRandom().nextGaussian());
		// Jamel.println(this.sector.getName(), this.getName() + " is now
		// closed");
	}

	@Override
	public String getName() {
		return "Agent " + id;
	}

	/**
	 * Opens this agent.
	 */
	public void open() {
		// Jamel.println(this.sector.getName(), this.getName() + " is now
		// open");
	}

	@Override
	public Double getData(String dataKey, String period) {
		return this.dataset.getData(dataKey);
	}

}