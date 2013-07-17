package jamel.agents.firms.typeE;

import java.util.Map;

import jamel.agents.firms.Firm;

/**
 * An interface for evolutionary firms.
 */
public interface EvolutionaryFirm extends Firm{

	/**
	 * Returns the fitness of the firm.
	 * @return the fitness (<code>null</code> if the fitness is not available)
	 */
	Double getFitness();

	/**
	 * Returns a map that contains the parameters of the firm.
	 * @return a map that contains the parameters of the firm.
	 */
	Map<String, Object> getParameters();

}
