package jamelV3.basic.data;

import jamelV3.basic.gui.BasicChartManager;
import jamelV3.basic.gui.ChartManager;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

import java.awt.Component;
import java.io.File;

import org.w3c.dom.Element;

/**
 * A basic data manager with a ChartManager and a GUI.
 */
public class BasicDataManager {

	/**
	 * Initializes and returns a new chart manager.
	 * @param dataManager the parent data manager.
	 * @param settings a XML element with the settings.
	 * @param path the path of the scenario file.
	 * @return a new chart manager.
	 * @throws InitializationException If something goes wrong.
	 */
	private static ChartManager getNewChartManager(final BasicDataManager dataManager, Element settings, String path) throws InitializationException {
		ChartManager chartManager = null;
		final String fileName = settings.getAttribute("chartsConfigFile");
		if (fileName != null) {
			final File file = new File(path+"/"+fileName);
			try {
				chartManager = new BasicChartManager(file,dataManager.macroDataset, dataManager.timer);
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while parsing this file: "+file.getAbsolutePath(),e);
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/** The timer. */
	final private Timer timer;

	/** The chart manager. */
	protected final ChartManager chartManager;

	/** The macro dataset. */
	protected final MacroDataset macroDataset = new BasicMacroDataset();

	/**
	 * Creates a new sector for data management.
	 * @param settings a XML element with the settings.
	 * @param timer the timer.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		this.timer = timer;
		this.chartManager = getNewChartManager(this, settings, path);
	}

	/**
	 * Add a marker to all time charts.
	 * @param label the label of the marker.
	 */
	public void addMarker(String label) {
		this.chartManager.addMarker(label,timer.getPeriod().intValue());
	}

	/**
	 * Returns the list of panel of the chart manager.
	 * @return the list of panel of the chart manager.
	 */
	public Component[] getPanelList() {
		return this.chartManager.getPanelList();
	}

	/**
	 * Puts the specified sector dataset into the macroeconomic dataset.
	 * @param sectorName the name of the sector.
	 * @param sectorDataset the dataset.
	 */
	public void putData(String sectorName, SectorDataset sectorDataset) {
		// TODO vérifier que les données ne sont pas déjà présentes ?
		this.macroDataset.putData(sectorName, sectorDataset);
	}

	/**
	 * Updates series and clears the macro dataset.
	 */
	public void update() {
		this.chartManager.update();
		this.macroDataset.clear();
	}

}

// ***
