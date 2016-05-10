package jamel.jamel.banks;

import java.util.List;
import java.util.Random;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import jamel.jamel.aggregates.Capitalists;
import jamel.jamel.roles.AccountHolder;
import jamel.jamel.roles.Corporation;
import jamel.jamel.roles.Shareholder;
import jamel.jamel.widgets.BankAccount;
import jamel.jamel.widgets.Cheque;

/**
 * A clone of Basic banking sector. Uses Bank2 (and not BasicBank).
 */
public class BasicBankingSector implements BankingSector {

	/** Key word for the <code>dependencies</code> element. */
	private static final String DEPENDENCIES = "dependencies";

	/** Key word for the "closure" phase. */
	private static final String PHASE_CLOSURE = "closure";

	/** Key word for the "debt recovery" phase. */
	private static final String PHASE_DEBT_RECOVERY = "debt_recovery";

	/** Key word for the "opening" phase. */
	private static final String PHASE_OPENING = "opening";

	/** Key word for the "pay dividend" phase. */
	private static final String PHASE_PAY_DIVIDEND = "pay_dividend";

	/** The collection of banks. */
	private final AgentSet<Bank> banks;

	/** The capitalist sector. */
	private Capitalists capitalistSector;

	/** The macroeconomic circuit. */
	private final Circuit circuit;

	/** The sector dataset (collected at the end of the previous period). */
	private SectorDataset dataset;

	/** The name of this sector. */
	private final String name;

	/** The parameters of this sector. */
	private final JamelParameters parameters;

	/** The random. */
	final private Random random;

	/** The timer. */
	final private Timer timer;

	/**
	 * Creates a new banking sector.
	 * 
	 * @param name
	 *            the name of the sector.
	 * @param circuit
	 *            the circuit.
	 */
	public BasicBankingSector(String name, Circuit circuit) {
		this.name = name;
		this.parameters = new BasicParameters(name);
		this.circuit = circuit;
		this.timer = this.circuit.getTimer();
		this.random = this.circuit.getRandom();
		this.banks = new BasicAgentSet<Bank>(this.random);
		this.dataset = this.banks.collectData();
	}

	/**
	 * Closes each bank and collects the data.
	 */
	private void close() {
		for (final Bank bank : banks.getList()) {
			bank.close();
		}
		this.dataset = this.banks.collectData();
	}

	/**
	 * Opens each bank in the sector.
	 */
	private void open() {
		for (final Bank bank : banks.getList()) {
			bank.open();
		}
	}

	@Override
	public Object askFor(String key) {
		/*
		 * 2016-03-17 / Utilisé par la banque pour accéder au taux d'inflation.
		 */
		return circuit.askFor(key);
	}

	@Override
	public void doEvent(Element event) {
		if (event.getNodeName().equals("shock")) {
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
	public BankAccount getNewAccount(AccountHolder accountHolder) {
		final List<Bank> list = this.banks.getList();
		if (list.size() == 0) {
			throw new RuntimeException("The sector is empty: " + this.name);
		}
		return this.banks.getList().get(0).getNewAccount(accountHolder);
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
					BasicBankingSector.this.open();
				}
			};
		}

		else if (phaseName.equals(PHASE_PAY_DIVIDEND)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Bank bank : banks.getList()) {
						bank.payDividend();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_DEBT_RECOVERY)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					for (final Bank bank : banks.getShuffledList()) {
						bank.debtRecovery();
					}
				}
			};
		}

		else if (phaseName.equals(PHASE_CLOSURE)) {
			result = new AbstractPhase(phaseName, this) {
				@Override
				public void run() {
					BasicBankingSector.this.close();
				}
			};
		}

		else {
			result = null;
		}

		return result;
	}

	@Override
	public void init(Element element) throws InitializationException {
		// Initialization of the dependencies:
		if (element == null) {
			throw new IllegalArgumentException("Element is null");
		}
		final Element refElement = (Element) element.getElementsByTagName(DEPENDENCIES).item(0);
		if (refElement == null) {
			throw new InitializationException("Element not found: " + DEPENDENCIES);
		}
		final String key1 = "CapitalistSector";
		final Element capitalistSectorElement = (Element) refElement.getElementsByTagName(key1).item(0);
		if (capitalistSectorElement == null) {
			throw new InitializationException("Element not found: " + key1);
		}
		final String capitalists = capitalistSectorElement.getAttribute("value");
		if (capitalists == "") {
			throw new InitializationException("Missing attribute: value");
		}
		this.capitalistSector = (Capitalists) circuit.getSector(capitalists);

		// Initialization of the parameters:
		final Element settingsElement = (Element) element.getElementsByTagName("settings").item(0);
		final NamedNodeMap attributes = settingsElement.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node node = attributes.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				final Attr attr = (Attr) node;
				try {
					this.parameters.put(attr.getName(), Float.parseFloat(attr.getValue()));
				} catch (NumberFormatException e) {
					throw new InitializationException(
							"For settings attribute: " + attr.getName() + "=\"" + attr.getValue() + "\"", e);
				}
			}
		}

		// 2016-04-03: creation de la banque unique qui peuple le secteur.
		
		this.banks.put(new BasicBank("Bank0", this, random, timer));
	}

	@Override
	public List<Shareholder> selectCapitalOwner(int n) {
		return this.capitalistSector.selectRandomCapitalOwners(n);
	}

	@Override
	public Cheque[] sellCorporation(Corporation corporation) {
		return this.capitalistSector.buyCorporation(corporation);
	}

}

// ***
