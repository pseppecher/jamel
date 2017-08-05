package jamel.v170804.gui;

import java.io.File;

import jamel.util.Parameters;
import jamel.util.Simulation;

/**
 * An interface for the graphical user interface of the simulation.
 */
public interface Gui {

	/**
	 * Closes the gui at the end of the period.
	 */
	void close();

	/**
	 * Brings up a dialog that displays an error message.
	 * 
	 * @param title
	 *            the title string for the dialog.
	 * @param message
	 *            the message to display.
	 */
	void displayErrorMessage(String title, String message);

	/**
	 * Executes the specified event.
	 * 
	 * @param event
	 *            the event to be executed.
	 */
	void doEvent(Parameters event);

	/**
	 * Returns the source file of this Gui.
	 * 
	 * @return the source file of this Gui.
	 */
	File getFile();

	/**
	 * Returns the parent simulation.
	 * 
	 * @return the parent simulation.
	 */
	Simulation getSimulation();

	/**
	 * Opens the gui at the beginning of the period.
	 */
	void open();

	/**
	 * TODO: remove this method.
	 * 
	 * @param b
	 *            a boolean.
	 */
	void notifyPause(boolean b);

	/**
	 * Refresh the gui.
	 */
	void refresh();

}
