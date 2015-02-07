package jamel.basic.data.util.xml;


/**
 *  Description of a series.
 */
public class SeriesDescription {

	/** color */
	private final String color;

	/** value */
	private final String key;
	
	/** label */
	private final String label;
	
	/**
	 * Creates a new Serie.
	 * @param value the value (<code>null</code> not permitted)
	 * @param color the color (<code>null</code> permitted)
	 * @param label the label (<code>null</code> permitted)
	 */
	public SeriesDescription(String value, String color, String label) {
		if (value == null) {
			throw new IllegalArgumentException("Null not permitted");
		}
		this.key = value;
		if ("".equals(color)) {
			color=null;
		}
		if ("".equals(label)) {
			label=null;
		}
		this.color = color;
		this.label = label;
	}

    /**
     * Returns the color.
     * @return the color.
     */
    public String getColor() {
		return this.color;
	}

    /**
     * Returns the key.
     * @return the key.
     */
    public String getKey() {
		return this.key;
	}
	
    /**
     * Returns the label.
     * @return the label.
     */
    public String getLabel() {
		return this.label;
	}

}

// ***
