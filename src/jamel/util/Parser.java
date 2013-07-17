package jamel.util;

import java.util.Random;

/**
 * A stupid object.
 */
public class Parser {
	
	/**
	 * Returns a value for the given parameters.
	 * @param string a string that contains parameters.
	 * @param random the random.
	 * @return a value.
	 */
	public static float parseFloat(String string, Random random) {
		final String[] param = string.split("\\-",2);
		final float target;
		if (param.length==1) {
			target = Float.parseFloat(param[0]);
		}
		else if (param.length==2) {
			final float min = Float.parseFloat(param[0]);
			final float max = Float.parseFloat(param[1]);
			target = min+random.nextFloat()*(max-min);
		}
		else {
			throw new RuntimeException("Incredible error.");
		}		
		return target;
	}

}
