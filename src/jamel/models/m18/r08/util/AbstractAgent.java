package jamel.models.m18.r08.util;

import jamel.models.m18.r08.data.AgentDataset;
import jamel.models.m18.r08.data.BasicAgentDataset;
import jamel.models.m18.r08.data.BasicPeriodDataset;
import jamel.models.m18.r08.data.PeriodDataset;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Sector;

/**
 * An abstract agent.
 */
abstract public class AbstractAgent extends JamelObject implements Agent {

	/**
	 * The id of this agent.
	 */
	final private int id;

	/** A flag that indicates if this firm is open or not. */
	private boolean open;

	/** The current period. */
	private Integer period = null;

	/**
	 * The dataset.
	 */
	protected final AgentDataset agentDataset;

	/**
	 * The dataset.
	 */
	protected PeriodDataset periodDataset = null;

	/**
	 * The parent sector.
	 */
	protected final Sector sector;

	/**
	 * Creates a new agent.
	 * 
	 * @param sector
	 *            the sector.
	 * @param id
	 *            the identification number of the agent.
	 */
	public AbstractAgent(final Sector sector, final int id) {
		super(sector.getSimulation());
		this.sector = sector;
		this.id = id;
		this.agentDataset = new BasicAgentDataset(this);
	}

	/**
	 * Inserts the specified value at the specified position in the period
	 * dataset.
	 * 
	 * @param index
	 *            index with which the specified value is to be associated.
	 * 
	 * @param value
	 *            value to be associated with the specified key.
	 */
	protected void putData(int index, Number value) {
		this.periodDataset.put(index, value);
	}

	@Override
	public void close() {
		if (!this.open) {
			throw new RuntimeException("Already closed.");
		}
		this.open = false;

		this.agentDataset.put(this.periodDataset);
	}

	@Override
	public final int getID() {
		return this.id;
	}

	@Override
	public final String getName() {
		return this.sector.getName() + "." + this.id;
	}
	
	@Override
	public void open() {
		if (this.open) {
			throw new RuntimeException("Already open.");
		}
		this.open = true;
		if (this.period == null) {
			this.period = this.getPeriod();
		} else {
			this.period++;
			if (this.period != getPeriod()) {
				throw new RuntimeException("Bad period");
			}
		}
		this.periodDataset = new BasicPeriodDataset(this);
	}

}
