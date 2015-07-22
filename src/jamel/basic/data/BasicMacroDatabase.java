package jamel.basic.data;

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

	/** A map that stores data so future requests can be served faster. */
	private final Map<String,Double> cache = new LinkedHashMap<String,Double>(1000) {

		private static final int MAX_ENTRIES = 1000;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String,Double> eldest) {
			return size() > MAX_ENTRIES;
		}

	};

	/** The macroeconomic dataset.*/
	final private Map<Integer,Map<String,SectorDataset>> macroDataset = new HashMap<Integer,Map<String,SectorDataset>>();

	/** The timer. */
	private final Timer timer;

	/**
	 * Creates a new dataset. 
	 * @param timer the timer.
	 */
	public BasicMacroDatabase(Timer timer){
		super();
		if (timer==null) {
			throw new IllegalArgumentException("Timer is null.");
		}
		this.timer=timer;
	}

	/**
	 * Returns the specified {@link SectorDataset}.
	 * @param sector the sector the dataset of which is to be returned.
	 * @param t the period of the dataset to be returned.
	 * @return the specified {@link SectorDataset}.
	 */
	private SectorDataset getSectorDataset(String sector, int t) {
		final SectorDataset result;
		final Map<String, SectorDataset> truc = macroDataset.get(t);
		if (truc!=null) {
			result = truc.get(sector);
		}
		else result =null;
		return result;
	}

	@Override
	public void clear() {
		this.cache.clear();
		this.macroDataset.clear();
	}

	@Override
	public Expression getFunction(final String query) {
		final Expression result;
		final String[] word = query.substring(0, query.length()-1).split("\\(",2);
		final String[] arg = word[1].split(",",4);
		final String sector = arg[0];
		final String data = arg[1];
		final String timeKey = arg[2];
		final String select;
		if (arg.length>3) {
			select=arg[3];
		}
		else {
			select="";
		}

		final int lag;
		if (timeKey.equals("t")) {
			lag = 0;
		}
		else if (timeKey.startsWith("t-")){
			lag = Integer.parseInt(timeKey.substring(2));
		}
		else {			
			throw new IllegalArgumentException("Malformed query: "+query);
		}

		if (word[0].equals(SUM)) {

			result=new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final String key = word[0]+"("+sector+","+data+","+t+","+select+")";
					if (cache.containsKey(key)) {
						value=cache.get(key);
					}
					else {
						final SectorDataset sectorData = getSectorDataset(sector,t);
						if (sectorData!=null) {
							value=sectorData.getSum(data, select);
						}
						else {
							value=null;
						}
						cache.put(key, value);
					}
					return value;
				}
			};
		}

		else if (word[0].equals(MEAN)) {

			result=new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final String key = word[0]+"("+sector+","+data+","+t+","+select+")";
					if (cache.containsKey(key)) {
						value=cache.get(key);
					}
					else {
						final SectorDataset sectorData = getSectorDataset(sector,t);
						if (sectorData!=null) {
							value=sectorData.getMean(data, select);
						}
						else {
							value=null;
						}
						cache.put(key, value);
					}
					return value;
				}
			};
		}

		else if (word[0].equals(MAX	)) {

			result=new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final String key = word[0]+"("+sector+","+data+","+t+","+select+")";
					if (cache.containsKey(key)) {
						value=cache.get(key);
					}
					else {
						final SectorDataset sectorData = getSectorDataset(sector,t);
						if (sectorData!=null) {
							value=sectorData.getMax(data, select);
						}
						else {
							value=null;
						}
						cache.put(key, value);
					}
					return value;
				}
			};
		}

		else if (word[0].equals(MIN)) {

			result=new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final String key = word[0]+"("+sector+","+data+","+t+","+select+")";
					if (cache.containsKey(key)) {
						value=cache.get(key);
					}
					else {
						final SectorDataset sectorData = getSectorDataset(sector,t);
						if (sectorData!=null) {
							value=sectorData.getMin(data, select);
						}
						else {
							value=null;
						}
						cache.put(key, value);
					}
					return value;
				}
			};
		}

		else if (word[0].equals(VAL)) {

			// On récupère une valeur donnée enregistrée au niveau du secteur.			

			result=new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final SectorDataset sectorData = getSectorDataset(sector,t);
					if (sectorData!=null) {
						value=sectorData.getSectorialValue(data);
					}
					else {
						value = null;
					}
					return value;
				}
			};
		}

		else if (word[0].equals(AGENT_VALUE)) {

			// On récupère une valeur donnée pour un agent donné.

			result = new Expression() {

				@Override
				public String toString(){
					return query;
				}

				@Override
				public Double value() {
					final int t = timer.getPeriod().intValue()-lag;
					final Double value;
					final SectorDataset sectorData = getSectorDataset(sector,t);
					if (sectorData!=null) {
						value=sectorData.getAgentValue(data,arg[3]);
					}
					else {
						value = null;
					}
					return value;
				}
			};
		}

		else {

			result=null;
			throw new RuntimeException("Not yet implemented: "+query);

		}

		return result;
	}

	@Override
	public List<XYDataItem> getScatterData(String sector, String x, String y, int t, String select) {
		final List<XYDataItem> result;
		final SectorDataset sectorDataset = getSectorDataset(sector,t);
		if (sectorDataset!=null) {
			result = sectorDataset.getScatter(x,y,select);
		}
		else {
			result=null;
		}		
		return result;
	}

	@Override
	public double[][] getXYZData(String sector, String x, String y, String z, int t) {
		final double[][] result;
		final SectorDataset sectorDataset = getSectorDataset(sector,t);
		if (sectorDataset==null) {
			result=null;
		}
		else {
			result = sectorDataset.getXYZData(x,y,z);
		}
		return result;
	}

	@Override
	public void putData(String sector, SectorDataset sectorDataset) {
		final int t = timer.getPeriod().intValue();
		if (!this.macroDataset.containsKey(t)) {
			this.macroDataset.put(t, new HashMap<String,SectorDataset>());
			this.macroDataset.remove(t-2);
		}
		final Map<String, SectorDataset> sectors = this.macroDataset.get(t);
		sectors.put(sector, sectorDataset);
	}

}

// ***
