package jamel.basic.agents.util;

import jamel.basic.agents.roles.Agent;

import java.util.List;
import java.util.Map;

/**
 * A set of agents.
 * @param <T> the type of agent.
 */
public interface AgentSet<T extends Agent> {

	/**
	 * Collects and returns the data from the agents in this set.
	 * @param keys the keys of the data to be collected.
	 * @param prefix a string to add at the start of the keys (usually the name of the sector).
	 * @return a map that associates each key with the corresponding data.
	 */
	Map<String,Double> collectData(final List<String> keys, final String prefix);

	/**
	 * Returns the list of agents.
	 * @return the list of agents.
	 */
	List<T> getList();
	
	/**
	 * Returns one agent selected at random.
	 * @return an agent.
	 */
	T getRandomAgent();

	/**
	 * Returns a list of agents selected at random.
	 * @param lim the number of agents to select.
	 * @return a list of agents selected at random.
	 */
	List<T> getRandomList(Integer lim);

	/**
	 * Returns the list of all agents in a random order.
	 * @return the list of all agents in a random order.
	 */
	List<T> getShuffledList();

	/**
	 * Appends all of the agents in the specified list to this AgentSet.
	 * @param list list containing agents to be added to this AgentSet.
	 */
	void putAll(List<T> list);

	/**
	 * Removes from this set all of its elements that are contained in the specified list.
	 * @param list list containing agents to be removed from this set.
	 */
	void removeAll(List<T> list);

	/**
	 * Removes the first occurrence of the specified agent from this set, if it is present. 
	 * If this set does not contain the element, a RuntimeException is thrown.
	 * @param agent agent to be removed from this list, if present.
	 */
	void remove(T agent);
	
}

