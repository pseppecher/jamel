package jamel;

import java.lang.reflect.InvocationTargetException;

import org.jfree.data.time.Month;

/**
 *
 */
public interface AbstractSimulator {

	/**
	 * Systemic crisis.
	 */
	public abstract void failure();

	/**
	 * Adds a marker to the time charts.
	 * @param label the label.
	 * @param month the month.
	 */
	public abstract void marker(String label, Month month);

	/**
	 * Sets the sate of the simulation (paused or running).
	 * @param b a flag that indicates whether or not the simulation must be paused.
	 */
	public abstract void pause(boolean b);

	/**
	 * Prints a String in the console panel.
	 * @param message the String to print.
	 */
	public abstract void println(String message);

	/**
	 * Sets the chart in the specified panel.
	 * @param tabIndex the index of the tab to customize.
	 * @param panelIndex the id of the ChartPanel to customize.
	 * @param chartPanelName the name of the ChartPanel to set.
	 * @throws ClassNotFoundException... 
	 * @throws NoSuchMethodException...
	 * @throws InvocationTargetException... 
	 * @throws IllegalAccessException...
	 * @throws InstantiationException...
	 * @throws SecurityException...
	 * @throws IllegalArgumentException... 
	 */
	public abstract void setChart(int tabIndex, int panelIndex,
			String chartPanelName) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException;

	/**
	 * Sets the selected index for the tabbedpane of the application window.
	 * @param index the index to be selected.
	 */
	public abstract void setVisiblePanel(int index);

	/**
	 * Zooms in the time charts.
	 * @param aZoom the zoom factor.
	 */
	public abstract void zoom(int aZoom);

}