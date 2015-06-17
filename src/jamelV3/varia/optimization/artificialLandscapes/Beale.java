package jamelV3.varia.optimization.artificialLandscapes;

import jamelV3.basic.Circuit;

/**
 * The Beale artificial landscape.<p>
 * 
 * The Beale's function is multimodal, with sharp peaks at the corners of the input domain.<p>
 * 
 * <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Beale%27s_function.pdf/page1-400px-Beale%27s_function.pdf.jpg" />
 */
public class Beale extends Landscape {

	/**
	 * Creates a new artificial landscape.
	 * @param name the name of the landscape.
	 * @param circuit the circuit.
	 */
	public Beale(String name, Circuit circuit) {
		super(name, circuit);
	}

	/**
	 * The Beale's function.
	 */
	@Override
	public double getZValue(double x, double y) {
		
		return Math.pow(1.5-x+x*y,2)+Math.pow(2.25-x+x*Math.pow(y,2),2)+Math.pow(2.625-x+x*Math.pow(y,3),2);
		
	}

}

// ***
