package jamel.basic.agents.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jamel.basic.agents.roles.Agent;
import jamel.util.Circuit;

/**
 * A basic set of agents.
 * @param <T> the type of agents in the set.
 */
public class BasicAgentSet<T extends Agent> implements AgentSet<T> {

	/** The arrayList. */
	private final ArrayList<T> arrayList = new ArrayList<T>();

	/** The hashMap. */
	private final Map<String,T> map = new TreeMap<String,T>();

	@Override
	public Map<String,Double> collectData(final List<String> keys, final String prefix) {
		return new HashMap<String,Double>() {
			private static final long serialVersionUID = 1L;
			{
				for (final Agent agent:arrayList) {
					final String agentPrefix=agent.getName()+".";
					for (final String key:keys) {
						if (key.startsWith(agentPrefix)) {
							final Double value = agent.getData(key.substring(agentPrefix.length()));
							if (value!=null) {
								this.put(prefix+key,value);								
							}
						}
						else {
							final Double value = agent.getData(key);
							if (value!=null) {
								if (this.containsKey(prefix+key)) {
									this.put(prefix+key,this.get(prefix+key) + value);							
								}
								else {
									this.put(prefix+key,value);
								}
							}
							else {
								//System.out.println("BasicAgentSet.collectData(): "+key+" not found"); // DELETE
							}
						}
					}
				}
			}};
	}

	@Override
	public List<T> getList() {
		return new ArrayList<T>(arrayList);
	}

	@Override
	public T getRandomAgent() {
		final int size = this.arrayList.size();
		return this.arrayList.get(Circuit.getRandom().nextInt(size));
	}

	@Override
	public List<T> getRandomList(Integer lim) {
		final int size = this.arrayList.size();
		if (lim>size) {
			throw new IllegalArgumentException("Not enough agents.");
		}
		final List<T> selection = new LinkedList<T>();
		for (int count = 0; count<lim; count++) {
			T selected = this.arrayList.get(Circuit.getRandom().nextInt(size));
			if (!selection.contains(selected)) {
				selection.add(selected);
			}
		}
		return selection;
	}

	@Override
	public List<T> getShuffledList() {
		final List<T> list = new ArrayList<T>(arrayList);
		Collections.shuffle(list, Circuit.getRandom());
		return list;
	}

	@Override
	public void putAll(List<T> list) {
		this.arrayList.addAll(list);
		for(T t:list) {
			this.map.put(t.getName(), t);
		}
	}

	@Override
	public void remove(T agent) {
		if (!this.arrayList.remove(agent)) {
			throw new RuntimeException("Not found.");
		}
		if (this.map.remove(agent.getName()) != agent) {
			throw new RuntimeException("Bad agent or null.");			
		};
	}

	@Override
	public void removeAll(List<T> list) {
		for(T t:list) {
			remove(t);
		}
	}

}
