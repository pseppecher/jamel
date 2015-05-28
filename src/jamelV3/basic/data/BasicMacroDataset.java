package jamelV3.basic.data;

import jamelV3.basic.sector.SectorDataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

/**
 * A basic dataset for macro-economic data.
 */
public class BasicMacroDataset extends HashMap<String,SectorDataset> implements MacroDataset {

	/** A cache for frequent queries. */
	private final Map<String,Double> cache = new HashMap<String,Double>();
	
	/**
	 * Returns a new <code>Double</code> initialized to the value 
	 * represented by the specified <code>String</code>.
	 * @param query the string the value of which is to be returned.
	 * @return the <code>Double</code> value represented by the string argument.
	 */
	private Double calculate(String query) {
		try {
			final Double result;
			if (query.contains("+")) {
				String[] truc = query.split("\\+", 2);
				result = calculate(truc[0])+calculate(truc[1]);			
			}
			else if (query.contains("/")) {
				String[] truc = query.split("/", 2);
				final Double val2 = calculate(truc[1]);
				if (val2!=0) {
					result = calculate(truc[0])/calculate(truc[1]);
				}
				else {
					result = null;
				}
			}
			else if (query.contains("*")) {
				String[] truc = query.split("\\*", 2);
				result = calculate(truc[0])*calculate(truc[1]);			
			}
			else if (query.contains("!")) {
				String[] truc = query.split("\\!", 2);
				result = calculate(truc[0])*-calculate(truc[1]);			
			}
			else if (query.contains("?")) {
				String[] truc = query.split("\\?", 2);
				final Double val2 = calculate(truc[1]);
				if (val2!=0) {
					result = calculate(truc[0])/-calculate(truc[1]);
				}
				else {
					result = null;
				}
			}
			else {
				Double val;
				try {
					val = Double.parseDouble(query);
				} catch (NumberFormatException e) {
					if (query.startsWith("-")) {
						val = -getValue(query.substring(1));
					}
					else {
						val = getValue(query);
					}
				}
				result = val;
			}
			return result;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Returns the value for the specified key.
	 * @param key the key of the value to return.
	 * @return the value for the specified key.
	 */
	private Double getValue(String key) {
		final Double result;
		if (cache.containsKey(key)) {
			result=cache.get(key);
		}
		else {
			final String[] keys = key.split("\\.",2);
			final SectorDataset sectorDataset = super.get(keys[0]);
			if (sectorDataset!=null) {
				try {
					result = sectorDataset.get(keys[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					throw new RuntimeException("Syntaw error in: "+key);
				}
			}
			else {
				result=null;
			}
			cache.put(key, result);
		}
		return result;
	}

	/**
	 * Returns a new <code>Double</code> initialized to the value 
	 * represented by the specified <code>String</code>.
	 * @param query the string the value of which is to be returned.
	 * @return the <code>Double</code> value represented by the string argument.
	 */
	private Double parseExpression(String query) {
		Double result = null;
		Integer beginIndex = null;
		Integer endIndex = null;
		if (!query.contains("(")) {
			result = valueOf(query);			
		}
		else {
			for (int i=0; i<query.length(); i++) {
				if (query.charAt(i)=='(') {
					beginIndex = i;
				}
				else if  (query.charAt(i)==')') {
					endIndex = i;
					final String sub = query.substring(beginIndex+1, endIndex);
					if (sub==null) {
						throw new NullPointerException("Unexpected problem with this string: "+query);
					}
					Double val = valueOf(sub);
					if (val!=null) {
						String query2 = query.replace("("+sub+")", val.toString());
						result = parseExpression(query2);
					}
					else {
						result = null;
					}
					break;
				}
			}
			if (beginIndex!=null&&endIndex==null) {
				throw new RuntimeException("Unbalanced parenthesis: "+query);
			}
		}
		return result;
	}
	
	/**
	 * Returns a new <code>Double</code> initialized to the value 
	 * represented by the specified <code>String</code>.
	 * @param query the string the value of which is to be returned.
	 * @return the <code>Double</code> value represented by the string argument.
	 */
	private Double valueOf(String query) {
		query = query.replace(" ", "");
		query = query.replace("*-", "!");
		query = query.replace("/-", "?");
		query = query.replace("-", "+-");
		if (query.startsWith("+")) {
			query = query.substring(1);
		}
		return calculate(query);
	}

	@Override
	public void clear() {
		this.cache.clear();
		super.clear();
	}

	@Override
	public Double get(String query) {
		return parseExpression(query);
	}
	
	@Override
	public List<XYDataItem> getScatterData(String sector, String xKey, String yKey, String select) {
		final List<XYDataItem> result;
		final SectorDataset sectorDataset = super.get(sector);
		if (sectorDataset!=null) {
			result = sectorDataset.getScatter(xKey,yKey,select);
		}
		else {
			result=null;
		}		
		return result;
	}
	
	@Override
	public void putData(String sector, SectorDataset sectorDataset) {
		this.put(sector, sectorDataset);
	}

	@Override
	public double[][] getXYZData(String sector, String xKey, String yKey, String zKey) {
		return super.get(sector).getXYZData(xKey,yKey,zKey);
	}

}

// ***
