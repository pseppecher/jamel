package jamelV3.basic.gui;

/**
 * Represents one (x, y, z) data item.
 */
public class XYZItem {
	
	/** The x-value. */
	final private double x;

	/** The y-value. */
	final private double y;

	/** The z-value. */
	final private double z;

	/**
	 * Constructs a new data item. 
	 * @param x  the x-value;
	 * @param y  the y-value;
	 * @param z  the z-value;
	 */
	public XYZItem (double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Returns the x-value.
	 * @return the x-value;
	 */
	public double getXValue() {
		return this.x;
	}

	/**
	 * Returns the y-value.
	 * @return the y-value;
	 */
	public double getYValue() {
		return this.y;
	}

	/**
	 * Returns the z-value.
	 * @return the z-value;
	 */
	public double getZValue() {
		return this.z;
	}

}
