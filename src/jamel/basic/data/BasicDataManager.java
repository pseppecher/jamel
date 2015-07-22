package jamel.basic.data;

import jamel.basic.gui.BasicXYZDataset;
import jamel.basic.gui.DynamicData;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYZDataset;
import org.w3c.dom.Element;

/**
 * A basic data manager.
 */
public class BasicDataManager {

	/**
	 * A dynamic XYSeries.
	 */
	private abstract class DynamicXYSeries extends XYSeries implements DynamicData {

		/**
		 * Constructs a new empty series, with the auto-sort flag set as <code>false</code>, and duplicate values allowed.
		 * @param key the series key (<code>null</code> not permitted).
		 */
		public DynamicXYSeries(String key) {
			super(key,false);
		}

	}

	/** 
	 * The collection of the dynamic data used by the GUI.
	 * These data are updated at each end of period by the call of {@link BasicDataManager#updateSeries()} 
	 */
	private final Map<String,DynamicData> dynamicData = new LinkedHashMap<String,DynamicData>();

	/** The macro dataset. */
	protected final MacroDatabase macroDatabase;

	/** The timer. */
	final protected Timer timer;

	/**
	 * Creates a new basic data manager.
	 * @param settings a XML element with the settings.
	 * @param timer the timer.
	 * @param path the path to the scenario file.
	 * @param name the name of the scenario file.
	 * @throws InitializationException If something goes wrong.
	 */
	public BasicDataManager(Element settings, Timer timer, String path, String name) throws InitializationException {
		if (timer==null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer = timer;
		this.macroDatabase = getNewMacroDataset();
		//this.chartManager = getNewChartManager(this, settings, path);
	}

	/**
	 * Creates and returns a new {@link MacroDatabase}. 
	 * @return a new {@link MacroDatabase}.
	 */
	protected MacroDatabase getNewMacroDataset() {
		return new BasicMacroDatabase(timer);
	}

	/**
	 * Updates all the registered series.
	 */
	protected void updateSeries() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override public void run() {
					for (DynamicData series:dynamicData.values()) {
						series.update();
					}
				}
			}) ;
		} catch (Exception e) {
			throw new RuntimeException("Something goes wrong while updating the series.",e);
		}		
	}

	/**
	 * Returns the macroeconomic database.
	 * @return the macroeconomic database.
	 */
	public MacroDatabase getMacroDatabase() {
		return this.macroDatabase;
	}

	/**
	 * Returns an {@link XYSeries}. Each point in the series represents the values of the observed variables for one agent in the selected population. 
	 * @param sector the sector of the population.
	 * @param xKey the key for the X variable.
	 * @param yKey the key for the Y variable.
	 * @param t definition of the lag.
	 * @param select the method of selection of the population. If <code>select</code> is empty, all agents within the specified sector are selected.
	 * @return an {@link XYSeries}.
	 */
	public XYSeries getScatterSeries(final String sector, final String xKey, final String yKey, String t, final String select) {
		final String seriesKey = sector+"."+t+"."+xKey+"."+yKey+"."+select;
		final DynamicXYSeries scatterSeries;
		final DynamicXYSeries result1=(DynamicXYSeries) this.dynamicData.get(seriesKey);
		if (result1!=null) {
			scatterSeries=result1;
		}
		else {
			final Integer lag;
			if (t.equals("t")) {
				lag=0;
			}
			else if (t.startsWith("t-")) {
				lag=Integer.getInteger(t.substring(2, t.length()));
			}
			else {
				lag=null;
				throw new RuntimeException("Error while creating scatter series.");
			}
			scatterSeries = new DynamicXYSeries(seriesKey){
				@SuppressWarnings("unchecked")
				@Override
				public void update() {
					final int t = timer.getPeriod().intValue()-lag;
					final List<XYDataItem> newData = macroDatabase.getScatterData(sector,xKey,yKey,t,select);
					this.data.clear();
					if (newData!=null) {
						this.data.addAll(newData);
					}
					this.fireSeriesChanged();
				}
			};
			this.dynamicData.put(seriesKey, scatterSeries);
		}
		return scatterSeries;
	}

	/**
	 * Returns an {@link XYSeries}. Each point in the series represents the values of the observed variables at different successive periods.
	 * @param x the key for the X values.
	 * @param y the key for the Y values. 
	 * @return an {@link XYSeries}.
	 */
	public XYSeries getTimeScatterSeries(final String x, final String y) {
		final String key = x+" && "+y;
		final DynamicXYSeries timeScatterSeries;
		final DynamicXYSeries xySeries=(DynamicXYSeries) this.dynamicData.get(key);
		if (xySeries==null) {
			final Expression expressionX = ExpressionFactory.newExpression(x, macroDatabase);
			final Expression expressionY = ExpressionFactory.newExpression(y, macroDatabase);
			timeScatterSeries = new DynamicXYSeries(key) {
				@Override
				public void update() {
					final Double xValue = expressionX.value();
					final Double yValue = expressionY.value();
					if (xValue!=null && yValue!=null) {
						this.add(xValue, yValue);
					}
				}
			};
			this.dynamicData.put(key, timeScatterSeries);
		} 
		else {
			timeScatterSeries=xySeries;
		}
		return timeScatterSeries;
	}

	/**
	 * Returns an {@link XYSeries}. For each data point in the series, X represents the time and Y the observed variable.
	 * @param seriesKey the key for the variable to be observed.
	 * @return an {@link XYSeries}.
	 */
	public XYSeries getTimeSeries(final String seriesKey) {
		final DynamicXYSeries timeSeries;
		final DynamicXYSeries xySeries=(DynamicXYSeries) this.dynamicData.get(seriesKey);
		if (xySeries!=null) {
			timeSeries=xySeries;
		} 
		else {
			final Expression expression = ExpressionFactory.newExpression(seriesKey, macroDatabase);
			timeSeries = new DynamicXYSeries(seriesKey) {
				@Override
				public void update() {
					final Double value = expression.value();
					final int period = timer.getPeriod().intValue();
					if (value!=null) {
						this.add(period, value);
					}
				}
			};
			this.dynamicData.put(seriesKey, timeSeries);
		}
		return timeSeries;
	}

	/**
	 * Returns an {@link XYZDataset}. Used to represents artificial landscapes.
	 * @param sector the sector.
	 * @param xKey the key for the X values.
	 * @param yKey the key for the Y values. 
	 * @param zKey the key for the Z values.
	 * @return an {@link XYZDataset}.
	 */
	public XYZDataset getXYBlockData(final String sector, final String xKey, final String yKey, final String zKey) {
		final BasicXYZDataset dataset;
		final String key = sector+".surf."+xKey+"."+yKey+"."+zKey;
		final BasicXYZDataset cache=(BasicXYZDataset) this.dynamicData.get(key);
		if (cache!=null) {
			dataset=cache;
		}
		else {
			dataset = new BasicXYZDataset(){
				@Override
				public void update() {
					final int t = timer.getPeriod().intValue();
					final double[][] data = macroDatabase.getXYZData(sector,xKey,yKey,zKey,t);
					this.addSeries(key, data);
				}
			};
			this.dynamicData.put(key, dataset);
		}
		return dataset;
	}

	/**
	 * Puts the specified sector dataset into the macroeconomic dataset.
	 * @param sectorName the name of the sector.
	 * @param sectorDataset the dataset.
	 */
	public void putData(String sectorName, SectorDataset sectorDataset) {
		this.macroDatabase.putData(sectorName, sectorDataset);
	}

	/**
	 * Updates series and clears the macro dataset.
	 */
	public void update() {
		this.updateSeries();
	}

}

// ***
