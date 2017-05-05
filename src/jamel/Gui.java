package jamel;

import java.io.File;

/**
 * An interface for the graphical user interface of the simulation.
 */
public interface Gui {

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
