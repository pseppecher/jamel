package jamel.basic.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

/**
 * A basic implementation of the {@link SectorDataset} interface.
 */
public class BasicSectorDataset implements SectorDataset {

	@SuppressWarnings("javadoc")
	private static final int MAX = 2;

	@SuppressWarnings("javadoc")
	private static final int MEAN = 1;

	@SuppressWarnings("javadoc")
	private static final int MIN = 3;

	@SuppressWarnings("javadoc")
	private static final int SUM = 0;

	/**
	 * Returns a string cleaned from useless parentheses and spaces.
	 * 
	 * @param dirty
	 *            the string to be cleaned up.
	 * @return the cleaned up string.
	 */
	private static String cleanUp(final String dirty) {
		final String result;
		if (dirty.contains(" ")) {
			final String str2 = dirty.replace(" ", "");
			result = cleanUp(str2);
		} else if (dirty.startsWith("+")) {
			final String str2 = dirty.substring(1, dirty.length());
			result = cleanUp(str2);
		} else if (dirty.charAt(0) == '(' && dirty.charAt(dirty.length() - 1) == ')') {
			int count = 1;
			for (int i = 1; i < dirty.length() - 1; i++) {
				if (dirty.charAt(i) == '(') {
					count++;
				} else if (dirty.charAt(i) == ')') {
					count--;
					if (count == 0) {
						break;
					}
				}
			}
			if (count == 1) {
				// Removes the global parentheses.
				final String str2 = dirty.substring(1, dirty.length() - 1);
				result = cleanUp(str2);
			} else {
				// Nothing to remove.
				result = dirty;
			}
		} else {
			// Nothing to remove.
			result = dirty;
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if parentheses in the specified query are
	 * balanced, <code>false</code> otherwise.
	 * 
	 * @param query
	 *            the query.
	 * @return <code>true</code> if parentheses in the specified query are
	 *         balanced, <code>false</code> otherwise.
	 */
	private static boolean isBalanced(final String query) {
		int count = 0;
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '(') {
				count++;
			} else if (query.charAt(i) == ')') {
				count--;
				if (count < 0) {
					// Not balanced !
					return false;
				}
			}
		}
		return count == 0;
	}

	/**
	 * Returns a selection of individual datasets.
	 * <p>
	 * FIXME: il est probable que cette methode plante si select contient
	 * sous-conditions (operateurs and() et or() imbriques).
	 * 
	 * @param data
	 *            the data in which the individual
	 * @param select
	 *            a String that describes the datasets to be selected (
	 *            <code>null</code> not permitted). If <b>select</b> is empty,
	 *            all datasets are selected.
	 * @return a selection of individual datasets.
	 */
	private static Map<String, AgentDataset> select(final Map<String, AgentDataset> data, final String select) {
		final Map<String, AgentDataset> result;
		if (select == null) {
			throw new IllegalArgumentException("Select is null");
		} else if (select.equals("")) {
			result = data;
		} else if (!isBalanced(select)) {
			throw new IllegalArgumentException("Parentheses not balanced: " + select);
		} else {
			final String query = cleanUp(select);
			if (query.startsWith("name=")) {
				final String name = query.substring(5, query.length());
				final AgentDataset dataset = data.get(name);
				if (dataset != null) {
					result = new HashMap<String, AgentDataset>();
					result.put(name, dataset);
				} else {
					result = null;
				}
			} else if (query.startsWith("name!=")) {
				final String name = query.substring(6, query.length());
				result = new HashMap<String, AgentDataset>();
				for (AgentDataset agentData : data.values()) {
					if (!agentData.getName().equals(name)) {
						result.put(agentData.getName(), agentData);
					}
				}
			} else if (query.startsWith("and(")) {
				if (!query.endsWith(")")) {
					throw new IllegalArgumentException("Select: " + select);
				}
				final String substring = query.substring(4, query.length() - 1);
				final String[] arg = substring.split(",");// split(substring);
				Map<String, AgentDataset> selection = data;
				for (int i = 0; i < arg.length; i++) {
					selection = select(selection, arg[i]);
				}
				result = selection;
			} else if (query.startsWith("or(")) {
				if (!query.endsWith(")")) {
					throw new IllegalArgumentException("Select: " + select);
				}
				final String substring = query.substring(3, query.length() - 1);
				final String[] arg = split(substring);
				final Map<String, AgentDataset> selection1 = select(data, arg[0]);
				final Map<String, AgentDataset> selection2 = select(data, arg[1]);
				selection1.putAll(selection2);
				result = selection1;
			} else if (query.contains("!=")) {
				final String term[] = query.split("!=", 2);
				final String key = term[0];
				final String value = term[1];
				result = new HashMap<String, AgentDataset>();
				for (AgentDataset agentData : data.values()) {
					if (agentData.get(key)!=null && !agentData.get(key).equals(Double.valueOf(value))) {
						result.put(agentData.getName(), agentData);
					}
				}
			} else if (query.contains("=")) {
				final String term[] = query.split("=", 2);
				final String key = term[0];
				final Double selectValue = Double.valueOf(term[1]);
				result = new HashMap<String, AgentDataset>();
				for (AgentDataset agentData : data.values()) {
					final Double agentValue = agentData.get(key);
					/*if (agentValue == null) {
						throw new NullPointerException("agent: " + agentData.getName() + "; field: " + key);
					}*/
					if (agentValue != null && agentValue.equals(selectValue)) {
						result.put(agentData.getName(), agentData);
					}
				}
			} else {
				result = null;
				throw new RuntimeException("Unexpected query: " + select);
			}
		}
		return result;
	}

	/**
	 * Breaks the specified String in two pieces around the first comma. Commas
	 * inside parentheses are ignored.
	 * 
	 * @param string
	 *            the string to be split.
	 * @return the array of strings computed by splitting this string around the
	 *         first comma.
	 */
	private static String[] split(final String string) {

		final String[] result;

		int count = 0;
		int split = -1;

		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);
			if (c == '(') {
				count++;
			} else if (c == ')') {
				count--;
			} else {
				if (count == 0) {
					// We are outside parentheses.
					if (c == ',') {
						split = i;
						break;
					}
				}
			}
		}
		if (split == -1) {
			result = new String[1];
			result[0] = string;
		} else {
			result = new String[2];
			result[0] = string.substring(0, split);
			result[1] = string.substring(split + 1, string.length());
		}
		return result;
	}

	/** A collection that maps agent names to agent dataset. */
	private final Map<String, AgentDataset> agentsData = new HashMap<String, AgentDataset>();

	/** A map that stores data so future requests can be served faster. */
	private final Map<String, Double> cache = new HashMap<String, Double>();

	/**
	 * Hendrik: The reason why I need this is that there are certain data that
	 * are sector-specific that can only be computed at the sector level (and
	 * not at the agent level). For example the calculation of GDP and real GDP.
	 * These require computations that are not simply the sum over the data of
	 * all agents in the sector (or something similar), but something more
	 * complicated as, for example, the credit market is excluded in those
	 * operations. Another example would be the number of firms that are created
	 * per time period.
	 */
	private final Map<String, Double> sectorialData = new HashMap<String, Double>();

	/**
	 * Returns an array that contains the sum, the mean, the max and the min of
	 * the specified collection of individual data.
	 * 
	 * @param data
	 *            the key for the data to analyse.
	 * @param select
	 *            how to select data.
	 * @return an array that contains the sum, the mean, the max and the min of
	 *         the specified collection of individual data.
	 */
	private Double[] getStats(String data, String select) {
		final Double[] result = new Double[5];
		Double sum = null;
		Double mean = null;
		Double max = null;
		Double min = null;
		final Map<String, AgentDataset> selection = select(this.agentsData, select);

		int count = 0;
		for (final AgentDataset agentDataset : selection.values()) {
			final Double val = agentDataset.get(data);
			if (val != null) {
				if (Double.isNaN(val)) {
					throw new RuntimeException("The value is not a number.");
				}
				if (sum == null) {
					sum = val;
				} else {
					sum += val;
				}
				if (max == null || max > val) {
					max = val;
				}
				if (min == null || min < val) {
					min = val;
				}
				count++;
			}
		}
		if (count == 0) {
			sum = null;
		} else {
			mean = sum / count;
		}
		result[SUM] = sum;
		result[MEAN] = mean;
		result[MAX] = max;
		result[MIN] = min;
		this.cache.put(SUM + "(" + data + "," + select + ")", sum);
		this.cache.put(MEAN + "(" + data + "," + select + ")", mean);
		this.cache.put(MAX + "(" + data + "," + select + ")", max);
		this.cache.put(MIN + "(" + data + "," + select + ")", min);
		return result;
	}

	@Override
	public Double getAgentValue(String dataKey, String agentName) {
		final Double result;
		final AgentDataset data = this.agentsData.get(agentName);
		if (data != null) {
			result = data.get(dataKey);
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public Double[] getField(String key, String select) {
		final Map<String, AgentDataset> selection = select(this.agentsData, select);
		final Double[] result;
		if (selection != null) {
			result = new Double[selection.size()];
			int i = 0;
			for (AgentDataset data : selection.values()) {
				result[i] = data.get(key);
				i++;
			}
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public Double getMax(String data, String select) {
		final Double result;
		final String key = MAX + "(" + data + "," + select + ")";
		if (this.cache.containsKey(key)) {
			result = this.cache.get(key);
		} else {
			result = this.getStats(data, select)[MAX];
		}
		return result;
	}

	@Override
	public Double getMean(String data, String select) {
		final Double result;
		final String key = MEAN + "(" + data + "," + select + ")";
		if (this.cache.containsKey(key)) {
			result = this.cache.get(key);
		} else {
			result = this.getStats(data, select)[MEAN];
		}
		return result;
	}

	@Override
	public Double getMin(String data, String select) {
		final Double result;
		final String key = MIN + "(" + data + "," + select + ")";
		if (this.cache.containsKey(key)) {
			result = this.cache.get(key);
		} else {
			result = this.getStats(data, select)[MIN];
		}
		return result;
	}

	@Override
	public List<XYDataItem> getScatter(String xKey, String yKey, String select) {
		final List<XYDataItem> result;
		final Map<String, AgentDataset> selection = select(this.agentsData, select);
		if (selection != null) {
			result = new ArrayList<XYDataItem>(selection.size());
			for (AgentDataset data : selection.values()) {
				final Double x = data.get(xKey);
				final Double y = data.get(yKey);
				if (x!=null && y!=null) {
					final XYDataItem item = new XYDataItem(data.get(xKey), data.get(yKey));
					result.add(item);
				}
			}
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public Double getSectorialValue(String key) {
		return this.sectorialData.get(key);
	}

	@Override
	public Double getSum(String data, String select) {
		final Double result;
		final String key = SUM + "(" + data + "," + select + ")";
		if (this.cache.containsKey(key)) {
			result = this.cache.get(key);
		} else {
			result = this.getStats(data, select)[SUM];
		}
		return result;
	}

	@Override
	public double[][] getXYZData(String xKey, String yKey, String zKey) {
		final double[][] result = new double[3][this.agentsData.size()];
		int i = 0;
		for (final AgentDataset data : agentsData.values()) {
			result[0][i] = data.get(xKey);
			result[1][i] = data.get(yKey);
			result[2][i] = data.get(zKey);
			i++;
		}
		return result;
	}

	@Override
	public void putIndividualData(AgentDataset agentDataset) {
		if (agentDataset == null) {
			throw new IllegalArgumentException("AgentDataset is null.");
		}
		this.agentsData.put(agentDataset.getName(), agentDataset);
	}

	@Override
	public void putSectorialValue(String key, Double value) {
		this.sectorialData.put(key, value);
	}

}

// ***
