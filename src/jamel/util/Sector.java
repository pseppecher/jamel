package jamel.util;

import java.util.List;

import jamel.data.DynamicSeries;
import jamel.data.Expression;

/**
 * Represents a sector.
 */
public interface Sector {

	/**
	 * Closes the sector.
	 */
	void close();

	/**
	 * Executes the specified event.
	 * 
	 * @param event
	 *            the event to be executed.
	 */
	void doEvent(Parameters event);

	/**
	 * Returns the {@code Class} of the agents.
	 * 
	 * @return the {@code Class} of the agents.
	 */
	Class<? extends Agent> getAgentClass();

	/**
	 * Returns the specified data.
	 * 
	 * @param args
	 *            arguments.
	 * @return the specified data.
	 */
	Expression getDataAccess(String[] args);

	/**
	 * Returns an expression that provides access to the specified data for the
	 * specified agent.
	 * 
	 * @param agentName
	 *            the name of the agent.
	 * @param args
	 *            strings describing the data to be returned.
	 * @return an expression that provides access to the specified data for the
	 *         specified agent.
	 */
	Expression getIndividualDataAccess(String agentName, String[] args);

	/**
	 * Returns the name of the sector.
	 * 
	 * @return the name of the sector.
	 */
	String getName();

	/**
	 * Returns the parameters of the sector.
	 * 
	 * @return the parameters of the sector.
	 */
	Parameters getParameters();

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	int getPeriod();

	/**
	 * Returns the specified phase.
	 * 
	 * @param phaseName
	 *            the name of the phase.
	 * @param shuffle
	 *            {@code true} if the agents must be shuffled before acting.
	 * @return the specified phase.
	 */
	Phase getPhase(String phaseName, boolean shuffle);

	/**
	 * Returns the specified scatter series.
	 * 
	 * @param xKey
	 *            the key for x values.
	 * @param yKey
	 *            the key for y values.
	 * @param conditions
	 *            the conditions.
	 * @param selection
	 *            the selection criteria.
	 * @return the specified scatter series.
	 */
	DynamicSeries getScatterSeries(String xKey, String yKey, Expression[] conditions, String selection);

	/**
	 * Returns the simulation.
	 * 
	 * @return the simulation.
	 */
	Simulation getSimulation();

	/**
	 * Opens the sector.
	 */
	void open();

	/**
	 * Returns an agent selected at random.
	 * 
	 * @return an agent selected at random.
	 */
	Agent select();

	/**
	 * Returns a random selection of <code>n</code> agents, exclusive of the
	 * specified special agent.
	 * 
	 * Used to avoid auto-selection.
	 * 
	 * @param n
	 *            the number of agents to be selected.
	 * @param special
	 *            an agent to be excluded from the selection.
	 * @return a random selection of <code>n</code> agents.
	 */
	Agent[] select(int n, Agent special);

	/**
	 * Returns a list of all agents of this sector, in a random order.
	 * 
	 * @return a list of all agents of this sector.
	 */
	List<? extends Agent> selectAll();

	/**
	 * Returns a random selection of <code>n</code> agents.
	 * 
	 * @param n
	 *            the number of agents to be selected.
	 * @return a random selection of <code>n</code> agents.
	 */
	Agent[] selectArray(int n);

	/**
	 * Returns a random selection of {@code n} agents.
	 * 
	 * @param n
	 *            the number of agents to be selected.
	 * @return a random selection of {@code n} agents.
	 */
	List<? extends Agent> selectList(int n);

}
