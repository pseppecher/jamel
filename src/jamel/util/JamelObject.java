package jamel.util;

import java.util.Random;

/**
 * An abstract class that provides convenience methods.
 */
public abstract class JamelObject {

	/**
	 * The parent simulation.
	 */
	private final Simulation simulation;

	/**
	 * Creates a JamelObject.
	 * 
	 * @param simulation
	 *            the parent simulation.
	 */
	public JamelObject(Simulation simulation) {
		this.simulation = simulation;
	}

	/**
	 * Retourne soit l'entier immédiatement inférieur, soit l'entier
	 * immédiatement supérieur à la valeur d.
	 * Le choix entre inférieur et supérieur est fait au hasard.
	 * La probabilité d'arrondire "au dessus" plutôt qu'"au dessous" est égale à
	 * la par la partie décimale de d.
	 * 
	 * @param d
	 *            a floating-point value to be rounded to an integer.
	 * @return the value of the argument rounded at random.
	 */
	final public int aleaRound(double d) {
		final int partieEntiere = (int) Math.floor(d);
		final double partieDecimale = d - partieEntiere;
		return partieEntiere + ((getRandom().nextFloat() > partieDecimale) ? 0 : 1);
	}

	/**
	 * Returns the value of the current period.
	 * 
	 * @return the value of the current period.
	 */
	final public int getPeriod() {
		return simulation.getPeriod();
	}

	/**
	 * Returns the specified public data (for example, the inflation rate).
	 * 
	 * @param key
	 *            the key for the data to be returned.
	 * @return the specified public data (for example, the inflation rate).
	 */
	final public Double getPublicData(String key) {
		return simulation.getPublicData(key);
	}

	/**
	 * Returns the random.
	 * 
	 * @return the random.
	 */
	final public Random getRandom() {
		return this.simulation.getRandom();
	}

	/**
	 * Returns the specified sector.
	 * 
	 * @param sectorName
	 *            the name of the sector to be returned.
	 * @return the specified sector.
	 */
	final public Sector getSector(final String sectorName) {
		return this.simulation.getSector(sectorName);
	}

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	final public Simulation getSimulation() {
		return this.simulation;
	}

}
