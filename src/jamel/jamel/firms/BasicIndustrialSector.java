package jamel.jamel.firms;

import jamel.basic.Circuit;
import jamel.basic.data.SectorDataset;
import jamel.basic.sector.AbstractPhase;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.sector.Sector;
import jamel.basic.util.BasicParameters;
import jamel.basic.util.InitializationException;
import jamel.basic.util.JamelParameters;
import jamel.basic.util.Timer;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.sectors.BankingSector;
import jamel.jamel.sectors.CapitalistSector;
import jamel.jamel.sectors.EmployerSector;
import jamel.jamel.sectors.SupplierSector;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.JobOffer;
import jamel.jamel.widgets.Supply;

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
import org.w3c.dom.NodeList;

/**
 * A basic industrial sector.
 */
public class BasicIndustrialSector implements Sector, IndustrialSector,
		SupplierSector, EmployerSector {

	/** The <code>dependencies</code> element. */
	private static final String ELEM_DEPENDENCIES = "dependencies";

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

	/** Key word for the "opening" phase. */
	private static final String PHASE_OPENING = "opening";

	/** Key word for the "pay dividend" phase. */
	private static final String PHASE_PAY_DIVIDEND = "pay_dividend";

	/** Key word for the "plan production" phase. */
	private static final String PHASE_PLAN_PRODUCTION = "plan_production";

	/** Key word for the "production" phase. */
	private static final String PHASE_PRODUCTION = "production";

	/** The type of the firms populating this sector. */
	private String agentType = null;

	/** The banking sector. */
	private BankingSector banks;

	/** The capitalist sector. */
	private CapitalistSector capitalists;

	/** The macroeconomic circuit. */
	private final Circuit circuit;

	/** The sector dataset (collected at the end of the previous period). */
	private SectorDataset dataset;

	/** The name of this sector. */
	private final String name;

	/** The random. */
	final private Random random;

	/** A scheduler for the regeneration of firms. */
	private final Map<Integer, Integer> regeneration = new HashMap<Integer, Integer>();

	/** The timer. */
	final private Timer timer;

	/** To count the number of firms created since the start of the simulation. */
	protected int countFirms;

	/** The collection of firms. */
	protected final AgentSet<Firm> firms;

	/** The parameters of this sector. */
	protected final JamelParameters parameters = new BasicParameters();

	/**
	 * Creates a new banking sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public BasicIndustrialSector(String name, Circuit circuit) {
		this.name = name;
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
	}

	/**
	 * Prepares the regeneration of a firm some periods later.
	 */
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

	/**
	 * Creates firms.
	 * 
	 * @param type
	 *            the type of firms to create.
	 * @param lim
	 *            the number of firms to create.
	 * @return a list containing the new firms.
	 */
	protected List<Firm> createFirms(String type, int lim) {
		final List<Firm> result = new ArrayList<Firm>(lim);
		try {
			for (int index = 0; index < lim; index++) {
				this.countFirms++;
				final String firmName = "Firm" + this.countFirms;
				final Firm firm = (Firm) Class
						.forName(type, false,
								ClassLoader.getSystemClassLoader())
						.getConstructor(String.class, IndustrialSector.class)
						.newInstance(firmName, this);
				result.add(firm);
			}
		} catch (Exception e) {
			throw new RuntimeException("Firm creation failure", e);
		}
		return result;
	}

	/**
	 * Opens each firm in the sector.
	 */
	protected void open() {
		regenerate();
		final List<Firm> bankrupted = new LinkedList<Firm>();
		for (final Firm firm : firms.getShuffledList()) {
			firm.open();
			if (firm.isBankrupted()) {
				bankrupted.add(firm);
				prepareRegeneration();
			}
		}
		this.firms.removeAll(bankrupted);
	}

	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("new")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.firms.putAll(this.createFirms(this.agentType, size));
		} else if (event.getNodeName().equals("shock")) {
			final NodeList nodes = event.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					final Element elem = (Element) nodes.item(i);
					if (elem.getNodeName().equals("param")) {
						final String key = elem.getAttribute("key");
						final float val = Float.parseFloat(elem
								.getAttribute("value"));
						this.parameters.put(key, val);
					}
				}
			}
		} else {
			throw new RuntimeException("Unknown event or not yet implemented: "
					+ event.getNodeName());
		}
	}

	@Override
	public SectorDataset getDataset() {
		return this.dataset;
	}

	@Override
	public JobOffer[] getJobOffers(int size) {
		final ArrayList<JobOffer> jobOffersList = new ArrayList<JobOffer>(size);
		for (final Firm firm : firms.getSimpleRandomSample(size)) {
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
	public float getParam(String key) {
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
		
		else {
			result=null;
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
	public List<Firm> getSimpleRandomSample(int size) {
		return this.firms.getSimpleRandomSample(size);
	}

	@Override
	public long getSimulationID() {
		return this.circuit.getSimulationID();
	}

	@Override
	public Supply[] getSupplies(int size) {
		final ArrayList<Supply> list = new ArrayList<Supply>(size);
		for (final Firm firm : firms.getSimpleRandomSample(size)) {
			final Supply supply = firm.getSupply();
			if (supply != null) {
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
		if (element == null) {
			throw new IllegalArgumentException("Element is null");
		}

		// Initialization of the agent type:
		final String agentAttribute = element.getAttribute("agent");
		this.agentType = agentAttribute;

		// Initialization of the dependencies:
		final Element refElement = (Element) element.getElementsByTagName(
				ELEM_DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: "
					+ ELEM_DEPENDENCIES);
		}

		// Looking for the capitalist sector.
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement
				.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement == null) {
			throw new InitializationException("Element not found: " + key1);
		}
		final String capitalistsKey = capitalistSectorElement
				.getAttribute("value");
		if (capitalistsKey == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalists = (CapitalistSector) circuit.getSector(capitalistsKey);

		// Looking for the banking sector.
		final String key3 = "Banks";
		final Element banksElement = (Element) refElement.getElementsByTagName(
				key3).item(0);
		if (banksElement == null) {
			throw new InitializationException("Element not found: " + key3);
		}
		final String banksKey = banksElement.getAttribute("value");
		if (banksKey == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.banks = (BankingSector) circuit.getSector(banksKey);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName(
				"settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				this.parameters.put(attr.getName(),
						Float.parseFloat(attr.getValue()));
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
