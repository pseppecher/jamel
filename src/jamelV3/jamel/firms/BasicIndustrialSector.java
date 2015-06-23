package jamelV3.jamel.firms;

import jamelV3.basic.Circuit;
import jamelV3.basic.sector.AbstractPhase;
import jamelV3.basic.sector.AgentSet;
import jamelV3.basic.sector.BasicAgentSet;
import jamelV3.basic.sector.Phase;
import jamelV3.basic.sector.Sector;
import jamelV3.basic.sector.SectorDataset;
import jamelV3.basic.util.BasicParameters;
import jamelV3.basic.util.InitializationException;
import jamelV3.basic.util.JamelParameters;
import jamelV3.basic.util.Timer;
import jamelV3.jamel.roles.Shareholder;
import jamelV3.jamel.sectors.BankingSector;
import jamelV3.jamel.sectors.CapitalistSector;
import jamelV3.jamel.sectors.EmployerSector;
import jamelV3.jamel.sectors.SupplierSector;
import jamelV3.jamel.widgets.BankAccount;
import jamelV3.jamel.widgets.JobOffer;
import jamelV3.jamel.widgets.Supply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A basic industrial sector.
 */
public class BasicIndustrialSector implements Sector, IndustrialSector, SupplierSector, EmployerSector {

	/**
	 * The keys of the parameters of the sector.
	 */
	private static class KEY {

		/** REGENERATION_MAX */
		public static final String REGENERATION_MAX = "regenerationLapse.max";

		/** REGENERATION_MIN */
		public static final String REGENERATION_MIN = "regenerationLapse.min";

	}

	@SuppressWarnings("javadoc")
	protected static class PHASE {
		public static final String CLOSURE = "closure";
		public static final String OPENING = "opening";
		public static final String PAY_DIVIDEND = "pay_dividend";
		public static final String PLAN_PRODUCTION = "plan_production";
		public static final String PRODUCTION = "production";
	}

	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The type of the agents. */
	private String agentType = null;

	/** The banking sector. */
	private BankingSector banks;

	/** The capitalist sector. */
	private CapitalistSector capitalists;

	/** The circuit. */
	private final Circuit circuit;

	/** To count the number of firms created since the start of the simulation. */
	private int countFirms;

	/** The sector name. */
	private final String name;

	/** The random. */
	final private Random random;

	/** A scheduler for the regeneration of firms. */
	private final Map<Integer,Integer> regeneration = new HashMap<Integer,Integer>();

	/** The timer. */
	final private Timer timer;

	/** The collection of firms. */
	protected final AgentSet<Firm> firms;

	/** The parameters of the household sector. */
	protected final JamelParameters parameters = new BasicParameters();

	/**
	 * Creates a new banking sector.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public BasicIndustrialSector(String name, Circuit circuit) {
		this.name=name;
		this.circuit=circuit;
		this.timer=this.circuit.getTimer();
		this.random=this.circuit.getRandom();
		this.firms=new BasicAgentSet<Firm>(this.random);
	}

	/**
	 * Closes the sector at the end of the period.
	 */
	private void close() {
		for (final Firm firm:firms.getList()) {
			firm.close();
		}
	}

	/**
	 * Creates firms.
	 * @param type the type of firms to create.
	 * @param lim the number of firms to create.
	 * @return a list containing the new firms. 
	 */
	private List<Firm> createFirms(String type, int lim) {
		final List<Firm> result = new ArrayList<Firm>(lim);
		try {
			for(int index=0;index<lim;index++) {
				this.countFirms++;
				final String name = "Firm"+this.countFirms;
				final Firm firm = (Firm) Class.forName(type,false,ClassLoader.getSystemClassLoader()).getConstructor(String.class,IndustrialSector.class).newInstance(name,this);
				result.add(firm);
			}
		} catch (Exception e) {
			throw new RuntimeException("Firm creation failure",e); 
		}
		return result;
	}

	/**
	 * Opens each firm in the sector.
	 */
	private void open() {
		regenerate();
		final List<Firm> bankrupted = new LinkedList<Firm>();
		for (final Firm firm:firms.getShuffledList()) {
			firm.open();
			if (firm.isBankrupted()) {
				bankrupted.add(firm);
				prepareRegeneration();
			}
		}
		this.firms.removeAll(bankrupted);
	}

	/**
	 * Prepares the regeneration of a firm some periods later.
	 */
	private void prepareRegeneration() {
		final int min = parameters.get(KEY.REGENERATION_MIN).intValue();
		final int max = parameters.get(KEY.REGENERATION_MAX).intValue();		
		final int now = timer.getPeriod().intValue();
		final int later = now + min + random.nextInt(max-min);
		Integer creations = this.regeneration.get(later);
		if (creations!=null){
			creations++;
		}
		else {
			creations=1;
		}
		this.regeneration.put(later,creations);
	}

	/**
	 * Regenerates firms.
	 */
	private void regenerate() {
		final Integer lim = this.regeneration.get(timer.getPeriod().intValue());
		if (lim != null) {
			this.firms.putAll(this.createFirms(this.agentType,lim));
		}
	}

	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("new")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.firms.putAll(this.createFirms(this.agentType,size));
		}
		else if(event.getNodeName().equals("shock")) {
			final String key = event.getAttribute("parameter");
			final float value = Float.parseFloat(event.getAttribute("value"));
			this.parameters.put(key, value);
		}
		else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName());			
		}
	}

	@SuppressWarnings({ "unused", "javadoc" })
	public Object forward(String request, Object ... args) {

		// TODO à revoir / gestion des événements

		/*else if (request.equals("change in parameters")) {
			// To simulate an exogenous shock.
			this.parameters.update();
			result = null;
		}

		else if (request.startsWith("agent.")) {
			// Execution of an instruction by an individual firm. 
			// (since 22-11-2014)
			final String[] key = request.split("\\.", 2);
			// key[0] equals "agent", key[1] contains the name of the agent targeted.
			result = this.firms.execute(key[1],args);
		}

		else if (request.equals("productivityShock")) {
			final List<Firm> list = this.firms.getList();
			for (Firm firm: list) {
				firm.execute("productivityShock", Float.parseFloat((String) args[0]));
			}
			result = null;
		}

		else {
			throw new IllegalArgumentException(this.name+": Unknown request <"+request+">");
		}*/

		return null;

	}

	@Override
	public SectorDataset getDataset() {
		return this.firms.collectData();
	}

	@Override
	public float getFloatParameter(String key) {
		return this.parameters.get(key);
	}

	@Override
	public JobOffer[] getJobOffers(int size) {
		final ArrayList<JobOffer> jobOffersList=new ArrayList<JobOffer>(size);
		for (final Firm firm:firms.getSimpleRandomSample(size)) {
			final JobOffer jobOffer = firm.getJobOffer();
			if (jobOffer!=null) {
				jobOffersList.add(jobOffer);
			}
		}
		return jobOffersList.toArray(new JobOffer[jobOffersList.size()]);
	}

	/**
	 * Returns the sector name.
	 * @return the sector name.
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public BankAccount getNewAccount(Firm firm) {
		return this.banks.getNewAccount(firm);
	}

	@Override
	public Phase getPhase(String name) {
		Phase result = null;

		if (name.equals(PHASE.OPENING)) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicIndustrialSector.this.open();
				}				
			};			
		}

		else if (name.equals(PHASE.PAY_DIVIDEND)) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getList()) {
						firm.payDividend();
					}
				}				
			};			
		}

		else if (name.equals(PHASE.PLAN_PRODUCTION)) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getShuffledList()) {
						firm.prepareProduction();
					}			
				}				
			};			
		}

		else if (name.equals(PHASE.PRODUCTION)) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					for (final Firm firm:firms.getShuffledList()) {
						firm.production();
					}			
				}				
			};			
		}

		else if (name.equals(PHASE.CLOSURE)) {
			result = new AbstractPhase(name, this){
				@Override public void run() {
					BasicIndustrialSector.this.close();
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
	public List<Firm> getSimpleRandomSample(int size) {
		return this.firms.getSimpleRandomSample(size);
	}

	@Override
	public long getSimulationID() {
		return this.circuit.getSimulationID();
	}

	@Override
	public Supply[] getSupplies(int size) {
		final ArrayList<Supply> list=new ArrayList<Supply>(size);
		for (final Firm firm:firms.getSimpleRandomSample(size)) {
			final Supply supply = firm.getSupply();
			if (supply!=null) {
				list.add(supply);
			}
		}
		return list.toArray(new Supply[list.size()]);
	}

	@Override
	public Timer getTimer() {
		return this.timer;
	}

	@Override
	public void init(Element element) throws InitializationException {
		if (element==null) {
			throw new IllegalArgumentException("Element is null");			
		}
		
		// Initialization of the agent type:
		final String agentAttribute = element.getAttribute("agent");
		if ("".equals(agentAttribute)) {
			throw new InitializationException("Attribute not found: agent");
		}
		this.agentType =agentAttribute;
		
		// Initialization of the dependencies:
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement==null) {
			throw new InitializationException("Element not found: "+DEPENDENCIES);
		}

		// Looking for the capitalist sector.
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement==null) {
			throw new InitializationException("Element not found: "+key1);
		}
		final String capitalists = capitalistSectorElement.getAttribute("value");
		if (capitalists=="") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalists = (CapitalistSector) circuit.getSector(capitalists);

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
	public Shareholder selectCapitalOwner() {
		return this.capitalists.selectRandomCapitalOwner();
	}

	@Override
	public List<Shareholder> selectCapitalOwner(int n) {
		return this.capitalists.selectRandomCapitalOwners(n);
	}

}

// ***
