package jamel.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

import jamel.Jamel;
import jamel.util.Agent;
import jamel.util.JamelObject;
import jamel.util.Sector;

/**
 * A manager for sector data.
 */
public class SectorDataManager extends JamelObject {

	/**
	 * A class for the caches.
	 * 
	 * @param <T>
	 *            type of the keys
	 * @param <Y>
	 *            type of the values
	 */
	private class Cache<T, Y> extends LinkedHashMap<T, Y> {

		/**
		 * Constructs an empty Cache instance with the specified initial
		 * capacity.
		 * 
		 * @param initialCapacity
		 *            the initial capacity
		 */
		public Cache(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<T, Y> eldest) {
			return size() > MAX_CACHE_SIZE;
		}
	}

	/**
	 * Constant for the MAX operator.
	 */
	private static final int MAX = 2;

	/**
	 * The maximum size of the cache.
	 */
	static final private int MAX_CACHE_SIZE = 100;

	/**
	 * Constant for the MIN operator.
	 */
	private static final int MIN = 1;

	/**
	 * Constant for the SUM operator.
	 */
	private static final int SUM = 0;

	/**
	 * Returns the {@code DataKeys} of the specified class of {@code Agent}.
	 * 
	 * @param agentClass
	 *            a class of {@code Agent}.
	 * @return the {@code DataKeys} of the specified class of {@code Agent}.
	 */
	private static DataKeys getDataKeys(Class<? extends Agent> agentClass) {
		DataKeys result;
		try {
			final Method getActionMethod = agentClass.getMethod("getDataKeys");
			result = (DataKeys) getActionMethod.invoke(null);
		} catch (@SuppressWarnings("unused") NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			result = null;
		}
		return result;
	}

	/**
	 * Parses the string argument as a positive integer.
	 * The string must be equals to "t" or must start with "t-", followed by a
	 * positive integer representing a lag.
	 * 
	 * <p>
	 * Examples:
	 * <blockquote>
	 * 
	 * <pre>
	 * parseLag("t") returns 0
	 * parseLag("t-1") returns 1
	 * parseLag("t-12") returns 12
	 * parseInt("t+1") throws a IllegalArgumentException
	 * parseInt("-1") throws a IllegalArgumentException
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param lag
	 *            a string containing the representation of the lag.
	 * @return a positive integer.
	 */
	private static int parseLag(final String lag) {
		final int result;
		if (lag.equals("t")) {
			result = 0;
		} else if (!lag.startsWith("t-")) {
			throw new IllegalArgumentException(lag);
		} else {
			result = Integer.parseInt(lag.substring(2));
		}
		if (result < 0) {
			throw new IllegalArgumentException(lag);
		}
		return result;
	}

	/**
	 * Parses the string argument representing a span and returns an integer
	 * representing a bound of the span.
	 * 
	 * <p>
	 * Examples:
	 * <blockquote>
	 * 
	 * <pre>
	 * parsePeriods("t-12...t",0) returns 12
	 * parsePeriods("t-12...t",1) returns 0
	 * parsePeriods("t",0) returns 0
	 * parsePeriods("t",1) returns 0
	 * parsePeriods("t-12",0) returns 12
	 * parsePeriods("t-12",1) returns 12
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param periods
	 *            a string representing a span of periods.
	 * @param index
	 *            0 (returns the lower bound) or 1 (returns the higher bound)
	 * @return an integer
	 */
	static private int parsePeriods(final String periods, int index) {
		final int result;
		if (periods.equals("t")) {
			result = 0;
		} else if (periods.contains("...")) {
			result = parseLag(periods.split("\\.\\.\\.", 2)[index].trim());
		} else {
			result = parseLag(periods);
		}
		return result;
	}

	/**
	 * The list of the agents.
	 */
	final private List<Agent> agents = new ArrayList<>();

	/**
	 * The cache of the results.
	 */
	final private Cache<String, Double> cache = new Cache<>(MAX_CACHE_SIZE);

	/**
	 * The indexes <-> keys dictionary for the sector.
	 */
	final private DataKeys dataKeys;

	/**
	 * The pool of the expressions.
	 */
	final private Map<String, Expression> expressions = new HashMap<>();

	/**
	 * The parent sector.
	 */
	final private Sector sector;

	/**
	 * Creates a new data manager.
	 * 
	 * @param agents
	 *            the list of the agents.
	 * @param sector
	 *            the parent sector.
	 */
	public SectorDataManager(List<? extends Agent> agents, Sector sector) {
		this(sector);
		this.agents.addAll(agents);
	}

	/**
	 * Creates a new data manager.
	 * 
	 * @param sector
	 *            the parent sector.
	 */
	public SectorDataManager(Sector sector) {
		super(sector.getSimulation());
		this.sector = sector;
		this.dataKeys = getDataKeys(this.sector.getAgentClass());
	}

	/**
	 * Returns the expression for the max of all specified values for the
	 * specified period.
	 * 
	 * @param dataKey
	 *            the key of the values.
	 * @param periods
	 *            the periods.
	 * @return the expression for the max.
	 */
	private Expression getMax(String dataKey, String periods) {

		final int min = parsePeriods(periods, 0);
		final int max = parsePeriods(periods, 1);
		if (min != max) {
			Jamel.println(min, max);
			throw new RuntimeException("Bad periods: " + periods);
		}

		final Expression expression = new Expression() {

			/**
			 * The date of the value in the cache.
			 */
			private Integer cacheDate = null;

			/**
			 * The value in the cache.
			 */
			private Double cacheValue = null;

			@Override
			public Double getValue() {
				final Double result;
				final int period = getPeriod();

				if (this.cacheDate != null && period == this.cacheDate) {
					result = this.cacheValue;
				} else {
					final int t = period - min;

					result = SectorDataManager.this.getValue(dataKey, MAX, t);

					// On place le résultat dans le cache de l'expression
					// pour éviter d'avoir à le
					// calculer à nouveau au cours de cette période.

					this.cacheValue = result;
					this.cacheDate = period;

				}

				return result;
			}

			@Override
			public String toString() {
				return "val(" + sector.getName() + ", " + dataKey + ", " + periods + ", max)";
			}

		};
		return expression;
	}

	/**
	 * Returns the expression for the minimum of all specified values for the
	 * specified period.
	 * 
	 * @param dataKey
	 *            the key of the values.
	 * @param periods
	 *            the periods.
	 * @return the expression for the minimum.
	 */
	private Expression getMin(String dataKey, String periods) {

		final int min = parsePeriods(periods, 0);
		final int max = parsePeriods(periods, 1);
		if (min != max) {
			Jamel.println(min, max);
			throw new RuntimeException("Bad periods: " + periods);
		}

		final Expression expression = new Expression() {

			/**
			 * The date of the value in the cache.
			 */
			private Integer cacheDate = null;

			/**
			 * The value in the cache.
			 */
			private Double cacheValue = null;

			@Override
			public Double getValue() {
				final Double result;
				final int period = getPeriod();
				if (this.cacheDate != null && period == this.cacheDate) {
					result = this.cacheValue;
				} else {
					final int t = period - min;

					result = SectorDataManager.this.getValue(dataKey, MIN, t);

					// On place le résultat dans le cache de l'expression
					// pour éviter d'avoir à le
					// calculer à nouveau au cours de cette période.

					this.cacheValue = result;
					this.cacheDate = period;

				}

				return result;
			}

			@Override
			public String toString() {
				return "val(" + sector.getName() + ", " + dataKey + ", " + periods + ", min)";
			}

		};
		return expression;
	}

	/**
	 * Returns the expression for the sum of all specified values for the
	 * specified period.
	 * 
	 * @param dataKey
	 *            the key of the values.
	 * @param periods
	 *            the periods.
	 * @return the expression for the sum.
	 */
	private Expression getSum(final String dataKey, final String periods) {

		if (!this.dataKeys.containsKey(dataKey)) {
			throw new RuntimeException("Unknown data key: '" + dataKey + "' for sector " + this.sector.getName());
		}

		final int min = parsePeriods(periods, 0);
		final int max = parsePeriods(periods, 1);
		if (min < max) {
			Jamel.println(min, max);
			throw new RuntimeException("Bad periods: " + periods);
		}

		final Expression expression = new Expression() {

			/**
			 * The date of the value in the cache.
			 */
			private Integer cacheDate = null;

			/**
			 * The value in the cache.
			 */
			private Double cacheValue = null;

			@Override
			public Double getValue() {
				Double result = null;
				final int period = getPeriod();
				if (this.cacheDate != null && period == this.cacheDate.intValue()) {
					result = this.cacheValue;
				} else {
					final int start = period - min;
					final int end = period - max;
					for (int t = start; t <= end; t++) {
						final Double sum = SectorDataManager.this.getValue(dataKey, SUM, t);
						if (sum != null) {
							if (result == null) {
								result = sum;
							} else {
								result += sum;
							}
						}
					}

					// On place le résultat dans le cache de l'expression
					// pour éviter d'avoir à le
					// calculer à nouveau au cours de cette période.

					this.cacheValue = result;
					this.cacheDate = period;

				}

				return result;
			}

			@Override
			public String toString() {
				return "val(" + sector.getName() + ", " + dataKey + ", " + periods + ", sum)";
			}

		};
		return expression;
	}

	/**
	 * Returns the specified value.
	 * 
	 * @param dataKey
	 *            the data key.
	 * @param op
	 *            the operator.
	 * @param t
	 *            the period.
	 * @return the specified value.
	 */
	@SuppressWarnings("null")
	private Double getValue(String dataKey, int op, int t) {
		final Double result;
		final String query = "dataKey=" + dataKey + ", op=" + op + ", t=" + t;
		if (cache.containsKey(query)) {
			result = cache.get(query);
		} else {
			final int dataIndex = this.dataKeys.indexOf(dataKey);
			Double sum = null;
			Double min = null;
			Double max = null;
			for (int i = 0; i < this.agents.size(); i++) {
				final Double val = this.agents.get(i).getData(dataIndex, t);
				if (val != null) {
					if (sum == null) {
						sum = val;
						min = val;
						max = val;
					} else {
						sum += val;
						if (val < min) {
							min = val;
						}
						if (val > max) {
							max = val;
						}
					}
				}
			}
			cache.put("dataKey=" + dataKey + ", op=" + SUM + ", t=" + t, sum);
			cache.put("dataKey=" + dataKey + ", op=" + MIN + ", t=" + t, min);
			cache.put("dataKey=" + dataKey + ", op=" + MAX + ", t=" + t, max);
			switch (op) {
			case SUM:
				result = sum;
				break;
			case MIN:
				result = min;
				break;
			case MAX:
				result = max;
				break;
			default:
				throw new RuntimeException("Bad operator: " + op);
			}
		}

		return result;
	}

	/**
	 * Returns an expression that provides an access to the specified data for
	 * the specified agent.
	 * 
	 * @param agentName
	 *            the name of the agent for the data to be accessed.
	 * 
	 * @param args
	 *            the arguments specifying the data to be accessed.
	 * 
	 * @return an expression.
	 */
	public Expression getDataAccess(String agentName, String[] args) {
		final Expression result;
		if (args.length != 2) {
			Jamel.println(args);
			throw new RuntimeException("Bad number of parameters: " + args.length);
		}
		final String key = agentName + "," + args[0] + "," + args[1];
		if (this.expressions.containsKey(key)) {
			result = this.expressions.get(key);
		} else {
			final String dataKey = args[0];
			final String lags = args[1];
			final int min = parsePeriods(lags, 0);
			final int max = parsePeriods(lags, 1);
			if (min != max) {
				Jamel.println(min, max);
				throw new RuntimeException("Bad time indexes: " + lags);
			}
			final int dataIndex = dataKeys.indexOf(dataKey);
			result = new Expression() {

				/**
				 * The date of the value in the cache.
				 */
				private Integer cacheDate = null;

				/**
				 * The value in the cache.
				 */
				private Double cacheValue = null;

				@Override
				public Double getValue() {
					Double value = null;
					final Integer currentPeriod = getPeriod();
					if (currentPeriod == this.cacheDate) {
						value = this.cacheValue;
					} else {
						final int t = currentPeriod - min;
						final String query = "agentName=" + agentName + ", key=" + dataKey + ", t=" + t;
						if (cache.containsKey(query)) {
							value = cache.get(query);
						} else {
							Double val = null;
							for (final Agent agent : SectorDataManager.this.agents) {
								if (agent.getName().equals(agentName)) {
									val = agent.getData(dataIndex, t);
									break;
								}
							}
							value = val;
							cache.put(query, value);
						}
						// On met en cache le résultat pour éviter d'avoir à
						// le
						// calculer à nouveau au cours de cette période.

						this.cacheValue = value;
						this.cacheDate = currentPeriod;

					}

					return value;
				}

				@Override
				public String toString() {
					return "val(" + sector.getName() + "." + agentName + ", " + dataKey + ", " + lags + ")";
				}

			};
			this.expressions.put(key, result);
		}
		return result;
	}

	/**
	 * Returns an expression that provides an access to the specified data.
	 * 
	 * @param args
	 *            the arguments specifying the data to be accessed through the
	 *            expression.
	 * @return an expression.
	 */
	public Expression getDataAccess(final String[] args) {
		final Expression result;
		if (args.length != 3) {
			Jamel.println(args);
			throw new RuntimeException("Bad number of parameters: " + args.length);
		}
		final String key = args[0] + "," + args[1] + "," + args[2];
		if (this.expressions.containsKey(key)) {
			result = this.expressions.get(key);
		} else {
			final String dataKey = args[0];
			final String period = args[1];
			final String operation = args[2];
			switch (operation) {
			case "sum":
				result = getSum(dataKey, period);
				break;
			case "min":
				result = getMin(dataKey, period);
				break;
			case "max":
				result = getMax(dataKey, period);
				break;
			default:
				throw new RuntimeException("Not yet implemented: '" + operation + "'");
			}
			this.expressions.put(key, result);
		}
		return result;
	}

	/**
	 * Returns the specified scatter {@code DynamicXYSeries}.
	 * 
	 * @param xKey
	 *            the key for the x values.
	 * @param yKey
	 *            the key for the y values.
	 * @param conditions
	 *            the conditions of update of the series.
	 * @param selection
	 *            a string that defines the selection criteria.
	 * @return the specified scatter {@code DynamicXYSeries}.
	 */
	public DynamicSeries getScatterSeries(final String xKey, final String yKey, final Expression[] conditions,
			final String selection) {

		final int xIndex = this.dataKeys.indexOf(xKey);
		final int yIndex = this.dataKeys.indexOf(yKey);

		final Integer selectIndex;
		final Double selectValue;
		if (selection != null) {
			final String[] select = selection.split("=", 2);
			selectIndex = this.dataKeys.indexOf(select[0]);
			selectValue = Double.parseDouble(select[1]);
		} else {
			selectIndex = null;
			selectValue = null;
		}

		return new AbstractScatterDynamicSeries(this.sector.getName() + "," + xKey + "," + yKey + "," + selection,
				false) {

			@SuppressWarnings("unchecked")
			@Override
			public void update(boolean refresh) {
				if (refresh) {
					boolean update = true;
					if (conditions != null) {
						for (int i = 0; i < conditions.length; i++) {
							if (conditions[i].getValue() == null || conditions[i].getValue() != 1) {
								update = false;
								break;
							}
						}
					}
					if (update) {
						final int t = getPeriod();
						this.data.clear();
						for (Agent agent : agents) {
							final Double x = agent.getData(xIndex, t);
							final Double y = agent.getData(yIndex, t);
							@SuppressWarnings("null")
							final boolean selected = (selectIndex == null
									|| selectValue.equals(agent.getData(selectIndex, t)));
							if (selected && x != null && y != null) {
								final XYDataItem item = new XYDataItem(x, y);
								this.data.add(item);
							}
						}
						this.fireSeriesChanged();
					}
				}
			}

		};
	}

	/**
	 * Appends all of the agent in the specified collection to this data
	 * manager.
	 * 
	 * @param list
	 *            the list of agents to be added.
	 */
	public void put(List<? extends Agent> list) {
		this.agents.addAll(list);
	}

}
