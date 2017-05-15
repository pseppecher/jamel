package jamel.util;

import java.util.function.Consumer;

/**
 * A basic agent.
 */
public class BasicAgent extends JamelObject implements Agent {

	/**
	 * Returns the specified action.
	 * 
	 * @param phaseName
	 *            the name of the action.
	 * @return the specified action.
	 */
	static public Consumer<? super Agent> getAction(final String phaseName) {
		final Consumer<? super Agent> action;
		switch (phaseName) {
		case "opening":
			action = (agent) -> {
				((BasicAgent) agent).open();
			};
			break;
		case "closure":
			action = (agent) -> {
				((BasicAgent) agent).close();
			};
			break;
		default:
			throw new IllegalArgumentException(phaseName);
		}
		return action;
	}

	/**
	 * The dataset.
	 */
	final private AgentDataset dataset;

	/**
	 * The id of this agent.
	 */
	final private int id;

	/**
	 * The parent sector.
	 */
	final private Sector sector;

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
	private void close() {
		// C'est ici qu'on met à jour les données de la période.
		this.dataset.put("countAgent", 1);
		this.dataset.put("alea", this.getRandom().nextGaussian());
		// Jamel.println(this.sector.getName(), this.getName() + " is now
		// closed");
	}

	/**
	 * Opens this agent.
	 */
	private void open() {
		// Jamel.println(this.sector.getName(), this.getName() + " is now
		// open");
	}

	@Override
	public Double getData(String dataKey, String period) {
		return this.dataset.getData(dataKey);
	}

	@Override
	public String getName() {
		return "Agent " + id;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

}