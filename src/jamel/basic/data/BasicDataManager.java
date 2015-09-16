package jamel.basic.data;

import jamel.basic.gui.BasicXYZDataset;
import jamel.basic.gui.DynamicHistogramDataset;
import jamel.basic.gui.Updatable;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYZDataset;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A basic data manager.
 */
public class BasicDataManager {

	/**
	 * A {@link XYSeries} that implements the {@link Updatable} interface.
	 */
	private abstract class DynamicXYSeries extends XYSeries implements Updatable {

		/**
		 * Constructs a new empty series, with the auto-sort flag set as
		 * <code>false</code>, and duplicate values allowed.
		 * 
		 * @param key
		 *            the series key (<code>null</code> not permitted).
		 */
		public DynamicXYSeries(String key) {
			super(key, false);
		}

	}

	/** The number format for exporting data. */
	final private static DecimalFormat nf = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));

	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** List of the data to be exported. */
	private final List<Expression> exports = new LinkedList<Expression>();

	/** The output file. */
	private File outputFile = null;

	/**
	 * The collection of the dynamic data used by the GUI. These data are
	 * updated at each end of period by the call of
	 * {@link BasicDataManager#updateSeries()}
	 */
	private final Map<String, Updatable> updatable = new LinkedHashMap<String, Updatable>();

	/** The macro dataset. */
	protected final MacroDatabase macroDatabase;

	/** The timer. */
	final protected Timer timer;

	/**
	 * Creates a new basic data manager.
	 * 
	 * @param elem
	 *            a XML element with the settings.
	 * @param timer
	 *            the timer.
	 * @param path
	 *            the path to the scenario file.
	 * @param name
	 *            the name of the scenario file.
	 * @throws InitializationException
	 *             If something goes wrong.
	 */
	public BasicDataManager(Element elem, Timer timer, String path, String name) throws InitializationException {
		if (timer == null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer = timer;
		this.macroDatabase = getNewMacroDataset();
		this.initExport(elem);
	}

	/**
	 * Exports the data into the output file.
	 * 
	 * @throws IOException
	 *             If something goes wrong.
	 */
	private void exportData() throws IOException {
		if (outputFile != null && outputFile.exists()) {
			FileWriter writer;
			writer = new FileWriter(outputFile, true);
			for (Expression query : exports) {
				final Double val = query.value();
				if (val != null) {
					writer.write(nf.format(val) + ";");
				} else {
					writer.write("null;");
				}
			}
			writer.write(rc);
			writer.close();
		}
	}

	/**
	 * Initializes the exportation of data.
	 * 
	 * @param elem
	 *            an XML element that contains the description of the data to be
	 *            exported.
	 */
	private void initExport(Element elem) {
		nf.applyPattern("###.##");
		final NodeList nodeList = elem.getChildNodes();
		NodeList exportNodeList = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeName().equals("export")) {
				final Element export = (Element) nodeList.item(i);
				final String fileName = export.getAttribute("file");
				outputFile = new File(fileName);
				if (this.outputFile.exists()) {
					this.outputFile.delete();
				}
				exportNodeList = export.getChildNodes();
				try {
					final File parentFile = outputFile.getParentFile();
					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}
					outputFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		if (outputFile != null && outputFile.exists()) {
			for (int i = 0; i < exportNodeList.getLength(); i++) {
				if (exportNodeList.item(i).getNodeName().equals("data")) {
					final Element data = (Element) exportNodeList.item(i);
					final String queryString = data.getAttribute("value");
					Expression expression;
					try {
						expression = this.macroDatabase.newQuery(queryString);
						this.exports.add(expression);
					} catch (InitializationException e) {
						e.printStackTrace();
						expression = ExpressionFactory.newNullExpression("Error: " + queryString);
						this.exports.add(expression);
					}
				}
			}
			try {
				final FileWriter writer = new FileWriter(outputFile, true);
				for (Expression query : exports) {
					writer.write(query.getQuery() + ";");
				}
				writer.write(rc);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates and returns a new {@link MacroDatabase}.
	 * 
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
				@Override
				public void run() {
					for (Updatable series : updatable.values()) {
						series.update();
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while updating the series.", e);
		}
	}

	/**
	 * Returns a new {@link DynamicHistogramDataset}.
	 * 
	 * @param sector
	 *            the sector of the values.
	 * @param valKey
	 *            the key of the values.
	 * @param t
	 *            definition of the lag.
	 * @param select
	 *            the method of selection of the population. If
	 *            <code>select</code> is empty, all agents within the specified
	 *            sector are selected.
	 * @param bins
	 *            the number of bins.
	 * @return a new {@link DynamicHistogramDataset}.
	 */
	public DynamicHistogramDataset getHistogramDataset(final String sector, final String valKey, String t,
			final String select, String bins) {
		if (valKey == null) {
			throw new IllegalArgumentException("Argument 'valKey' is null");
		}
		if (valKey.equals("")) {
			throw new IllegalArgumentException("Argument 'valKey' is empty");
		}
		final String seriesKey = "HistogramDataset," + sector + "," + t + "," + valKey + "," + select + "," + bins;
		final DynamicHistogramDataset histogramDataset;
		final DynamicHistogramDataset cached = (DynamicHistogramDataset) this.updatable.get(seriesKey);
		if (cached != null) {
			histogramDataset = cached;
		} else {
			final Integer lag;
			if (t.equals("t")) {
				lag = 0;
			} else if (t.startsWith("t-")) {
				lag = Integer.valueOf(t.substring(2, t.length()));
			} else {
				lag = null;
				throw new RuntimeException("Error while creating histogramDataset.");
			}
			final int nBins;
			if (bins.equals("")) {
				nBins = 10;
			} else {
				nBins = Integer.valueOf(bins);
			}
			histogramDataset = new DynamicHistogramDataset() {
				@Override
				public void update() {
					final int t1 = timer.getPeriod().intValue() - lag;
					final Double[] series = macroDatabase.getDistributionData(sector, valKey, t1, select);
					if (series != null && series.length > 0) {
						this.addSeries(seriesKey, series, nBins);
					} else {
						// TODO: remove this series.
					}
				}
			};
			this.updatable.put(seriesKey, histogramDataset);
		}
		return histogramDataset;
	}

	/**
	 * Returns the macroeconomic database.
	 * 
	 * @return the macroeconomic database.
	 */
	public MacroDatabase getMacroDatabase() {
		return this.macroDatabase;
	}

	/**
	 * Returns an {@link XYSeries}. Each point in the series represents the
	 * values of the observed variables for one agent in the selected
	 * population.
	 * 
	 * @param sector
	 *            the sector of the population.
	 * @param xKey
	 *            the key for the X variable.
	 * @param yKey
	 *            the key for the Y variable.
	 * @param t
	 *            definition of the lag.
	 * @param select
	 *            the method of selection of the population. If
	 *            <code>select</code> is empty, all agents within the specified
	 *            sector are selected.
	 * @return an {@link XYSeries}.
	 */
	public XYSeries getScatterSeries(final String sector, final String xKey, final String yKey, String t,
			final String select) {
		final String seriesKey = sector + "." + t + "." + xKey + "." + yKey + "." + select;
		final DynamicXYSeries scatterSeries;
		final DynamicXYSeries result1 = (DynamicXYSeries) this.updatable.get(seriesKey);
		if (result1 != null) {
			scatterSeries = result1;
		} else {
			final Integer lag;
			if (t.equals("t")) {
				lag = 0;
			} else if (t.startsWith("t-")) {
				lag = Integer.valueOf(t.substring(2, t.length()));
			} else {
				lag = null;
				throw new RuntimeException("Error while creating scatter series.");
			}
			scatterSeries = new DynamicXYSeries(seriesKey) {
				@SuppressWarnings("unchecked")
				@Override
				public void update() {
					final int t1 = timer.getPeriod().intValue() - lag;
					final List<XYDataItem> newData = macroDatabase.getScatterData(sector, xKey, yKey, t1, select);
					this.data.clear();
					if (newData != null) {
						this.data.addAll(newData);
					}
					this.fireSeriesChanged();
				}
			};
			this.updatable.put(seriesKey, scatterSeries);
		}
		return scatterSeries;
	}

	/**
	 * Returns an {@link XYSeries}. Each point in the series represents the
	 * values of the observed variables at different successive periods.
	 * 
	 * @param x
	 *            the key for the X values.
	 * @param y
	 *            the key for the Y values.
	 * @param mod
	 *            the modulus.
	 * @return an {@link XYSeries}.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	public XYSeries getTimeScatterSeries(final String x, final String y, final String mod)
			throws InitializationException {
		final DynamicXYSeries timeScatterSeries;
		final String key = x + " && " + y + " mod" + mod;
		final int modulus;
		if (mod != "") {
			modulus = Integer.parseInt(mod);
		} else {
			modulus = 1;
		}
		final DynamicXYSeries xySeries = (DynamicXYSeries) this.updatable.get(key);
		if (xySeries == null) {
			final Expression expressionX = macroDatabase.newQuery(x);
			final Expression expressionY = macroDatabase.newQuery(y);
			timeScatterSeries = new DynamicXYSeries(key) {
				@Override
				public void update() {
					final int period = timer.getPeriod().intValue();
					if (period % modulus == 0) {
						final Double xValue = expressionX.value();
						final Double yValue = expressionY.value();
						if (xValue != null && yValue != null) {
							this.add(xValue, yValue);
						}
					}
				}
			};
			this.updatable.put(key, timeScatterSeries);
		} else {
			timeScatterSeries = xySeries;
		}
		return timeScatterSeries;
	}

	/**
	 * Returns an {@link XYSeries}. For each data point in the series, X
	 * represents the time and Y the observed variable.
	 * 
	 * @param seriesKey
	 *            the key for the variable to be observed.
	 * @param mod
	 *            the modulus.
	 * @return an {@link XYSeries}.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	public XYSeries getTimeSeries(final String seriesKey, String mod) throws InitializationException {
		final DynamicXYSeries timeSeries;
		final int modulus;
		if (mod != "") {
			modulus = Integer.parseInt(mod);
		} else {
			modulus = 1;
		}
		final DynamicXYSeries xySeries = (DynamicXYSeries) this.updatable.get(seriesKey + ".mod" + modulus);
		if (xySeries != null) {
			timeSeries = xySeries;
		} else {
			final Expression expression = macroDatabase.newQuery(seriesKey);
			timeSeries = new DynamicXYSeries(seriesKey + ".mod" + modulus) {
				@Override
				public void update() {
					final int period = timer.getPeriod().intValue();
					if (period % modulus == 0) {
						final Double value = expression.value();
						if (value != null) {
							this.add(period, value);
						}
					}
				}
			};
			this.updatable.put(seriesKey + ".mod" + modulus, timeSeries);
		}
		return timeSeries;
	}

	/**
	 * Returns an {@link XYZDataset}. Used to represents artificial landscapes.
	 * 
	 * @param sector
	 *            the sector.
	 * @param xKey
	 *            the key for the X values.
	 * @param yKey
	 *            the key for the Y values.
	 * @param zKey
	 *            the key for the Z values.
	 * @return an {@link XYZDataset}.
	 */
	public XYZDataset getXYBlockData(final String sector, final String xKey, final String yKey, final String zKey) {
		final BasicXYZDataset dataset;
		final String key = sector + ".surf." + xKey + "." + yKey + "." + zKey;
		final BasicXYZDataset cache = (BasicXYZDataset) this.updatable.get(key);
		if (cache != null) {
			dataset = cache;
		} else {
			dataset = new BasicXYZDataset() {
				@Override
				public void update() {
					final int t = timer.getPeriod().intValue();
					final double[][] data = macroDatabase.getXYZData(sector, xKey, yKey, zKey, t);
					this.addSeries(key, data);
				}
			};
			this.updatable.put(key, dataset);
		}
		return dataset;
	}

	/**
	 * Puts the specified sector dataset into the macroeconomic dataset.
	 * 
	 * @param sectorName
	 *            the name of the sector.
	 * @param sectorDataset
	 *            the dataset.
	 */
	public void putData(String sectorName, SectorDataset sectorDataset) {
		this.macroDatabase.putData(sectorName, sectorDataset);
	}

	/**
	 * Updates series.
	 */
	public void update() {
		this.updateSeries();
		try {
			this.exportData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

// ***
