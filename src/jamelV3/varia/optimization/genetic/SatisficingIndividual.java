package jamelV3.varia.optimization.genetic;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A satisficing individual.
 */
public class SatisficingIndividual extends BasicIndividual {

	/**
	 * Creates a new agent.
	 * @param basicGA  the parent algorithm.
	 * @param random  the random.
	 */
	public SatisficingIndividual(BasicGA basicGA, Random random) {
		super(basicGA, random);
	}

	/**
	 * The tournament procedure.
	 */
	@Override
	public void tournament() {
		final List<Individual> selection = this.basicGA.getTournament(this.tournamentSize);
		selection.remove(this);
		selection.add(this);
		Collections.sort(selection,fitnessComparator);
		final Individual ind0 = selection.get(0);
		final Individual ind1 = selection.get(1);
		final Individual last = selection.get(selection.size()-1);
		if (this.equals(last)) {
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
