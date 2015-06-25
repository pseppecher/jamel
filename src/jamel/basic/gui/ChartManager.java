package jamel.basic.gui;

import java.awt.Component;

/**
 * A manager for the charts.
 */
public interface ChartManager {

	/**
	 * Add a marker to all time charts.
	 * @param label the label of the marker to add.
	 * @param periodValue the value of the marker (the current period). 
	 */
	public abstract void addMarker(String label, int periodValue);

	/**
	 * Returns the list of the panels that contains the charts.
	 * @return the list of the panels that contains the charts.
	 */
	public abstract Component[] getPanelList();

}

// ***
