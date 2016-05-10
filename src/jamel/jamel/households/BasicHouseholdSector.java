package jamel.jamel.households;

import jamel.Jamel;
import jamel.basic.Circuit;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.util.InitializationException;
import jamel.basic.util.Timer;
import jamel.jamel.aggregates.Banks;
import jamel.jamel.aggregates.Capitalists;
import jamel.jamel.aggregates.Employers;
import jamel.jamel.aggregates.Suppliers;
import jamel.jamel.capital.StockCertificate;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic household sector.
 */
public class BasicHouseholdSector implements HouseholdSector, Capitalists {

	/**
	 * A class to manage multiple employers.
	 */
	private class MultiSectoralEmployers implements Employers {
	
		/**
		 * The employer sectors.
		 */
		private final Map<String, Employers> sectors = new LinkedHashMap<String, Employers>();
	
		/**
		 * The total size (= the total of potential jobs).
		 */
		private int totalSize = 0;
	
		/**
		 * The weight of the each sector.
		 */
		private final Map<String, Integer> weight = new HashMap<String, Integer>();
	
		@Override
		public JobOffer[] getJobOffers(final int size) {
			final JobOffer[] result;
			if (this.sectors.isEmpty()) {
				throw new RuntimeException("There is no employer sector.");
			}
			final List<JobOffer> offers = new LinkedList<JobOffer>();			
			for(Entry<String,Employers> entry: this.sectors.entrySet()) {
				final int n = Math.max(1,size*this.weight.get(entry.getKey())/totalSize);
				final JobOffer[] offer = entry.getValue().getJobOffers(n);
				for(JobOffer jobOffer: offer) {
					offers.add(jobOffer);
				}
			}
			Collections.shuffle(offers,random);
			if (offers.size()>size) {
				result = offers.subList(0, size).toArray(new JobOffer[0]);
			}
			else {
				result = offers.toArray(new JobOffer[0]);
			}
			return result;
		}
	
		@Override
		public int getSize() {
			return this.totalSize;
		}
	
		/**
		 * Returns <tt>true</tt> if this record contains no employer.
		 *
		 * @return <tt>true</tt> if this record contains no employer.
		 */
		public boolean isEmpty() {
			return this.sectors.isEmpty();
		}
	
		/**
		 * Associates the specified employer with the specified key in this map.
		 *
		 * @param key
		 *            key with which the specified employer is to be associated.
		 * @param employer
		 *            employer to be associated with the specified key.
		 */
		public void register(String key, Employers employer) {
			if (key == null) {
				throw new IllegalArgumentException("Key cannot be null.");
			}
			if (employer == null) {
				throw new IllegalArgumentException("Employer cannot be null.");
			}
			if (this.sectors.containsKey(key)) {
				throw new IllegalArgumentException("This employer is already registred.");
			}
			this.sectors.put(key, employer);
			this.weight.put(key, 1);
			this.totalSize++;
		}
	
		/**
		 * Updates the weight of each sector.
		 */
		public void updateWeight() {
			this.totalSize = 0;
			for (Entry<String, Employers> entry : sectors.entrySet()) {
				final String key = entry.getKey();
				final Employers employer = entry.getValue();
				final int size = employer.getSize();
				this.weight.put(key, size);
				this.totalSize+=size;
			}
		}
	
	}

	/** The key word for the "consumption" phase. */
	private static final String PHASE_CONSUMPTION = "consumption";

	/** The key word for the "job search" phase. */
	private static final String PHASE_JOB_SEARCH = "job_search";

	/** The key word for the "opening" phase. */
	private static final String PHASE_OPENING = "opening";

	/** The key word for the "take dividends" phase. */
	private static final String PHASE_TAKE_DIVIDENDS = "take_dividends";

	/** The <code>dependencies</code> element. */
	protected static final String DEPENDENCIES = "dependencies";

	/** The key word for the "closure" phase. */
	protected static final String PHASE_CLOSURE = "closure";

	/**
	 * The type of goods the households want to consume.
	 */
	private String typeOfTheConsumptionGood = null;

	/** The type of the agents. */
	protected String agentType;

	/**
	 * The banking sector. The banking sector provides new accounts to
	 * households.
	 */
	protected Banks banks = null;

	/** The circuit. */
	protected final Circuit circuit;

	/** The agent counter. */
	protected int countAgents;

	/** The employers. */
	protected Employers employers;

	/** The collection of agents. */
	protected final AgentSet<Household> households;

	/** The sector name. */
	protected final String name;

	/** The parameters of the household sector. */
	protected final Map<String, Float> parameters = new HashMap<String, Float>();

	/** The random. */
	protected final Random random;

	/**
	 * The supplier sector. The supplier sector supplies commodities to
	 * households.
	 */
	protected Suppliers suppliers = null;

	/** The timer. */
	protected Timer timer;

	/**
	 * Creates a new sector for households.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public BasicHouseholdSector(String name, Circuit circuit) {
		this.circuit = circuit;
		this.random = this.circuit.getRandom();
		this.timer = this.circuit.getTimer();
		this.name = name;
		this.households = new BasicAgentSet<Household>(this.random);
	}

	/**
	 * Creates households.
	 * 
	 * @param type
	 *            the type of households to create.
	 * @param lim
	 *            the number of households to create.
	 * @return a list containing the new households.
	 */
	protected List<Household> createHouseholds(String type, int lim) {
		final List<Household> list = new ArrayList<Household>(lim);
		for (int index = 0; index < lim; index++) {
			this.countAgents++;
			final String householdName = "Household" + this.countAgents;
			try {
				list.add((Household) Class.forName(type, false, ClassLoader.getSystemClassLoader())
						.getConstructor(String.class, HouseholdSector.class).newInstance(householdName, this));
			} catch (Exception e) {
				throw new RuntimeException("Something goes wrong while creating households.", e);
			}
		}
		return list;
	}

	@Override
	public Object askFor(String key) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Cheque[] buyCorporation(Corporation firm) {
		final long firmValue = firm.getBookValue();
		if (firmValue < 0) {
			throw new RuntimeException("firmValue: " + firmValue);
		}
		final List<Shareholder> all = new LinkedList<Shareholder>(this.households.getShuffledList());
		final List<Shareholder> buyers = new ArrayList<Shareholder>(10);
		final List<Long> prices = new ArrayList<Long>(10);
		final List<Integer> shares = new ArrayList<Integer>(10);
		long priceOfOneShare = firmValue / 100;
		if (priceOfOneShare < 2) {
			priceOfOneShare = 1;
			// throw new RuntimeException("priceOfOneShare: " +
			// priceOfOneShare);
			// FIXME
		}

		class Auctioneer {
			// TODO: It is not really an auctioneer. To be renamed.
			int auction(long sharePrice, long minimalFinancialCapacity) {
				int nonIssuedShares = 100;
				for (Shareholder shareholder : all) {
					final long shareholderFinancialCapacity = shareholder.getFinancialCapacity();
					if (shareholderFinancialCapacity > minimalFinancialCapacity) {

						final int nShares0 = (int) (shareholderFinancialCapacity / sharePrice);
						final int nShares = Math.min(nShares0, nonIssuedShares);
						final long priceOfTheShares = sharePrice * nShares;
						buyers.add(shareholder);
						prices.add(priceOfTheShares);
						shares.add(nShares);
						nonIssuedShares -= nShares;
					}
					if (nonIssuedShares == 0) {
						break;
					}
				}
				return nonIssuedShares;
			}
		}

		final Auctioneer auctioneer = new Auctioneer();

		int nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 10);
		if (nonIssuedShares > 0) {
			buyers.clear();
			prices.clear();
			shares.clear();
			nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 5);
			if (nonIssuedShares > 0) {
				buyers.clear();
				prices.clear();
				shares.clear();
				nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare * 2);
				if (nonIssuedShares > 0) {
					int count = 0;
					while (true) {
						buyers.clear();
						prices.clear();
						shares.clear();
						nonIssuedShares = auctioneer.auction(priceOfOneShare, priceOfOneShare);
						if (nonIssuedShares == 0) {
							break;
						}
						priceOfOneShare -= 0.1 * priceOfOneShare;
						if (count > 10 || priceOfOneShare == 0) {
							// On n'a pas réussi à vendre tout le capital de la
							// firme !!!
							// Pourtant on a baissé le prix 10 fois.
							Jamel.println("priceOfOneShare: " + priceOfOneShare);
							throw new RuntimeException("Non issued shares: " + nonIssuedShares);
							// FIXME: il faut implémenter une solution à ce cas.
						}
					}
				}
			}
		}

		if (buyers.size() == 0) {
			throw new RuntimeException(
					"Buyers list is empty: " + firm.getName() + ", period " + timer.getPeriod().intValue());
		}

		final StockCertificate[] newShares = firm.getNewShares(shares);
		// Tout le capital est maintenant partagé proportionnellement aux
		// contributions de chacun.

		final Cheque[] cheques = new Cheque[buyers.size()];
		for (int i = 0; i < buyers.size(); i++) {
			cheques[i] = buyers.get(i).buy(newShares[i], prices.get(i));
		}

		return cheques;
	}

	@Override
	public void doEvent(Element event) {
		// 2016-03-27: changement de la syntaxe des événements
		final String eventType = event.getAttribute("event"); 
		if (eventType.equals("Create new households")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.households.putAll(this.createHouseholds(this.agentType, size));
		} else if (eventType.equals("shock")) {
			// 2016-03-27: TODO: à vérifier			
			final NodeList nodes = event.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elem = (Element) nodes.item(i);
					if (elem.getNodeName().equals("param")) {
						final String key = elem.getAttribute("key");
						final float val = Float.parseFloat(elem.getAttribute("value"));
						this.parameters.put(key, val);
					}
				}
			}
		} else {
			throw new RuntimeException("Unknown event or not yet implemented: " + event.getNodeName());
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
	 * 
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
	public Phase getPhase(String phaseName) throws InitializationException {
		final Phase result;

		if (phaseName.equals(PHASE_OPENING)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Household household : households.getList()) {
						household.open();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_TAKE_DIVIDENDS)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Household household : households.getList()) {
						household.takeDividends();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_JOB_SEARCH)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Household household : households.getShuffledList()) {
						household.jobSearch();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_CONSUMPTION)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Household household : households.getShuffledList()) {
						household.consumption();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_CLOSURE)) {
			result = getClosurePhase();
		}

		else {
			result = null;
			throw new InitializationException("Unknown phase: \"" + phaseName + "\".");
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
	public String getTypeOfConsumptionGood() {
		return this.typeOfTheConsumptionGood;
	}

	@Override
	public void init(Element element) throws InitializationException {

		// Initialization of the dependencies:
		if (element == null) {
			throw new IllegalArgumentException("Element is null");
		}

		// Initialization of the agent type:
		final String agentAttribute = element.getAttribute("agent");
		if ("".equals(agentAttribute)) {
			throw new InitializationException("Attribute not found: agent");
		}
		this.agentType = agentAttribute;

		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: " + DEPENDENCIES);
		}

		// Looking for the supplier sector.

		final String key1 = "Suppliers";
		final Element suppliersElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (suppliersElement == null) {
			throw new InitializationException("Element not found: " + key1);
		}
		final String suppliersKey = suppliersElement.getAttribute("value");
		if (suppliersKey == "") {
			throw new InitializationException("Missing attribute: value");
		}
		Suppliers newSuppliers = (Suppliers) circuit.getSector(suppliersKey);
		if (newSuppliers == null) {
			throw new InitializationException("Suppliers sector: unknown sector: \"" + suppliersKey + "\"");
		}
		this.suppliers = newSuppliers;
		this.typeOfTheConsumptionGood = suppliersElement.getAttribute("good");

		// Looking for the employer sector.

		initEmployers(refElement.getElementsByTagName("Employers"));

		// Looking for the banking sector.
		final String key3 = "Banks";
		final Element banksElement = (Element) refElement.getElementsByTagName(key3).item(0);
		if (banksElement == null) {
			throw new InitializationException("Element not found: " + key3);
		}
		final String banksKey = banksElement.getAttribute("value");
		if (banksKey == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.banks = (Banks) circuit.getSector(banksKey);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.parameters.put(attr.getName(), Float.parseFloat(attr.getValue()));
			}
		}
	}

	@Override
	public List<Shareholder> selectCapitalOwners() {
		return new ArrayList<Shareholder>(households.getShuffledList());
	}

	@Override
	public Shareholder selectRandomCapitalOwner() {
		return households.getRandomAgent();
	}

	@Override
	public List<Shareholder> selectRandomCapitalOwners(int n) {
		return new ArrayList<Shareholder>(households.getSimpleRandomSample(n));
	}

	@Override
	public List<Household> selectRandomSample(int n) {
		return new ArrayList<Household>(households.getSimpleRandomSample(n));
	}


	/**
	 * Creates and returns a closure phase.
	 * 
	 * @return a closure phase.
	 */
	protected Phase getClosurePhase() {
		return new AbstractPhase(PHASE_CLOSURE, this) {
			@Override
			public void run() {
				for (final Household household : households.getList()) {
					household.close();
				}
				((MultiSectoralEmployers) employers).updateWeight();
			}
		};
	}

	/**
	 * Initializes the employer sectors.
	 * 
	 * @param list
	 *            the list of the employers.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	protected void initEmployers(NodeList list) throws InitializationException {
		final MultiSectoralEmployers multiEmployers = new MultiSectoralEmployers();
		for (int i = 0; i < list.getLength(); i++) {
			final Element element = (Element) list.item(i);
			final String key = element.getAttribute("value");
			final Employers newEmployer = (Employers) circuit.getSector(key);
			if (newEmployer == null) {
				throw new InitializationException("Employers sector not found: " + key);
			}
			multiEmployers.register(key, newEmployer);
		}
		
		if (multiEmployers.isEmpty()) {
			// TODO: Faut-il obligatoirement des employeurs ? Par exemple un
			// secteur de ménages capitalistes ne devrait pas avoir besoin
			// d'employeurs.
			// On devrait donc accepter qu'il n'y en n'ait pas.
			// A décider sans doute en amont de cette procédure.
			throw new InitializationException("Employers not found.");
		}
		this.employers = multiEmployers;
	}

}

// ***
