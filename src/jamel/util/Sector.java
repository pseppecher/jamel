package jamel.util;

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
	 * Returns an expression that provides an access to the specified data.
	 * 
	 * @param args
	 *            the arguments specifying the data to be accessed through the
	 *            expression.
	 * @return an expression.
	 */
	Expression getDataAccess(String[] args);

	/**
	 * Returns the name of this sector.
	 * 
	 * @return the name of this sector.
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
	Integer getPeriod();

	/**
	 * Returns the specified phase.
	 * 
	 * @param name
	 *            the name of the phase to be returned.
	 * @param options
	 *            an array of strings, each of them specifying one option of the
	 *            phase.
	 * 
	 * @return the specified phase.
	 */
	Phase getPhase(String name, String[] options);

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	Simulation getSimulation();

	/**
	 * Opens the sector.
	 */
	void open();

	/**
	 * Returns a random selection of <code>n</code> agents.
	 * 
	 * @param n
	 *            the number of agents to be selected.
	 * @return a random selection of <code>n</code> agents.
	 */
	Agent[] select(int n);

}
