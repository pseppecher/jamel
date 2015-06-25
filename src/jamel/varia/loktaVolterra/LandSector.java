package jamel.varia.loktaVolterra;

/**
 * The land sector.
 */
public interface LandSector {

	/**
	 * Eats some grass at the given location.
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param volume the quantity of grass to eat.
	 * @return the effective quantity of grass eaten.
	 */
	double eat(double x, double y, double volume);

	/**
	 * Returns the land height.
	 * @return the land height.
	 */
	Integer getLandHeight();

	/**
	 * Returns the land width.
	 * @return the land width.
	 */
	Integer getLandWidth();

}

// ***
