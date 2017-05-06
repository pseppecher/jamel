package jamel.basic.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jamel.basic.gui.BasicXYZDataset;
import jamel.basic.gui.DynamicHistogramDataset;
import jamel.basic.gui.Updatable;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

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

	/**
	 * Definition of an export of data in an external file.
	 */
	private abstract class Export {

		/**
		 * The end of the export.
		 */
		final private Integer end;

		/**
		 * Periodicity of the export (=12 if annual).
		 */
		final private Integer mod;

		/**
		 * The start of the export.
		 */
		final private Integer start;

		/** The number format for exporting data. */
		protected final DecimalFormat nf = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));

		/**
		 * The output file.
		 */
		protected final File outputFile;

		/**
		 * Characters used to separate rows in the output file.
		 * Any character may be used, but the most common delimiters are the
		 * comma, tab, and colon. The vertical bar (also referred to as pipe)
		 * and space are also sometimes used.
		 */
		protected final String separator;

		/**
		 * Creates an new Export.
		 * 
		 * @param description
		 *            the description of the Export to be created.
		 * @param path
		 *            the path to the parent file.
		 */
		public Export(Element description, String path) {

			if (path == null) {
				throw new IllegalArgumentException("Path is null");
			}

			if (description.getAttribute("numberFormat").equals("")) {
				nf.applyPattern("###.####");
			} else {
				nf.applyPattern(description.getAttribute("numberFormat"));
			}

			if (description.getAttribute("separator").equals("")) {
				separator = ";";
			} else {
				separator = description.getAttribute("separator");
			}

			final String when = description.getAttribute("when");
			if (when.equals("")) {
				start = 0;
				end = null;
			} else {
				final String[] bounds = when.split("-");
				if (bounds.length == 2) {
					start = Integer.parseInt(bounds[0]);
					end = Integer.parseInt(bounds[1]);
				} else {
					start = Integer.parseInt(bounds[0]);
					end = start;
				}
			}

			final String each = description.getAttribute("each");
			if (each.equals("")) {
				mod = null;
			} else {
				mod = Integer.parseInt(each);
			}

			final String fileName = path + "/" + description.getAttribute("file");
			this.outputFile = new File(fileName);
			/*if (this.outputFile.exists()) {
				this.outputFile.delete();
			}*/

			try {
				final File parentFile = outputFile.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				outputFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Unable to create the file: " + fileName, e);
			}

		}

		/**
		 * Writes data in the output file.
		 */
		protected abstract void write();

		/**
		 * Exports the data in the output file.
		 */
		final public void run() {
			final int now = timer.getPeriod().intValue();
			if ((start == null && end == null) || (start == null && now <= end) || (start <= now && end == null)
					|| (start <= now && now <= end)) {
				if (mod == null || now % mod == 0) {
					write();
				}
			}
		}

	}

	/**
	 * Export of aggregate data.
	 */
	private class ExportAggregate extends Export {

		/** List of the expressions to be exported. */
		private final List<Expression> expressions = new LinkedList<Expression>();

		/**
		 * Creates an new Export.
		 * 
		 * @param description
		 *            the description of the export to be created.
		 * @param path
		 *            the path to the parent file.
		 */
		public ExportAggregate(Element description, String path) {

			super(description, path);

			final NodeList exportNodeList = description.getChildNodes();

			for (int i = 0; i < exportNodeList.getLength(); i++) {
				if (exportNodeList.item(i).getNodeName().equals("data")) {
					final Element data = (Element) exportNodeList.item(i);
					final String queryString = data.getAttribute("value");
					final String name;
					if (data.getAttribute("name").equals("")) {
						name = null;
					} else {
						name = data.getAttribute("name");
					}
					Expression expression;
					try {
						expression = macroDatabase.newQuery(queryString);
						expression.setName(name);
						this.expressions.add(expression);
					} catch (InitializationException e) {
						e.printStackTrace();
						expression = ExpressionFactory.newNullExpression("Error: " + queryString);
						this.expressions.add(expression);
					}
				}
			}

			try {
				// Writes the headers in the file.
				final FileWriter writer = new FileWriter(outputFile, true);
				for (Expression query : expressions) {
					final String title;
					if (query.getName() != null) {
						title = query.getName();
					} else {
						title = query.getQuery();
					}
					writer.write(title + this.separator);
				}
				writer.write(rc);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		protected void write() {
			try {
				FileWriter writer = new FileWriter(outputFile, true);
				for (Expression query : this.expressions) {
					final Double val = query.value();
					if (val != null && !val.isNaN() && !val.isInfinite()) {
						writer.write(nf.format(val) + separator);
					} else {
						writer.write("nan" + separator);
					}
				}
				writer.write(rc);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Export of individual data.
	 */
	private class ExportIndividual extends Export {

		/**
		 * The labels of the data to be exported.
		 */
		private final List<String> labels = new LinkedList<String>();

		/**
		 * The queries defining the data to be exported.
		 */
		private final List<String> queries = new LinkedList<String>();

		/**
		 * The sector.
		 */
		private final String sector;

		/**
		 * Creates an new Export.
		 * 
		 * @param description
		 *            the description of the export to be created.
		 * @param path
		 *            the path to the parent file.
		 */
		public ExportIndividual(Element description, String path) {

			super(description, path);

			this.sector = description.getAttribute("sector");

			final NodeList exportNodeList = description.getChildNodes();

			for (int i = 0; i < exportNodeList.getLength(); i++) {
				if (exportNodeList.item(i).getNodeName().equals("data")) {
					final Element data = (Element) exportNodeList.item(i);
					final String queryString = data.getAttribute("value");
					final String name;
					if (data.getAttribute("name").equals("")) {
						name = null;
					} else {
						name = data.getAttribute("name");
					}

					this.labels.add(name);
					this.queries.add(queryString);
				}
			}

		}

		@Override
		protected void write() {
			try {
				FileWriter writer = new FileWriter(outputFile, true);
				int index = 0;
				for (String name : this.labels) {
					if (name != null) {
						name = this.queries.get(index);
					}
					writer.write(name + separator);
					index++;
				}
				writer.write(rc);

				final Object[][] data = macroDatabase.getData(sector, queries.toArray(new String[0]),
						timer.getPeriod().intValue(), "");
				for (int i = 0; i < data.length; i++) {
					for (int j = 0; j < data[i].length; j++) {
						final String result;
						final Object value = data[i][j];
						if (value == null) {
							result = "nan";
						} else if (value instanceof Number) {
							result = nf.format(value);
						} else {
							result = value.toString();
						}
						writer.write(result + separator);
					}
					writer.write(rc);
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** The line separator. */
	final private static String rc = System.getProperty("line.separator");

	/** List of the export events for the current period. */
	// private final List<Element> exportEvents = new LinkedList<Element>();

	/** List of the exports. */
	private final List<Export> exports = new LinkedList<Export>();

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
		this.initExport(elem, path);
	}

	/**
	 * Initializes the exportation of data.
	 * 
	 * @param truc
	 *            an XML element that contains the description of the data to be
	 *            exported.
	 * @param path
	 *            the path to the scenario file.
	 */
	private void initExport(Element truc, String path) {

		// 2016-03-28: petite modification de la syntaxe.
		// Un tag 'exports' doit encadrer toutes les tags 'export'.
		// Seule la première balise <exports> est prise en compte.
		// 2016-04-03: implémentation du cas où le tag 'exports' est absente.

		if (path == null) {
			throw new IllegalArgumentException("Path is null");
		}

		final Node elem = truc.getElementsByTagName("exports").item(0);
		if (elem != null) {
			final NodeList nodeList = elem.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i).getNodeName().equals("export")) {
					final Element exportElem = (Element) nodeList.item(i);
					final String type = exportElem.getAttribute("data.type");
					final Export export;
					if (type.equals("aggregate")) {
						export = new ExportAggregate(exportElem, path);
					} else if (type.equals("individual")) {
						export = new ExportIndividual(exportElem, path);
					} else {
						export = null;
						throw new RuntimeException("Unknown type: " + type);
					}

					this.exports.add(export);
				}

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
	 * Creates and returns a collection of expressions providing access to the MacroDataBase.
	 * @param elem the XML description of the expressions to be created.
	 * @return a collection of expressions.
	 * @throws InitializationException if something goes wrong.
	 */
	public Map<String, Expression> getNewDataMap(Element elem) throws InitializationException {
		/*
		 * 2016-03-17 / Creation d'un registre contenant les données
		 * statistiques qui peuvent être délivrées aux agents du modèle. Par
		 * exemple, la banque peut avoir besoin de connaître l'inflation pour
		 * calculer son taux d'intérêt nominal.
		 * 2016-05-01 / Deplacement de cette methode de BasicCircuit vers BasicDataManager.
		 */
		final Map<String, Expression> result = new HashMap<String, Expression>();
		final Element queriesNode = (Element) elem.getElementsByTagName("queries").item(0);
		if (queriesNode != null) {
			final NodeList queriesList = queriesNode.getChildNodes();
			for (int i = 0; i < queriesList.getLength(); i++) {
				final Node item = queriesList.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					final Element element = (Element) item;
					final String query = element.getNodeName();
					final String value = element.getAttribute("value");
					if (value == null) {
						throw new InitializationException(query + ". Value is missing.");
					}
					final Expression expression = this.getMacroDatabase().newQuery(value);
					result.put(query, expression);
				}
			}
		}
		return result;
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
						if (value != null && !value.isNaN() && !value.isInfinite()) {
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
		// Exports the data into the output file.
		for (Export export : exports) {
			export.run();
		}
		/*
		 * 2016-05-01: inutile.
		 * for (Element event : exportEvents) {
			try {
				exportData(event);
			} catch (IOException e) {
				throw new RuntimeException("Something went wrong while exporting data.", e);
			}
		}
		exportEvents.clear();*/
	}

}

// ***
