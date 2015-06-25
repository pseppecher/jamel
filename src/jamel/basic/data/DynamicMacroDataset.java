package jamel.basic.data;

import jamel.basic.sector.SectorDataset;
import jamel.basic.util.Timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;

/**
 * A dynamic dataset for macro-economic data.
 */
public class DynamicMacroDataset extends HashMap<String,SectorDataset> implements MacroDataset {

	/** The max number of period in memory. */
	private static final int memory = 12;

	/** A cache for frequent queries. */
	private final Map<String,Double> cache = new HashMap<String,Double>();

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new dataset. 
	 * @param timer the timer.
	 */
	public DynamicMacroDataset(Timer timer){
		super();
		if (timer==null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer=timer;
	}

	/**
	 * Transforms the name of the sector, adding a suffix with the number of the period.<p>
	 * If the current period is 20:
	 * <ul>
	 * <li>Industry_t => Industry_20</li>
	 * <li>Industry_t#1 => Industry_18</li>
	 * </ul>
	 * @param sector the name of the sector. 
	 * @return the name of the sector after the transformation.
	 */
	private String getDatedSector(String sector) {
		final String[] split = sector.split("_",2);
		if (split.length!=2) {
			throw new IllegalArgumentException("Malformed query: "+sector);
		}
		final String sector1 = split[0];
		final String time = split[1];		
		final int now = this.timer.getPeriod().intValue();
		final int t;
		if (time.equals("t")) {
			t = now;
		}
		else if (time.startsWith("t#")){
			final int past = Integer.parseInt(time.substring(2));
			t = now-past;
		}
		else {			
			throw new IllegalArgumentException("Malformed query: "+sector);
		}
		return sector1+"_"+t;
	}

	/**
	 * Returns the value for the specified query.
	 * @param query the query (format: 'sector[t].subQuery').
	 * @return the value for the specified query.
	 */
	private Double getValue(final String query) {
		final Double result;
		final String[] split = query.split("\\.",2);
		if (split.length!=2) {
			throw new IllegalArgumentException("Something went wrong with '"+query+"'");
		}
		final String sectorQuery = split[1];
		String datedSector;
		try {
			datedSector = getDatedSector(split[0]);
		} catch (Exception e) {
			throw new IllegalArgumentException("Something went wrong with '"+query+"'",e);
		}
		
		// The new query:
		final String query2 = datedSector+"."+sectorQuery;
		if (cache.containsKey(query2)) {
			result=cache.get(query2);
		}
		else {
			final SectorDataset sectorDataset = super.get(datedSector);
			if (sectorDataset!=null) {
				try {
					result = sectorDataset.get(sectorQuery);
				} catch (Exception e) {
					throw new IllegalArgumentException("Something went wrong with '"+query+"'",e);
				}
			}
			else {
				result=null;
			}
			cache.put(query2, result);
		}
		return result;
	}

	/**
	 * Returns a new <code>Double</code> initialized to the value 
	 * represented by the specified <code>String</code>.
	 * @param query the string the value of which is to be returned.
	 * @return the <code>Double</code> value represented by the string argument.
	 */
	Double calculate(String query) {
		final String query2;
		if (query.startsWith("+")) {
			query2 = query.substring(1);
		}
		else {
			query2 = query;			
		}
		try {
			try {
				final Double result;
				if (query2.contains("+")) {
					String[] truc = query2.split("\\+", 2);
					result = calculate(truc[0])+calculate(truc[1]);			
				}
				else if (query2.contains("/")) {
					String[] truc = query2.split("/", 2);
					final Double val2 = calculate(truc[1]);
					if (val2!=0) {
						result = calculate(truc[0])/calculate(truc[1]);
					}
					else {
						result = null;
					}
				}
				else if (query2.contains("*")) {
					String[] truc = query2.split("\\*", 2);
					result = calculate(truc[0])*calculate(truc[1]);			
				}
				else if (query2.contains("!")) {
					String[] truc = query2.split("\\!", 2);
					result = calculate(truc[0])*-calculate(truc[1]);			
				}
				else if (query2.contains("?")) {
					String[] truc = query2.split("\\?", 2);
					final Double val2 = calculate(truc[1]);
					if (val2!=0) {
						result = calculate(truc[0])/-calculate(truc[1]);
					}
					else {
						result = null;
					}
				}
				else if (query2.startsWith(":")) {
					result=-calculate(query2.substring(1));
				}
				else {
					Double val;
					try {
						val = Double.parseDouble(query2);
					} catch (NumberFormatException e) {
						val = getValue(query2);
					}
					result = val;
				}
				return result;
			} catch (NullPointerException e) {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while calculating '"+query+"'",e);
		}
	}

	/**
	 * Encodes the query.
	 * @param query the query to encode.
	 * @return the encoded query.
	 */
	String encode(String query) {
		query = query.replace(" ", "");
		query = query.replace("_t-", "_t#");
		query = query.replace("*-", "!");
		query = query.replace("/-", "?");
		query = query.replace("-", "+:");
		if (query.startsWith("+")) {
			query = query.substring(1);
		}
		return query;
	}

	/**
	 * Returns a new <code>Double</code> initialized to the value 
	 * represented by the specified <code>String</code>.
	 * @param query the string the value of which is to be returned.
	 * @return the <code>Double</code> value represented by the string argument.
	 */
	Double parseExpression(String query) {
		Double result = null;
		Integer beginIndex = null;
		Integer endIndex = null;
		if (!query.contains("(")) {
			result = calculate(query);			
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
					final Double val = calculate(sub);
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

	@Override
	public void clear() {
		//this.cache.clear();
		//super.clear();
	}

	@Override
	public Double get(String query) {
		final String encoded = encode(query);
		Double result = null;
		try {
			result = parseExpression(encoded);
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong while parsing '"+query+"'",e);
		}
		return result;
	}

	@Override
	public List<XYDataItem> getScatterData(String sector, String xKey, String yKey, String select) {
		final List<XYDataItem> result;
		final String datedSector=getDatedSector(sector);
		final SectorDataset sectorDataset = super.get(datedSector);
		if (sectorDataset!=null) {
			result = sectorDataset.getScatter(xKey,yKey,select);
		}
		else {
			result=null;
		}		
		return result;
	}

	@Override
	public double[][] getXYZData(String sector, String xKey, String yKey, String zKey) {
		final double[][] result;
		final SectorDataset sectorDataset = super.get(sector);
		if (sectorDataset==null) {
			result=null;
		}
		else {
			result = sectorDataset.getXYZData(xKey,yKey,zKey);
		}
		return result;
	}

	@Override
	public void putData(String sector, SectorDataset sectorDataset) {
		final int t = timer.getPeriod().intValue();
		this.put(sector+"_"+t, sectorDataset);
		this.remove(sector+"_"+(t-memory));
	}

}

// ***
