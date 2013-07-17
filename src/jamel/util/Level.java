package jamel.util;

import java.util.LinkedList;

/**
 * A fuzzy representation of a level.
 */
public class Level {

	/**
	 * Returns the average deviation of a list of levels.
	 * @param levels a list of levels.
	 * @return a float in <code>[0;1]</code>
	 */
	public static float getAverageDeviation(Level... levels) {
		float sum = 0;
		for(Level level: levels) {
			sum+= Math.abs(level.getDeviation());
		}
		return sum/levels.length;
	}

	/**
	 * Returns the weighted average deviation of a list of levels.
	 * @param levels a list of weighted levels.
	 * @return a float in <code>[0;1]</code>
	 */
	public static float getWeightedAverageDeviation(LinkedList<Level> levels) {
		float sum = 0;
		float totalWeight = 0;
		for(Level level : levels) {
			sum+=Math.abs(level.getWeightedDeviation());
			totalWeight+=level.weight;
		}		
		return sum/totalWeight;
	}

	/** The normalized deviation between the value and the norm. */
	final private float deviation;
	
	/** The weight of the level. */
	private float weight;

	/**
	 * Creates a new level.
	 * @param min the minimum value.
	 * @param max the maximum value.
	 * @param normal the normal value.
	 * @param value the level value.
	 */
	public Level(double min, double max, double normal, double value) {
		if (max<min) 
			throw new RuntimeException();
		if (normal<min) 
			throw new RuntimeException();
		if (max<normal) 
			throw new RuntimeException();
		if (value==normal)
			deviation = 0;
		else if (value<min)
			deviation = -1;
		else if (value>max)
			deviation = 1;
		else if (value<normal)
			deviation = (float) ((value-normal)/(normal-min));
		else
			deviation = (float) ((value-normal)/(max-normal));
	}

	/**
	 * Returns the deviation.
	 * @return a float between <code>-1</code> and <code>1</code>.
	 */
	public float getDeviation() {
		return this.deviation;
	}

	/**
	 * Returns the weighted deviation.
	 * @return the weighted deviation.
	 */
	public float getWeightedDeviation() {
		return this.deviation*this.weight;
	}

	/**
	 * Returns <code>true</code> if the level is higher than the normal value, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	public boolean isHigh() {
		return (0<this.deviation);
	}

	/**
	 * Returns <code>true</code> if this level is higher than the other level.
	 * @param other the other level.
	 * @return a boolean.
	 */
	public boolean isHigherThan(Level other) {
		return this.deviation>other.deviation;
	}

	/**
	 * Returns <code>true</code> if the level is lower than the normal value, <code>false</code> otherwise.
	 * @return a boolean.
	 */
	public boolean isLow() {
		return (this.deviation<0);
	}

	/**
	 * Returns <code>true</code> if this level is lower than the other level.
	 * @param other the other level.
	 * @return a boolean.
	 */
	public boolean isLowerThan(Level other) {
		return this.deviation<other.deviation;
	}
	
	/**
	 * Sets the weight.
	 * @param aWeight the weight to set.
	 */
	public void setWeight(float aWeight) {
		this.weight = aWeight;
	}
	
	/**
	 * Returns a string representation of the level (deviation,weight,weightedDeviation).
	 * @return a string.
	 */
	@Override
	public String toString() {
		return deviation+","+weight+","+getWeightedDeviation();
	}

}
