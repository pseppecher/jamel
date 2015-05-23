package jamelV3.jamel.households;

import jamelV3.basic.Circuit;
import jamelV3.basic.sector.AbstractPhase;
import jamelV3.basic.sector.AgentSet;
import jamelV3.basic.sector.BasicAgentSet;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.Timer;
import jamelV3.jamel.roles.Shareholder;
import jamelV3.jamel.sectors.BankingSector;
import jamelV3.jamel.sectors.CapitalistSector;
import jamelV3.jamel.sectors.EmployerSector;
import jamelV3.jamel.sectors.HouseholdSector;
import jamelV3.jamel.sectors.SupplierSector;
import jamelV3.jamel.widgets.BankAccount;
import jamelV3.jamel.widgets.JobOffer;
import jamelV3.jamel.widgets.Supply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A basic household sector.
 */
public class BasicHouseholdSector implements Sector, HouseholdSector, CapitalistSector {

	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The type of the agents. */
	protected String agentType;

	/** 
	 * The banking sector.
	 * The banking sector provides new accounts to households. 
	 */
	protected BankingSector banks = null;

	/** The circuit. */
	protected final Circuit circuit;

	/** The agent counter. */
	protected int countAgents;

	/** The employers. */
	protected EmployerSector employers;

	/** The collection of agents. */
	protected final AgentSet<Household> households;

	/** The sector name. */
	protected final String name;

	/** The parameters of the household sector. */
	protected final Map<String,Float> parameters = new HashMap<String,Float>();

	/** The random. */
	protected final Random random;

	/** 
	 * The supplier sector.
	 * The supplier sector supplies commodities to households. 
	 */
	protected SupplierSector suppliers = null;

	/** The timer. */
	protected Timer timer;

	/**
	 * Creates a new sector for households.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BasicHouseholdSector(String name, Circuit circuit) {
		this.circuit = circuit;
		this.random = this.circuit.getRandom();
		this.timer = this.circuit.getTimer();
		this.name = name;
		this.households = new BasicAgentSet<Household>(this.random);
	}

	/**
	 * Closes the sector at the end of the period.
	 */
	protected void close() {
		for (final Household household:this.households.getList()) {
			household.close();
		}
	}

	/**
	 * Creates households.
	 * @param type the type of households to create.
	 * @param lim the number of households to create.
	 * @return a list containing the new households.
	 */
	protected List<Household> createHouseholds(String type, int lim) {
		final List<Household> list = new ArrayList<Household>(lim);
		for(int index=0;index<lim;index++) {
			this.countAgents++;
			final String name = this.name+"-"+this.countAgents;
			try {
				list.add((Household) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,HouseholdSector.class).newInstance(name,this));
			} catch (Exception e) {
				throw new RuntimeException("Something goes wrong while creating households.",e);
			}
		}
		return list;
	}

	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("new")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.households.putAll(this.createHouseholds(this.agentType,size));
		}
		else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName());			
		}
	}

	@Override
	public SectorDataset getDataset() {
		return this.households.collectData();
	}

	@Override
	public JobOffer[] getJobOffers(int i) {
		return this.employers.getJobOffers(i);			
	}

	/**
	 * Returns the sector name.
	 * @return the sector name.
	 */
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public BankAccount getNewAccount(Household household) {
		return this.banks.getNewAccount(household);
	}

	@Override
	public Float getParam(String key) {
		return this.parameters.get(key);
	}

	@Override
	public Phase getPhase(String name) {
		Phase result = null;
		
		if (name.equals("opening")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Household household:households.getList()) {
						household.open();
					}
				}				
			};			
		}
		
		else if (name.equals("job_search")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Household household:households.getShuffledList()) {
						household.jobSearch();
					}
				}				
			};			
		}
		
		else if (name.equals("consumption")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Household household:households.getShuffledList()) {
						household.consumption();
					}
				}				
			};			
		}
		
		else if (name.equals("closure")) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicHouseholdSector.this.close();
				}				
			};			
		}		
		
		return result;
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public Supply[] getSupplies(int i) {
		return this.suppliers.getSupplies(i);			
	}

	@Override
	public Timer getTimer() {
		return this.timer;
	}

	@Override
	public void init(Element element) throws InitializationException {
		
		// Initialization of the dependencies:
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}
		
		// Initialization of the agent type:
		final String agentAttribute = element.getAttribute("agent");
		if ("".equals(agentAttribute)) {
			throw new InitializationException("Attribute not found: agent");
		}
		this.agentType =agentAttribute;		
		
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement==null) {
			throw new InitializationException("Element not found: "+DEPENDENCIES);
		}
		
		// Looking for the supplier sector.
		final String key1 = "Suppliers";
		final Element suppliersElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (suppliersElement==null) {
			throw new InitializationException("Element not found: "+key1);
		}
		final String suppliersKey = suppliersElement.getAttribute("value");
		if (suppliersKey=="") {
			throw new InitializationException("Missing attribute: value");
		}
		this.suppliers = (SupplierSector) circuit.getSector(suppliersKey);
		
		// Looking for the employer sector.
		final String key2 = "Employers";
		final Element employersElement = (Element) refElement.getElementsByTagName(key2).item(0);
		if (employersElement==null) {
			throw new InitializationException("Element not found: "+key2);
		}
		final String employersKey = employersElement.getAttribute("value");
		if (employersKey=="") {
			throw new InitializationException("Missing attribute: value");
		}
		this.employers = (EmployerSector) circuit.getSector(employersKey);
		
		// Looking for the banking sector.
		final String key3 = "Banks";
		final Element banksElement = (Element) refElement.getElementsByTagName(key3).item(0);
		if (banksElement==null) {
			throw new InitializationException("Element not found: "+key3);
		}
		final String banksKey = banksElement.getAttribute("value");
		if (banksKey=="") {
			throw new InitializationException("Missing attribute: value");
		}
		this.banks = (BankingSector) circuit.getSector(banksKey);
		
		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.parameters.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
	}

	@Override
	public Shareholder selectRandomCapitalOwner() {
		return households.getRandomAgent();
	}

	@Override
	public List<Shareholder> selectRandomCapitalOwners(int n) {
		List<Shareholder> result = new ArrayList<Shareholder>(n);
		result.addAll(households.getSimpleRandomSample(n));
		return result;
	}

}

// ***
