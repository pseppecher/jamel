package jamel.austrian.sfc;

import java.util.Random;

import jamel.austrian.Parameters;
import jamel.austrian.banks.AbstractBankingSector;
import jamel.austrian.banks.InvestmentBank;
import jamel.austrian.firms.FirmSector;
import jamel.austrian.households.HouseholdSector;
import jamel.austrian.markets.Market;
import jamel.austrian.markets.MarketSector;
import jamel.austrian.widgets.ProductionFunction;
import jamel.basic.Circuit;
import jamel.basic.util.Timer;

/**
 * Represents the sector of the households.
 */
public abstract class SFCObject {

	/** The agent name. */
	protected final String name;
	
	/** The circuit. */
	protected final Circuit circuit;
	
	/** The random. */
	protected final Random random;

	/** The timer. */
	protected Timer timer;
	
	/** The general parameters. */
	protected static Parameters parameters;
	
	/** The production function of the economy. */
	protected static ProductionFunction productionFunction;
	
	
	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public SFCObject(String aName, Circuit aCircuit) {
		name = aName;
		circuit = aCircuit;
		parameters = new Parameters();
		productionFunction = new ProductionFunction(parameters);
		random = circuit.getRandom();
		timer = circuit.getTimer();
	}


	/**
	 * Returns the name of the agent.
	 * @return the name.
	 */
	public String getName() {
		return this.name;
	}
	
	
	/**
	 * Returns the random of the agent.
	 * @return the random.
	 */
	public Random getRandom() {
		return this.random;
	}


	/**
	 * Returns the time of the agent.
	 * @return the timer.
	 */
	public Timer getTimer() {
		return this.timer;
	}

	
	
	/**
	 * Returns the firms sector.
	 */
	public FirmSector getFirmsSector() {
		return (FirmSector) circuit.getSector("Firms");
	}
	
	
	/**
	 * Returns the households sector.
	 */
	public HouseholdSector getHouseholdsSector() {
		return (HouseholdSector) circuit.getSector("Households");
	}
	
	
	/**
	 * Returns the banking sector.
	 */
	public AbstractBankingSector getBankingSector() {
		return (AbstractBankingSector) circuit.getSector("Banks");
	}
	
	
	/**
	 * Returns the investment bank.
	 */
	public InvestmentBank getInvestmentBank() {
		return (InvestmentBank) circuit.getSector("InvestmentBank");
	}
	
	
	/**
	 * Returns the market sector.
	 */
	public MarketSector getMarketSector() {
		return (MarketSector) circuit.getSector("Markets");
	}
	
	
	/**
	 * Returns the market sector.
	 */
	public Market getMarket(String market) {
		 MarketSector markets = (MarketSector) circuit.getSector("Markets");
		 return markets.getMarket(market);
	}
	
}
