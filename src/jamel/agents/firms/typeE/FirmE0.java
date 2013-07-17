package jamel.agents.firms.typeE;

import jamel.agents.firms.BasicFirm;
import jamel.agents.firms.Labels;
import jamel.agents.roles.CapitalOwner;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An extension of the BasicFirm that implements the EvolutionaryFirm interface.
 * When created, the firm uses an evolutionary procedure to select its sector of production.
 */
public class FirmE0 extends BasicFirm implements EvolutionaryFirm {

	/** The size of the memory of the manager. */
	private static final long memory = 12; // TODO should be a parameter.

	/** The fitness of the firm. */
	private Double fitness;

	/** The list of the lasts profits. */
	final private LinkedList<Long> profitsList = new LinkedList<Long>();

	/**
	 * Creates a new firm with the given parameters.
	 * @param aName the name. 
	 * @param owner the owner.
	 * @param someParameters a map that contains parameters.
	 */
	public FirmE0(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}

	/**
	 * Initializes the new firm.
	 */
	@Override
	protected void init() {
		super.init();
		final EvolutionManager0 evolutionManager0 = new EvolutionManager0(this);
		evolutionManager0.selectNewParents();
		final Parents parents = evolutionManager0.getParents();
		if (parents!=null) {
			final Map<String, Object> parameters = parents.getFirst().getParameters();
			for(Entry<String, Object> entry : parameters.entrySet()) {
				this.blackboard.put(entry.getKey(), entry.getValue(), null);
			}
			return;
		}
	}

	/**
	 * Calculates the profit of the period.
	 */
	private void updateFitness() {
		this.profitsList.add((Long) this.blackboard.get(Labels.GROSS_PROFIT));
		if (this.profitsList.size()>memory)
			this.profitsList.removeFirst();
		Double averageProfit=null;
		if (this.profitsList.size()==memory) {
			long sum = 0;
			for (Long profit: profitsList) {
				sum+=profit;
			}
			averageProfit = (double) (sum/memory);
		}		
		this.fitness=null;
		if (averageProfit!=null) {
			final int machinery = (Integer)this.blackboard.get(Labels.MACHINERY);
			this.fitness = averageProfit/machinery;
		}
	}
	
	/**
	 * Updates the fitness and then closes the firm.
	 */
	@Override
	public void close() {
		this.updateFitness();
		super.close();
	}

	/**
	 * Returns the fitness of this firm.
	 * @return the fitness.
	 */
	@Override
	public Double getFitness() {
		return this.fitness;
	}
	
}
