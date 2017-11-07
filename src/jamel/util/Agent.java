package jamel.util;

import org.jfree.data.xy.XYDataItem;

/**
 * Represents an agent.
 */
public interface Agent {

	void close();

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
	 * Returns the specified data.
	 * 
	 * @param dataKey
	 *            the key of the data to be returned.
	 * @param t
	 *            the period of the data to be returned.
	 * @return the specified data.
	 */
	Double getData(String dataKey, int t);

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

	XYDataItem getXYDataItem(String x, String y, int period);

	void open();

	boolean satisfy(String criteria);

}
