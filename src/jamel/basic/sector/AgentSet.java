package jamel.basic.sector;

import jamel.basic.agent.Agent;

import java.util.List;

/**
 * A set of agents.
 * @param <A> the type of agent.
 */
public interface AgentSet<A extends Agent> {

	/**
	 * Collects and returns the data from the agents in this set.
	 * @return an object that contains the data of each agent in this set.
	 */
	SectorDataset collectData();

	/**
	 * Returns <code>true</code> if this set contains an agent for the specified key.
	 * @param key key whose presence in this set is to be tested.
	 * @return <code>true</code> if this set contains an agent for the specified key.
	 */
	boolean contains(String key);
	
	/**
	 * Returns the agent to which the specified key is mapped, or null if this map contains no agent for the key.
	 * @param key the key whose associated agent is to be returned.
	 * @return the agent to which the specified key is mapped, or null if this map contains no agent for the key.
	 */
	A get(String key);

	/**
	 * Returns the list of agents.
	 * @return the list of agents.
	 */
	List<A> getList();

	/**
	 * Returns one agent selected at random.
	 * @return an agent.
	 */
	A getRandomAgent();

	/**
	 * Returns the list of all agents in a random order.
	 * @return the list of all agents in a random order.
	 */
	List<A> getShuffledList();

	/**
	 * Returns a list of agents selected at random.
	 * @param lim the number of agents to select.
	 * @return a list of agents selected at random.
	 */
	List<A> getSimpleRandomSample(Integer lim);

	/**
	 * Appends the specified agent to this AgentSet.
	 * @param agent the agent to be added to this AgentSet.
	 */
	void put(A agent);

	/**
	 * Appends all of the agents in the specified list to this AgentSet.
	 * @param list list containing agents to be added to this AgentSet.
	 */
	void putAll(List<A> list);

	/**
	 * Removes the first occurrence of the specified agent from this set, if it is present. 
	 * If this set does not contain the element, a RuntimeException is thrown.
	 * @param agent agent to be removed from this list, if present.
	 */
	void remove(A agent);

	/**
	 * Removes from this set all of its elements that are contained in the specified list.
	 * @param list list containing agents to be removed from this set.
	 */
	void removeAll(List<A> list);
	
}

// ***
