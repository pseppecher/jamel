package jamel.basic.sector;

import jamel.basic.agent.Agent;
import jamel.basic.data.BasicSectorDataset;
import jamel.basic.data.SectorDataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * A basic set of agents.
 * @param <A> the type of agents in the set.
 */
public class BasicAgentSet<A extends Agent> implements AgentSet<A> {

	/** The arrayList. */
	private final ArrayList<A> arrayList = new ArrayList<A>();

	/** The map. */
	private final Map<String,A> map = new TreeMap<String,A>();

	/** The random. */
	private final Random random;

	/**
	 * Creates a new BasicAgentSet.
	 * @param random the random generator.
	 */
	public BasicAgentSet(Random random) {
		this.random = random;
	}

	@Override
	public void clear() {
		this.arrayList.clear();
		this.map.clear();
	}

	@Override
	public SectorDataset collectData() {
		final SectorDataset sectorDataset = new BasicSectorDataset();
		for (final Agent agent:arrayList) {
			sectorDataset.putIndividualData(agent.getData());
		}
		return sectorDataset;	
	}

	@Override
	public boolean contains(A agent) {
		return false;
	}

	@Override
	public boolean contains(String agentName) {
		return this.map.containsKey(agentName);
	}

	@Override
	public A get(String agentName) {
		return this.map.get(agentName);
	}

	@Override
	public List<A> getList() {
		return new ArrayList<A>(arrayList);
	}

	@Override
	public A getRandomAgent() {
		final int size = this.arrayList.size();
		final A result;
		if (size>0) {
			result = this.arrayList.get(this.random.nextInt(size));
		}
		else {
			result = null;
		}
		return result;
	}

	@Override
	public List<A> getShuffledList() {
		final List<A> list = new ArrayList<A>(arrayList);
		Collections.shuffle(list, random);
		return list;
	}

	@Override
	public List<A> getSimpleRandomSample(Integer lim) {
		final int size = this.arrayList.size();
		final List<A> selection = new LinkedList<A>();
		if (lim<size) {
			for (int count = 0; count<lim; count++) {
				A selected = this.arrayList.get(this.random.nextInt(size));
				if (!selection.contains(selected)) {
					selection.add(selected);
				}
			}
		}
		return selection;
	}

	@Override
	public void put(A agent) {
		this.arrayList.add(agent);
		this.map.put(agent.getName(), agent);
	}

	@Override
	public void putAll(List<A> list) {
		for(A agent:list) {
			put(agent);
		}
	}

	@Override
	public void remove(A agent) {
		if (!this.arrayList.remove(agent)) {
			throw new RuntimeException("Not found.");
		}
		if (this.map.remove(agent.getName()) != agent) {
			throw new RuntimeException("Bad agent or null.");			
		}
	}

	@Override
	public void removeAll(List<A> list) {
		for(A t:list) {
			remove(t);
		}
	}

}

// ***
