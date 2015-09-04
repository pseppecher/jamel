package jamel.austrian.markets;

import jamel.austrian.markets.Market;
import jamel.basic.Circuit;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.data.SectorDataset;
import jamel.austrian.markets.MarketSector;
import jamel.austrian.sfc.SFCSector;

import org.w3c.dom.Element;

/**
 * A sector that contains the markets of the economy.
 */
public class MarketSector extends SFCSector {

	/** The markets of the economy.<br>
	 * 	Mind that the markets are zero-indexed, i.e. B1 firms sell in the market B0.  */
	protected final AgentSet<Market> markets;

	/** The median wage in the economy. */
	private double prevailingWage ; 

	/** The value of real GDP in the base year. */
	private static double realGDPBase = Float.NaN; 

	/** The value of real consumption in the base year. */
	private static double realCBase = Float.NaN; 
	

	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public MarketSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
		this.markets = new BasicAgentSet<Market>(this.random);
		markets.put(new Market("labor", circuit, this));
		markets.put(new Market("savings", circuit, this));
		markets.put(new Market("equity", circuit, this));
		markets.put(new Market("loans", circuit, this));
		markets.put(new Market("machines", circuit, this));
	}
	
	
	/**
	 * Returns the prevailing wage.
	 */
	public double getPrevailingWage() {
		return prevailingWage;
	}


	@Override
	public Phase getPhase(String name) {
		Phase result = null;

		if (name.equals("opening")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Market market:markets.getList()) {
						market.open();
					}
				}				
			};			
		}

		else if (name.equals("generalTrading")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					while (getHouseholdsSector().getDemanders()+getFirmsSector().getDemanders()!=0){ 			//The reason for this increased simultaneity is a smoothening of production. 
						if (getHouseholdsSector().getDemanders()>0) getHouseholdsSector().budgetAllocation(); //Firms can now plan their production just-in-time.
						if (getFirmsSector().getDemanders()>0)	getFirmsSector().investment();
					}
				}				
			};			
		}
		

		else if (name.equals("generalTrading2")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					while (getHouseholdsSector().getDemanders()+getFirmsSector().getDemanders()!=0){ 	
						if (getFirmsSector().getDemanders()>0) getFirmsSector().investment(); 
						if (getHouseholdsSector().getDemanders()>0) getHouseholdsSector().budgetAllocation();					
					}
				}				
			};			
		}

		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Market market:markets.getList()) {
						market.close();
					}
					if (!Float.isNaN(markets.get("labor").getPriceLevel())) 
						prevailingWage = markets.get("labor").getPriceLevel();
				}				
			};			
		}

		return result;
	}


	/**
	 * Adds a new market to the set of goods markets if necessary. 
	 */
	public void addMarkets(String type, int stage){

		int nextLowerStage = stage - 1;
		//int typeNumber = Alphabet.getPosition(type);

		String marketIndex = type+nextLowerStage;
		if (!markets.contains(marketIndex)) {					
			markets.put(new Market(type,nextLowerStage, circuit, this));
		}

		//This is needed if intermediate goods have multiple specificity.
		/*if (nextLowerStage==0){
			String marketIndex = type+0;
			if (!markets.containsKey(marketIndex)) {					
				markets.put(marketIndex, new Market(type,0));
			}
			return;
		}

		for (int i=typeNumber-1; i<=typeNumber+1; i++){
			if (i>=0){
				String targetType = Alphabet.getLetter(i);
				String marketIndex = targetType+nextLowerStage;
				if (!markets.containsKey(marketIndex)) {					
					markets.put(marketIndex, new Market(targetType,nextLowerStage));
				}
			}
		}*/
	}
	

	/**
	 * Returns a market as specified by the key. 
	 */
	public Market getMarket(String key) {
		if (key==null) return null;
		if (!markets.contains(key)) return null;
		return markets.get(key);
	}


	@Override
	public SectorDataset getDataset() {

		SectorDataset marketData = markets.collectData();
		if ((Double.isNaN(realCBase) | realCBase == 0D)) {
			realGDPBase = getFixedPrice_GDP_Index();
			realCBase = getFixedPrice_C_Index();
			marketData.putSectorialValue("realGDP", Double.valueOf(100));
			marketData.putSectorialValue("realConsumption", Double.valueOf(100));
		}
		else {
			// Calculates the real indices
			double realIndex = getFixedPrice_GDP_Index() / realGDPBase * 100;
			double cIndex = getFixedPrice_C_Index() / realCBase * 100;
			marketData.putSectorialValue("realGDP", Double.valueOf(realIndex));
			marketData.putSectorialValue("realConsumption", Double.valueOf(cIndex));
		}

		return marketData;
	}


	private double getFixedPrice_C_Index()
	{
		double index = 0F;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			if (market.getName().equals("labor")) continue;
			if (market.getStage() >= 1) continue; //getStage() returns the stage where goods are sold, not where they are produced. Hence, >=1. 
			index += market.getQIndex();
		}

		return index;
	}


	private double getFixedPrice_GDP_Index()
	{
		double index = 0F;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			index += market.getQIndex();
		}
		return index;
	}

	@SuppressWarnings("unused")
	private double getLaspeyres_Q_Index()
	{
		double numerator = 0F; double denominator = 0F;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			if (market.getSales() == 0) continue;
			if (market.getLastSales() == 0) continue;
			double x = market.getSales() * market.getLastTurnover() / market.getLastSales();
			numerator = numerator + x;
			denominator = denominator + market.getLastTurnover();
		}
		double quantityIndex = numerator / denominator;
		return quantityIndex;
	}

	@SuppressWarnings("unused")
	private double getLaspeyres_C_Index()
	{
		double numerator = 0F; double denominator = 0F;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			if (market.getStage() > 1) continue;
			if (market.getSales() == 0) continue;
			if (market.getLastSales() == 0) continue;
			double x = market.getSales() * market.getLastTurnover() / 
					market.getLastSales();
			numerator = numerator + x;
			denominator = denominator + market.getLastTurnover();
		}

		double quantityIndex = numerator / denominator;
		return quantityIndex;
	}

	@SuppressWarnings("unused")
	private double getLaspeyres_P_Index()
	{
		double numerator = 0D; double denominator = 0D;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			if (market.getSales() == 0) continue;
			if (market.getLastSales() == 0) continue;
			double x = market.getTurnover() / market.getSales() * market.getLastSales();
			numerator += x;
			denominator += market.getLastTurnover();
		}

		double priceIndex = numerator / denominator;
		return priceIndex;
	}

	@SuppressWarnings("unused")
	private double getPaascheIndex()
	{
		double numerator = 0D; double denominator = 0D;
		for (Market market : markets.getList()){
			if (market.getName().equals("loans")) continue;
			if (market.getName().equals("equity")) continue;
			if (market.getName().equals("savings")) continue;
			if (market.getSales() == 0) continue;
			if (market.getLastSales() == 0) continue;
			numerator += market.getTurnover();
			double x = market.getLastTurnover() / market.getLastSales() * market.getSales();
			denominator += x;
		}

		double priceIndex = numerator / denominator;
		return priceIndex;
	}
	
	
	@Override
	public void doEvent(Element event) {
		// Respects the Sector interface
	}
	
}
