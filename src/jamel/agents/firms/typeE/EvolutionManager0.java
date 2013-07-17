package jamel.agents.firms.typeE;

import jamel.Circuit;
import jamel.JamelObject;
import jamel.agents.firms.Firm;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A manager for the evolution procedures of the firm.
 */
public class EvolutionManager0 extends JamelObject {

	/** The size of the random selection in which the parents will be selected. */
	static private final int size = 10; // Should be a parameter.
	
	/** The firm comparator. */
	protected static final Comparator<EvolutionaryFirm> FIRM_COMPARATOR = new FirmComparator();

	/**
	 * Calculates a new value according to an evolutionary procedure.// TODO value n'est pas le bon terme.
	 * @param value1 the best value to imitate.
	 * @param value2 the second best value to imitate.
	 * @return the new value.
	 */
	public static double crossover(double value1, double value2) {
		final double average = (value1+value2)/2f;
		final double range = Math.abs(value1-value2);
		final double newValue = average + 2*range*getRandom().nextFloat() - range;
		return newValue;
	}

	/** The firm. */
	final protected EvolutionaryFirm firm;

	/** The parents of the firm. */
	protected Parents parents = null;

	/**
	 * Creates a new EvolutionManager for the given firm.
	 * @param aFirm the firm.
	 */
	public EvolutionManager0(EvolutionaryFirm aFirm) {
		this.firm = aFirm;
	}

	/**
	 * Returns the parents.
	 * @return the parents.
	 */
	public Parents getParents() {
		return this.parents;
	}

	/**
	 * Selects a pair of firms to be the parents.
	 */
	public void selectNewParents() {			
		final Set<EvolutionaryFirm> firmsSet = new HashSet<EvolutionaryFirm>();// TODO don't use HashSet
		final List<Firm> list2 = new LinkedList<Firm>(Circuit.getRandomFirms(size));
		for (Firm firm: list2) {
			if ((firm!=this.firm) && (((EvolutionaryFirm) firm).getFitness()!=null)) {
				firmsSet.add((EvolutionaryFirm) firm);
			}
		}
		final Parents pair;
		if (firmsSet.size()>2) {
			final LinkedList<EvolutionaryFirm> firmsList = new LinkedList<EvolutionaryFirm>();
			firmsList.addAll(firmsSet);
			Collections.sort(firmsList,FIRM_COMPARATOR);
			EvolutionaryFirm firm1 = firmsList.removeFirst();
			EvolutionaryFirm firm2 = firmsList.removeFirst();
			if (!firm1.getProduction().equals(firm2.getProduction())) {
				EvolutionaryFirm firm3 = firmsList.removeFirst();
				if (!firm1.getProduction().equals(firm3.getProduction())) {
					firm2 = firm3;
				}
				else if (!firm2.getProduction().equals(firm3.getProduction())) {
					firm1 = firm3;
				}
				else {
					throw new RuntimeException("Unexpected mode of production.");
				}
			}
			pair = new Parents(firm1,firm2);
		}
		else {
			pair = null;
		}
		this.parents = pair;			
	}

}
