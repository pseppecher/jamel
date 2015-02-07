package jamel.basic.agents.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jamel.basic.agents.roles.Agent;
import jamel.basic.data.dataSets.BasicSectorDataSet;
import jamel.basic.data.dataSets.SectorDataset;
import jamel.util.Circuit;

/**
 * A basic set of agents.
 * @param <A> the type of agents in the set.
 */
public class BasicAgentSet<A extends Agent> implements AgentSet<A> {

	/**
	 * Represents an instruction targeting one individual agent.
	 * @since 22-11-2014
	 */
	private interface Instruction {

		/**
		 * Returns the additional arguments of the instruction.
		 * @return the additional arguments of the instruction.
		 */
		Object[] getArgs();

		/**
		 * Returns the key of the instruction.
		 * @return the key of the instruction.
		 */
		String getKey();

		/**
		 * Returns the name of the recipient of this instruction.
		 * @return the name of the recipient of this instruction.
		 */
		Object getRecipient();

	}

	/** The arrayList. */
	private final ArrayList<A> arrayList = new ArrayList<A>();

	/** The hashMap. */
	private final Map<String,A> map = new TreeMap<String,A>();

	/** 
	 * A list of instructions for not-yet-created agents.
	 * @since 22-11-2014
	 */
	private final List<Instruction> waitingInstructions = new LinkedList<Instruction>();

	@Override
	public SectorDataset collectData() {
		final SectorDataset sectorDataset = new BasicSectorDataSet(arrayList.size());
		for (final Agent agent:arrayList) {
			sectorDataset.put(agent.getData());
		}
		return sectorDataset;	
	}

	@Override
	public boolean contains(String agentName) {
		return this.map.containsKey(agentName);
	}

	/*
	 * (non-Javadoc)
	 * @see jamel.basic.agents.util.AgentSet#execute(java.lang.String, java.lang.Object[])
	 * @since 23-11-2014
	 */
	@Override
	public Object execute(final String recipient, Object[] args) {
		final Object result;
		final String instructionKey = (String) args[0];
		final Object[] instructionArgs;
		if (args.length==1) {
			instructionArgs=new Object[0];
		}
		else {
			instructionArgs = Arrays.copyOfRange(args, 1, args.length);	
		}
		if (this.contains(recipient)) {
			// The targeted agent exists: immediate execution of the instruction.
			result = this.get(recipient).execute(instructionKey,instructionArgs);				
		}
		else {
			// The targeted agent does not exist yet: the instruction is stored, waiting for the agent creation.
			this.waitingInstructions.add(new Instruction(){
				
				@Override
				public Object[] getArgs() {
					return instructionArgs;
				}
				
				@Override
				public String getKey() {
					return instructionKey;
				}
				
				@Override
				public Object getRecipient() {
					return recipient;
				}
				
			});
			result = null;
		}
		return result;
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
			result = this.arrayList.get(Circuit.getRandom().nextInt(size));
		}
		else {
			result = null;
		}
		return result;
	}

	@Override
	public List<A> getShuffledList() {
		final List<A> list = new ArrayList<A>(arrayList);
		Collections.shuffle(list, Circuit.getRandom());
		return list;
	}

	@Override
	public List<A> getSimpleRandomSample(Integer lim) {
		final int size = this.arrayList.size();
		final List<A> selection = new LinkedList<A>();
		if (lim<size) {
			for (int count = 0; count<lim; count++) {
				A selected = this.arrayList.get(Circuit.getRandom().nextInt(size));
				if (!selection.contains(selected)) {
					selection.add(selected);
				}
			}
		}
		return selection;
	}

	@Override
	public void putAll(List<A> list) {
		this.arrayList.addAll(list);
		for(A agent:list) {
			this.map.put(agent.getName(), agent);
			// Handling waiting instructions.
			// (since 23-11-2014)
			final Iterator<Instruction> it = this.waitingInstructions.iterator();
			while(it.hasNext()) {
				final Instruction instruction = it.next();
				if (agent.getName().equals(instruction.getRecipient())) {
					it.remove();
					agent.execute(instruction.getKey(), instruction.getArgs());
				}
			}
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
