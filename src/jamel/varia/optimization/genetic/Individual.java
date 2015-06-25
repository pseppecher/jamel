package jamel.varia.optimization.genetic;

import jamel.basic.agent.Agent;

/**
 * Represents an element of a genetic algorithm.
 */
public interface Individual extends Agent {

	/**
	 * Adapts the individual (changes its position in the search space).
	 */
	void adapt();

	/**
	 * Returns the current fitness of the individual.
	 * @return the current fitness of the individual.
	 */
	double getFitness();

	/**
	 * Returns the X coordinate of the individual in the search space.
	 * @return the X coordinate of the individual in the search space.
	 */
	double getX();

	/**
	 * Returns the Y coordinate of the individual in the search space.
	 * @return the Y coordinate of the individual in the search space.
	 */
	double getY();

	/**
	 * The tournament procedure.
	 */
	void tournament();

}

// ***
