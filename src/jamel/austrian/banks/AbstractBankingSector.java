package jamel.austrian.banks;



import jamel.austrian.banks.AbstractBankingSector;
import jamel.austrian.sfc.SFCSector;
import jamel.basic.Circuit;
import jamel.basic.sector.AgentSet;
import jamel.basic.sector.BasicAgentSet;
import jamel.basic.sector.Phase;
import jamel.basic.data.SectorDataset;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Represents the banking sector.
 */
public abstract class AbstractBankingSector extends SFCSector {

	/** The list of defaulting banks. */
	protected final LinkedList<CommercialBank> bankFailures;

	/** The collection of agents. */
	protected final AgentSet<CommercialBank> banks;

	/** The number of newly created banks. */
	protected int  newBanks;

	
	/**
	 * Creates a new sector for banks.
	 * @param name the name of the sector.
	 * @param circuit the circuit.
	 */
	public AbstractBankingSector(String name, Circuit aCircuit) {
		super(name, aCircuit);
		this.banks = new BasicAgentSet<CommercialBank>(this.random);
		this.bankFailures = new LinkedList<CommercialBank>();
	}

	
	/**
	 * Creates banks.
	 * 
	 * @param lim
	 *            the number of banks
	 */
	protected abstract List<CommercialBank> createBanks(int lim);
	
	
	/**
	 * An agent files bankruptcy.
	 */
	public void bankruptcy(CommercialBank aBankruptAgent){	
		bankFailures.add(aBankruptAgent);	
	} 
	
	
	@Override
	public void doEvent(Element event) {
		final String eventType = event.getAttribute("event");
		if (eventType.equals("Create new banks")) {
			final int size = Integer.parseInt(event.getAttribute("size"));
			this.banks.putAll(this.createBanks(size));
			getInvestmentBank().setBankID(size);
		}
		else if (eventType.equals("assignOwnership")) {
			for (CommercialBank bank:banks.getList()) bank.setOwnershipStructure();
		}
		else {
			throw new RuntimeException("Unknown event or not yet implemented: "+event.getNodeName()+": "+eventType);
		}
	} 
	
	
	@Override
	public SectorDataset getDataset() {
		SectorDataset data = banks.collectData();
		data.putSectorialValue("bankFailures", (double) bankFailures.size());
		data.putSectorialValue("bankNumber", (double) banks.getList().size());
		data.putSectorialValue("newBanks", (double) newBanks);

		return data;
	}
	
	
	@Override
	public abstract Phase getPhase(String name);
	
	
	/**
	 * Returns a bank selected at random.
	 */
	public CommercialBank selectRandomBank() {
		return banks.getRandomAgent();
	}

}
