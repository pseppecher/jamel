package jamel.agents.firms.typeE;


/**
 * A class that contains a pair firms representing the "parents" of an other firm.
 */
public class Parents {

	/** The parent 1. */
	final private EvolutionaryFirm p1;

	/** The parent 2. */
	final private EvolutionaryFirm p2;

	/**
	 * Creates a new pair of firms.
	 * @param firm1 the first firm.
	 * @param firm2 the second firm.
	 */
	public Parents(EvolutionaryFirm firm1, EvolutionaryFirm firm2) {
		this.p1 = firm1;
		this.p2 = firm2;			
	}

	/**
	 * Returns the first parent.
	 * @return the first parent.
	 */
	public EvolutionaryFirm getFirst() {
		return this.p1;
	}

	/**
	 * Returns the second parent.
	 * @return the second parent.
	 */
	public EvolutionaryFirm getSecond() {
		return this.p2;
	}

}

