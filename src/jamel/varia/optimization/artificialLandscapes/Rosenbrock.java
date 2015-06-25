package jamel.varia.optimization.artificialLandscapes;

import jamel.basic.Circuit;

/**
 * The Rosenbrock artificial landscape.<p>
 * 
 * The Rosenbrock function is also known as Rosenbrock's valley or Rosenbrock's banana function.
 * The global minimum is inside a long, narrow, parabolic shaped flat valley. 
 * To find the valley is trivial; to converge to the global minimum, however, is difficult.<p>
 * 
 * <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/Rosenbrock_function.svg/400px-Rosenbrock_function.svg.png" />
 */
public class Rosenbrock extends Landscape {

	/**
	 * Creates a new artificial landscape.
	 * @param name the name of the landscape.
	 * @param circuit the circuit.
	 */
	public Rosenbrock(String name, Circuit circuit) {
		super(name, circuit);
	}

	/**
	 * The Rosenbrock function.
	 */
	@Override
	public double getZValue(double x, double y) {
		final int a=1;
		final int b=100;
		return Math.pow(a-x,2)+b*Math.pow((y-Math.pow(x,2)),2);
	}

}

// ***
