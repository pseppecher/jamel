package jamel.varia.optimization.genetic;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A basic extension of {@link AbstractIndividual}.
 */
public class BasicIndividual extends AbstractIndividual{

	/**
	 * Creates a new agent.
	 * @param basicGA  the parent algorithm.
	 * @param random  the random.
	 */
	public BasicIndividual(BasicGA basicGA, Random random) {
		super(basicGA, random);
	}

	/**
	 * The tournament procedure.
	 */
	@Override
	public void tournament() {
		final List<Individual> selection = this.basicGA.getTournament(this.tournamentSize);
		selection.remove(this);
		Collections.sort(selection,fitnessComparator);
		final Individual ind0 = selection.get(0);
		final Individual ind1 = selection.get(1);
		if (ind1.getFitness()<this.getFitness()) {
			this.adaptation(ind0,ind1);
		}
		else {
			// No adaptation
			xx=null;
			yy=null;
		}
	}

}

// ***
