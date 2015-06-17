package jamelV3.varia.optimization.artificialLandscapes;

import jamelV3.basic.Circuit;

/**
 * The Himmelblau's artificial landscape.<p>
 * 
 * Himmelblau's function is a multi-modal function.<p>
 * 
 * <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Himmelblau_function.svg/400px-Himmelblau_function.svg.png" />
 */
public class Himmelblau extends Landscape {

	/**
	 * Creates a new artificial landscape.
	 * @param name the name of the landscape.
	 * @param circuit the circuit.
	 */
	public Himmelblau(String name, Circuit circuit) {
		super(name, circuit);
	}

	/**
	 * The Himmelblau's function.
	 */
	@Override
	public double getZValue(double x, double y) {
		return Math.pow(Math.pow(x, 2)+y-11,2)+Math.pow(x+Math.pow(y, 2)-7,2);
	}

}

// ***
