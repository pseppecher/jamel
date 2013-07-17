package jamel.agents.firms.typeE;

import jamel.Circuit;
import jamel.agents.firms.BasicFirm;
import jamel.agents.firms.Firm;
import jamel.agents.firms.Labels;
import jamel.agents.roles.CapitalOwner;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * An extension of the BasicFirm that implements the EvolutionaryFirm interface.
 * When created, the firm uses an evolutionary procedure to select its sector of production.
 */
public class FirmE1 extends BasicFirm implements EvolutionaryFirm {

	/** The firm comparator. */
	protected static final Comparator<EvolutionaryFirm> FIRM_COMPARATOR = new FirmComparator();

	/** The number of observations to calculate the average profit. */
	private static final long observations = 12; // TODO should be a parameter.

	/** The size of the random selection in which the parents will be selected. */
	protected static final int tournament = 20; // Should be a parameter.

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
	public FirmE1(String aName, CapitalOwner owner, Map<String, String> someParameters) {
		super(aName, owner, someParameters);
	}

	/**
	 * Imitates the characteristics of the best firm selected by a tournament procedure.
	 */
	protected void imitation() {			
		final TreeSet<EvolutionaryFirm> firmsSet = new TreeSet<EvolutionaryFirm>(FIRM_COMPARATOR);
		final List<Firm> list2 = new LinkedList<Firm>(Circuit.getRandomFirms(tournament));
		for (Firm firm: list2) {
			if ((firm!=this) && (((EvolutionaryFirm) firm).getFitness()!=null)) {
				firmsSet.add((EvolutionaryFirm) firm);
			}
		}
		if (firmsSet.size()>0) {
			final Map<String, Object> parameters = firmsSet.first().getParameters();
			for(Entry<String, Object> entry : parameters.entrySet()) {
				this.blackboard.put(entry.getKey(), entry.getValue(), null);
			}
		}
	}
	
	/**
	 * Calculates the profit of the period.
	 */
	private void updateFitness() {
		this.profitsList.add((Long) this.blackboard.get(Labels.GROSS_PROFIT));
		if (this.profitsList.size()>observations)
			this.profitsList.removeFirst();
		Double averageProfit=null;
		if (this.profitsList.size()==observations) {
			long sum = 0;
			for (Long profit: profitsList) {
				sum+=profit;
			}
			averageProfit = (double) (sum/observations);
		}		
		this.fitness=null;
		if (averageProfit!=null) {
			final int machinery = (Integer)this.blackboard.get(Labels.MACHINERY);
			this.fitness = averageProfit/machinery;
		}
	}

	/**
	 * Initializes the new firm.
	 */
	@Override
	protected void init() {
		super.init();
		this.imitation();
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





















