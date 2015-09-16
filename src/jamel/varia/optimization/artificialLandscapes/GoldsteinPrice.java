package jamel.varia.optimization.artificialLandscapes;

import jamel.basic.Circuit;

/**
 * The Goldstein-Price artificial landscape.<p>
 * 
 * This function has one global optimum and several local minima.<p>
 * 
 * <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Goldstein_Price_function.pdf/page1-400px-Goldstein_Price_function.pdf.jpg">
 */
public class GoldsteinPrice extends Landscape {

	/**
	 * Creates a new artificial landscape.
	 * @param name the name of the landscape.
	 * @param circuit the circuit.
	 */
	public GoldsteinPrice(String name, Circuit circuit) {
		super(name, circuit);
	}

	/**
	 * The Goldstein-Price function.
	 */
	@Override
	public double getZValue(double x, double y) {
		final double f1 = ((1+Math.pow(x+y+1,2))*(19-14*x+3*Math.pow(x, 2)-14*y+6*x*y+3*Math.pow(y, 2)));
		final double f2 = 30+Math.pow(2*x-3*y, 2)*(18-32*x+12*Math.pow(x, 2)+48*y-36*x*y+27*Math.pow(y, 2));
		return f1*f2;
	}

}

// ***
