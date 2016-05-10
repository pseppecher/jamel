package jamel.jamel.firms;

import jamel.basic.Circuit;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;
import jamel.basic.util.Timer;
import jamel.jamel.aggregates.Banks;
import jamel.jamel.aggregates.Capitalists;
import jamel.jamel.aggregates.Employers;
import jamel.jamel.aggregates.Suppliers;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A basic industrial sector.
 */
public class BasicIndustrialSector implements IndustrialSector, Suppliers, Employers {

	/** The <code>dependencies</code> element. */
	private static final String ELEM_DEPENDENCIES = "dependencies";

	/** The <code>technology</code> element. */
	private static final String ELEM_TECHNOLOGY = "technology";

	/**
	 * Key word ford the parameter fixing maximum lapse between the disparition
	 * of a firm and its regeneration.
	 */
	private static final String PARAM_REGENERATION_MAX = "regenerationLapse.max";

	/**
	 * Key word ford the parameter fixing minimum lapse between the disparition
	 * of a firm and its regeneration.
	 */
	private static final String PARAM_REGENERATION_MIN = "regenerationLapse.min";

	/** Key word for the "closure" phase. */
	private static final String PHASE_CLOSURE = "closure";

	/** Key word for the "input purchase" phase. */
	private static final String PHASE_INPUTS_PURCHASE = "inputs_purchase";

	/** Key word for the "opening" phase. */
	private static final String PHASE_OPENING = "opening";

	/** Key word for the "pay dividend" phase. */
	private static final String PHASE_PAY_DIVIDEND = "pay_dividend";

	/** Key word for the "plan production" phase. */
	private static final String PHASE_PLAN_PRODUCTION = "plan_production";

	/** Key word for the "production" phase. */
	private static final String PHASE_PRODUCTION = "production";

	/**
	 * Creates and returns a new technology.
	 * 
	 * @param element
	 *            the XML description of the new technology.
	 * @return a new technology.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	private static Technology getNewTechnology(final Element element) throws InitializationException {
		final long inputVolumeForANewMachine = Long.parseLong(element.getAttribute("machine.creation.input.volume"));
		final String typeOfInputForMachineCreation = element.getAttribute("machine.creation.input.type");
		final int timelifeMean = Integer.parseInt(element.getAttribute("machine.timelife.mean"));
		final int timelifeStDev = Integer.parseInt(element.getAttribute("machine.timelife.stDev"));
		final String typeOfProduction = element.getAttribute("production.type");
		final int productionTime = Integer.parseInt(element.getAttribute("production.time"));
		final long productivity = Long.parseLong(element.getAttribute("production.productivity"));
		final Map<String, Float> techCoefs = new HashMap<String, Float>();
		final Element techCoeficients = (Element) element.getElementsByTagName("techCoeficients").item(0);
		if (techCoeficients != null) {
			final NodeList nodeList = techCoeficients.getElementsByTagName("input");
			for (int i = 0; i < nodeList.getLength(); i++) {
				final Element elem = (Element) nodeList.item(i);
				final String type = elem.getAttribute("type");
				final Float value = Float.parseFloat(elem.getAttribute("coef"));
				techCoefs.put(type, value);
			}
		}
		return new Technology() {

			@Override
			public long getInputVolumeForANewMachine() {
				return inputVolumeForANewMachine;
			}

			@Override
			public int getProductionTime() {
				return productionTime;
			}

			@Override
			public long getProductivity() {
				return productivity;
			}

			@Override
			public Map<String, Float> getTechnicalCoefficients() {
				return techCoefs;
			}

			@Override
			public double getTimelifeMean() {
				return timelifeMean;
			}

			@Override
			public double getTimelifeStDev() {
				return timelifeStDev;
			}

			@Override
			public String getTypeOfInputForMachineCreation() {
				return typeOfInputForMachineCreation;
			}

			@Override
			public String getTypeOfProduction() {
				return typeOfProduction;
			}

		};
	}

	/** The type of the firms populating this sector. */
	private String agentType = null;

	/** The banking sector. */
	private Banks banks;

	/** The capitalist sector. */
	private Capitalists capitalists;

	/** The macroeconomic circuit. */
	private final Circuit circuit;

	/**
	 * To count the number of firms created since the start of the simulation.
	 */
	private int countFirms;

	/** The sector dataset (collected at the end of the previous period). */
	private SectorDataset dataset;

	/** The collection of firms. */
	private final AgentSet<Firm> firms;

	/** The name of this sector. */
	private final String name;

	/** The parameters of this sector. */
	private final JamelParameters parameters;

	/** The random. */
	final private Random random;

	/** A scheduler for the regeneration of firms. */
	private final Map<Integer, Integer> regeneration = new HashMap<Integer, Integer>();

	/**
	 * The size of the sector, <i>ie</i> the sum of the workforce of each firm.
	 */
	private int size = 0;

	/**
	 * The suppliers of inputs.
	 */
	final private Map<String, Suppliers> suppliers = new LinkedHashMap<String, Suppliers>();

	/**
	 * The current technology.
	 */
	private Technology technology;

	/** The timer. */
	final private Timer timer;

	/**
	 * Creates a new industrial sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public BasicIndustrialSector(String name, Circuit circuit) {
		this.name = name;
		this.parameters = new BasicParameters(name);
		this.circuit = circuit;
		this.timer = this.circuit.getTimer();
		this.random = this.circuit.getRandom();
		this.firms = new BasicAgentSet<Firm>(this.random);
		this.dataset = this.firms.collectData();
	}

	/**
	 * Closes the sector at the end of the period.
	 */
	private void close() {
		for (final Firm firm : firms.getList()) {
			firm.close();
		}
		this.dataset = this.firms.collectData();
		final Double[] truc = this.dataset.getField("capacity", "");
		if (truc.length > 0) {
			// Herfindahl–Hirschman Index
			double sum = 0;
			for (double d : truc) {
				sum += d;
			}
			double hhi = 0;
			for (double d : truc) {
				hhi += Math.pow(d / sum, 2);
			}
			this.dataset.putSectorialValue("hhi", hhi);
		}
	}

	/**
	 * Creates firms.
	 * 
	 * @param type
	 *            the type of firms to create.
	 * @param lim
	 *            the number of firms to create.
	 * @return a list containing the new firms.
	 */
	private List<Firm> createFirms(String type, int lim) {
		final List<Firm> result = new ArrayList<Firm>(lim);
		try {
			for (int index = 0; index < lim; index++) {
				this.countFirms++;
				final String firmName = "Firm" + this.countFirms;
				final Firm firm = (Firm) Class.forName(type, false, ClassLoader.getSystemClassLoader())
						.getConstructor(String.class, IndustrialSector.class).newInstance(firmName, this);
				result.add(firm);
			}
		} catch (Exception e) {
			throw new RuntimeException("Firm creation failure", e);
		}
		return result;
	}

	/**
	 * Initializes the suppliers sectors.
	 * 
	 * @param list
	 *            list of the supplier sectors.
	 * @throws InitializationException
	 *             if something goes wrong.
	 */
	private void initSuppliers(final NodeList list) throws InitializationException {
		for (int i = 0; i < list.getLength(); i++) {
			final Element elem = (Element) list.item(i);
			final String sector = elem.getAttribute("sector");
			final String supply = elem.getAttribute("supply");
			final Suppliers supplier = (Suppliers) circuit.getSector(sector);
			if (supplier == null) {
				throw new InitializationException("Supplier sector not found: " + sector);
			}
			this.suppliers.put(supply, supplier);
		}
	}

	/**
	 * The inputs purchase phase: when firms buy raw materials and other inputs.
	 */
	private void inputsPurchase() {
		for (final Firm firm : firms.getShuffledList()) {
			firm.inputsPurchase();
		}
	}

	/**
	 * Opens each firm in the sector.
	 */
	private void open() {
		this.size = 0;
		regenerate();
		final List<Firm> bankrupted = new LinkedList<Firm>();
		for (final Firm firm : firms.getShuffledList()) {
			firm.open();
			if (firm.isBankrupted()) {
				bankrupted.add(firm);
				// prepareRegeneration();
			} else {
				this.size += firm.getSize();
			}
		}
		this.firms.removeAll(bankrupted);
	}

	/**
	 * Prepares the regeneration of a firm some periods later.
	 */
	@SuppressWarnings("unused")
	private void prepareRegeneration() {
		final int min = parameters.get(PARAM_REGENERATION_MIN).intValue();
		final int max = parameters.get(PARAM_REGENERATION_MAX).intValue();
		final int now = timer.getPeriod().intValue();
		final int later = now + min + random.nextInt(max - min);
		Integer creations = this.regeneration.get(later);
		if (creations != null) {
			creations++;
		} else {
			creations = 1;
		}
		this.regeneration.put(later, creations);
	}

	/**
	 * Regenerates firms.
	 */
	private void regenerate() {
		final Integer lim = this.regeneration.get(timer.getPeriod().intValue());
		if (lim != null) {
			this.firms.putAll(this.createFirms(this.agentType, lim));
		}
	}

	@Override
	public Object askFor(String key) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void doEvent(Element event) {
		// 2016-03-27: changement de la syntaxe des événements
		final String eventType = event.getAttribute("event");
		if (eventType.equals("Create new firms")) {
			final int newFirms = Integer.parseInt(event.getAttribute("size"));
			this.firms.putAll(this.createFirms(this.agentType, newFirms));
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
		return this.dataset;
	}

	@Override
	public JobOffer[] getJobOffers(int nOffers) {
		final ArrayList<JobOffer> jobOffersList = new ArrayList<JobOffer>(nOffers);
		for (final Firm firm : firms.getSimpleRandomSample(nOffers)) {
			final JobOffer jobOffer = firm.getJobOffer();
			if (jobOffer != null) {
				jobOffersList.add(jobOffer);
			}
		}
		return jobOffersList.toArray(new JobOffer[jobOffersList.size()]);
	}

	/**
	 * Returns the sector name.
	 * 
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
	public Float getParam(String key) {
		return this.parameters.get(key);
	}

	@Override
	public Phase getPhase(String phaseName) {
		final Phase result;

		if (phaseName.equals(PHASE_OPENING)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicIndustrialSector.this.open();
				}
			};
		}

		else if (phaseName.equals(PHASE_INPUTS_PURCHASE)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicIndustrialSector.this.inputsPurchase();
				}
			};
		}

		else if (phaseName.equals(PHASE_PAY_DIVIDEND)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Firm firm : firms.getList()) {
						firm.payDividend();
					}

				}
			};
		}

		else if (phaseName.equals(PHASE_PLAN_PRODUCTION)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Firm firm : firms.getShuffledList()) {
						firm.prepareProduction();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_PRODUCTION)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Firm firm : firms.getShuffledList()) {
						firm.production();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_CLOSURE)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicIndustrialSector.this.close();
				}
			};
		}

		else if ("investment".equals(phaseName)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Firm firm : firms.getShuffledList()) {
						firm.invest();
					}
				}
			};
		}

		else {
			result = null;
		}

		return result;
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public Double getRandomWage() {
		final Double result;
		if (this.dataset == null) {
			result = null;
		} else {
			final Double[] values = this.dataset.getField("wages", "");
			if (values != null) {
				final int length = values.length;
				if (length > 0) {
					final int rand = random.nextInt(values.length);
					result = values[rand];
				} else {
					result = null;
				}
			} else {
				result = null;
			}
		}
		return result;
	}

	@Override
	public List<Firm> getSimpleRandomSample(int nFirm) {
		return this.firms.getSimpleRandomSample(nFirm);
	}

	@Override
	public long getSimulationID() {
		return this.circuit.getSimulationID();
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public Supply[] getSupplies(int nSupply) {
		final ArrayList<Supply> list = new ArrayList<Supply>(nSupply);
		for (final Firm firm : firms.getSimpleRandomSample(nSupply)) {
			final Supply supply = firm.getSupply();
			if (supply != null) {
				list.add(supply);
			}
		}
		return list.toArray(new Supply[list.size()]);
	}

	@Override
	public Supply[] getSupplies(String type, int n) {
		final Suppliers sup = this.suppliers.get(type);
		if (sup == null) {
			throw new RuntimeException("No suppliers found for this type of input: " + type);
		}
		return sup.getSupplies(n);
	}

	@Override
	public Technology getTechnology() {
		return this.technology;
	}

	@Override
	public Timer getTimer() {
		return this.timer;
	}

	@Override
	public void init(Element element) throws InitializationException {
		if (element == null) {
			throw new IllegalArgumentException("Element is null");
		}

		// Initialization of the agent type:

		final String agentAttribute = element.getAttribute("agent");
		this.agentType = agentAttribute;

		// Initialization of the technology:

		final Element technologyElement = (Element) element.getElementsByTagName(ELEM_TECHNOLOGY).item(0);
		if (technologyElement == null) {
			throw new InitializationException("Element not found: " + ELEM_TECHNOLOGY);
		}
		this.technology = getNewTechnology(technologyElement);

		// Initialization of the dependencies:

		final Element refElement = (Element) element.getElementsByTagName(ELEM_DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: " + ELEM_DEPENDENCIES);
		}

		// Looking for the capitalist sector.

		{
			final String key1 = "CapitalistSector";
			final Element capitalistSectorElement = (Element) refElement.getElementsByTagName(key1).item(0);
			if (capitalistSectorElement == null) {
				throw new InitializationException("Element not found: " + key1);
			}
			final String capitalistsKey = capitalistSectorElement.getAttribute("value");
			if (capitalistsKey == "") {
				throw new InitializationException("Missing attribute: value");
			}
			this.capitalists = (Capitalists) circuit.getSector(capitalistsKey);
		}

		// Initialization of the suppliers sectors.

		this.initSuppliers(refElement.getElementsByTagName("Suppliers"));

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
	public Shareholder selectCapitalOwner() {
		return this.capitalists.selectRandomCapitalOwner();
	}

	@Override
	public List<Shareholder> selectCapitalOwner(int n) {
		return this.capitalists.selectRandomCapitalOwners(n);
	}

}

// ***
