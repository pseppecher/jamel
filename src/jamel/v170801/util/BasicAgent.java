package jamel.v170801.util;

import java.util.function.Consumer;

import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Sector;
import jamel.v170801.data.AgentDataset;

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
		case "doesSomething":
			action = (agent) -> {
				((BasicAgent) agent).doesSomething();
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
	 * Does something.
	 */
	private void doesSomething() {
		// C'est ici qu'on met à jour les données de la période.
		this.dataset.put("count", 1);
		for(int i=0;i<10;i++) {
			this.dataset.put("alea"+i, this.getRandom().nextGaussian());
		}
	}

	/**
	 * Closes this agent.
	 */
	@Override
	public void close() {
		super.close();
		this.dataset.close();
	}

	@Override
	public Double getData(String dataKey, int period) {
		return this.dataset.getData(dataKey,period);
	}

	@Override
	public String getName() {
		return "Agent " + id;
	}

	@Override
	public Sector getSector() {
		return this.sector;
	}

	/**
	 * Opens this agent.
	 */
	@Override
	public void open() {
		super.open();
		this.dataset.open();
	}

}