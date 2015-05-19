package jamelV3.loktaVolterra;

/**
 * The land sector.
 */
public interface LandSector {

	/**
	 * Eats some grass at the given location.
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param volume the quantity of grass to eat.
	 * @return the effective quantity of grass eated.
	 */
	double eat(double x, double y, double volume);

}

// ***