package jamel.gui;

import java.io.File;

import org.w3c.dom.Element;

import jamel.util.Simulation;

/**
 * An interface for the graphical user interface of the simulation.
 */
public interface Gui {

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
	void doEvent(Element event);

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
	 * Updates the content of the Gui.
	 */
	void update();

}
