package jamel.models.m18.r01.households;

import jamel.util.Agent;

/**
 * A household is an agent with consumption capacity.
 * 
 * 2018-03-02: introduit pour permettre de brasser des consommateurs appartenant
 * à des secteurs différents au sein d'un seul marché.
 */
public interface Household extends Agent {

	/**
	 * The consumption phase.
	 */
	void consumption();

}
