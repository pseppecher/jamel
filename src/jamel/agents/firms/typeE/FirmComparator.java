package jamel.agents.firms.typeE;


import java.util.Comparator;

/**
 * Compares the firms according to their fitness.
 */
public class FirmComparator implements Comparator<EvolutionaryFirm> {

	/**
	 * Compares two firms for order. 
	 * Returns a negative integer, zero, or a positive integer as the fitness of the first firm is less than, equal to, or greater than the fitness of the second firm. 
	 * @return a negative integer, zero, or a positive integer as the fitness of the first firm is less than, equal to, or greater than the fitness of the second firm.
	 */
	@Override
	public int compare(EvolutionaryFirm firm1, EvolutionaryFirm firm2) {
		if ((firm1==null) || (firm2==null))
			throw new RuntimeException("The firm is null");
		final Double f1  = firm1.getFitness();
		if (f1==null)
			throw new RuntimeException("The fitness is null, firm "+firm1.toString());
		final Double f2  = firm2.getFitness();
		if (f2==null)
			throw new RuntimeException("The fitness is null, firm "+firm2.toString());
		if (f1 < f2) return 1;
		if (f1 > f2) return -1;
		return 0;
	};

}
