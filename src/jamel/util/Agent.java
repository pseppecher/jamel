package jamel.util;

import jamel.Jamel;

/**
 * Represents an agent.
 */
public interface Agent {

	/**
	 * Closes this agent.
	 * 
	 * Should be called at the end of the period.
	 */
	void close();

	/**
	 * Forces the execution of the specified event.
	 * 
	 * @param event
	 *            the event to be executed.
	 */
	void doEvent(Parameters event);

	/**
	 * Returns the specified data.
	 * 
	 * @param dataIndex
	 *            the index of the data to be returned.
	 * @param t
	 *            the period of the data to be returned.
	 * @return the specified data.
	 */
	Double getData(int dataIndex, int t);

	/**
	 * Returns the ID number of this agent.
	 * @return the ID number of this agent.
	 */
	default int getID() {
		Jamel.notUsed();
		return 0;
	}

	/**
	 * Returns the name of the agent.
	 * 
	 * @return the name of the agent.
	 */
	String getName();

	/**
	 * Returns the simulation.
	 * 
	 * @return the simulation.
	 */
	Simulation getSimulation();

	/**
	 * Opens this agent.
	 * 
	 * Should be called at the beginning of the period.
	 */
	void open();

	/**
	 * Returns {@code true} if this agent satisfies the specified criteria,
	 * {@code false} otherwise.
	 * 
	 * @param criteria
	 *            a string that contains the criteria to be satisfied.
	 * @return {@code true} if this agent satisfies the specified criteria,
	 *         {@code false} otherwise.
	 */
	boolean satisfy(String criteria);

}
