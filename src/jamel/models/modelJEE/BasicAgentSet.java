package jamel.models.modelJEE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import jamel.util.Agent;

/**
 * A basic set of agents.
 * 
 * @param <A>
 *            the type of agents in the set.
 */
class BasicAgentSet<A extends Agent> {

	/** The arrayList. */
	private final ArrayList<A> arrayList = new ArrayList<A>();

	/** The map. */
	private final Map<String, A> map = new TreeMap<String, A>();

	/** The random. */
	private final Random random;

	/**
	 * Creates a new BasicAgentSet.
	 * 
	 * @param random
	 *            the random generator.
	 */
	BasicAgentSet(Random random) {
		this.random = random;
	}

	/**
	 * Removes all elements from the <code>AgentSet</code>.
	 */
	void clear() {
		this.arrayList.clear();
		this.map.clear();
	}

	/**
	 * Returns <code>true</code> if this set contains the specified agent.
	 * 
	 * @param agent
	 *            the agent to be tested.
	 * @return <code>true</code> if this set contains the specified agent.
	 */
	boolean contains(A agent) {
		return this.map.containsValue(agent);
	}

	/**
	 * Returns <code>true</code> if this set contains an agent for the specified
	 * key.
	 * 
	 * @param key
	 *            key whose presence in this set is to be tested.
	 * @return <code>true</code> if this set contains an agent for the specified
	 *         key.
	 */
	boolean contains(String key) {
		return this.map.containsKey(key);
	}

	/**
	 * Returns the agent to which the specified key is mapped, or null if this
	 * map contains no agent for the key.
	 * 
	 * @param key
	 *            the key whose associated agent is to be returned.
	 * @return the agent to which the specified key is mapped, or null if this
	 *         map contains no agent for the key.
	 */
	A get(String key) {
		return this.map.get(key);
	}

	/**
	 * Returns the list of agents.
	 * 
	 * @return the list of agents.
	 */
	List<A> getList() {
		return new ArrayList<A>(arrayList);
	}

	/**
	 * Returns one agent selected at random.
	 * 
	 * @return an agent.
	 */
	A getRandomAgent() {
		final int size = this.arrayList.size();
		final A result;
		if (size > 0) {
			result = this.arrayList.get(this.random.nextInt(size));
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Returns the list of all agents in a random order.
	 * 
	 * @return the list of all agents in a random order.
	 */
	List<A> getShuffledList() {
		final List<A> list = new ArrayList<A>(arrayList);
		Collections.shuffle(list, random);
		return list;
	}

	/**
	 * Appends the specified agent to this AgentSet.
	 * 
	 * @param agent
	 *            the agent to be added to this AgentSet.
	 */
	void put(A agent) {
		this.arrayList.add(agent);
		this.map.put(agent.getName(), agent);
	}

	/**
	 * Appends all of the agents in the specified list to this AgentSet.
	 * 
	 * @param list
	 *            list containing agents to be added to this AgentSet.
	 */
	void putAll(List<A> list) {
		for (A agent : list) {
			put(agent);
		}
	}

	/**
	 * Removes the first occurrence of the specified agent from this set, if it
	 * is present.
	 * If this set does not contain the element, a RuntimeException is thrown.
	 * 
	 * @param agent
	 *            agent to be removed from this list, if present.
	 */
	void remove(A agent) {
		if (!this.arrayList.remove(agent)) {
			throw new RuntimeException("Not found.");
		}
		if (this.map.remove(agent.getName()) != agent) {
			throw new RuntimeException("Bad agent or null.");
		}
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified list.
	 * 
	 * @param list
	 *            list containing agents to be removed from this set.
	 */
	void removeAll(List<A> list) {
		for (A t : list) {
			remove(t);
		}
	}

	/**
	 * Returns a list of agents selected at random.
	 * 
	 * @param lim
	 *            the number of agents to select.
	 * @return a list of agents selected at random.
	 */
	List<A> select(Integer lim) {
		final int size = this.arrayList.size();
		final List<A> selection;
		if (lim < size) {
			selection = new LinkedList<A>();
			for (int count = 0; count < lim; count++) {
				A selected = this.arrayList.get(this.random.nextInt(size));
				if (!selection.contains(selected)) {
					selection.add(selected);
				}
			}
		} else {
			selection = getShuffledList();
		}
		return selection;
	}

	/**
	 * Returns the number of agents in this set.
	 * 
	 * @return the number of agents in this set.
	 */
	int size() {
		return this.arrayList.size();
	}

}
