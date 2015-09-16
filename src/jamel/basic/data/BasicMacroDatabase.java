package jamel.basic.data;

import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

/**
 * A basic database for the macro-economic level.
 */
public class BasicMacroDatabase implements MacroDatabase {

	@SuppressWarnings("javadoc")
	private static final String AGENT_VALUE = "agentValue";

	@SuppressWarnings("javadoc")
	private static final String MAX = "max";

	@SuppressWarnings("javadoc")
	private static final String MEAN = "mean";

	@SuppressWarnings("javadoc")
	private static final String MIN = "min";

	@SuppressWarnings("javadoc")
	private static final String SUM = "sum";

	@SuppressWarnings("javadoc")
	private static final String VAL = "val";

	/**
	 * Parses the specified string and returns the time lag. The string "t", for
	 * example, yields 0 as result, "t-1" yields 1, etc.
	 * 
	 * @param string
	 *            the string to be parsed.
	 * @return an <code>int</code> representing the time lag.
	 */
	private static int parseLag(String string) {
		final int lag;
		if (string.equals("t")) {
			lag = 0;
		} else if (string.startsWith("t-")) {
			lag = Integer.parseInt(string.substring(2));
		} else {
			throw new IllegalArgumentException("Bad time lag: " + string);
		}
		return lag;
	}

	/** A map that stores data so future requests can be served faster. */
	private final Map<String, Double> cache = new LinkedHashMap<String, Double>(1000) {

		private static final int MAX_ENTRIES = 2000;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Double> eldest) {
			return size() > MAX_ENTRIES;
		}

	};

	/** The macroeconomic dataset. */
	final private Map<Integer, Map<String, SectorDataset>> macroDataset = new HashMap<Integer, Map<String, SectorDataset>>();

	/** The maximum time lag used by the scenario. */
	private int maxLag = 0;

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new dataset.
	 * 
	 * @param timer
	 *            the timer.
	 */
	public BasicMacroDatabase(Timer timer) {
		super();
		if (timer == null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer = timer;
	}

	/**
	 * Returns the specified {@link SectorDataset}.
	 * 
	 * @param sector
	 *            the sector the dataset of which is to be returned.
	 * @param t
	 *            the period of the dataset to be returned.
	 * @return the specified {@link SectorDataset}.
	 */
	private SectorDataset getSectorDataset(String sector, int t) {
		final SectorDataset result;
		final Map<String, SectorDataset> truc = macroDataset.get(t);
		if (truc != null) {
			result = truc.get(sector);
		} else
			result = null;
		return result;
	}

	@Override
	public void clear() {
		this.cache.clear();
		this.macroDataset.clear();
	}

	@Override
	public Double[] getDistributionData(String sector, String key, int t, String select) {
		final Double[] result;
		final SectorDataset sectorDataset = getSectorDataset(sector, t);
		if (sectorDataset != null) {
			result = sectorDataset.getField(key, select);
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public Expression getFunction(final String query) throws InitializationException {
		final String formated = ExpressionFactory.format(query);
		final Expression result;
		if (query.equals("time()")) {
			result = new Expression() {

				@Override
				public String getQuery() {
					return formated;
				}

				@Override
				public Double value() {
					return (double) timer.getPeriod().intValue();
				}

			};
		} else {
			
			
			final String[] word = query.substring(0, query.length() - 1).split("\\(", 2);
			final String[] arg = word[1].split(",", 4);
			if (arg.length < 3) {
				throw new InitializationException("Malformed query: "+query);
			}
			final String sector = arg[0];
			final String data = arg[1];
			final String timeKey = arg[2];
			final String select;
			if (arg.length > 3) {
				select = arg[3];
			} else {
				select = "";
			}

			final int lag0;
			final int lag1;
			if (timeKey.contains("...")) {
				final String[] keys = timeKey.split("\\.\\.\\.", 2);
				if (keys.length !=2) {
					throw new InitializationException("Malformed query: "+query);
				}
				lag0 = parseLag(keys[0]);
				lag1 = parseLag(keys[1]) - 1;
			} else {
				lag0 = parseLag(timeKey);
				lag1 = lag0 - 1;
			}

			if (lag0 > this.maxLag) {
				maxLag = lag0;
			}

			if (word[0].equals(SUM)) {

				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						Double sum = null;
						for (int t = timer.getPeriod().intValue() - lag0; t < timer.getPeriod().intValue()
								- lag1; t++) {
							final Double value;
							final String key = word[0] + "(" + sector + "," + data + "," + t + "," + select + ")";
							if (cache.containsKey(key)) {
								value = cache.get(key);
							} else {
								final SectorDataset sectorData = getSectorDataset(sector, t);
								if (sectorData != null) {
									value = sectorData.getSum(data, select);
								} else {
									value = null;
								}
								cache.put(key, value);
							}
							if (value != null) {
								if (sum == null) {
									sum = value;
								} else {
									sum += value;
								}
							}
						}
						return sum;
					}
				};
			}

			else if (word[0].equals(MEAN)) {

				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						Double sum = null;
						int count = 0;
						for (int t = timer.getPeriod().intValue() - lag0; t < timer.getPeriod().intValue()
								- lag1; t++) {
							final Double value;
							final String key = word[0] + "(" + sector + "," + data + "," + t + "," + select + ")";
							if (cache.containsKey(key)) {
								value = cache.get(key);
							} else {
								final SectorDataset sectorData = getSectorDataset(sector, t);
								if (sectorData != null) {
									value = sectorData.getMean(data, select);
								} else {
									value = null;
								}
								cache.put(key, value);
							}
							if (value != null) {
								count++;
								if (sum == null) {
									sum = value;
								} else {
									sum += value;
								}
							}
						}
						if (count == 0) {
							return null;
						}
						return sum / count;
					}
				};
			}

			else if (word[0].equals(MAX)) {

				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						Double max = null;
						for (int t = timer.getPeriod().intValue() - lag0; t < timer.getPeriod().intValue()
								- lag1; t++) {
							final Double value;
							final String key = word[0] + "(" + sector + "," + data + "," + t + "," + select + ")";
							if (cache.containsKey(key)) {
								value = cache.get(key);
							} else {
								final SectorDataset sectorData = getSectorDataset(sector, t);
								if (sectorData != null) {
									value = sectorData.getMax(data, select);
								} else {
									value = null;
								}
								cache.put(key, value);
							}
							if (value != null) {
								if (max == null || value > max) {
									max = value;
								}
							}
						}
						return max;
					}
				};
			}

			else if (word[0].equals(MIN)) {

				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						Double min = null;
						for (int t = timer.getPeriod().intValue() - lag0; t < timer.getPeriod().intValue()
								- lag1; t++) {
							final Double value;
							final String key = word[0] + "(" + sector + "," + data + "," + t + "," + select + ")";
							if (cache.containsKey(key)) {
								value = cache.get(key);
							} else {
								final SectorDataset sectorData = getSectorDataset(sector, t);
								if (sectorData != null) {
									value = sectorData.getMin(data, select);
								} else {
									value = null;
								}
								cache.put(key, value);
							}
							if (value != null) {
								if (min == null || value < min) {
									min = value;
								}
							}
						}
						return min;
					}
				};
			}

			else if (word[0].equals(VAL)) {

				// On r�cup�re une valeur donn�e enregistr�e au niveau du
				// secteur.

				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						Double sum = null;
						for (int t = timer.getPeriod().intValue() - lag0; t < timer.getPeriod().intValue()
								- lag1; t++) {
							final Double value;
							final SectorDataset sectorData = getSectorDataset(sector, t);
							if (sectorData != null) {
								value = sectorData.getSectorialValue(data);
							} else {
								value = null;
							}
							if (value != null) {
								if (sum == null) {
									sum = value;
								} else {
									sum += value;
								}
							}
						}
						return sum;
					}

				};
			}

			else if (word[0].equals(AGENT_VALUE)) {

				// On r�cup�re une valeur donn�e pour un agent donn�.
				
				if (arg.length!=4) {
					throw new InitializationException("Malformed query: "+query);
				}
				
				final String agentName = arg[3];
				
				result = new Expression() {

					@Override
					public String getQuery() {
						return formated;
					}

					@Override
					public Double value() {
						final int t = timer.getPeriod().intValue() - lag0;
						final Double value;
						final SectorDataset sectorData = getSectorDataset(sector, t);
						if (sectorData != null) {
							value = sectorData.getAgentValue(data, agentName);
						} else {
							value = null;
						}
						return value;
					}
				};
			}

			else {
				result = null;
				throw new RuntimeException("Not yet implemented: " + query);
			}
		}
		return result;
	}

	@Override
	public List<XYDataItem> getScatterData(String sector, String x, String y, int t, String select) {
		final List<XYDataItem> result;
		final SectorDataset sectorDataset = getSectorDataset(sector, t);
		if (sectorDataset != null) {
			result = sectorDataset.getScatter(x, y, select);
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public double[][] getXYZData(String sector, String x, String y, String z, int t) {
		final double[][] result;
		final SectorDataset sectorDataset = getSectorDataset(sector, t);
		if (sectorDataset == null) {
			result = null;
		} else {
			result = sectorDataset.getXYZData(x, y, z);
		}
		return result;
	}

	@Override
	public Expression newQuery(String query) throws InitializationException {
		try {
			return ExpressionFactory.newExpression(query, this);
		} catch (Exception e) {
			throw new InitializationException("Something went wrong while parsing this query: \"" + query + "\"", e);
		}
	}

	@Override
	public void putData(String sector, SectorDataset sectorDataset) {
		final int t = timer.getPeriod().intValue();
		if (!this.macroDataset.containsKey(t)) {
			this.macroDataset.put(t, new HashMap<String, SectorDataset>());
			this.macroDataset.remove(t - (1 + this.maxLag));
		}
		final Map<String, SectorDataset> sectors = this.macroDataset.get(t);
		sectors.put(sector, sectorDataset);
	}

}

// ***
