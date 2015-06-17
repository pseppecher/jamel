package jamelV3.varia.optimization.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A "pyramiding" individual.
 */
public class PyramidingIndiv extends BasicIndividual {

	/** The social network of the individual. */
	private final List<Individual> network;

	/**
	 * Creates a new agent.
	 * @param basicGA  the parent algorithm.
	 * @param random  the random.
	 */
	public PyramidingIndiv(BasicGA basicGA, Random random) {
		super(basicGA, random);
		this.network= new ArrayList<Individual>(this.tournamentSize);
	}

	/**
	 * The tournament procedure.
	 */
	@Override
	public void tournament() {
		final int gap = this.tournamentSize-this.network.size();
		if (gap>0) {
			final List<Individual> selection = this.basicGA.getTournament(gap);
			for(Individual indiv: selection) {
				if(!indiv.equals(this) && !network.contains(indiv)) {
					network.add(indiv);
				}
			}
		}
		network.add(this);
		Collections.sort(network,fitnessComparator);
		final Individual ind0 = network.get(0);
		final Individual ind1 = network.get(1);
		final Individual last = network.get(network.size()-1);
		if (this.equals(last)) {
			this.adaptation(ind0,ind1);
			network.remove(this);			
		}
		else {
			// No adaptation
			network.remove(network.size()-1);
			network.remove(this);			
			xx=null;
			yy=null;
		}
	}

}

// ***
