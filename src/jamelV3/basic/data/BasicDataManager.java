package jamelV3.basic.data;

import jamelV3.basic.data.BasicMacroDataset;
import jamelV3.basic.gui.AbstractChartManager;
import jamelV3.basic.gui.ChartManager;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;

import java.awt.Component;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
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
				chartManager = new AbstractChartManager(file){
					@Override protected XYSeries getScatterSeries(String seriesX, String seriesY) {
						return dataManager.getScatterSeries(seriesX,seriesY);
					}
					@Override protected XYSeries getSeries(String serieskey) {
						return dataManager.getSeries(serieskey);
					}
				};
			} catch (Exception e) {
				throw new InitializationException("Something goes wrong while parsing this file: "+file.getAbsolutePath(),e);
			}
		}
		else {
			chartManager = null;
		}
		return chartManager;
	}

	/** The chart manager. */
	private final ChartManager chartManager;

	/** The macro dataset. */
	private final MacroDataset macroDataset = new BasicMacroDataset();

	/** Description of the scatter series. */
	private final Map<String,String[]> scatterSeriesDescription = new LinkedHashMap<String,String[]>();

	/** A collection of XYSeries. */
	private final Map<String,JamelXYSeries> series = new LinkedHashMap<String,JamelXYSeries>();

	/** The timer. */
	final private Timer timer;

	/** Description of the time series. */
	private final List<String> timeSeriesDescription = new LinkedList<String>();

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
	 * Creates and returns a new scatter series.
	 * @param name the name of the series.
	 * @param keyX the key for x data.
	 * @param keyY the key for y data.
	 * @return a new scatter series.
	 */
	private XYSeries getNewScatterSeries(String name, String keyX, String keyY) {
		final JamelXYSeries result = new JamelXYSeries(name);
		final String[] val = {keyX, keyY};
		this.scatterSeriesDescription.put(name, val);
		this.series.put(name, result);				
		return result;
	}

	/**
	 * Creates and returns a new series.
	 * @param name the name of the series.
	 * @return a new series.
	 */
	private XYSeries getNewSeries(String name) {
		final JamelXYSeries result = new JamelXYSeries(name);
		this.timeSeriesDescription.add(name);
		this.series.put(name, result);				
		return result;
	}

	/**
	 * Returns a scatter series.
	 * @param seriesX the key for X data. 
	 * @param seriesY the key for Y data.
	 * @return the scatter series.
	 */
	private XYSeries getScatterSeries(String seriesX, String seriesY) {
		final XYSeries result;
		final String key = seriesX+"&"+seriesY;
		XYSeries series=this.series.get(key);
		if (series!=null) {
			result=series;
		} else {
			result=getNewScatterSeries(key,seriesX,seriesY);
		}
		return result;
	}

	/**
	 * Returns a series.
	 * @param seriesKey the key whose associated series is to be returned. 
	 * @return the series.
	 */
	private XYSeries getSeries(String seriesKey) {
		final XYSeries result;
		XYSeries series=this.series.get(seriesKey);
		if (series!=null) {
			result=series;
		} else {
			result=getNewSeries(seriesKey);
		}
		return result;
	}

	/**
	 * Updates series.
	 * Called at the closure of the period. 
	 */
	private void updateSeries() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					final int period = timer.getPeriod().intValue();

					for (String seriesName:timeSeriesDescription) {
						final Double value = macroDataset.get(seriesName);
						if (value!=null) {
							BasicDataManager.this.series.get(seriesName).add(period, value);
						}
						else {
							//throw new RuntimeException("Not found: "+seriesName); // TODO à revoir
						}
					}

					for (Entry<String,String[]>entry:scatterSeriesDescription.entrySet()) {
						final List<XYDataItem> data = macroDataset.getScatter(entry.getValue()[0],entry.getValue()[1]);
						series.get(entry.getKey()).update(data );
					}

				}
			}) ;
		} catch (InterruptedException e) {
			throw new RuntimeException("Something goes wrong while updating the series.",e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Something goes wrong while updating the series.",e);
		}

		/*for(Entry<String,XYSeries> entry: this.series.entrySet()){
			final String key=entry.getKey();
			if (key.contains("&&")) {
				final String serieskey[]=key.split("&&",2);
				final XYSeries series0 = this.series.get(serieskey[0]);
				final XYSeries series1 = this.series.get(serieskey[1]);
				if (series0!=null && series1!=null) {
					final Period period = this.circuit.getPeriod();
					final int index0 = series0.getItemCount()-1;
					final int index1 = series1.getItemCount()-1;
					if (index0>=0 && index1>=0) {
						final Number t0 = series0.getX(index0);
						final Number t1 = series1.getX(index1);
						if (period.equals(t0.intValue()) && period.equals(t1.intValue())) {
							entry.getValue().add(series0.getY(index0), series1.getY(index1));
						}
					}
				}
			}
		}*/

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
		// TODO vérifier que les données ne sont pas déjà présentes.
		this.macroDataset.putData(sectorName, sectorDataset);
	}

	/**
	 * Updates series and clears the macro dataset.
	 */
	public void updatesSeries() {
		updateSeries();
		this.macroDataset.clear();
	}

}

// ***
