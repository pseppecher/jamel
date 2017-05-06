package jamel.basic.sector;

import jamel.basic.data.SectorDataset;
import jamel.basic.util.InitializationException;
import jamel.jamel.firms.managers.Askable;

import org.w3c.dom.Element;

/**
 * The interface for the sector components of the macro-economic circuit. 
 */
public interface Sector extends Askable {
	
	/**
	 * Executes the specified event.
	 * @param event a XML element that describes the event to be executed.
	 */
	void doEvent(Element event);

	/**
	 * Returns the sector dataset.
	 * @return the sector dataset.
	 */
	SectorDataset getDataset();

	/**
	 * Returns the name of the sector.
	 * @return the name of the sector.
	 */
	String getName();

	/**
	 * Returns the float value of the specified parameter.  
	 * @param key the key of the parameter.
	 * @return  the float value of the specified parameter.
	 */
	Float getParam(String key);

	/**
	 * Returns the specified phase.
	 * @param phaseName the name of the phase.
	 * @return a phase.
	 * @throws InitializationException If something goes wrong. 
	 */
	Phase getPhase(String phaseName) throws InitializationException;

	/**
	 * Initializes the sector.
	 * Must be called once each sector is already created.
	 * @param element an XML element that contains the description of the parameters of the sector.
	 * @throws InitializationException if something goes wrong.
	 */
	void init(Element element) throws InitializationException;

	
}

// ***
